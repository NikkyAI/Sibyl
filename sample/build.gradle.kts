plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
}

dependencies {
    implementation(kotlin("reflect", "_"))

    implementation(project(":core"))
    implementation(project(":client:polling"))
    implementation(project(":client:websocket"))
    implementation(project(":module:accounts"))
    implementation(project(":module:logging"))
    implementation(project(":module:failtest"))
    implementation(project(":module:reminders"))
    implementation(project(":module:roleplay"))
    implementation(project(":module:paste"))
    implementation(project(":util:paste:pastee"))
    implementation(project(":util:paste:pastebin"))
    implementation(project(":util:paste:hastebin"))

//    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:_")
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:_")

//    implementation("io.github.microutils:kotlin-logging:_")
    implementation("ch.qos.logback:logback-classic:_")

//    implementation("io.ktor:ktor-client-core:_")
    implementation("io.ktor:ktor-client-okhttp:_")
    implementation("io.ktor:ktor-client-websockets:_")
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