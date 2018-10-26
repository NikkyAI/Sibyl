private fun create(
    group: String,
    name: String,
    version: String? = null
): String = buildString {
    append(group)
    append(':')
    append(name)
    version?.let {
        append(':')
        append(it)
    }
}


object Kotlin {
    const val version = "1.3.0-rc-190"
}

object Coroutines {
    const val version = "1.0.0-RC1"
    val dependency = create(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = version)
}

object Serialization {
    const val version = "0.8.2-rc13"
    const val plugin = "kotlinx-serialization"
    const val module = "org.jetbrains.kotlinx:kotlinx-gradle-serialization-plugin"
    val dependency = create(group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-runtime", version = version)
}

object Fuel {
    const val version = "1.16.0"
    val dependency = create(group = "com.github.kittinunf.fuel", name = "fuel", version = version)
    val dependencyCoroutines = create(group = "com.github.kittinunf.fuel", name = "fuel-coroutines", version = version)
    val dependencySerialization = create(group = "com.github.kittinunf.fuel", name = "fuel-kotlinx-serialization", version = version)
}

object Logging {
    const val version = "1.6.10"

    val dependency = create(group = "io.github.microutils", name = "kotlin-logging", version = version)
    val dependencyLogbackClassic = create(group = "ch.qos.logback", name = "logback-classic", version = "1.3.0-alpha4")
}