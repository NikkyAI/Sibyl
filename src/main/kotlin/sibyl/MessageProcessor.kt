package sibyl

import sibyl.api.ApiMessage
import sibyl.commands.BufferConsole
import sibyl.commands.asMessage
import sibyl.commands.runCommand
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import sibyl.modules.SibylModule
import sibyl.modules.core.CoreModule
import sibyl.modules.test.TestModule
import mu.KotlinLogging
import java.util.*

class MessageProcessor {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    // TODO: make this funtion instead
    internal val modules: MutableList<SibylModule> = mutableListOf()
    private val userid = "sibyl.${UUID.randomUUID()}"

    init {
        addModule(CoreModule(this))
    }

    fun addModule(newModule: SibylModule) {
        modules.forEach { mod ->
            mod.commands.forEach { cmd ->
                newModule.commands.forEach { newCmd ->
                    require(newCmd.commandName != cmd.commandName) {
                        "duplicate command ${cmd.commandName}"
                    }
                }
            }
        }

        modules += newModule
    }

    suspend fun start(send: SendChannel<ApiMessage>, receive: Flow<ApiMessage>) {
        receive.filter { msg ->
            msg.userid != userid
        }.onEach { msg ->
            process(
                message = msg,
                sendChannel = send
            )
        }.collect()
        logger.info { "messageProcessor closed" }
    }

    // TODO: return value? any kind of response ?
    suspend fun process(message: ApiMessage, sendChannel: SendChannel<ApiMessage>) {
        if (message.userid == userid) {
            logger.debug { "ignoring loopback message $message" }
            return
        }

        // TODO: create context from message

        // TODO: create response holder object
        // if response holder object contains reponse -> break
        // or allow multiple processors to respond to a single message ?

        // TODO: pass functionality for sending messages
        val bufferConsole = BufferConsole()

        modules@ for (module in modules) {
            logger.debug { "processing message $message" }
            if (message.text.startsWith(module.commandPrefix)) {
                logger.debug { "searching for commands in ${module.commands.map { module.commandPrefix + it.commandName }}" }
                val command = module.commands.find { cmd ->
//                    val regex = "^\\Q$\\E\\s".toRegex(RegexOption.IGNORE_CASE)
//                    message.text.matches(regex)
//                    cmd.commandName.equals(firstWord, ignoreCase = true)
                    message.text.startsWith("${module.commandPrefix}${cmd.commandName} ", true)
                            || message.text.equals("${module.commandPrefix}${cmd.commandName}", true)
                }
                if (command != null) {
                    command.runCommand(
                        commandPrefix = module.commandPrefix,
                        message = message,
                        sendChannel = sendChannel,
                        bufferConsole = bufferConsole
                    )

                    // message was consumed by command
                    break@modules
                }
            }
            logger.debug { "no command found" }

            val consumed = module.process(message)
            if (consumed) break@modules
        }

//        logger.debug { "stdout: ${bufferConsole.stdOutBuilder}" }
//        logger.debug { "stderr: ${bufferConsole.stdErrBuilder}" }

        bufferConsole.stdOutBuilder.toString()
            .trim()
            .takeIf { it.isNotBlank() }
            ?.let { responseText ->
                ApiMessage(
                    username = "Sibyl",
                    text = responseText,
                    gateway = message.gateway,
                    channel = "api",
                    userid = userid
                )
            }
            ?.let { responseMessage ->
                sendChannel.send(responseMessage)
            }
    }
}


@OptIn(ExperimentalCoroutinesApi::class)
fun main(args: Array<String>) = runBlocking {
    val logger = KotlinLogging.logger {}
    val msgProcessor = MessageProcessor()
    msgProcessor.addModule(TestModule())
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
                msgProcessor.process(msg, channel)
                delay(100)
            }
    }
    outgoing.collect { message ->
        logger.debug { "(not) sending: $message" }
        logger.info { message.text }
        if(message.text.lines().size > 1) {
            logger.error { "output lines: ${message.text.lines().size}" }
        }
    }
}