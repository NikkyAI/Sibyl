import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
    idea
}

repositories {
    jcenter()
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation(kotlin("stdlib", "_"))

    implementation(KotlinX.serialization.runtime)
    implementation(KotlinX.coroutines.core)

    implementation(Ktor.client.core)
    implementation(Ktor.client.okHttp)
    implementation(Ktor.client.jetty)
    implementation(Ktor.client.apache)
    implementation(Ktor.client.json)
    implementation(Ktor.client.serialization)
    implementation(Ktor.client.websockets)

//    implementation("com.github.kittinunf.fuel:fuel:3.x-SNAPSHOT")
    implementation("joda-time:joda-time:_")

    implementation("io.github.microutils:kotlin-logging:_")
    implementation("ch.qos.logback:logback-classic:_")

    implementation("com.github.ajalt:clikt:_")
}

application {
    mainClassName = "Main"
}

tasks {
    withType(JavaExec::class) {
        logger.lifecycle("configuring JavaExec task: $name")

        standardInput = System.`in`

        workingDir = rootDir.resolve("run")
        workingDir.mkdirs()

        systemProperty("kotlinx.coroutines.debug", "on")
    }
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}


