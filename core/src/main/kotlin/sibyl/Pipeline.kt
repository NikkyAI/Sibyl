package sibyl

import mu.KotlinLogging
import sibyl.api.ApiMessage

typealias Interceptor = suspend (ApiMessage) -> ApiMessage?
class Pipeline(val order: Int = 1) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }
    private val interceptors: MutableMap<Stage, MutableList<Interceptor>> = mutableMapOf()

    fun registerInterceptor(stage: Stage, interceptor: Interceptor) {
        require(interceptors.none { (s, i) -> i.contains(interceptor) }) {"duplicate interceptor $interceptor"}
        val interceptorList = interceptors.getOrPut(stage) { mutableListOf() }
        interceptorList += interceptor
    }

    // TODO: replace sendChannel with buffer something ?
    suspend fun process(message: ApiMessage): ApiMessage? {
        logger.info { "processing $message" }

        val sortedInterceptors = interceptors.toSortedMap(compareBy{ k-> k.priority * order })
        // TODO:

        val transformedMessage = run {
            sortedInterceptors.entries.fold(message) { msg, (stage, list) ->
                logger.info { "processing stage $stage" }
                val orderedList = if(order < 0) list.asReversed() else list
                orderedList.fold(msg) innerFold@{ msg : ApiMessage, interceptor: Interceptor ->
                    interceptor.invoke(msg) ?: return@run run {
                        logger.info { "message consumed by $stage, $interceptor" }
                        null
                    }
                }
            }
        }

        return transformedMessage
    }
}


