package sibyl.haste
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.content.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import sibyl.pastee.PasteService

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

    override suspend fun paste(content: String): String {

        val jsonResponse = httpClient.post<String>("$hasteServer/documents") {
            body = TextContent(
                text = content,
                contentType = ContentType.Text.Plain
            )
        }

        val response = jsonSerializer.decodeFromString(HasteServerResponse.serializer(), jsonResponse)

        val url = "$hasteServer/${response.key}"

        logger.debug { "response: $url" }
        return url
    }
}