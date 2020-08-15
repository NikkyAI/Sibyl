import mu.KotlinLogging
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import sibyl.*
import sibyl.api.ApiMessage
import java.io.File

class LoggingModule(
    val dtFormat: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"),
    val messageFormat: (ApiMessage, DateTimeFormatter) -> String = { message, dtf ->
        val prefix = "${message.timestamp.toString(dtf)} <${message.username} ${message.userid}> "
        prefix + message.text.withIndent("", " ".repeat(prefix.length))
    }
) : SibylModule("log", "logs messages and allows you to retrieve them") {
    companion object {
        private val logger = KotlinLogging.logger {}

        // this has to be done before fixOutgoing
        val LOGGING_OUTGOING = Stage("LOGGING", -5)
    }

    val logsFolder = File("logs")

    override val commands = listOf(
        LogCommand(logsFolder)
    )

    override fun MessageProcessor.setup() {
        registerIncomingInterceptor(Stage.POST_FILTER, ::processRequest)
        registerOutgoingInterceptor(LOGGING_OUTGOING, ::processResponse)
    }

    override fun start() {
        logsFolder.mkdirs()
    }

    suspend fun processRequest(message: ApiMessage, stage: Stage): ApiMessage {
        val logFile = logsFolder.resolve(message.gateway + ".log")
        logFile.appendText(messageFormat(message, dtFormat).withIndent(">> ", "   ") + "\n")
        return message
    }

    suspend fun processResponse(response: ResponseMessage, stage: Stage): ResponseMessage? {
        if(response.fromCommand is LogCommand) {
            // TODO: detect messages coming from log command better
            logger.info { "skipping logging for log message output" }
            return response
        }
        val logFile = logsFolder.resolve(response.message.gateway + ".log")
        logFile.appendText(messageFormat(response.message, dtFormat).withIndent("<< ", "   ") + "\n")
        return response
    }
}