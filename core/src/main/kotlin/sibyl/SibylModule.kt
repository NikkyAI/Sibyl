package sibyl

import sibyl.api.ApiMessage
import sibyl.commands.SibylCommand

abstract class SibylModule(
    val name: String,
    val commandPrefix: String = "!"
) {
    open val commands: List<SibylCommand> = listOf()

    private lateinit var sendResponse: suspend (ResponseMessage, Stage?) -> Unit

    suspend fun sendMessage(message: ApiMessage, fromCommand: SibylCommand? = null, stage: Stage? = null) {
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
        with(messageProcessor) {
            setup()
        }
    }

    open fun MessageProcessor.setup() {

    }
    open fun start() {

    }

    // TODO pre check interceptor (can check for permissions on all commands from a module)
}