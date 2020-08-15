plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":core"))

    implementation("io.github.microutils:kotlin-logging:_")
    implementation("ch.qos.logback:logback-classic:_")
}