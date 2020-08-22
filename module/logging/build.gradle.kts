plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("moe.nikky.sibyl.plugin.database")
    `maven-publish`
}

dependencies {
    implementation(project(":core"))
}