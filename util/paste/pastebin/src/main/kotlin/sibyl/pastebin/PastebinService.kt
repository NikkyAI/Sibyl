package sibyl.pastebin
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import mu.KotlinLogging
import sibyl.pastee.PasteService

class PastebinService(
    val httpClient: HttpClient,
    val apiKey: String = DEFAULT_KEY
): PasteService {
    companion object {
        private val logger = KotlinLogging.logger {}
        private const val DEFAULT_KEY = "2d458134442ff80767f32c873c37e441"

        fun toRaw(url: String): String {
            val code = url.substringAfterLast("https://pastebin.com/")
            return "https://pastebin.com/raw/$code"
        }
    }

    suspend fun paste(paste: PastebinRequest): String {

        val urlResponse = httpClient.post<String>("https://pastebin.com/api/api_post.php") {
            body = FormDataContent(Parameters.build {
                append("api_dev_key", apiKey)
                append("api_option", "paste")
                append("api_paste_code", paste.content)
                paste.userKey?.let { append("api_user_key", it) }
                paste.name?.let { append("api_paste_name", it) }
                paste.format?.let { append("api_paste_format", it) }
                append("api_paste_private", paste.visibility.code.toString())
                append("api_paste_expire_date", paste.expire.code)
            })
        }

        logger.debug { "response: $urlResponse" }
        return urlResponse
    }
    override suspend fun paste(content: String): String = paste(
            PastebinRequest(
                content = content
            )
        )

}