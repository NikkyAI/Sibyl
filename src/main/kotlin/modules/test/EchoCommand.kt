package modules.test

import api.ApiMessage
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.arguments.transformAll
import commands.SibylCommand

class EchoCommand : SibylCommand(
    name = "echo",
    invokeWithoutSubcommand = true
) {
    val words by argument("words").multiple().transformAll { words ->
        words.joinToString(" ")
    }
    override fun run(message: ApiMessage) {
        echo(words)
    }
}