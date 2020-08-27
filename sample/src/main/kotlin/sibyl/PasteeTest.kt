package sibyl

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import sibyl.pastee.PasteeService
import sibyl.pastee.paste.Paste
import sibyl.pastee.paste.PasteSection

fun main(args: Array<String>) {
    val logger = KotlinLogging.logger {}
    val client = HttpClient(OkHttp)
    runBlocking {
        val service = PasteeService(client)

        val syntaxes = service.syntaxes()
        syntaxes.syntaxes.forEach { syntax ->
            logger.info { "${syntax.id}: `${syntax.short}` ${syntax.name}" }
        }

        val response = service.paste(
            Paste(
                description = "test paste",
                sections = listOf(
                    PasteSection(
                        name = "text.json",
                        syntax = "autodetect",
                        contents = """
                        just
                        some
                        multiline
                        test
                    """.trimIndent()
                    )
                ),
                encrypted = true
            )
        )

        logger.info { "response: $response" }

        val urlResponse = service.paste(
            content = """
                just
                some
                multiline
                test
                """.trimIndent()
        )

        logger.info { "urlResponse: $urlResponse" }
    }

}