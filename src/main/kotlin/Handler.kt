import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.broadcast
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.io.IOException
import matterlink.api.ApiMessage
import matterlink.api.StreamConnection
import mu.KLogging
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.Arrays
import kotlin.coroutines.CoroutineContext

class Handler(
    val host: String,
    val token: String = ""
) : CoroutineScope {
    override val coroutineContext: CoroutineContext = Job()

    var broadcast = broadcastStream()
        private set

    private var sendChannel: SendChannel<ApiMessage> = apiSender()

    var enabled = true
        private set

    init {
        launch {
            while (enabled) {
                delay(100)
                if (broadcast.isClosedForSend && enabled) {
                    broadcast = broadcastStream()
                }
                if(sendChannel.isClosedForSend && enabled) {
                    sendChannel = apiSender()
                }
            }
        }
    }

    suspend fun close() {
        coroutineContext.cancel()
    }

    suspend fun connect() {
        if (broadcast.isClosedForSend && enabled) {
            broadcast = broadcastStream()
        }
        if(sendChannel.isClosedForSend && enabled) {
            sendChannel = apiSender()
        }
    }
//
    fun disconnect() {
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

    private fun CoroutineScope.broadcastStream() = broadcast<ApiMessage>(context = Dispatchers.IO) {
        try {
            val urlConnection: HttpURLConnection
            val serviceURL = "$host/api/stream"
            val myURL: URL

            myURL = URL(serviceURL)
            urlConnection = myURL.openConnection() as HttpURLConnection
            urlConnection.requestMethod = "GET"
            if (!token.isEmpty()) {
                val bearerAuth = "Bearer $token"
                urlConnection.setRequestProperty("Authorization", bearerAuth)
            }
            urlConnection.inputStream.use { input ->
                logger.info("connection opened")
                val buffer = StringBuilder()
                while (isActive) {
                    val buf = ByteArray(1024)
                    Thread.sleep(10)
                    while (input.available() <= 0) {
                        if (isClosedForSend) break
                        Thread.sleep(10)
                    }
                    val chars = input.read(buf)

                    StreamConnection.logger.trace(String.format("read %d chars", chars))
                    if (chars > 0) {
                        val added = String(Arrays.copyOfRange(buf, 0, chars))
                        logger.debug("json: $added")
                        buffer.append(added)
                        while (buffer.toString().contains("\n")) {
                            val index = buffer.indexOf("\n")
                            val line = buffer.substring(0, index)
                            buffer.delete(0, index + 1)
                            send(ApiMessage.decode(line))
//                        rcvQueue.add()
                        }
                    } else if (chars < 0) {
                        break
                    }
                }
            }
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            close()
        } catch (e: ConnectException) {
            e.printStackTrace()
            close()
        } catch (e: IOException) {
            e.printStackTrace()
            close()
        } catch (e: InterruptedException) {
            e.printStackTrace()
            close()
        }

    }
}