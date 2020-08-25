import okhttp3.internal.toImmutableList

plugins {
    id("com.jfrog.bintray") apply false
}

group = "moe.nikky.sibyl.plugin"
description = "gradle plugins for building sibyl modules"

val bintrayOrg: String? = System.getenv("BINTRAY_USER")
val bintrayApiKey: String? = System.getenv("BINTRAY_API_KEY")
var bintrayRepository = "github"
val bintrayPackage = "sibyl"

apply(from="${rootDir.parentFile.path}/version.gradle.kts")
val isSnapshot = extra["isSnapshot"] as Boolean
if(isSnapshot) {
    bintrayRepository = "snapshot"
}

subprojects {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
        gradlePluginPortal()
    }

    project.group = rootProject.group
    project.version = rootProject.version

    afterEvaluate {
        plugins.withId("maven-publish") {
            val publicationName = "sibylPlugin"

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
                        artifactId = project.path.drop(1).replace(':', '-')
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
            apply(from="${rootDir.parentFile.path}/mavenPom.gradle.kts")
            val pluginNames = the<GradlePluginDevelopmentExtension>().plugins.names
            logger.lifecycle("pluginNames: $pluginNames")
            val markerPublicationNames = pluginNames.map { pluginName ->
                "${pluginName}PluginMarkerMaven"
            }.toTypedArray()
            logger.lifecycle("markerPublicationNames: ${markerPublicationNames.joinToString()}")
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
                setPublications(publicationName, *markerPublicationNames)
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
}
