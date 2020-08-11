package modules.core

import MessageProcessor
import commands.SibylCommand
import modules.SibylModule

class CoreModule(messageProcessor: MessageProcessor) : SibylModule("core") {
    override val commands: List<SibylCommand> = listOf(
        CommandsCommand(messageProcessor)
    )

}