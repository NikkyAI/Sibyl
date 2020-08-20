package sibyl.test

import sibyl.commands.SibylCommand
import kotlin.system.exitProcess

class ExitCommand : SibylCommand(
//    name = "exit",
    invokeWithoutSubcommand = true
) {
    override fun run() {
        require(causeMessage.username == "Nikky") {"needs to be sent by admin"}
        exitProcess(0)
    }
}