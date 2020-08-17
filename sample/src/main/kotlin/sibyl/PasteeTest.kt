package sibyl

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import sibyl.pastee.paste.Paste
import sibyl.pastee.paste.PasteSection
import sibyl.pastee.PasteeService

fun main(args: Array<String>) {
    val logger = KotlinLogging.logger {}
    val client = HttpClient(OkHttp)
    runBlocking {
        val syntaxes = PasteeService.syntaxes(client)
        syntaxes.syntaxes.forEach { syntax ->
            logger.info { "${syntax.id}: `${syntax.short}` ${syntax.name}" }
        }

        val response = PasteeService.paste(client, Paste(
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
    }

}