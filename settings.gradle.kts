import de.fayard.dependencies.bootstrapRefreshVersionsAndDependencies
buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies.classpath("de.fayard:dependencies:0.5.8")
}

bootstrapRefreshVersionsAndDependencies(
    listOf(rootDir.resolve("dependencies-rules.txt").readText())
)

rootProject.name = "Sibyl"
