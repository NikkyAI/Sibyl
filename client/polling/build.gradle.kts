plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    `maven-publish`
}

kotlin {
    
}

dependencies {
//    implementation(kotlin("stdlib", "_"))

    implementation(project(":core"))

//    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:_")
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:_")

    api("io.ktor:ktor-client-core:_")
    api("io.ktor:ktor-client-okhttp:_")
}