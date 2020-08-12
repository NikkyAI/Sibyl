package sibyl

import sibyl.api.ApiMessage
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.websocket.*
import io.ktor.client.request.*
import io.ktor.content.*
import io.ktor.http.ContentType.Application
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.*
import kotlinx.serialization.builtins.list
import sibyl.modules.test.TestModule
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}
fun main(args: Array<String>) {
    val client = HttpClient(OkHttp) {
//            install(JsonFeature) {
//                serializer = KotlinxSerializer()
//            }
        install(WebSockets)
    }

    val messageProcessor = MessageProcessor().apply {
        addModule(TestModule())
    }

    runBlocking {
        val (send, receive) = connect(client, "localhost", 4242, token = "mytoken")

        logger.info { "client started" }
        messageProcessor.start(send = send, receive = receive)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
private suspend fun connect(
    client: HttpClient,
    host: String,
    port: Int,
    token: String? = null
): Pair<SendChannel<ApiMessage>, Flow<ApiMessage>> = withContext(Dispatchers.IO + CoroutineName("polling-client")) {
    val sendMessages: Channel<ApiMessage> = Channel(capacity = Channel.CONFLATED)

    val initialMessages = withTimeoutOrNull(5000) {
        val json = client.get<String>(host = host, path = "/api/messages", port = port) {
            if (token != null) {
                header("Authorization", "Bearer $token")
            }
        }
        jsonSerializerCompact.parse(ApiMessage.serializer().list, json)
    } ?: error("cannot connect")

    val receiveFlow = channelFlow<ApiMessage> {
        initialMessages.forEach {
            logger.info { "received $it" }
            channel.send(it)
        }
        while (!sendMessages.isClosedForSend) {
            val messages = withTimeoutOrNull(5000) {
                val json = client.get<String>(host = host, path = "/api/messages", port = port) {
                    if (token != null) {
                        header("Authorization", "Bearer $token")
                    }
                }
                jsonSerializerCompact.parse(ApiMessage.serializer().list, json)
            }
            messages?.forEach {
                logger.info { "received $it" }
                channel.send(it)
            }
            delay(500)
        }
        logger.info { "done with polling client" }
    }

    GlobalScope.launch(Dispatchers.IO + CoroutineName("sender")) {
        for (message in sendMessages) {
            // send message
            logger.info { "sending $message" }
            client.post<Unit>(host = host, path = "/api/message", port = port) {
                if (token != null) {
                    header("Authorization", "Bearer $token")
                }
                body = TextContent(text = jsonSerializerCompact.stringify(ApiMessage.serializer(), message), contentType = Application.Json)
            }
        }
    }

    sendMessages to receiveFlow
}

