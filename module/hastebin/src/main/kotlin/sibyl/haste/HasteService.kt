package sibyl.haste
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.content.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import mu.KotlinLogging

object HasteService {
    private val logger = KotlinLogging.logger {}
    private val jsonSerializer = Json {
        prettyPrint = false
        encodeDefaults = false
    }

    suspend fun paste(httpClient: HttpClient, hasteServer: String, content: String): String {

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