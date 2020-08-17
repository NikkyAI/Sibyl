package sibyl

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import io.ktor.content.*
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import sibyl.pastee.PasteeService
import io.ktor.http.*
import kotlinx.serialization.Serializable
import sibyl.haste.HasteService


fun main(args: Array<String>) {
    val logger = KotlinLogging.logger {}
    val client = HttpClient(OkHttp)
    runBlocking {
        val hasteServer = "https://hastebin.com"
        val url = HasteService.paste(
            client,
            hasteServer = hasteServer,
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