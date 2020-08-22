import de.fayard.refreshVersions.bootstrapRefreshVersions

buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies.classpath("de.fayard.refreshVersions:refreshVersions:0.9.5")
}

bootstrapRefreshVersions(
    listOf(rootDir.parentFile.resolve("dependencies-rules.txt").readText()),
    rootDir.parentFile.resolve("versions.properties")
)

rootProject.name = "plugin"

//include("database")