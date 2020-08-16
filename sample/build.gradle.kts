plugins {
    kotlin("jvm")
    application
}

dependencies {
    implementation(kotlin("reflect", "_"))

    implementation(project(":core"))
    implementation(project(":client:polling"))
    implementation(project(":client:websocket"))
    implementation(project(":module:logging"))
    implementation(project(":module:roleplay"))

//    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:_")
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:_")

//    implementation("io.github.microutils:kotlin-logging:_")
//    implementation("ch.qos.logback:logback-classic:_")

    // TODO: move into core, polling-client and websocket-client
//    implementation(Ktor.client.core)
    implementation("io.ktor:ktor-client-okhttp:_")
//    implementation(Ktor.client.okHttp)
//    implementation(Ktor.client.json)
//    implementation(Ktor.client.serialization)
//    implementation(Ktor.client.websockets)
    implementation("io.ktor:ktor-client-websockets:_") // not available on 1.3.2-1.4.0-rc
}


application {
    mainClassName = "sibyl.MainKt"
}

tasks {
    val run by existing(JavaExec::class) {
        workingDir = rootDir.resolve("run")
        workingDir.mkdirs()
    }
}