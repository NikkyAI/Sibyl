plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("org.flywaydb.flyway")
    `maven-publish`
}

dependencies {
    api(kotlin("stdlib", "_"))

    api("org.jetbrains.kotlinx:kotlinx-serialization-core:_")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:_")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:_")

    api(project(":util:json-schema-serialization"))

    api("org.jooq:jooq:_")

    api("joda-time:joda-time:_")

    api("com.github.ajalt:clikt:_")

    api("io.github.microutils:kotlin-logging:_")
    implementation("ch.qos.logback:logback-classic:_")
}

