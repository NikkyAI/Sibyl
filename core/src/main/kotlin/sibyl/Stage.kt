package sibyl

data class Stage(
    val name: String,
    val priority: Int
) {
    companion object {

        val PRE_FILTER = Stage("PRE_FILTER", -1)
        val FILTER = Stage("FILTER", 0)
        val POST_FILTER = Stage("POST_FILTER", 1)

        val PRE_COMMAND = Stage("PRE_COMMAND", 2)
        val COMMANDS = Stage("COMMANDS", 3)
        val POST_COMMAND = Stage("POST_COMMAND", 4)
    }
}