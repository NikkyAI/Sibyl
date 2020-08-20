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

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:_")

//    implementation("com.squareup.sqldelight:jdbc-driver:_")
}
