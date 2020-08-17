package sibyl.pastebin

import io.ktor.client.*
import sibyl.MessageProcessor
import sibyl.ResponseMessage
import sibyl.SibylModule
import sibyl.Stage
import sibyl.core.CoreModule

class PastebinModule(
    private val client: HttpClient,
    private val maxLines: Int = 2
) : SibylModule("pastee") {
    override fun MessageProcessor.setup() {
        require(messageProcessor.hasModule(CoreModule::class)) { "core module is not installed" }

        registerOutgoingInterceptor(Stage.FILTER, interceptor = ::shortenOutgoing)
    }

    override fun start() {

    }

    private suspend fun shortenOutgoing(response: ResponseMessage, stage: Stage): ResponseMessage? {
        val message = response.message
        if (message.text.lines().size > maxLines) {
            val urlResponse = PastebinService.paste(
                client,
                PastebinRequest(
                    content = message.text
                )
            )

            return response.copy(
                message = response.message.copy(
                    text = PastebinService.toRaw(urlResponse)
                )
            )
        }

        return response
    }
}