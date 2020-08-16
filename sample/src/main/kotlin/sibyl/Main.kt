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
val messageProcessor = MessageProcessor().apply {
    addModule(LoggingModule())
    addModule(RoleplayModule())
    addModule(TestModule())
}

private val logger = KotlinLogging.logger {}
fun main(args: Array<String>) {
    System.setProperty(DEBUG_PROPERTY_NAME, DEBUG_PROPERTY_VALUE_ON)
    val client = HttpClient(OkHttp) {
//        install(WebSockets)
    }

    runBlocking {
        // TODO: drop all history on first connect
        val (send, receive) = PollingClient.connectPolling(client, "localhost", 4242/*, token = "mytoken"*/)

        logger.info { "client started" }
        messageProcessor.start(send = send, receive = receive)
    }
}
