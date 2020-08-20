package sibyl.module.logging

import kotlinx.coroutines.future.await
import mu.KotlinLogging
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import sibyl.*
import sibyl.api.ApiMessage
import sibyl.config.ConfigUtil
import sibyl.db.Database
import sibyl.db.JOOQBuilder
import sibyl.db.runOperation
import sibyl.db.runTransaction
import sibyl.sibyl_logging.db.generated.Tables
import sibyl.sibyl_logging.db.generated.tables.records.LogsRecord
import java.io.File
import java.sql.Timestamp
import javax.sql.DataSource
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class LoggingModule(
    internal val dtFormat: DateTimeFormatter = DateTimeFormat.forPattern(defaultConfig.dateFormat ?: "yyyy-MM-dd HH:mm:ss"),
    internal val messageFormat: (LogsRecord, DateTimeFormatter) -> String = { logsRecord, dtf ->
        val prefix = "${DateTime(logsRecord.timestamp.toInstant().toEpochMilli()).toString(dtf)} <${logsRecord.username} ${logsRecord.userid}> "
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

    override fun MessageProcessor.setup() {
        registerIncomingInterceptor(Stage.POST_FILTER, ::processRequest)
        registerOutgoingInterceptor(LOGGING_OUTGOING, ::processResponse)

        dataSource = Database.dataSourceForSchema(schema = "sibyl-logging")
        Database.flywayMigrate(dataSource)
        Database.flywayValidate(dataSource)
        logger.info { "loaded data source" }
    }

    override suspend fun start() {
        logsFolder.mkdirs()
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
        dataSource.runTransaction { context ->
            context
                .insertInto(
                    Tables.LOGS,
                    Tables.LOGS.GATEWAY,
                    Tables.LOGS.TIMESTAMP,
                    Tables.LOGS.USERNAME,
                    Tables.LOGS.USERID,
                    Tables.LOGS.TEXT,
                    Tables.LOGS.EVENT,
                    Tables.LOGS.PROTOCOL,
                    Tables.LOGS.INCOMING
                )
                .values(
                    message.gateway,
                    Timestamp(message.timestamp.millis),
                    message.username,
                    message.userid,
                    message.text,
                    message.event.takeIf { it.isNotBlank() },
                    message.protocol,
                    incoming
                )
                .returning()
                .fetchOne()
        }
    }

    suspend fun getLogs(gateway: String, amount: Int, skipCommands: Boolean): List<LogsRecord> {
        val messages = dataSource.runOperation { context ->
            JOOQBuilder(context, Tables.LOGS)
                .where(Tables.LOGS.GATEWAY.equal(gateway))
                .applyIf(skipCommands) {
                    where(Tables.LOGS.TEXT.startsWith("!").not())
                }
                .select()
                .orderBy(Tables.LOGS.TIMESTAMP.desc())
                .limit(amount)
                .fetchAsync()
        }.await().filterNotNull().reversed()
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