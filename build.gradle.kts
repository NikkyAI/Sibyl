import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") apply false
    kotlin("plugin.serialization") apply false
}

subprojects {
    repositories {
        jcenter()
        mavenCentral()
        maven(url = "https://kotlin.bintray.com/ktor")
    }
}

// TODO: add bintray publishing
