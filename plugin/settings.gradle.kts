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

fun includeAndRename(path: String) {
    include(path)
    project(path).name = "plugin-" + path.drop(1).replace(':', '-')
}


includeAndRename(":base")
includeAndRename(":database")
