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
val vcs = "https://github.com/NikkyAI/Sibyl"
val issues = "$vcs/issues"
//val tags = setOf()

group = "moe.nikky.sibyl"
description = "modular chatbot framework for matterlink"

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

val describeAbbrevAlwaysTags = captureExec {
    commandLine("git", "describe", "--abbrev=0", "--always", "--tags")
}?.trim() ?: "v0.0.0"

logger.lifecycle( "describeTagsAlways: '$describeTagsAlways'" )
logger.lifecycle( "tag: '$describeAbbrevTags'" )
logger.lifecycle( "tag2: '$describeTags'" )
logger.lifecycle( "commit-hash: '$describeAbbrevAlwaysTags'" )

val isSnapshot = describeTagsAlways != describeAbbrevTags
version = if(isSnapshot && describeAbbrevTags.startsWith("v")) {
    val lastVersion = describeAbbrevTags.substringAfter("v")
    var (major, minor, patch) = lastVersion.split('.').map { it.toInt() }
    patch++
    val nextVersion = "$major.$minor.$patch"
    "$nextVersion-dev+$describeTagsAlways"

    bintrayPackage = "sibyl-dev"
} else {
    describeAbbrevTags.substringAfter('v')
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
        val artifactId = project.path.drop(1).replace(':', '-') // TODO: try . separator again
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
            publish = true
            override = false
//            dryRun = true // TODO: disable on github actions
            setPublications(publicationName)
            pkg(delegateClosureOf<com.jfrog.bintray.gradle.BintrayExtension.PackageConfig> {
                repo =  bintrayRepository
                name = bintrayPackage
                userOrg = bintrayOrg
                version = VersionConfig().apply {
                    // do not put commit hashes in vcs tag
//                    if(!isSnapshot) {
//                        vcsTag = describeAbbrevAlwaysTags
//                    }
                    vcsTag = describeAbbrevAlwaysTags
                    name = project.version.toString()
                    githubReleaseNotesFile = "RELEASE_NOTES.md"
                }
                description = rootProject.description
//                websiteUrl = "https://...."
                vcsUrl = vcs
                setLabels("kotlin", "matterbridge", "chatbot")
                setLicenses("MIT")
                issueTrackerUrl = issues
            })
        }
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
