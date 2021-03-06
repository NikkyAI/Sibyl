package sibyl.haste
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.content.*
import io.ktor.http.*
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import sibyl.module.paste.PasteService

class HasteService(
    val httpClient: HttpClient,
    val hasteServer: String = "https://hastebin.com"
): PasteService {
    companion object {
        private val logger = KotlinLogging.logger {}
        private val jsonSerializer = Json {
            prettyPrint = false
            encodeDefaults = false
        }
    }

    override suspend fun paste(content: String): String? {

        val url = withTimeoutOrNull(1000) {
            val jsonResponse = httpClient.post<String>("$hasteServer/documents") {
                body = TextContent(
                    text = content,
                    contentType = ContentType.Text.Plain
                )
            }

            val response = jsonSerializer.decodeFromString(HasteServerResponse.serializer(), jsonResponse)

            val url = "$hasteServer/${response.key}"

            logger.debug { "response: $url" }
            url
        }
        return url
    }
}