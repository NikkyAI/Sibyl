package sibyl

import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.*
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

    private val incomingPipeline = Pipeline<ApiMessage>()
    private val outgoingPipeline = Pipeline<ResponseMessage>(reversed = true)

    internal val userid = "sibyl.${UUID.randomUUID().toString().substringBefore('-')}"

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
            sendResponse = { response: ResponseMessage, stage: Stage? ->
                // TODO: pass in output buffer ?
                val transformedResponse = if(stage != null) outgoingPipeline.process(response, stage) else  outgoingPipeline.process(response)
                if(transformedResponse != null) {
                    logger.info {"response from ${transformedResponse.from}"}
                    sendChannel.send(transformedResponse.message)
                }
            }
        )
        modules += newModule
    }

    fun registerIncomingInterceptor(stage: Stage, interceptor: Interceptor<ApiMessage>) {
        incomingPipeline.registerInterceptor(stage, interceptor)
    }

    fun registerOutgoingInterceptor(stage: Stage, interceptor: Interceptor<ResponseMessage>) {
        outgoingPipeline.registerInterceptor(stage, interceptor)
    }

    suspend fun start(send: SendChannel<ApiMessage>, receive: Flow<ApiMessage>) {
        sendChannel = send
        modules.forEach { module ->
            module.start()
        }
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

