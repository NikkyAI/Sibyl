plugins {
    kotlin("jvm")
    `maven-publish`
}

dependencies {
    implementation(project(":module:paste"))

    api("io.ktor:ktor-client-core:_")

    implementation("io.github.microutils:kotlin-logging:_")
}