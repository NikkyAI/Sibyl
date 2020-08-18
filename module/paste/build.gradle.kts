plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    `maven-publish`
}

dependencies {
    implementation(project(":core"))

    api("io.ktor:ktor-client-core:_")
}