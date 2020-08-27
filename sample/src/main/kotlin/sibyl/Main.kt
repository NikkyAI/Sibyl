package sibyl

import RoleplayModule
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.websocket.*
import kotlinx.coroutines.DEBUG_PROPERTY_NAME
import kotlinx.coroutines.DEBUG_PROPERTY_VALUE_ON
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import sibyl.client.PollingClient
import sibyl.client.WebsocketClient
import sibyl.config.ConfigUtil
import sibyl.haste.HasteService
import sibyl.module.fail.FailTestModule
import sibyl.module.fail.FailOnLoadModule
import sibyl.module.logging.LoggingModule
import sibyl.module.paste.PasteModule
import sibyl.module.accounts.AccountsModule
import sibyl.module.reminders.RemindersModule
import sibyl.privatebin.PrivatebinResponse
import sibyl.privatebin.PrivatebinService
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
    install(WebSockets)
}
val messageProcessor = MessageProcessor().apply {
    addModule(AccountsModule())
    addModule(LoggingModule())
    addModule(RemindersModule())
    addModule(PasteModule(PrivatebinService(client)))
    addModule(RoleplayModule())
//    addModule(FailOnLoadModule())
//    addModule(FailTestModule())
    addModule(TestModule())
}

private val logger = KotlinLogging.logger {}
fun main(args: Array<String>) {
    System.setProperty(DEBUG_PROPERTY_NAME, DEBUG_PROPERTY_VALUE_ON)
    System.setProperty("org.jooq.no-logo", "true")

    val sampleConfig = ConfigUtil.load(
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
        val (send, receive) = if(sampleConfig.useWebsocket) {
            WebsocketClient.connectWebsocket(
                client,
                host = sampleConfig.host,
                port = sampleConfig.port,
                token = sampleConfig.token
            )
        } else {
            PollingClient.connectPolling(
                client,
                host = sampleConfig.host,
                port = sampleConfig.port,
                token = sampleConfig.token
            )
        }


        logger.info { "client started" }
        messageProcessor.start(send = send, receive = receive)
    }
}
