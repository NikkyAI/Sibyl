package sibyl.core

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import sibyl.MessageProcessor
import sibyl.ResponseMessage
import sibyl.SibylModule
import sibyl.Stage
import sibyl.api.ApiMessage
import sibyl.commands.BufferConsole
import sibyl.commands.SibylCommand
import sibyl.commands.runCommand
import sibyl.config.ConfigUtil
import java.io.File

class CoreModule : SibylModule("core", "core framework functionality") {
    companion object {
        private val logger = KotlinLogging.logger {}

        val CHECK = Stage("CHECK", 0)
        val FILTER = Stage("FILTER", 0)
    }

    override val commands: List<SibylCommand> by lazy {
        listOf(
            CommandsCommand(messageProcessor),
            ModulesCommand(messageProcessor)
        )
    }

    override fun MessageProcessor.install() {
        registerIncomingInterceptor(Stage.FILTER, ::filterIncoming)
        registerOutgoingInterceptor(Stage.PRE_FILTER, ::fixResponses)
        registerIncomingInterceptor(Stage.COMMANDS, ::processCommands)
    }

    override suspend fun start() {
        val config = ConfigUtil.load(File("core.json"), CoreConfig.serializer()) {
            CoreConfig()
        }

        if(config.controlGateway != null) {
            Runtime.getRuntime().addShutdownHook(Thread {
                runBlocking {
                    logger.info { "sending shutdown message" }
                    sendMessage(
                        ApiMessage(
                            gateway = config.controlGateway,
                            text = "shutting down"
                        ),
                        stage = null
                    )
                    delay(100)
                }
            })
            sendMessage(
                ApiMessage(
                    gateway = config.controlGateway,
                    text = "starting up"
                ),
                stage = null
            )
        }
    }

    private suspend fun filterIncoming(message: ApiMessage, stage: Stage): ApiMessage? {
        if (message.userid == messageProcessor.userid) {
            logger.debug { "ignoring loopback message $message" }
            return null
        }
        return message
    }

    private suspend fun fixResponses(response: ResponseMessage, stage: Stage): ResponseMessage? {
        return response.copy(
            message=response.message.run {
                copy(
                    username = username.takeIf { it.isNotBlank() } ?: messageProcessor.botname,
                    userid = messageProcessor.userid,
                    channel = "api"
                )
            }
        )
    }

    suspend fun processCommands(message: ApiMessage, stage: Stage): ApiMessage? {
        val bufferConsole = BufferConsole()
        var hasCommandPrefix = false
        modules@ for (module in messageProcessor.modules) {
            logger.debug { "processing message $message" }
            if (message.text.startsWith(module.commandPrefix)) {
                hasCommandPrefix = true
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
//                        sendChannel = sendChannel,
                        bufferConsole = bufferConsole
                    )

                    val responseText = bufferConsole.stdOutBuilder.toString()
                        .trim()
                        .takeIf(String::isNotBlank)

                    if (responseText != null) {
                        val responseMessage = ApiMessage(
                            username = "Sibyl",
                            text = responseText,
                            gateway = message.gateway
//                            channel = "api",
//                            userid = messageProcessor.userid
                        )

                        logger.info { "sending to outgoing: $responseMessage" }
                        sendMessage(responseMessage, stage, command)
//                        val processedResponseMessage = outgoingPipeline.process(responseMessage)
//                        if(processedResponseMessage != null)  {
//                            sendChannel.send(responseMessage)
//                        }
                        return null
                    }

                }
            }
            logger.debug { "no command found" }
        }

        if(hasCommandPrefix) {
            // message looks like a command but is not
            // TODO: send list ov available commands
            return null
        }
        return message
    }
}