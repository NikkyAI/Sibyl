import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.broadcast
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import matterlink.api.ApiMessage
import mu.KLogging
import java.io.Reader
import kotlin.coroutines.CoroutineContext

class Handler(
    val host: String,
    val token: String = ""
) : CoroutineScope {
    override val coroutineContext: CoroutineContext = Job()

    var broadcast = messageBroadcast()
        private set

    private var sendChannel: SendChannel<ApiMessage> = senderActor()

    val keepOpenManager = FuelManager().apply {
        timeoutInMillisecond = 1000
        timeoutReadInMillisecond = 0
    }

    var enabled = true
        private set

    init {
        ApiMessage.serializer()
        launch {
            while (enabled) {
                delay(100)
                if (broadcast.isClosedForSend && enabled) {
                    broadcast = messageBroadcast()
                }
                if (sendChannel.isClosedForSend && enabled) {
                    sendChannel = senderActor()
                }
            }
        }
    }

    suspend fun close() {
        enabled = false
        coroutineContext.cancel()
    }

    suspend fun connect() {
        enabled = true
        if (broadcast.isClosedForSend && enabled) {
            broadcast = messageBroadcast()
        }
        if (sendChannel.isClosedForSend && enabled) {
            sendChannel = senderActor()
        }
    }

    //
    fun disconnect() {
        enabled = false
        coroutineContext.cancelChildren()
    }

    suspend fun send(msg: ApiMessage) = sendChannel.send(msg)

    companion object : KLogging()

    private fun CoroutineScope.senderActor() = actor<ApiMessage>(context = Dispatchers.IO) {
        val url = "$host/api/message"

        consumeEach {
            val (request, response, result) = url.httpPost()
                .apply {
                    if (token.isNotEmpty()) {
                        headers["Authorization"] = "Bearer $token"
                    }
                }
                .jsonBody(it.encode())
                .responseString()
            when (result) {
                is Result.Success -> {
                    logger.info("sent $it")
                }
                is Result.Failure -> {
                    logger.error("failed to deliver: $it")
                    logger.error("url: $url")
                    logger.error("cUrl: ${request.cUrlString()}")
                    logger.error("response: $response")
                    logger.error(result.error.exception) { result.error }
                    close()
                    throw result.error.exception
                }
            }
        }
    }

    private fun CoroutineScope.messageBroadcast() = broadcast<ApiMessage>(context = Dispatchers.IO) {
        while (isActive) {
            logger.info("opening connection")
            val (_, response, result) = keepOpenManager.request(Method.GET, "$host/api/stream")
                .awaitObjectResponse(object : ResponseDeserializable<Unit> {
                    override fun deserialize(reader: Reader) = runBlocking (Dispatchers.IO + CoroutineName("msgReceiver")) {
                        logger.info("connection open")
                        reader.useLines { lines ->
                            lines.forEach { line ->
                                val msg = ApiMessage.decode(line)
                                if (msg.event != "api_connect") {
                                    send(msg)
                                }
                            }
                        }
                    }
                })

            when (result) {
                is Result.Success -> {
                    logger.info("connection closed")
                }
                is Result.Failure -> {
                    logger.error("connection error")
                    logger.error("response: $response")
                    logger.error/*(result.error.exception)*/ {
                        result.error.localizedMessage
                    }
//                    throw result.error.exception
                }
            }
            delay(1000) // reconnect delay
        }
    }
}