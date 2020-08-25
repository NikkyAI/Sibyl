import de.fayard.refreshVersions.bootstrapRefreshVersions

pluginManagement {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
        gradlePluginPortal()
    }
}
buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies.classpath("de.fayard.refreshVersions:refreshVersions:0.9.5")
}

bootstrapRefreshVersions(
    listOf(rootDir.resolve("dependencies-rules.txt").readText())
)

plugins {
    id("com.gradle.enterprise") version "3.4.1"
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
//        publishAlwaysIf(true)
    }
}
rootProject.name = "Sibyl"

fun includeAndRename(path: String) {
    include(path)
    project(path).name = path.drop(1).replace(':', '-')
}

includeBuild("plugin")
include(":core")
include(":client:polling")
include(":client:websocket")
include(":module:logging")
include(":module:accounts")
include(":module:reminders")
include(":module:roleplay")
include(":module:failtest")
include(":util:paste:pastee")
include(":util:paste:pastebin")
include(":util:paste:hastebin")
include(":util:json-schema-serialization")
include(":module:paste")
include(":sample")
