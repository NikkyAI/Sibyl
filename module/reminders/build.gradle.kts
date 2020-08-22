plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("moe.nikky.sibyl.database")
    `maven-publish`
}

dependencies {
    implementation(project(":core"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:_")

//    implementation("com.squareup.sqldelight:jdbc-driver:_")
}
