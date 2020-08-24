plugins {
    kotlin("jvm") apply false
    kotlin("plugin.serialization") apply false
    id("org.flywaydb.flyway") apply false
//    id("com.squareup.sqldelight") apply false
    id("com.jfrog.bintray") apply false
    id("com.vanniktech.dependency.graph.generator")
}

// Add first-class support for included/composite builds: https://github.com/jmfayard/refreshVersions/issues/205
val dummy = configurations.create("dummy") {
    isCanBeConsumed = true
}

dependencies {
    add(dummy.name, "com.squareup:kotlinpoet:_")
}

allprojects {
    repositories {
        jcenter()
        mavenCentral()
        maven(url = "https://kotlin.bintray.com/ktor")
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    }
}

val bintrayOrg: String? = System.getenv("BINTRAY_USER")
val bintrayApiKey: String? = System.getenv("BINTRAY_API_KEY")
var bintrayRepository = "github"
val bintrayPackage = "sibyl"

group = "moe.nikky.sibyl"
description = "modular chatbot framework for matterbridge"

apply(from="${rootDir.path}/version.gradle.kts")
val isSnapshot = extra["isSnapshot"] as Boolean
if(isSnapshot) {
    bintrayRepository = "snapshot"
}

subprojects {
    project.group = rootProject.group
    project.version = rootProject.version

    plugins.withId("maven-publish") {
        val artifactId = project.path.drop(1).replace(':', '-')
        val publicationName = "sibyl"

        val sourcesJar by tasks.creating(Jar::class) {
            dependsOn(JavaPlugin.CLASSES_TASK_NAME)
            archiveClassifier.set("sources")
            val sourceSets = project.extensions.getByName("sourceSets") as SourceSetContainer
            from(sourceSets["main"].allSource)
        }

        val javadocJar by tasks.creating(Jar::class) {
            dependsOn(JavaPlugin.JAVADOC_TASK_NAME)
            archiveClassifier.set("javadoc")
            from(tasks["javadoc"])
        }


        configure<PublishingExtension> {
            publications {
                create<MavenPublication>(publicationName) {
                    from(components["kotlin"])
                    artifact(sourcesJar)
                    artifact(javadocJar)
                    this.artifactId = artifactId
                }
            }
//            repositories {
//                val publish= properties["publish"] as? String ?: "0"
//                val override= properties["override"] as? String ?: "0"
//                maven(url = "https://api.bintray.com/maven/$bintrayOrg/$bintrayRepository/$bintrayPackage/;publish=$publish;override=$override") {
//                    name = "bintray"
//                    credentials {
//                        username = bintrayOrg
//                        password = bintrayApiKey
//                    }
//                }
//            }
        }
        apply(from="${rootDir.path}/pom.gradle.kts")
        if (bintrayOrg == null || bintrayApiKey == null) {
            logger.error("bintray credentials not configured properly")
            return@withId
        }
        project.apply(plugin = "com.jfrog.bintray")
        configure<com.jfrog.bintray.gradle.BintrayExtension> {
            user = bintrayOrg
            key = bintrayApiKey
            publish = true
            override = false
            dryRun = !properties.containsKey("nodryrun")
            setPublications(publicationName)
            pkg(delegateClosureOf<com.jfrog.bintray.gradle.BintrayExtension.PackageConfig> {
                repo = bintrayRepository
                name = bintrayPackage
                userOrg = bintrayOrg
                version = VersionConfig().apply {
                    // do not put commit hashes in vcs tag
                    if (!isSnapshot) {
                        vcsTag = extra["vcsTag"] as String
                    }
                    name = project.version as String
                    githubReleaseNotesFile = "RELEASE_NOTES.md"
                }
            })
        }
    }
}

dependencyGraphGenerator {
    generators += com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorExtension.Generator(
        name = "projects",
        include = { dep ->
            logger.lifecycle("include: $dep ${dep.moduleGroup} ${dep.parents}")
            dep.moduleGroup == rootProject.group || dep.parents.any { it.moduleGroup == rootProject.group }
        },
        children = { dep ->
//            logger.lifecycle("children: $dep ${dep.parents}")
//            dep.moduleGroup == "Sibyl" || dep.parents.any { it.moduleGroup == "Sibyl" }
            true
        },
        projectNode = { node, b ->
            node
//                .setName(b.path.drop(1).replace(':', '-'))
                .add(
                    guru.nidi.graphviz.attribute.Color.SLATEGRAY,
                    guru.nidi.graphviz.attribute.Color.AQUAMARINE.background().fill(),
                    guru.nidi.graphviz.attribute.Style.FILLED
                )
        },
        includeProject = { project ->
            logger.lifecycle("project: $project")
            project.buildFile.exists()
        },
        dependencyNode = { node, dep ->
            logger.lifecycle("dep node $dep ${dep::class}")
            node
        }
    )
}

tasks {
    withType(com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorTask::class).all {
        doFirst {
            logger.lifecycle("cleaning output")
            outputDirectory = outputDirectory.resolve(generator.name)
            outputDirectory.deleteRecursively()
            outputDirectory.mkdirs()
        }
    }
}
