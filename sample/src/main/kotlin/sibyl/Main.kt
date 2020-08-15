package sibyl

import LogModule
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.websocket.*
import kotlinx.coroutines.*
import sibyl.test.TestModule
import mu.KotlinLogging
import sibyl.client.PollingClient

private val logger = KotlinLogging.logger {}
fun main(args: Array<String>) {
    val client = HttpClient(OkHttp) {
//            install(JsonFeature) {
//                serializer = KotlinxSerializer()
//            }
        install(WebSockets)
    }

    val messageProcessor = MessageProcessor().apply {
        addModule(LogModule())
        addModule(TestModule())
    }

    runBlocking {
        val (send, receive) = PollingClient.connect(client, "localhost", 4242, token = "mytoken")

        logger.info { "client started" }
        messageProcessor.start(send = send, receive = receive)
    }
}
