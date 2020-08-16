plugins {
    kotlin("jvm") apply false
    kotlin("plugin.serialization") apply false
    id("com.vanniktech.dependency.graph.generator") version "0.5.0"
    id("com.jfrog.bintray") version "1.8.5" apply false
}

val bintrayOrg: String? = System.getenv("BINTRAY_USER")
val bintrayApiKey: String? = System.getenv("BINTRAY_API_KEY")
val bintrayRepository = "github"
val bintrayPackage = "sibyl"
val vcs = "https://github.com/NikkyAI/Sibyl"
val issues = "https://github.com/NikkyAI/Sibyl/issues"
//val tags = setOf()

group = "moe.nikky.sibyl"

fun captureExec(configure: ExecSpec.()->Unit): String? {
    val stdout = java.io.ByteArrayOutputStream()
    try {
        exec {
            configure()
            standardOutput = stdout
        }
    }catch (e: org.gradle.process.internal.ExecException) {
        logger.error(e.message)
        return null
    }
    return stdout.toString()
}

// tag when current ref is on a tag, otherwise commit hash
val describeAll = captureExec {
    commandLine("git", "describe", "--tags", "--always")
}?.trim()?.substringAfterLast('-')

// current or last tag
val describeTag = captureExec {
    commandLine("git", "describe", "--abbrev=0", "--tags")
}?.trim() ?: "v0.0.0"

logger.lifecycle( "all: '$describeAll'" )
logger.lifecycle( "tag: '$describeTag'" )

val isSnapshot = describeAll != describeTag
version = if(isSnapshot && describeTag.startsWith("v")) {
    val lastVersion = describeTag.substringAfter("v")
    var (major, minor, patch) = lastVersion.split('.').map { it.toInt() }
    patch++
    val nextVersion = "$major.$minor.$patch"
    "$nextVersion-dev+$describeAll"

    // TODO: use sibyl-dev package
} else {
    describeTag.substringAfter('v')
}

logger.lifecycle("version is $version")

subprojects {
    repositories {
        jcenter()
        mavenCentral()
        maven(url = "https://kotlin.bintray.com/ktor")
    }

    group = rootProject.group
    version = rootProject.version

    plugins.withId("maven-publish") {
        val artifactId = project.path.drop(1).replace(':', '-')
        val publicationName = "sibyl"
        configure<PublishingExtension> {
            publications {
                create<MavenPublication>(publicationName) {
                    from(components["kotlin"])
                    this.artifactId = artifactId
                }
            }
        }
        if (bintrayOrg == null || bintrayApiKey == null) {
            logger.error("bintray credentials not configured properly")
            return@withId
        }
        project.apply(plugin = "com.jfrog.bintray")
        configure<com.jfrog.bintray.gradle.BintrayExtension> {
            user = bintrayOrg
            key = bintrayApiKey
            publish = false
            override = false
//            dryRun = true // TODO: disable on github actions
            setPublications(publicationName)
            pkg(delegateClosureOf<com.jfrog.bintray.gradle.BintrayExtension.PackageConfig> {
                repo =  bintrayRepository
                name = bintrayPackage
                userOrg = bintrayOrg
                version = VersionConfig().apply {
                    vcsTag = describeAll
                    name = project.version.toString()
                }
//                websiteUrl = "https://...."
                vcsUrl = vcs
                setLabels("kotlin", "matterbridge", "chatbot")
                setLicenses("MIT")
                issueTrackerUrl = issues
            })
        }
//        configure<PublishingExtension> {
//            repositories {
//                val pkg = project.path.drop(1).replace(':', '-')
//                maven("https://api.bintray.com/maven/$bintrayOrg/$bintrayRepository/sibyl/;publish=0;override=1") {
//                    name = "bintray"
//                    credentials {
//                        username = bintrayOrg
//                        password = bintrayApiKey
//                    }
//                }
//            }
//        }
        tasks.withType<PublishToMavenRepository>().all {
            doFirst {
                logger.lifecycle("running ${name}")
                logger.lifecycle("publishing ${artifactId}")
            }
        }
        val publishTasks = tasks.withType<PublishToMavenRepository>()
        tasks.register("smartPublish") {
            group = "publishing"
            dependsOn(publishTasks)
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
            node.add(
                guru.nidi.graphviz.attribute.Color.SLATEGRAY,
                guru.nidi.graphviz.attribute.Color.AQUAMARINE.background().fill(),
                guru.nidi.graphviz.attribute.Style.FILLED
            )
        },
        includeProject = { project ->
            logger.lifecycle("project: $project")
            project.buildFile.exists()
//            true
        }
    )
}

tasks {
    withType(com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorTask::class).all {
//        outputs.upToDateWhen { false }
        doFirst {
            outputDirectory = outputDirectory.resolve(generator.name)
            outputDirectory.deleteRecursively()
            outputDirectory.mkdirs()
//            outputs.files.forEach {
//                it.delete()
//            }
        }
    }
//    val generateDependencyGraph by existing() {
//
//        doFirst {
//            outputs.cacheIf { false }
//            outputDirectory.deleteRecursively()
//            outputDirectory.mkdirs()
//        }
//    }
}

// TODO: add bintray publishing
