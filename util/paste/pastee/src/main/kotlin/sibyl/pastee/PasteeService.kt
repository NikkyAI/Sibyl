package sibyl.pastee

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.content.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import sibyl.pastee.paste.Paste
import sibyl.pastee.paste.PasteResponse
import sibyl.pastee.paste.PasteSection
import sibyl.pastee.syntax.SyntaxesResponse

class PasteeService(
    val httpClient: HttpClient,
    val apiKey: String = DEFAULT_KEY
) : PasteService {
    companion object {
        private val logger = KotlinLogging.logger {}
        private const val DEFAULT_KEY = "uKJoyicVJFnmpnrIZMklOURWxrCKXYaiBWOzPmvon"
        private val jsonSerializer = Json {
            prettyPrint = false
            encodeDefaults = false
        }
    }

    override suspend fun paste(content: String): String {
        val pasteResponse = paste(
            Paste(
                sections = listOf(
                    PasteSection(
                        contents = content
                    )
                )
            )
        )
        require(pasteResponse.success) { "paste was not successful" }
        return pasteResponse.link
    }

    suspend fun paste(paste: Paste): PasteResponse {
        val jsonResponse = httpClient.post<String>("https://api.paste.ee/v1/pastes") {
            body = TextContent(
                text = jsonSerializer.encodeToString(Paste.serializer(), paste),
                contentType = ContentType.Application.Json.withCharset(Charsets.UTF_8)
            )
            header("X-Auth-Token", apiKey)
        }

        logger.debug { "response: $jsonResponse" }
        return jsonSerializer.decodeFromString(PasteResponse.serializer(), jsonResponse)
    }

    suspend fun syntaxes(): SyntaxesResponse {
        val jsonResponse = httpClient.get<String>("https://api.paste.ee/v1/syntaxes") {
            header("X-Auth-Token", apiKey)
        }

        logger.debug { "response: $jsonResponse" }
        return jsonSerializer.decodeFromString(SyntaxesResponse.serializer(), jsonResponse)
    }
}