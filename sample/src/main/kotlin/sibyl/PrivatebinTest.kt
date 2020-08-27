package sibyl

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import sibyl.privatebin.PrivatebinService

fun main(args: Array<String>) {


    val logger = KotlinLogging.logger {}
    val client = HttpClient(OkHttp)
    runBlocking {
        val service = PrivatebinService(client)

        val encrypted = service.paste(
            """test""".trimIndent()
        )
        logger.info { "encrypted: $encrypted" }
    }

}