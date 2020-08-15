package sibyl.test

import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.arguments.transformAll
import sibyl.commands.SibylCommand
import kotlin.system.exitProcess

class ExitCommand : SibylCommand(
//    name = "exit",
    invokeWithoutSubcommand = true
) {
    override fun run() {
        require(message.username == "Nikky") {"needs to be sent by admin"}
        exitProcess(0)
    }
}