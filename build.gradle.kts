plugins {
    kotlin("jvm") apply false
    kotlin("plugin.serialization") apply false
    id("org.flywaydb.flyway") apply false
    id("com.squareup.sqldelight") apply false
    id("com.vanniktech.dependency.graph.generator")
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
    repositories {
        jcenter()
        mavenCentral()
        maven(url = "https://kotlin.bintray.com/ktor")
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    }

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
            repositories {
//                if (bintrayOrg != null && bintrayApiKey != null) {
                val publish= properties["publish"] as? String ?: "0"
                val override= properties["override"] as? String ?: "0"
                maven(url = "https://api.bintray.com/maven/$bintrayOrg/$bintrayRepository/$bintrayPackage/;publish=$publish;override=$override") {
                    name = "bintray"
                    credentials {
                        username = bintrayOrg
                        password = bintrayApiKey
                    }
                }
//                }
            }
        }
//        if (bintrayOrg == null || bintrayApiKey == null) {
//            logger.error("bintray credentials not configured properly")
//            return@withId
//        }
//        project.apply(plugin = "com.jfrog.bintray")
//        configure<com.jfrog.bintray.gradle.BintrayExtension> {
//            user = bintrayOrg
//            key = bintrayApiKey
//            publish = true
//            override = false
//            dryRun = !properties.containsKey("nodryrun")
//            setPublications(publicationName)
//            pkg(delegateClosureOf<com.jfrog.bintray.gradle.BintrayExtension.PackageConfig> {
//                repo = bintrayRepository
//                name = bintrayPackage
//                userOrg = bintrayOrg
//                version = VersionConfig().apply {
//                    // do not put commit hashes in vcs tag
//                    if (!isSnapshot) {
//                        vcsTag = extra["vcsTag"] as String
//                    }
////                    vcsTag = describeAbbrevAlwaysTags
//                    name = project.version as String
//                    githubReleaseNotesFile = "RELEASE_NOTES.md"
//                }
////                description = rootProject.description
////                websiteUrl = "https://...."
////                vcsUrl = vcs
////                setLabels("kotlin", "matterbridge", "chatbot")
////                setLicenses("MIT")
////                issueTrackerUrl = issues
//            })
//        }
    }

//    afterEvaluate {
//        if(project.plugins.hasPlugin("org.jetbrains.kotlin.jvm")) {
//            tasks {
//                val compileKotlin by existing(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class) { kotlinOptions.jvmTarget = "1.8" }
//                val compileTestKotlin by existing(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class) { kotlinOptions.jvmTarget = "1.8" }
//            }
//        }
//
//        if(pluginManager.hasPlugin("com.squareup.sqldelight")) {
//            logger.lifecycle("configuring sqldelight")
//            apply(plugin = "org.flywaydb.flyway")
//            val databaseName = project.name.capitalize()+"Database"
//            val schema = "sibyl-" + project.name // TODO: pass this to the code somehow as a constant
//
//            val migrationLocationOutput = file("$buildDir/resources/main/migrations/$schema")
//            configure<SourceSetContainer> {
//                named<SourceSet>("main") {
//                    resources {
//                        srcDirs(file("$buildDir/resources/main"))
//                    }
//                }
//            }
//
//            apply(plugin = "idea")
//            configure<com.squareup.sqldelight.gradle.SqlDelightExtension> {
//                database(databaseName) {
//                    packageName = "sibyl.db"
//                    dialect = "postgresql"
//                    sourceFolders = listOf("sqldelight").also { sourceFolders ->
//                        configure<org.gradle.plugins.ide.idea.model.IdeaModel> {
//                            module {
//                                sourceDirs = sourceDirs +
//                                        sourceFolders.map { folder -> file("src/main/$folder")}
////                                testSourceDirs = testSourceDirs +
////                                        sourceFolders.map { folder -> file("src/test/$folder")}
//                            }
//                        }
//                    }
//                    deriveSchemaFromMigrations = true
//                    migrationOutputDirectory = migrationLocationOutput
//                    migrationOutputFileFormat = ".sql"
//                }
//            }
//            configure<org.gradle.plugins.ide.idea.model.IdeaModel> {
//                module {
//                    generatedSourceDirs = generatedSourceDirs + file("$buildDir/resources/main")
//                }
//            }
//
//            val testingMigrations = false // TODO: enable via tasks or flags
//
//            val envProps = loadProperties(rootDir.resolve(".env"))
//            val pgHost = "localhost"
//            val pgDb = envProps["POSTGRES_DB"].toString()
//            val pgUser = envProps["POSTGRES_USER"].toString()
//            val pgPass = envProps["POSTGRES_PASS"].toString()
//            val pgPort = envProps["POSTGRES_PORT"].toString()
//            configure<org.flywaydb.gradle.FlywayExtension> {
//                url = "jdbc:postgresql://$pgHost:$pgPort/$pgDb"
//                user = pgUser
//                password = pgPass
//                schemas = arrayOf(schema)
//                // TODO: only add test sources in dev env
//                locations = arrayOf(
//                    "filesystem:" + migrationLocationOutput.path //file("$buildDir/resources/main").path
//                ) + if(testingMigrations) {
//                    arrayOf(
//                        "filesystem:" + file("src/test/resources/migrations/").path // TODO: enable test data in migrations when running tests
//                    )
//                } else arrayOf()
//                baselineVersion = "0"
//            }
//
//            project.dependencies {
//                add("implementation", "joda-time:joda-time:_")
//                add("implementation", "org.postgresql:postgresql:_") // do i need this at all plugins ?
//                add("implementation", "com.zaxxer:HikariCP:_")
////                add("implementation", "org.flywaydb:flyway-core:_") // just required on core
//                add("implementation", "com.squareup.sqldelight:jdbc-driver:_")
//            }
//
//            tasks {
////                val generateMigrationsTask = getByName("generateMain${databaseName}Migrations") {
////                    outputs.upToDateWhen { false }
////                }
//                val compileKotlin by existing {
//                    dependsOn("generateMain${databaseName}Migrations")
//                }
//                withType(org.flywaydb.gradle.task.AbstractFlywayTask::class) {
//                    dependsOn("generateMain${databaseName}Migrations")
//                }
//            }
//        }
//    }
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
