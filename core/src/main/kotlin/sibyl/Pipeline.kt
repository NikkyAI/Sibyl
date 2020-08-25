package sibyl

import mu.KotlinLogging
import java.lang.IllegalStateException

typealias Interceptor<E> = suspend (E, Stage) -> E?
typealias InterceptorWithStage<E> = suspend (E, Stage) -> E?

class Pipeline<MSG: Any>(
    val name: String,
    val reversed: Boolean = false,
    val sendErrorMessage: suspend (String, Exception, MSG, Stage, Interceptor<MSG>) -> Unit
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }
    private val interceptors: MutableMap<Stage, MutableList<Interceptor<MSG>>> = mutableMapOf()

    fun registerInterceptor(stage: Stage, interceptor: Interceptor<MSG>) {
        require(interceptors.none { (s, i) -> i.contains(interceptor) }) {"duplicate interceptor $interceptor"}
        val interceptorList = interceptors.getOrPut(stage) { mutableListOf() }
        interceptorList += interceptor
    }

    // TODO: replace sendChannel with buffer something ?
    /**
     * @param message object to transform through interceptors
     * @param stageFilter only process stages of the same and lower priority
     */
    suspend fun process(message: MSG, stageFilter: Stage? = null): MSG? {
        logger.info { "processing $message" }

        val filteredInterceptors = if(stageFilter != null) interceptors.filterKeys { s -> s.priority <= stageFilter.priority } else interceptors
        val sortedInterceptors = filteredInterceptors.toSortedMap(
            if(reversed)
                compareByDescending(Stage::priority)
            else
                compareBy(Stage::priority)
        )

        logger.info { "interceptors: ${sortedInterceptors.keys}" }

        val transformedMessage = run {
            sortedInterceptors.entries.fold(message) { msg, (stage, list) ->
                logger.info { "$name processing stage $stage" }
                val orderedList = if(reversed) list.asReversed() else list
                orderedList.fold(msg) innerFold@{ innerMsg : MSG, interceptor: Interceptor<MSG> ->
                    val interceptorResult = try {
                        interceptor.invoke(innerMsg, stage)
                    } catch(e: IllegalStateException) {
                        logger.error(e) { "error during invocation of $interceptor" }
                        // TODO: send message containing error here ?
                        sendErrorMessage(name, e, innerMsg, stage, interceptor)
                        null
                    }
                    interceptorResult?.also {
                        logger.debug { "after $interceptor: $it" }
                    } ?: return@run run {
                        logger.debug { "message consumed by $stage, $interceptor" }
                        null
                    }
                }.also {
                    logger.debug { "processed result after $stage: $it" }
                }
            }
        }

        return transformedMessage
    }
}


