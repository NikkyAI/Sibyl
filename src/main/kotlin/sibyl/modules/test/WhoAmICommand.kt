package sibyl.modules.test

import sibyl.api.ApiMessage
import sibyl.commands.SibylCommand
import kotlinx.coroutines.channels.SendChannel

class WhoAmICommand : SibylCommand(
    name = "whoami",
    help = "displays userid on the current platform"
) {
    override fun run() {
        echo(message.userid + " " + message.account)
    }
}