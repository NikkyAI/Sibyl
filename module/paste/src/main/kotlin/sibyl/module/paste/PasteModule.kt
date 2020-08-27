package sibyl.module.paste

import mu.KotlinLogging
import sibyl.MessageProcessor
import sibyl.ResponseMessage
import sibyl.SibylModule
import sibyl.Stage
import sibyl.core.CoreModule

class PasteModule(
    private val pasteService: PasteService,
    private val maxLines: Int = 2
) : SibylModule("pastee") {
    companion object {
        private val logger = KotlinLogging.logger {}
    }
    override fun MessageProcessor.install() {
        require(messageProcessor.hasModule(CoreModule::class)) { "core module is not installed" }

        registerOutgoingInterceptor(Stage.FILTER, interceptor = ::shortenOutgoing)
    }

    private suspend fun shortenOutgoing(response: ResponseMessage, stage: Stage): ResponseMessage? {
        val message = response.message
        if (message.text.lines().size > maxLines) {
            val pasteResponse = pasteService.paste(
                content = message.text
            )
            if(pasteResponse != null) {
                return response.copy(
                    message = response.message.copy(
                        text = pasteResponse
                    )
                )
            } else {
                logger.error("paste service failed")
            }
        }

        return response
    }
}