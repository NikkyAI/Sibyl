import mu.KotlinLogging
import sibyl.MessageProcessor
import sibyl.Stage
import sibyl.api.ApiMessage
import sibyl.SibylModule

class LogModule : SibylModule("log") {
    companion object {
        private val logger = KotlinLogging.logger {}

        val LOGGING = Stage("LOGGING", 1)
    }
    override val commands = listOf(
        LogCommand()
    )

    override fun MessageProcessor.start() {
        registerIncomingInterceptor(LOGGING, ::processRequest)
        registerOutgoingInterceptor(LOGGING, ::processResponse)
    }

    suspend fun processRequest(message: ApiMessage): ApiMessage {
        logger.info { "-> $message" }
        return message
    }
    suspend fun processResponse(message: ApiMessage): ApiMessage {
        logger.info { "<- $message" }
        return message
    }
}