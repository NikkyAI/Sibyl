package sibyl.module.logging

import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.sqlite.driver.asJdbcDriver
import mu.KotlinLogging
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat
import sibyl.*
import sibyl.api.ApiMessage
import sibyl.config.ConfigUtil
import sibyl.db.*
import java.io.File
import javax.sql.DataSource
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class LoggingModule(
    internal val dtFormat: DateTimeFormatter = DateTimeFormat.forPattern(defaultConfig.dateFormat ?: "yyyy-MM-dd HH:mm:ss"),
    internal val messageFormat: (Logs, DateTimeFormatter) -> String = { logsRecord, dtf ->
        val prefix = "${logsRecord.timestamp.toString(dtf)} <${logsRecord.username} ${logsRecord.userid}> "
        prefix + logsRecord.text.withIndent("", " ".repeat(prefix.length))
    }
) : SibylModule("log", "logs messages and allows you to retrieve them") {
    companion object {
        private val logger = KotlinLogging.logger {}

        val defaultConfig: LogConfig = ConfigUtil.load(File("logging.json"), LogConfig.serializer()) {
            LogConfig()
        }

        // this has to be done before fixOutgoing
        val LOGGING_OUTGOING = Stage("LOGGING", -5)
    }

    val logsFolder = File("logs")

    override val commands = listOf(
        LogCommand(this)
    )

    private lateinit var dataSource: DataSource

    override fun MessageProcessor.install() {
        registerIncomingInterceptor(Stage.POST_FILTER, ::processRequest)
        registerOutgoingInterceptor(LOGGING_OUTGOING, ::processResponse)

        dataSource = Database.dataSourceForSchema(schema = SCHEMA_NAME_LOGGING)
        Database.flywayMigrate(dataSource)
        Database.flywayValidate(dataSource)
        logger.info { "loaded data source" }
    }

    override suspend fun start() {
        logsFolder.mkdirs()
    }


    val logsDB by lazy {
        val timestampAdapter = object : ColumnAdapter<LocalDateTime, String> {
            val timestampWriteFormat = ISODateTimeFormat.dateTimeNoMillis()
            val timestampReadFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
            override fun decode(databaseValue: String) = LocalDateTime.parse(databaseValue, timestampReadFormat)
            override fun encode(value: LocalDateTime) = value.toString(timestampWriteFormat)
        }

        val dataSource = Database.dataSourceForSchema("sibyl-logging")

        LoggingDatabase(
            driver = dataSource.asJdbcDriver(),
            logsAdapter = Logs.Adapter(
                timestampAdapter = timestampAdapter,
            )
        )
    }

    suspend fun processRequest(message: ApiMessage, stage: Stage): ApiMessage {
//        val logFile = logsFolder.resolve(message.gateway + ".log")
        appendLog(message = message, incoming = true)
//        logFile.appendText(messageFormat(message, dtFormat).withIndent(">> ", "   ") + "\n")
        return message
    }

    suspend fun processResponse(response: ResponseMessage, stage: Stage): ResponseMessage? {
        if(response.fromCommand is LogCommand) {
            // TODO: detect messages coming from log command better
            logger.info { "skipping logging for log message output" }
            return response
        }
//        val logFile = logsFolder.resolve(response.message.gateway + ".log")
        appendLog(message = response.message, incoming = false)
//        logFile.appendText(messageFormat(response.message, dtFormat).withIndent("<< ", "   ") + "\n")
        return response
    }

    suspend fun appendLog(message: ApiMessage, incoming: Boolean) {
        if(!this::dataSource.isInitialized) {
            logger.error { "datasource not initlaized" }
            return
        }
        if(message.text.isBlank()) {
            logger.debug { "blank message is ignored" }
            return
        }
        val maxId = logsDB.logsQueries.selectMaxId().executeAsOneOrNull() ?: 0
        logsDB.logsQueries.insert(
            Logs(
                id = maxId + 1,
                gateway = message.gateway,
                timestamp = LocalDateTime(message.timestamp.millis),
                username = message.username,
                userid = message.userid,
                text = message.text,
                event = message.event.takeIf { it.isNotBlank() },
                protocol = message.protocol.takeIf { it.isNotBlank() },
                incoming = if(incoming) 1 else 0
            )
        )
    }

    suspend fun getLogs(gateway: String, amount: Int, skipCommands: Boolean): List<Logs> {
        val messages = logsDB.logsQueries.getLogsForGateway(
            gateway = gateway,
            skipCommands = if(skipCommands) 1 else 0,
            amount = amount.toLong()
        ).executeAsList().reversed()
        return messages
    }
}

@OptIn(ExperimentalContracts::class)
fun <T> T.applyIf(condition: Boolean, block: T.() -> T): T {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    return if(condition) block() else this
}