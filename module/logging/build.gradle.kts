plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("moe.nikky.sibyl.database")
    `maven-publish`
}

dependencies {
    implementation(project(":core"))
}