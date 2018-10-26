import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
//    `java-gradle-plugin`
    idea
    kotlin("jvm") version Kotlin.version
    id(Serialization.plugin) version Kotlin.version
}

repositories {
    maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
    maven(url = "https://kotlin.bintray.com/kotlinx")
    jcenter()
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(Serialization.dependency)
    implementation(Coroutines.dependency)

    implementation(Fuel.dependency)
    implementation(Fuel.dependencyCoroutines)
    implementation(Fuel.dependencySerialization)

    compile(Logging.dependency)
    compile(Logging.dependencyLogbackClassic)
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

