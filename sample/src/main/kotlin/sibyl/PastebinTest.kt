package sibyl

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import sibyl.pastebin.PastebinService

fun main(args: Array<String>) {
    val logger = KotlinLogging.logger {}
    val client = HttpClient(OkHttp)
    runBlocking {
        val response = PastebinService(
            httpClient = client
        ).paste(
            content = """
                just
                some
                multiline
                test
                """.trimIndent()
        )

        logger.info { "response: $response" }
        logger.info { "response: ${PastebinService.toRaw(response)}" }
    }
}