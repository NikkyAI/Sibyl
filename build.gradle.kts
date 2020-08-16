plugins {
    kotlin("jvm") apply false
    kotlin("plugin.serialization") apply false
    id("com.vanniktech.dependency.graph.generator") version "0.5.0"
}

val bintrayOrg: String? = System.getenv("BINTRAY_USER")
val bintrayApiKey: String? = System.getenv("BINTRAY_API_KEY")
val bintrayRepository = "github"
val bintrayPackage = "sibyl"
val vcs = "https://github.com/NikkyAI/Sibyl"

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
    "$nextVersion-SNAPSHOT" // +$describeAll"

    // TODO: publish snapshots somewhere
} else {
    describeTag.substringAfter('v')
}


subprojects {
    repositories {
        jcenter()
        mavenCentral()
        maven(url = "https://kotlin.bintray.com/ktor")
    }

    group = rootProject.group
    version = rootProject.version

    plugins.withId("maven-publish") {
        configure<PublishingExtension> {
            publications {
                create<MavenPublication>("maven") {
                    from(components["kotlin"])
                    artifactId = project.path.drop(1).replace(':', '-')
                }
            }

            publications.withType<MavenPublication> {
                pom {
                    name.set(project.name)
                    description.set(project.description)
                    url.set(vcs)
                    licenses {
                        license {
                            name.set("The Apache Software License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                            distribution.set("repo")
                        }
                    }
                    developers {
                        developer {
                            id.set("nikkyai")
                            name.set("NikkyAi")
                        }
                    }
                    scm {
                        connection.set("$vcs.git")
                        developerConnection.set("$vcs.git")
                        url.set(vcs)
                    }
                }
            }
        }
        if (bintrayOrg == null || bintrayApiKey == null) {
            logger.error("bintray credentials not configured properly")
            return@withId
        }
        configure<PublishingExtension> {
//            publications {
//                create<MavenPublication>("maven") {
//                    from(components["kotlin"])
//                    artifactId = project.path.drop(1).replace(':', '.')
//                }
//            }

//            val vcs: String by project
//            val bintrayOrg: String by project
//            val bintrayRepository: String by project

            repositories {
                maven("https://api.bintray.com/maven/$bintrayOrg/$bintrayRepository/$bintrayPackage/;publish=0;override=0") {
                    name = "bintray"
                    credentials {
                        username = bintrayOrg
                        password = bintrayApiKey
                    }
                }
            }
        }

//        val publishTasks = tasks.withType<PublishToMavenRepository>()
//            .matching {
//                when {
//                    org.jetbrains.kotlin.konan.target.HostManager.hostIsMingw -> it.name.startsWith("publishMingw")
//                    org.jetbrains.kotlin.konan.target.HostManager.hostIsMac -> it.name.startsWith("publishMacos") || it.name.startsWith("publishIos")
//                    org.jetbrains.kotlin.konan.target.HostManager.hostIsLinux -> it.name.startsWith("publishLinux") ||
//                            it.name.startsWith("publishJs") ||
//                            it.name.startsWith("publishJvm") ||
//                            it.name.startsWith("publishMetadata") ||
//                            it.name.startsWith("publishKotlinMultiplatform")
//                    else -> TODO("Unknown host")
//                }
//            }
//        tasks.register("smartPublish") {
//            group = "publishing"
//            dependsOn(publishTasks)
//        }
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
