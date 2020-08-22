import de.fayard.refreshVersions.bootstrapRefreshVersions

//pluginManagement {
//    repositories {
//        google()
//        mavenCentral()
//        maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
//        gradlePluginPortal()
//    }
//}
buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies.classpath("de.fayard.refreshVersions:refreshVersions:0.9.5")
}

bootstrapRefreshVersions(
    listOf(rootDir.parentFile.resolve("buildSrc/dependencies-rules.txt").readText()),
    rootDir.parentFile.resolve("versions.properties")
)

rootProject.name = "plugin"

//include("database")