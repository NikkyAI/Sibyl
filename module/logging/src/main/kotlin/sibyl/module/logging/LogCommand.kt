package sibyl.module.logging

import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.restrictTo
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import sibyl.commands.SibylCommand
import sibyl.sibyl_logging.db.generated.tables.records.LogsRecord
import sibyl.withIndent

class LogCommand(
    val loggingModule: LoggingModule,
    val dtFormat: DateTimeFormatter = DateTimeFormat.forPattern(LoggingModule.defaultConfig.dateFormat ?: "yyyy-MM-dd HH:mm:ss"),
    val messageFormat: (LogsRecord, DateTimeFormatter) -> String = { logsRecord, dtf ->
        val prefix = "${DateTime(logsRecord.timestamp.toInstant().toEpochMilli()).toString(dtf)} <${logsRecord.username} ${logsRecord.userid}> "
        prefix + logsRecord.text.withIndent("", " ".repeat(prefix.length))
    }
) : SibylCommand(
    name = "log",
    help = "replay log"
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }
    val numberOfLines by argument("LINES").int().restrictTo(min = 1)
    val skipCommands by option("--skip-commands").flag(default = false)
    override fun run() {
        val messages = runBlocking {
            loggingModule.getLogs(message.gateway, numberOfLines, skipCommands = skipCommands)
        }
        messages.forEach { record ->
            echo(messageFormat(record, dtFormat))
        }
    }
}