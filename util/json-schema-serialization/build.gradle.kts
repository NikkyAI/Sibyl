plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    `maven-publish`
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:_")

    testImplementation(kotlin("test-junit5", "_"))
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:_")
}

tasks {
    test {
        useJUnit()
    }
}

