package sibyl.modules.core

import sibyl.MessageProcessor
import sibyl.Stage
import sibyl.api.ApiMessage
import sibyl.commands.SibylCommand
import sibyl.modules.SibylModule

class CoreModule(private val messageProcessor: MessageProcessor) : SibylModule("core") {
    override val commands: List<SibylCommand> = listOf(
        CommandsCommand(messageProcessor)
//        HelpCommand(messageProcessor)
    )

    val CHECK = Stage("CHECK", 0)

    override fun MessageProcessor.start(sendMessage: suspend (ApiMessage) -> Unit) {
        registerOutgoingInterceptor(CHECK, ::transformOutgoing)
    }

    suspend fun transformOutgoing(message: ApiMessage): ApiMessage? {
        return message.copy(
            userid = messageProcessor.userid,
            channel = "api"
        )
    }
}