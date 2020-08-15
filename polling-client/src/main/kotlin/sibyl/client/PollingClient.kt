package sibyl.client

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.content.*
import io.ktor.http.ContentType
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.builtins.list
import mu.KotlinLogging
import sibyl.api.ApiMessage
import sibyl.jsonSerializer

object PollingClient {
    private val logger = KotlinLogging.logger {}
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun connect(
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
            jsonSerializer.parse(ApiMessage.serializer().list, json)
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
                    jsonSerializer.parse(ApiMessage.serializer().list, json)
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
                    body = TextContent(
                        text = jsonSerializer.stringify(ApiMessage.serializer(), message),
                        contentType = ContentType.Application.Json
                    )
                }
            }
        }

        sendMessages to receiveFlow
    }
}