plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(kotlin("stdlib", "_"))

    implementation(project(":core"))

    api(KotlinX.serialization.runtime)
    api(KotlinX.coroutines.core)

    implementation(Ktor.client.core)
    implementation(Ktor.client.okHttp)
    implementation(Ktor.client.json)
    implementation(Ktor.client.serialization)
    implementation(Ktor.client.websockets)
}