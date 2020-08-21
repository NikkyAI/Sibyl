plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.squareup.sqldelight")
    id("org.flywaydb.flyway")
    `maven-publish`
}

dependencies {
    implementation(project(":core"))
}