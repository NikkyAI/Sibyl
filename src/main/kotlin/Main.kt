import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import matterlink.api.ApiMessage
import mu.KLogging

object Main : KLogging() {
    @ExperimentalCoroutinesApi
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

//            delay(3000)
//            incomingMessages.cancel()
//            logger.info("incoming: ${incomingMessages.isClosedForReceive}")
//
//            val incomingMessages2 = handler.broadcast.openSubscription()
//            launch {
//                for (msg in incomingMessages2) {
////                    delay(100)
//                    logger.info("received_2 msg: $msg")
//                }
//            }
        }
        logger.info("closing")
    }

}