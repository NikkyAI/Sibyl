package sibyl

fun String.removeBlankLines() = lineSequence().filter { it.isNotBlank() }.joinToString("\n")