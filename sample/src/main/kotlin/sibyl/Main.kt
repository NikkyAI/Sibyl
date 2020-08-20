package sibyl

import RoleplayModule
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import kotlinx.coroutines.DEBUG_PROPERTY_NAME
import kotlinx.coroutines.DEBUG_PROPERTY_VALUE_ON
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import sibyl.client.PollingClient
import sibyl.config.ConfigUtil
import sibyl.haste.HasteService
import sibyl.module.logging.LoggingModule
import sibyl.module.paste.PasteModule
import sibyl.test.TestModule
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
    addModule(PasteModule(HasteService(client)))
    addModule(RoleplayModule())
    addModule(TestModule())
}

private val logger = KotlinLogging.logger {}
fun main(args: Array<String>) {
    System.setProperty(DEBUG_PROPERTY_NAME, DEBUG_PROPERTY_VALUE_ON)
    System.setProperty("org.jooq.no-logo", "true")

    val connectData = ConfigUtil.load(
        file = File("sample.json"),
        serializer = SampleConfig.serializer()
    ) {
        SampleConfig(
            host = "localhost",
            port = 4242,
            token = null
        )
    }

    runBlocking {
        // TODO: drop all history on first connect
        val (send, receive) = PollingClient.connectPolling(
            client,
            host = connectData.host,
            port = connectData.port,
            token = connectData.token
        )

        logger.info { "client started" }
        messageProcessor.start(send = send, receive = receive)
    }
}
