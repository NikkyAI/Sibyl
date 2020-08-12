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
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import java.net.ProtocolException
import java.util.*

val jsonSerializer = Json {
    prettyPrint = true
    encodeDefaults = false
}
val jsonSerializerCompact = Json {
    prettyPrint = false
    encodeDefaults = false
}

val ApiMessage.frame: Frame.Text
    get() = Frame.Text(jsonSerializerCompact.stringify(ApiMessage.serializer(), this))

object Main {
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
//            install(JsonFeature) {
//                serializer = KotlinxSerializer()
//            }
            install(WebSockets)
        }

        val myuserid = "sibyl.${UUID.randomUUID()}"

        runBlocking {
            try {
                val (send, receive) = startClient(client, "localhost", 4242,
                    token = "mytoken",
                    skipBefore = DateTime.now()
                )
                delay(2000)
                send.send(
                    ApiMessage(
                        gateway = "matterbridgetest",
                        userid = myuserid,
                        username = "sibyl",
                        channel = "api",
                        text = "token login works"
                    )
                )
                send.close()
                delay(500)
            } catch (e: ProtocolException) {
                logger.error(e) { "error starting matterbridge client" }
            }
            logger.info { "continuing" }
            println()
            val (send, receive) = startClient(client, "localhost", 4343,
                skipBefore = DateTime.now()
            )

            logger.info { "client started" }

            receive
//                .receiveAsFlow()
                .filter { msg ->
                    msg.userid != myuserid
                }
                .onEach { msg ->
                    logger.info { "received $msg" }

                    when {
                        msg.event == "api_connected" -> {
                            logger.info { "sending: connected" }
                            send.send(
                                ApiMessage(
                                    gateway = "matterbridgetest",
                                    userid = myuserid,
                                    username = "sibyl",
                                    channel = "api",
                                    text = "connected to api"
                                )
                            )
                        }
                        msg.text.startsWith("echo ") -> {
                            send.send(
                                ApiMessage(
                                    gateway = "matterbridgetest",
                                    userid = myuserid,
                                    username = "sibyl",
                                    channel = "api",
                                    text = msg.text.substringAfter("echo").trimStart()
                                )
                            )
                        }
                        msg.text == "close" -> {
                            send.send(
                                ApiMessage(
                                    gateway = "matterbridgetest",
                                    userid = myuserid,
                                    username = "Sibyl",
                                    channel = "api",
                                    text = "disconnecting"
                                )
                            )
                            send.close()
                        }
                    }
                }.collect()

//            client.webSocket(
//                host = "localhost",
//                port = 4242,
//                path = "/api/websocket",
//                request = {
//                    header("Authorization", "Bearer mytoken")
//                }
//            ) {
//                suspend fun send(message: ApiMessage) {
//                    logger.info { "sending $message" }
//                    outgoing.send(
//                        message.frame
//                    )
//                }
//
//                loop@for(frame in incoming) {
//                    val msg = when(frame) {
//                        is Frame.Text -> {
//                            val json = frame.readText()
//
//                            val msg = try {
//                                jsonSerializer.parse(ApiMessage.serializer(), json)
//                            } catch (e: JsonDecodingException) {
//                                logger.error(e) { "error parsing $json" }
//                                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "cannot parse json"))
//                                break@loop
//                            }
//
//                            msg
//                        }
//                        else -> {
//                            logger.error { "unhandled frame $frame" }
//                            null
//                        }
//                    }
//
//                    if(msg == null) continue
//
//                    logger.info { "received $msg" }
//
//                    when {
//                        msg.event == "api_connected" -> {
//                            logger.info { "sending: connected" }
//                            send(
//                                ApiMessage(
//                                    gateway = "matterbridgetest",
//                                    userid = "sibyl",
//                                    username = "sibyl",
//                                    channel = "api",
//                                    text = "connected to api"
//                                )
//                            )
//                        }
//                        msg.text.startsWith("echo ") -> {
//                            send(
//                                ApiMessage(
//                                    gateway = "matterbridgetest",
//                                    userid = "sibyl",
//                                    username = "sibyl",
//                                    channel = "api",
//                                    text = msg.text.substringAfter("echo").trimStart()
//                                )
//                            )
//                        }
//                        msg.text == "close" -> {
//                            send(
//                                ApiMessage(
//                                    gateway = "matterbridgetest",
//                                    userid = "sibyl",
//                                    username = "Sibyl",
//                                    channel = "api",
//                                    text = "disconnecting"
//                                )
//                            )
//                            close(CloseReason(CloseReason.Codes.NORMAL, "NORMAL"))
////                            this.close(Exception("fast close"))
//                            break@loop
//                        }
//                    }
//                }
//            }
//
//            logger.info { "client closed" }
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
//                    cont.resume(receiveFlow)
                    connected = true

                    logger.info { "processing sendMessages" }
                    sendMessages
                        .consumeAsFlow()
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
