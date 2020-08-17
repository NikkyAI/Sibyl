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
import sibyl.core.CoreModule
import sibyl.haste.HasteModule
import sibyl.pastebin.PastebinModule
import sibyl.pastee.PasteeModule
import java.io.File

val jsonSerializer = Json {
    prettyPrint = true
    encodeDefaults = false
}
val jsonSerializerCompact = Json {
    prettyPrint = false
    encodeDefaults = false
}
private val client = HttpClient(OkHttp) {
//        install(WebSockets)
}
val messageProcessor = MessageProcessor().apply {
    addModule(LoggingModule())
    addModule(HasteModule(client))
    addModule(RoleplayModule())
    addModule(TestModule())
}

private val logger = KotlinLogging.logger {}
fun main(args: Array<String>) {
    System.setProperty(DEBUG_PROPERTY_NAME, DEBUG_PROPERTY_VALUE_ON)

    val connectData = File("connect.json").takeIf { it.exists() }?.readText()?.let {
        jsonSerializerPretty.decodeFromString(ConnectData.serializer(), it)
    } ?: ConnectData(
        controlGateway = "matterbridgetest",
        host = "localhost",
        port = 4242,
        token = null
    )

    runBlocking {
        // TODO: drop all history on first connect
        val (send, receive) = PollingClient.connectPolling(
            client,
            host = connectData.host,
            port = connectData.port,
            token = connectData.token
        )

        messageProcessor.getModule(CoreModule::class)?.controlGateway = connectData.controlGateway

        logger.info { "client started" }
        messageProcessor.start(send = send, receive = receive)
    }
}
