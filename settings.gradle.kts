import de.fayard.dependencies.bootstrapRefreshVersionsAndDependencies
buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies.classpath("de.fayard:dependencies:0.5.8")
}

bootstrapRefreshVersionsAndDependencies(
    listOf(rootDir.resolve("buildSrc/dependencies-rules.txt").readText())
)

rootProject.name = "Sibyl"

fun includeAndRename(path: String) {
    include(path)
    project(path).name = path.drop(1).replace(':', '-')
}

include(":core")
include(":client:polling")
include(":client:websocket")
include(":module:logging")
include(":module:roleplay")
include(":util:paste:pastee")
include(":util:paste:pastebin")
include(":util:paste:hastebin")
include(":util:json-schema-serialization")
include(":module:paste")
include(":sample")
