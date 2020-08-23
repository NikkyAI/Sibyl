package sibyl

import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach
import mu.KotlinLogging
import sibyl.api.ApiMessage
import sibyl.core.CoreModule
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.cast

class MessageProcessor(
    val botname: String = "Sibyl"
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private lateinit var sendChannel: SendChannel<ApiMessage>

    // TODO: make this function instead
    private val mutModules: MutableList<SibylModule> = mutableListOf()
    val modules: List<SibylModule> by ::mutModules

    private val incomingPipeline = Pipeline<ApiMessage>("incoming")
    private val outgoingPipeline = Pipeline<ResponseMessage>("outgoing", reversed = true)

    internal val userid = "${botname.toLowerCase()}.${UUID.randomUUID().toString().substringBefore('-')}"

    init {
        addModule(CoreModule())
    }

    private suspend fun sendResponse(response: ResponseMessage, stage: Stage?) {
        // TODO: pass in output buffer ?
        val transformedResponse =
            if (stage != null) outgoingPipeline.process(response, stage) else outgoingPipeline.process(response)
        if (transformedResponse != null) {
            logger.info { "response from ${transformedResponse.from}" }
            sendChannel.send(transformedResponse.message)
        }
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

        try {
            newModule.init(
                messageProcessor = this,
                sendResponse = ::sendResponse
            )
            mutModules += newModule
        } catch (e: MissingModuleDependency) {
            logger.error(e) {
                "module requires ${e.missing}"
            }
        } catch (e: Exception) {
            logger.error(e) {
                "module failed initialization"
            }
//            throw e
        }

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
        if (processedMessage != null) {
            logger.info { "message was not consumed" }
        }
    }

    fun <T : SibylModule> hasModule(clazz: KClass<T>): Boolean {
        return modules.any { module ->
            clazz.isInstance(module)
        }
    }
    fun <T : SibylModule> getModule(clazz: KClass<T>): T? {
        return modules.find { module ->
            clazz.isInstance(module)
        }?.let { module ->
            clazz.cast(module)
        }
    }
}

