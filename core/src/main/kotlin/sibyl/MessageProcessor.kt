package sibyl

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import sibyl.api.ApiMessage
import sibyl.core.CoreModule
import java.util.*

class MessageProcessor {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private lateinit var sendChannel: SendChannel<ApiMessage>

    // TODO: make this funtion instead
    internal var modules: List<SibylModule> = listOf()
        private set

//    // outgoing
//    private var outputTranforms: List<(ApiMessage) -> ApiMessage> = listOf() // TODO: move into module ?
//
//    // incoming
//    private var interceptors: List<(ApiMessage) -> Unit> = listOf() // TODO: move into module ?
//    private var filters: List<(ApiMessage) -> Boolean> = listOf() // TODO: move into module ?

    private val incomingPipeline = Pipeline()
    private val outgoingPipeline = Pipeline(-1)
    // TODO: pipeline with dynamic amount of stages
    // TODO: (ApiMessage) -> ApiMessage?
    // TODO: make commands a interceptor on a defined step/stage

    internal val userid = "sibyl.${UUID.randomUUID()}"

    init {
        addModule(CoreModule(this))

    }

    @OptIn(ExperimentalStdlibApi::class)
    fun addModule(newModule: SibylModule) {
        require(newModule !in modules) { "module ${newModule.name} cannot be registered multiple times" }
        modules.forEach { mod ->
            mod.commands.forEach { cmd ->
                newModule.commands.forEach { newCmd ->
                    require(newCmd.commandName != cmd.commandName) {
                        "duplicate command ${cmd.commandName}"
                    }
                }
            }
        }

        newModule.init(
            messageProcessor = this,
            sendMessage = { message: ApiMessage ->
                // TODO: pass in output buffer ?
                val transformedMessage = outgoingPipeline.process(message)
                if(transformedMessage != null) {
                    sendChannel.send(transformedMessage)
                }
            }
        )
        modules += newModule
    }

    fun registerIncomingInterceptor(stage: Stage, interceptor: Interceptor) {
        incomingPipeline.registerInterceptor(stage, interceptor)
    }

    fun registerOutgoingInterceptor(stage: Stage, interceptor: Interceptor) {
        outgoingPipeline.registerInterceptor(stage, interceptor)
    }

    suspend fun start(send: SendChannel<ApiMessage>, receive: Flow<ApiMessage>) {
        sendChannel = send
        receive.filter { msg ->
            msg.userid != userid
        }.onEach { msg ->
            process(
                message = msg
            )
        }.collect()
        logger.info { "messageProcessor closed" }
    }

    suspend fun process(message: ApiMessage) {
        val processedMessage = incomingPipeline.process(message)
        if(processedMessage != null) {
            logger.info { "message was not consumed" }
        }
    }
}


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