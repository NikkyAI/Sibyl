plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
//    id("com.squareup.sqldelight")
//    id("org.flywaydb.flyway")
    `maven-publish`
}

dependencies {
    api(kotlin("stdlib", "_"))

    api("org.jetbrains.kotlinx:kotlinx-serialization-core:_")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:_")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:_")

    api(project(":util:json-schema-serialization"))

    api("joda-time:joda-time:_")

    api("org.flywaydb:flyway-core:_")
    api("com.zaxxer:HikariCP:_")

    api("com.github.ajalt:clikt:_")

    api("io.github.microutils:kotlin-logging:_")
    implementation("ch.qos.logback:logback-classic:_")
}

