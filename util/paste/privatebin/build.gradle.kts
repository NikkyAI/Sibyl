plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    `maven-publish`
}

dependencies {
    implementation(project(":module:paste"))

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:_")
    implementation("io.github.novacrypto:Base58:_")
    api("io.ktor:ktor-client-core:_")
//    api("com.soywiz.korlibs.krypto:krypto:_")

    implementation("org.bouncycastle:bcpg-jdk15to18:_")
    implementation("org.bouncycastle:bcprov-jdk15to18:_")
    implementation("org.bouncycastle:bctls-jdk15to18:_")
    implementation("org.bouncycastle:bcprov-ext-jdk15to18:_")

    implementation("io.github.microutils:kotlin-logging:_")
}