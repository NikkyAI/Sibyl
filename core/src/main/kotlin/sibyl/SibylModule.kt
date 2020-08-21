package sibyl

import sibyl.api.ApiMessage
import sibyl.commands.SibylCommand

abstract class SibylModule(
    val name: String,
    val description: String = "missing description",
    val commandPrefix: String = "!"
) {
    open val commands: List<SibylCommand> = listOf()

    protected lateinit var messageProcessor: MessageProcessor
        private set
    private lateinit var sendResponse: suspend (ResponseMessage, Stage?) -> Unit

    suspend fun sendMessage(message: ApiMessage, stage: Stage? = null, fromCommand: SibylCommand? = null) {
        sendResponse.invoke(
            ResponseMessage(
                message,
                this,
                fromCommand
            ),
            stage
        )
    }

    fun init(messageProcessor: MessageProcessor, sendResponse: suspend (ResponseMessage, Stage?) -> Unit) {
        this.sendResponse = sendResponse
        this.messageProcessor = messageProcessor
        with(messageProcessor) {
            install()
        }
    }

    open fun MessageProcessor.install() {

    }
    open suspend fun start() {

    }

    // TODO pre check interceptor (can check for permissions on all commands from a module)
}