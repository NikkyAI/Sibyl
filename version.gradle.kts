
fun captureExec(configure: ExecSpec.() -> Unit): String? {
    val stdout = java.io.ByteArrayOutputStream()
    try {
        exec {
            configure()
            standardOutput = stdout
        }
    } catch (e: org.gradle.process.internal.ExecException) {
        logger.error(e.message)
        return null
    }
    return stdout.toString()
}

// tag or commit hash
val currentTagOrHash = captureExec {
    commandLine("git", "describe", "--tags", "--always")
}?.trim()?.substringAfterLast('-')

// current or last tag
val currentOrLastTag = captureExec {
    commandLine("git", "describe", "--abbrev=0", "--tags")
}?.trim() ?: "v0.0.0"

val describeTags = captureExec {
    commandLine("git", "describe", "--tags")
}?.trim() ?: "v0.0.0"

val describeAbbrevAlways = captureExec {
    commandLine("git", "describe", "--abbrev=0", "--always")
}?.trim() ?: "v0.0.0"

logger.lifecycle("describeTagsAlways: '$currentTagOrHash'")
logger.lifecycle("tag: '$currentOrLastTag'")
logger.lifecycle("tag2: '$describeTags'")
logger.lifecycle("commit-hash: '$describeAbbrevAlways'")

val currentIsATag = currentTagOrHash != currentOrLastTag
val isSnapshot = currentIsATag && currentOrLastTag.startsWith("v")
val versionStr: String = if (isSnapshot) {
    val lastVersion = currentOrLastTag.substringAfter("v")
    var (major, minor, patch) = lastVersion.split('.').map { it.toInt() }
    patch++
    val nextVersion = "$major.$minor.$patch"
//    bintrayRepository = "snapshot"

//    "$nextVersion-SNAPSHOT"
    "$nextVersion-dev+$currentTagOrHash"
} else {
    extra["vcsTag"] = currentTagOrHash
    currentOrLastTag.substringAfter('v')
}
extra["isSnapshot"] = isSnapshot

project.version = versionStr
logger.lifecycle("project.version: ${project.version}")


