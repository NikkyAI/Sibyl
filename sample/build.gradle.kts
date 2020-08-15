plugins {
    kotlin("jvm")
    application
}

dependencies {
    implementation(kotlin("reflect", "_"))

    implementation(project(":core"))
    implementation(project(":polling-client"))
    implementation(project(":module:logging"))
    implementation(project(":module:roleplay"))

    implementation("io.github.microutils:kotlin-logging:_")
    implementation("ch.qos.logback:logback-classic:_")

    // TODO: move into core, polling-client and websocket-client
    implementation(Ktor.client.core)
    implementation(Ktor.client.okHttp)
    implementation(Ktor.client.json)
    implementation(Ktor.client.serialization)
    implementation(Ktor.client.websockets)
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