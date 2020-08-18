plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    `maven-publish`
}

dependencies {
    api(kotlin("stdlib", "_"))

    api("org.jetbrains.kotlinx:kotlinx-serialization-core:_")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:_")

    api(project(":util:json-schema-serialization"))

    api("io.github.microutils:kotlin-logging:_")
    implementation("ch.qos.logback:logback-classic:_")

    api("joda-time:joda-time:_")

    api("com.github.ajalt:clikt:_")
}
