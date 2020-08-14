plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(kotlin("stdlib", "_"))

    implementation(KotlinX.serialization.runtime)
    implementation(KotlinX.coroutines.core)

    implementation("io.github.microutils:kotlin-logging:_")
    implementation("ch.qos.logback:logback-classic:_")

    api("joda-time:joda-time:_")

    api("com.github.ajalt:clikt:_")
}