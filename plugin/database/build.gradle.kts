plugins {
//    id("org.gradle.kotlin.kotlin-dsl")
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}

dependencies {
    implementation(project(":plugin-base"))
    implementation("org.flywaydb.flyway:org.flywaydb.flyway.gradle.plugin:_")
    implementation("com.squareup.sqldelight:com.squareup.sqldelight.gradle.plugin:_")
}

gradlePlugin {
    plugins.asMap.keys
    plugins {
        create("sibylDatabase") {
            id = "moe.nikky.sibyl.plugin.database"
            implementationClass = "sibyl.db.SibylDatabasePlugin"
        }
    }
}