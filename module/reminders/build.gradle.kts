plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.squareup.sqldelight")
    id("org.flywaydb.flyway")
    `maven-publish`
    idea
}

dependencies {
    implementation(project(":core"))
//    implementation("com.squareup.sqldelight:jdbc-driver:_")
}
//
//sqldelight {
//    database("RemindersDatabase") {
//        packageName = "sibyl.db"
//        dialect = "postgresql"
//        sourceFolders = listOf("sqldelight")
//        deriveSchemaFromMigrations = true
//        migrationOutputDirectory = file("$buildDir/resources/main/migrations")
//        migrationOutputFileFormat = ".sql"
//    }
//}
//
//idea {
//    module {
//        sourceDirs = sourceDirs + file("src/main/sqldelight")
//    }
//}
//
//tasks {
//    compileKotlin.configure {
//        dependsOn("generateRemindersDatabaseMigrations")
//    }
//}