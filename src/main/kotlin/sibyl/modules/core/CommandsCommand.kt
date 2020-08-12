package sibyl.modules.core

import sibyl.MessageProcessor
import sibyl.api.ApiMessage
import sibyl.commands.SibylCommand
import kotlinx.coroutines.channels.SendChannel
import mu.KotlinLogging

class CommandsCommand(private val messageProcessor: MessageProcessor) : SibylCommand(
    name = "commands",
    help = "lists command names grouped by module",
    invokeWithoutSubcommand = true
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }
    override fun run() {
        messageProcessor.modules
            .filter { it.commands.isNotEmpty() }
            .forEach { module ->
                logger.info { "appending commands for ${module.name}"}
                echo("${module.name}: ${module.commands.joinToString(" ") { module.commandPrefix + it.commandName }}")
            }
    }

}