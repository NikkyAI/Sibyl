plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("org.flywaydb.flyway")
    `maven-publish`
}

dependencies {
    implementation(project(":core"))
}