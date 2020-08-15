plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(kotlin("stdlib", "_"))

    implementation(project(":core"))

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:_")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:_")

//    implementation(Ktor.client.core)
    implementation(Ktor.client.okHttp)
//    implementation(Ktor.client.json)
//    implementation(Ktor.client.serialization)
//    implementation(Ktor.client.websockets) // not available on 1.3.2-1.4.0-rc

    implementation("io.github.microutils:kotlin-logging:_")
    implementation("ch.qos.logback:logback-classic:_")
}