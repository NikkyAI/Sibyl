package sibyl.test

import org.gradle.api.Plugin
import org.gradle.api.Project

open class SibylTestPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.logger.lifecycle("loaded test")
    }
}