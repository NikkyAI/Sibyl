plugins {
//    id("com.jfrog.bintray") apply false
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}

repositories {
    google()
    mavenCentral()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    gradlePluginPortal()
}

dependencies {
    // base
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:_")

    //db
    implementation("org.flywaydb.flyway:org.flywaydb.flyway.gradle.plugin:_")
    implementation("com.squareup.sqldelight:com.squareup.sqldelight.gradle.plugin:_")
}

gradlePlugin {
    plugins {
        create("sibylBase") {
            id = "moe.nikky.sibyl"
            implementationClass = "sibyl.SibylBasePlugin"
        }
        create("sibylDatabase") {
            id = "moe.nikky.sibyl.database"
            implementationClass = "sibyl.db.SibylDatabasePlugin"
        }
    }
}

group = "moe.nikky.sibyl"
description = "gradle plugins for building sibyl modules"

val bintrayOrg: String? = System.getenv("BINTRAY_USER")
val bintrayApiKey: String? = System.getenv("BINTRAY_API_KEY")
var bintrayRepository = "github"
val bintrayPackage = "sibyl"

group = "moe.nikky.sibyl"
description = "modular chatbot framework for matterbridge"

apply(from="${rootDir.parentFile.path}/version.gradle.kts")
val isSnapshot = extra["isSnapshot"] as Boolean
if(isSnapshot) {
    bintrayRepository = "snapshot"
}

val artifactId = project.properties["POM_ARTIFACT_ID"] ?: "plugin" // project.path.drop(1).replace(':', '-')
val publicationName = "sibylPlugin"
val sourcesJar by tasks.creating(Jar::class) {
    dependsOn(JavaPlugin.CLASSES_TASK_NAME)
    archiveClassifier.set("sources")
    val sourceSets = project.extensions.getByName("sourceSets") as SourceSetContainer
    from(sourceSets["main"].allSource)
}
val javadocJar by tasks.creating(Jar::class) {
    dependsOn(JavaPlugin.JAVADOC_TASK_NAME)
    archiveClassifier.set("javadoc")
    from(tasks["javadoc"])
}
configure<PublishingExtension> {
    publications {
        create<MavenPublication>(publicationName) {
            from(components["kotlin"])
            artifact(sourcesJar)
            artifact(javadocJar)
            this.artifactId = artifactId
        }
    }
    repositories {
        if (bintrayOrg != null && bintrayApiKey != null) {
            maven(url = "https://api.bintray.com/maven/$bintrayRepository/$bintrayPackage/$artifactId/;publish=0;override=0") {
                name = "bintray"
                credentials {
                    username = bintrayOrg
                    password = bintrayApiKey
                }
            }
        }
    }
}
//if (bintrayOrg == null || bintrayApiKey == null) {
//    logger.error("bintray credentials not configured properly")
//} else {
//    project.apply(plugin = "com.jfrog.bintray")
//    configure<com.jfrog.bintray.gradle.BintrayExtension> {
//        user = bintrayOrg
//        key = bintrayApiKey
//        publish = true
//        override = false
//        dryRun = !properties.containsKey("nodryrun")
////        dryRun = true // TODO: disable on github actions
//        setPublications(publicationName)
//        pkg(delegateClosureOf<com.jfrog.bintray.gradle.BintrayExtension.PackageConfig> {
//            repo = bintrayRepository
//            name = bintrayPackage
//            userOrg = bintrayOrg
//            version = VersionConfig().apply {
//                // do not put commit hashes in vcs tag
//                if (!isSnapshot) {
//                    vcsTag = extra["vcsTag"] as String
//                }
////                vcsTag = describeAbbrevAlwaysTags
//                name = project.version as String
//                githubReleaseNotesFile = "RELEASE_NOTES.md"
//            }
//        })
//    }
//}