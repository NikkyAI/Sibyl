plugins {
    id("org.gradle.kotlin.kotlin-dsl")
    `java-gradle-plugin`
    `maven-publish`
}

gradlePlugin {
    plugins {
        create("testPlugin") {
            id = "moe.nikky.sibyl.plugin.test"
            implementationClass = "sibyl.test.SibylTestPlugin"
        }
    }
}