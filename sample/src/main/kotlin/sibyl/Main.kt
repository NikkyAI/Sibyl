package sibyl

import LoggingModule
import RoleplayModule
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import kotlinx.coroutines.*
import sibyl.test.TestModule
import mu.KotlinLogging
import sibyl.client.PollingClient
import kotlinx.serialization.json.Json

val jsonSerializer = Json {
    prettyPrint = true
    encodeDefaults = false
}
val jsonSerializerCompact = Json {
    prettyPrint = false
    encodeDefaults = false
}

private val logger = KotlinLogging.logger {}
fun main(args: Array<String>) {
    val client = HttpClient(OkHttp) {
//        install(WebSockets)
    }

    val messageProcessor = MessageProcessor().apply {
        addModule(LoggingModule())
        addModule(RoleplayModule())
        addModule(TestModule())
    }

    runBlocking {
        // TODO: drop all history on first connect
        val (send, receive) = PollingClient.connect(client, "localhost", 4242/*, token = "mytoken"*/)

        logger.info { "client started" }
        messageProcessor.start(send = send, receive = receive)
    }
}
