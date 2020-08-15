package sibyl.core

import sibyl.MessageProcessor
import sibyl.api.ApiMessage
import sibyl.commands.SibylCommand
import kotlinx.coroutines.channels.SendChannel
import mu.KotlinLogging

class ModulesCommand(private val messageProcessor: MessageProcessor) : SibylCommand(
    name = "modules",
    help = "lists modules",
    invokeWithoutSubcommand = true
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }
    override fun run() {
        messageProcessor.modules
            .forEach { module ->
                echo("${module.name}: ${module.description}")
            }
    }

}