import api.ApiMessage
import com.sun.xml.internal.ws.encoding.soap.SerializationException
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.ClientRequestException
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.webSocket
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.close
import io.ktor.http.cio.websocket.readText
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import java.net.ProtocolException
import kotlin.coroutines.resume

val jsonSerializer = Json {
    prettyPrint = true
    encodeDefaults = false
}

object Main {
    private val logger = KotlinLogging.logger {}

    @ExperimentalCoroutinesApi
    @JvmStatic
    fun main(args: Array<String>) {
        val client = HttpClient(OkHttp) {
//            install(JsonFeature) {
//                serializer = KotlinxSerializer()
//            }
            install(WebSockets)
        }

        runBlocking {
//            try {
//                val (send, receive) = startClient(client, "localhost", 4242)
//            } catch (e: ProtocolException) {
//                logger.error(e) { "error starting matterbridge client" }
//            }
            delay(500)
            println()
            val (send, receive) = startClient(client, "localhost", 4343).await()

            logger.info { "client started" }

            receive
//                .receiveAsFlow()
                .onEach { msg ->
                    logger.info { "received $msg" }

                    when {
                        msg.event == "api_connected" -> {
                            logger.info { "sending: connected" }
                            send.send(
                                ApiMessage(
                                    gateway = "matterbridgetest",
                                    userid = "sibyl",
                                    username = "sibyl",
                                    channel = "api",
                                    text = "connected to api"
                                )
                            )
                        }
                        msg.text.startsWith("echo ") -> {
                            send.send(
                                msg.copy(
                                    text = msg.text.substringAfter("echo").trimStart(),
                                    username = "sibyl"
                                )
                            )
                        }
                        msg.text == "close" -> {
                            send.send(
                                ApiMessage(
                                    gateway = "matterbridgetest",
                                    userid = "sibyl",
                                    username = "Sibyl",
                                    channel = "api",
                                    text = "disconnecting"
                                )
                            )
                            send.close()
                        }
                    }
                }.collect()

            logger.info { "client closed" }
        }
        logger.info { "main done" }
    }

    fun CoroutineScope.startClient(
        client: HttpClient,
        host: String,
        port: Int
    ): Deferred<Pair<SendChannel<ApiMessage>, Flow<ApiMessage>>> {
        val sendMessages: Channel<ApiMessage> = Channel(capacity = Channel.CONFLATED)
//        val receivedMessages: Channel<ApiMessage> = Channel(capacity = Channel.CONFLATED)

        var connected: Boolean = false
//        var transformedIncoming: Flow<ApiMessage>? = null
        var exception: Exception? = null

        return async {
            val receiveFlow = suspendCancellableCoroutine<Flow<ApiMessage>> { cont ->
                this@startClient.launch(Dispatchers.IO + CoroutineName("matterbridge-websocket")) {
                    try {
                        client.webSocket(
                            host = host,
                            port = port,
                            path = "/api/websocket",
                            request = {
//                        if (wsUrl.protocol == URLProtocol.HTTPS)
//                            url.protocol = URLProtocol.WSS
                            }
                        ) {
//                        launch {
//                            for (frame in incoming) {
//                                logger.info { "received frame: $frame" }
//                                when (frame) {
//                                    is Frame.Text -> {
//                                        val json = frame.readText()
//                                        logger.info { "received: $json" }
//                                        val msg = try {
//                                            jsonSerializer.parse(ApiMessage.serializer(), json)
//                                        } catch (e: SerializationException) {
//                                            logger.catching(e)
//                                            null
//                                        }
//                                        if (msg != null)
//                                            receivedMessages.send(msg)
//                                    }
//                                    else -> logger.info { "unhandled frame $frame" }
//                                }
//                                logger.info { "handled frame: $frame" }
//                            }
//                            logger.info { "incoming closed" }
//                            receivedMessages.close()
//                            logger.info { "receivedMessages closed" }
//                        }

                            val receiveFlow = incoming
                                .receiveAsFlow()
                                .mapNotNull { frame ->
                                    logger.info { "received frame: $frame" }
                                    when (frame) {
                                        is Frame.Text -> {
                                            val json = frame.readText()
                                            logger.info { "received: $json" }

                                            try {
                                                jsonSerializer.parse(ApiMessage.serializer(), json)
                                            } catch (e: SerializationException) {
                                                logger.catching(e)
                                                null
                                            }
                                        }
                                        else -> {
                                            logger.info { "unhandled frame $frame" }
                                            null
                                        }
                                    }
                                }


                            logger.info { "websocket connected" }
                            cont.resume(receiveFlow)
                            connected = true

                            logger.info { "processing sendMessages" }
                            sendMessages
                                .receiveAsFlow()
                                .map { msg ->
                                    jsonSerializer.stringify(ApiMessage.serializer(), msg)
                                }
                                .onEach { json ->
                                    logger.info { "sending: $json" }
                                }
                                .onEach { jsonMsg ->
                                    outgoing.send(
                                        Frame.Text(jsonMsg)
                                    )
                                }.collect()
                            logger.info { "sendMessages closed" }

                            close(CloseReason(CloseReason.Codes.NORMAL, "NORMAL"))
                            logger.info { "sent close" }
                        }
                        logger.info { "websocket closed" }
                    } catch (e: ClientRequestException) {
                        logger.catching(e)
                        exception = e
                        cont.cancel(e)
//                    throw e
                    } catch (e: ProtocolException) {
                        logger.catching(e)
                        exception = e
                        cont.cancel(e)
//                    throw e
                    } catch (e: Exception) {
                        logger.catching(e)
                        exception = e
                        cont.cancel(e)
//                    throw e
                    } finally {
                        logger.info { "cleanup" }
                        sendMessages.close()
//                    receivedMessages.close()
                    }
                    logger.info { "after cleanup" }
                }
            }
//        while (!connected) {
//            exception?.let { e ->
//                throw logger.throwing(e)
//            }
//            delay(100)
//
//            logger.info { "waiting for connect" }
//            // waiting for websocket to connect
//        }

//        val broadcastchannel = receivedMessages.broadcast(Channel.BUFFERED)

            Pair(sendMessages, receiveFlow)
        }
    }
}
