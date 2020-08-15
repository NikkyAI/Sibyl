package sibyl

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import sibyl.api.ApiMessage


@OptIn(ExperimentalCoroutinesApi::class)
fun main(vararg args: String) = runBlocking {
    val logger = KotlinLogging.logger {}
    logger.info { "args: ${args.joinToString()}" }
    val msgProcessor = MessageProcessor()
//    msgProcessor.addModule(TestModule())
    val outgoing = channelFlow<ApiMessage> {
        listOf(
//        "!sibyl.commands",
            "!test",
            "!test --help",
            "!test sub --help",
            "!test",
//            "!test sub 42",
//            "!test msg",
            "!test opt --help",
            "!test opt -t 20",
            "!test opt --test 23",
            "!help test opt",
//            "!help test opt",
            "!help test",
//            "!help test -v",
            "!commands",
            "!whoami",
            "!echo hello world"
        )
            .map {
                logger.info { it }
                it.asMessage()
            }
            .forEach { msg ->
                msgProcessor.process(msg)
                delay(100)
            }
    }
    outgoing.collect { message ->
        logger.debug { "(not) sending: $message" }
        logger.info { message.text }
        if (message.text.lines().size > 1) {
            logger.error { "output lines: ${message.text.lines().size}" }
        }
    }
}