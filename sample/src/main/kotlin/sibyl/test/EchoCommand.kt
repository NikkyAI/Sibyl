package sibyl.test

import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.arguments.transformAll
import sibyl.commands.SibylCommand

class EchoCommand : SibylCommand(
    name = "echo",
    invokeWithoutSubcommand = true
) {
    val words by argument("words").multiple().transformAll { words ->
        words.joinToString(" ")
    }
    override fun run() {
        echo(words)
    }
}