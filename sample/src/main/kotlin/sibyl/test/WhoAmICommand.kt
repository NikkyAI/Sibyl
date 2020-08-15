package sibyl.test

import sibyl.commands.SibylCommand

class WhoAmICommand : SibylCommand(
    name = "whoami",
    help = "displays userid on the current platform"
) {
    override fun run() {
        echo(message.userid + " " + message.account)
    }
}