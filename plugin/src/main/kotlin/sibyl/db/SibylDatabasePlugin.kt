package sibyl.db

import com.squareup.sqldelight.gradle.SqlDelightExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.*
import sibyl.SibylBasePlugin
import sibyl.loadProperties

open class SibylDatabasePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.setupDatabase()
    }

    companion object {
        fun Project.setupDatabase() {
            // add base plugin
//            apply(plugin = "moe.nikky.sibyl")
            apply<SibylBasePlugin>() // add this by type since no plugin marker is published

            apply(plugin = "com.squareup.sqldelight")
            apply(plugin = "org.flywaydb.flyway")
            apply(plugin = "idea")

            logger.lifecycle("configuring sqldelight")
            // TODO: configure database name via extension
            val databaseName = project.name.capitalize()+"Database"
            // TODO: configure schema via extension
            val schema = "sibyl-" + project.name // TODO: pass this to the code somehow as a constant
            val migrationLocationOutput = file("$buildDir/resources/main/migrations/$schema")
            configure<SourceSetContainer> {
                named<SourceSet>("main") {
                    resources {
                        srcDirs(file("$buildDir/resources/main"))
                    }
                }
            }

            configure<SqlDelightExtension> {
                database(databaseName) {
                    packageName = "sibyl.db"
                    dialect = "postgresql"
                    sourceFolders = listOf("sqldelight").also { sourceFolders ->
                        configure<org.gradle.plugins.ide.idea.model.IdeaModel> {
                            module {
                                sourceDirs = sourceDirs +
                                        sourceFolders.map { folder -> file("src/main/$folder")}
//                                testSourceDirs = testSourceDirs +
//                                        sourceFolders.map { folder -> file("src/test/$folder")}
                            }
                        }
                    }
                    deriveSchemaFromMigrations = true
                    migrationOutputDirectory = migrationLocationOutput
                    migrationOutputFileFormat = ".sql"
                }
            }
            configure<org.gradle.plugins.ide.idea.model.IdeaModel> {
                module {
                    generatedSourceDirs = generatedSourceDirs + file("$buildDir/resources/main")
                }
            }

            val testingMigrations = false // TODO: enable via tasks or flags

            val envProps = loadProperties(rootDir.resolve(".env"))
            val pgHost = "localhost"
            val pgDb = envProps["POSTGRES_DB"].toString()
            val pgUser = envProps["POSTGRES_USER"].toString()
            val pgPass = envProps["POSTGRES_PASS"].toString()
            val pgPort = envProps["POSTGRES_PORT"].toString()

            configure<org.flywaydb.gradle.FlywayExtension> {
                url = "jdbc:postgresql://$pgHost:$pgPort/$pgDb"
                user = pgUser
                password = pgPass
                schemas = kotlin.arrayOf(schema)
                // TODO: only add test sources in dev env
                locations = kotlin.arrayOf(
                    "filesystem:" + migrationLocationOutput.path //file("$buildDir/resources/main").path
                ) + if(testingMigrations) {
                    kotlin.arrayOf(
                        "filesystem:" + file("src/test/resources/migrations/").path // TODO: enable test data in migrations when running tests
                    )
                } else kotlin.arrayOf()
                baselineVersion = "0"
            }

            dependencies {
                add("implementation", "joda-time:joda-time:_")
                add("implementation", "org.postgresql:postgresql:_") // do i need this at all plugins ?
                add("implementation", "com.zaxxer:HikariCP:_")
//                add("implementation", "org.flywaydb:flyway-core:_") // just required on core
                add("implementation", "com.squareup.sqldelight:jdbc-driver:_")
            }

            tasks {
//                val generateMigrationsTask = getByName("generateMain${databaseName}Migrations") {
//                    outputs.upToDateWhen { false }
//                }
                val compileKotlin by existing {
                    dependsOn("generateMain${databaseName}Migrations")
                }
                withType(org.flywaydb.gradle.task.AbstractFlywayTask::class) {
                    dependsOn("generateMain${databaseName}Migrations")
                }
            }

        }
    }

}