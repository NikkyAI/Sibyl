plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    `maven-publish`
}

dependencies {
    implementation(project(":module:paste"))

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:_")
    api("io.ktor:ktor-client-core:_")

    implementation("io.github.microutils:kotlin-logging:_")
}