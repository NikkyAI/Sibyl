package sibyl

import sibyl.api.ApiMessage
import sibyl.commands.SibylCommand

abstract class SibylModule(
    val name: String,
    val commandPrefix: String = "!"
) {
    open val commands: List<SibylCommand> = listOf()

    lateinit var sendMessage: suspend (ApiMessage) -> Unit
        private set

    fun init(messageProcessor: MessageProcessor, sendMessage: suspend (ApiMessage) -> Unit) {
        this.sendMessage = sendMessage
        with(messageProcessor) {
            start()
        }
    }

    open fun MessageProcessor.start() {

    }

    // TODO pre check interceptor (can check for permissions on all commands from a module)
}