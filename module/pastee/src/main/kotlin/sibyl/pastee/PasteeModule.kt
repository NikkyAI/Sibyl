package sibyl.pastee

import io.ktor.client.*
import sibyl.MessageProcessor
import sibyl.ResponseMessage
import sibyl.SibylModule
import sibyl.Stage
import sibyl.core.CoreModule
import sibyl.pastee.paste.Paste
import sibyl.pastee.paste.PasteSection

class PasteeModule(
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
            val pasteResponse = PasteeService.paste(
                client,
                Paste(
                    description = "",
                    sections = listOf(
                        PasteSection(
                            contents = message.text
                        )
                    )
                )
            )
            require(pasteResponse.success) { "publishing paste was not successful" }

            return response.copy(
                message = response.message.copy(
                    text = pasteResponse.link
                )
            )
        }

        return response
    }
}