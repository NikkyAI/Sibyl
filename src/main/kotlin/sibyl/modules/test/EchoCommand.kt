package sibyl.modules.test

import sibyl.api.ApiMessage
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.arguments.transformAll
import sibyl.commands.SibylCommand
import kotlinx.coroutines.channels.SendChannel

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