plugins {
    kotlin("jvm")
    `maven-publish`
}

dependencies {
    implementation(project(":core"))

    api("io.ktor:ktor-client-core:_")
}