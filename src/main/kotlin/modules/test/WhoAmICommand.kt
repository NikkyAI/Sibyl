package modules.test

import api.ApiMessage
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import commands.SibylCommand

class WhoAmICommand : SibylCommand(
    name = "whoami"
) {
    override fun run(message: ApiMessage) {
        echo(message.userid)
    }
}