package modules.core

import MessageProcessor
import api.ApiMessage
import commands.SibylCommand

class CommandsCommand(private val messageProcessor: MessageProcessor) : SibylCommand(
    name = "commands",
    help = "lists command names grouped by module"
) {
    override fun run(message: ApiMessage) {
        messageProcessor.modules
            .filter { it.commands.isNotEmpty() }
            .forEach { module ->
                echo("${module.name}: ${module.commands.joinToString(" ") { module.commandPrefix + it.commandName }}")
            }
    }

}