import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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

object Main : KLogging() {
    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {

            val msg = ApiMessage(
                username = "Nikky",
                text = """Hi message
            |new line
        """.trimMargin()
            )

            val json = msg.encode()

            logger.info(json)

            val msg2 = ApiMessage.decode(json)
            logger.info { msg2 }

            val handler = Handler("http://localhost:4242")
            val handler2 = Handler("http://localhost:4343")

            val incomingMessages = handler.broadcast.openSubscription()

            launch {
                for (msg in incomingMessages) {
                    logger.info("received msg: $msg")
                }
                logger.info("no more messages")
            }
            logger.info("launched listener")

            launch {
                (0..5).forEach {
                    delay(1000)
                    logger.info("send $it")
                    handler2.send(
                        ApiMessage(
                            username = "Tester",
                            text = "message $it",
                            gateway = "matterbridgetest",
                            avatar = "https://nikky.moe/me/Nikky_Lineart.png"
                        )
                    )
                }
            }
            logger.info("launched sender")

            delay(2000)
            incomingMessages.cancel()
            logger.info("incoming: ${incomingMessages.isClosedForReceive}")

            val incomingMessages2 = handler.broadcast.openSubscription()
            launch {
                for (msg in incomingMessages2) {
//                    delay(100)
                    logger.info("received_2 msg: $msg")
                }
            }
        }
        logger.info("closing")
    }

}