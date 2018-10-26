pluginManagement {
    repositories {
        maven("http://dl.bintray.com/kotlin/kotlin-eap")
        mavenCentral()
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "kotlinx-serialization") {
                useModule("org.jetbrains.kotlin:kotlin-serialization:${requested.version}")
            }
        }
    }
}
rootProject.name = "Sibyl"
