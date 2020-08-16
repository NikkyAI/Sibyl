package sibyl

import sibyl.api.ApiMessage
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.websocket.*
import io.ktor.client.request.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.*
import kotlinx.serialization.SerializationException
import mu.KotlinLogging
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import java.net.ProtocolException
import java.util.*


object WebsocketClient {
    private val logger = KotlinLogging.logger {}

    val ApiMessage.frame: Frame.Text
        get() = Frame.Text(jsonSerializerCompact.encodeToString(ApiMessage.serializer(), this))

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
//            install(JsonFeature) {
//                serializer = KotlinxSerializer()
//            }
            install(WebSockets)
        }

        val myuserid = "sibyl.${UUID.randomUUID()}"

        runBlocking {
            val (send, receive) = startClient(
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

    suspend fun startClient(
        client: HttpClient,
        host: String,
        port: Int,
        token: String? = null,
        skipBefore: DateTime? = null
    ): Pair<SendChannel<ApiMessage>, Flow<ApiMessage>> = withContext(CoroutineName("startClient")) {
        val sendMessages: Channel<ApiMessage> = Channel(capacity = Channel.CONFLATED)
//        val receivedMessages: Channel<ApiMessage> = Channel(capacity = Channel.CONFLATED)

        var connected: Boolean = false
        var receiveFlow: Flow<ApiMessage>? = null
        var exception: Exception? = null

        GlobalScope.launch(Dispatchers.IO + CoroutineName("matterbridge-websocket")) {
            try {
                client.webSocket(
                    host = host,
                    port = port,
                    path = "/api/websocket",
                    request = {
                        if (token != null) {
                            header("Authorization", "Bearer $token")
                        }
                        if(skipBefore != null) {
                            header("skip_before", skipBefore.toString())
                        }
                    }
                ) {
                    receiveFlow = incoming
                        .receiveAsFlow()
                        .mapNotNull { frame ->
                            logger.info { "received frame: $frame" }
                            when (frame) {
                                is Frame.Text -> {
                                    val json = frame.readText()
                                    logger.info { "received: $json" }

                                    try {
                                        jsonSerializer.decodeFromString(ApiMessage.serializer(), json)
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
//                    cont.resume(receiveFlow)
                    connected = true

                    logger.info { "processing sendMessages" }
                    sendMessages
                        .consumeAsFlow()
                        .map { msg ->
                            jsonSerializer.encodeToString(ApiMessage.serializer(), msg)
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
//                receivedMessages.close()

            } catch (e: ProtocolException) {
                logger.catching(e)
                exception = e
//                this@withContext.cancel(e.message ?: "", e)
            } catch (e: Exception) {
                logger.catching(e)
                exception = e
//                this@withContext.cancel(e.message ?: "", e)
            } finally {
                logger.info { "cleanup" }
            }
            logger.info { "GlobalScope launch finished" }
        }

        while (!connected) {
            exception?.let { e ->
                throw e
            }
            delay(100)
            logger.info { "waiting for websocket connection" }
        }

        Pair(sendMessages, receiveFlow!!)
    }
}
