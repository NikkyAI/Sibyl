package sibyl.modules.core

import sibyl.MessageProcessor
import sibyl.commands.SibylCommand
import sibyl.modules.SibylModule

class CoreModule(messageProcessor: MessageProcessor) : SibylModule("core") {
    override val commands: List<SibylCommand> = listOf(
        CommandsCommand(messageProcessor)
//        HelpCommand(messageProcessor)
    )

}