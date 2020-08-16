plugins {
    kotlin("jvm") apply false
    kotlin("plugin.serialization") apply false
    id("com.vanniktech.dependency.graph.generator") version "0.5.0"
    id("com.jfrog.bintray") version "1.8.5" apply false
}

val bintrayOrg: String? = System.getenv("BINTRAY_USER")
val bintrayApiKey: String? = System.getenv("BINTRAY_API_KEY")
val bintrayRepository = "github"
var bintrayPackage = "sibyl"
//val vcs = "https://github.com/NikkyAI/Sibyl"
//val issues = "$vcs/issues"

group = "moe.nikky.sibyl"
description = "modular chatbot framework for matterbridge"

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

// tag or commit hash
val describeTagsAlways = captureExec {
    commandLine("git", "describe", "--tags", "--always")
}?.trim()?.substringAfterLast('-')

// current or last tag
val describeAbbrevTags = captureExec {
    commandLine("git", "describe", "--abbrev=0", "--tags")
}?.trim() ?: "v0.0.0"

val describeTags = captureExec {
    commandLine("git", "describe", "--tags")
}?.trim() ?: "v0.0.0"

val describeAbbrevAlways = captureExec {
    commandLine("git", "describe", "--abbrev=0", "--always")
}?.trim() ?: "v0.0.0"

logger.lifecycle( "describeTagsAlways: '$describeTagsAlways'" )
logger.lifecycle( "tag: '$describeAbbrevTags'" )
logger.lifecycle( "tag2: '$describeTags'" )
logger.lifecycle( "commit-hash: '$describeAbbrevAlways'" )

val isSnapshot = describeTagsAlways != describeAbbrevTags
val versionStr: String = if(isSnapshot && describeAbbrevTags.startsWith("v")) {
    val lastVersion = describeAbbrevTags.substringAfter("v")
    var (major, minor, patch) = lastVersion.split('.').map { it.toInt() }
    patch++
    val nextVersion = "$major.$minor.$patch"
    bintrayPackage = "sibyl-dev"

    "$nextVersion-dev+$describeTagsAlways"
} else {
    describeAbbrevTags.substringAfter('v')
}

project.version = versionStr

logger.lifecycle("version is $version")

subprojects {
    repositories {
        jcenter()
        mavenCentral()
        maven(url = "https://kotlin.bintray.com/ktor")
    }

    project.group = rootProject.group
    project.version = versionStr

    plugins.withId("maven-publish") {
        val artifactId = project.path.drop(1).replace(':', '-') // TODO: try . separator again
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
        }
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
//            dryRun = true // TODO: disable on github actions
            setPublications(publicationName)
            pkg(delegateClosureOf<com.jfrog.bintray.gradle.BintrayExtension.PackageConfig> {
                repo =  bintrayRepository
                name = bintrayPackage
                userOrg = bintrayOrg
                version = VersionConfig().apply {
                    // do not put commit hashes in vcs tag
                    if(!isSnapshot) {
                        vcsTag = describeTagsAlways
                    }
//                    vcsTag = describeAbbrevAlwaysTags
                    name = versionStr
                    githubReleaseNotesFile = "RELEASE_NOTES.md"
                }
//                description = rootProject.description
//                websiteUrl = "https://...."
//                vcsUrl = vcs
//                setLabels("kotlin", "matterbridge", "chatbot")
//                setLicenses("MIT")
//                issueTrackerUrl = issues
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
            node.add(
                guru.nidi.graphviz.attribute.Color.SLATEGRAY,
                guru.nidi.graphviz.attribute.Color.AQUAMARINE.background().fill(),
                guru.nidi.graphviz.attribute.Style.FILLED
            )
        },
        includeProject = { project ->
            logger.lifecycle("project: $project")
            project.buildFile.exists()
        }
    )
}

tasks {
    withType(com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorTask::class).all {
        doFirst {
            outputDirectory = outputDirectory.resolve(generator.name)
            outputDirectory.deleteRecursively()
            outputDirectory.mkdirs()
        }
    }
}

// TODO: add bintray publishing
