plugins {
//    id("org.gradle.kotlin.kotlin-dsl")
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}

dependencies {
    api("org.jetbrains.kotlin:kotlin-gradle-plugin:_")
}

gradlePlugin {
    plugins {
        create("sibylBase") {
            id = "moe.nikky.sibyl.plugin.base"
            implementationClass = "sibyl.SibylBasePlugin"
        }
    }
}