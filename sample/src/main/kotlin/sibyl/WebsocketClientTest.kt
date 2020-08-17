package sibyl

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.websocket.*
import kotlinx.coroutines.*
import mu.KotlinLogging
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import sibyl.client.WebsocketClient
import java.util.*


object WebsocketClientTest {
    private val logger = KotlinLogging.logger {}

    @ExperimentalCoroutinesApi
    @JvmStatic
    fun main(args: Array<String>) {
        System.setProperty(DEBUG_PROPERTY_NAME, DEBUG_PROPERTY_VALUE_ON)

        val dt = DateTime.now()
        val fmt = ISODateTimeFormat.dateTime()
        val timestamp = fmt.print(dt)
        val timestampNow = DateTime.now().toString()

        logger.info { timestamp }
        logger.info { timestampNow }

        val client = HttpClient(OkHttp) {
            install(WebSockets)
        }

        runBlocking {
            val (send, receive) = WebsocketClient.connectWebsocket(
                client, "localhost", 4242,
                token = "mytoken",
                skipBefore = DateTime.now()
            )

            logger.info { "client started" }
            messageProcessor.start(send = send, receive = receive)
        }
        logger.info { "runblocking finished" }
        logger.info { "main done" }
    }
}
