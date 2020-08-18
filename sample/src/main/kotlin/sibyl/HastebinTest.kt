package sibyl

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import sibyl.haste.HasteService


fun main(args: Array<String>) {
    val logger = KotlinLogging.logger {}
    val client = HttpClient(OkHttp)
    runBlocking {
        val hasteServer = "https://hastebin.com"
        val url = HasteService(
            httpClient = client,
            hasteServer = hasteServer
        ).paste(
            content = """
                    some
                    multiline
                    test
                    string
                """.trimIndent(),
        )

        logger.info { "url: $url" }
    }

}