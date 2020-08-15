package sibyl

data class Stage(
    val name: String,
    val priority: Int
) {
    companion object {
        val COMMANDS = Stage("COMMANDS", 3)
    }
}