package sibyl

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

open class SibylBasePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.configure()
    }

    private fun Project.configure() {
        repositories {
            jcenter()
            mavenCentral()
            maven(url = "https://kotlin.bintray.com/ktor")
            maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
        }

        apply(plugin="org.jetbrains.kotlin.jvm")

        tasks {
            val compileKotlin by existing(KotlinCompile::class) { kotlinOptions.jvmTarget = "1.8" }
            val compileTestKotlin by existing(KotlinCompile::class) { kotlinOptions.jvmTarget = "1.8" }
        }

    }
}