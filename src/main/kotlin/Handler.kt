import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

    private var sendChannel: SendChannel<ApiMessage> = apiSender()

    val keepOpenManager = FuelManager().apply {
        timeoutInMillisecond = 0 // 10_000
        timeoutReadInMillisecond = 0 // 10_000
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
                    sendChannel = apiSender()
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
            sendChannel = apiSender()
        }
    }

    //
    fun disconnect() {
        enabled = false
        coroutineContext.cancelChildren()
    }

    suspend fun send(msg: ApiMessage) = sendChannel.send(msg)

    companion object : KLogging()

    private fun CoroutineScope.apiSender() = actor<ApiMessage>(context = Dispatchers.IO) {
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

    private fun ProducerScope<ApiMessage>.deserializer() = object : ResponseDeserializable<Unit> {
        override fun deserialize(reader: Reader) {
            logger.info("connection open")
            reader.forEachLine { line ->
                val msg = ApiMessage.decode(line)
                launch {
                    send(msg)
                }
            }
        }
    }

    private fun CoroutineScope.messageBroadcast() = broadcast<ApiMessage>(context = Dispatchers.IO) {
        while (isActive) {
            logger.info("opening connection")
            val (_, response, result) = keepOpenManager.request(Method.GET, "$host/api/stream")
                .responseObject(deserializer())

            when (result) {
                is Result.Success -> {
                    logger.info("connection closed")
                }
                is Result.Failure -> {
                    logger.error("connection error")
                    logger.error("response: $response")
                    logger.error(result.error.exception) {
                        result.error.localizedMessage
                    }
//                    throw result.error.exception
                }
            }
            delay(1000)
        }
    }
}