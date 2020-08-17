package sibyl.pastee
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.content.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import sibyl.pastee.paste.Paste
import sibyl.pastee.paste.PasteResponse
import sibyl.pastee.syntax.SyntaxesResponse

object PasteeService {
    private val logger = KotlinLogging.logger {}
    private const val DEFAULT_KEY = "uKJoyicVJFnmpnrIZMklOURWxrCKXYaiBWOzPmvon"
    private val jsonSerializer = Json {
        prettyPrint = false
        encodeDefaults = false
    }

    suspend fun paste(httpClient: HttpClient, paste: Paste, key: String? = null): PasteResponse {
        val apiKey = key ?: DEFAULT_KEY

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

    suspend fun syntaxes(httpClient: HttpClient, key: String? = null): SyntaxesResponse {
        val apiKey = key ?: DEFAULT_KEY

        val jsonResponse = httpClient.get<String>("https://api.paste.ee/v1/syntaxes") {
            header("X-Auth-Token", apiKey)
        }

        logger.debug { "response: $jsonResponse" }
        return jsonSerializer.decodeFromString(SyntaxesResponse.serializer(), jsonResponse)
    }
}