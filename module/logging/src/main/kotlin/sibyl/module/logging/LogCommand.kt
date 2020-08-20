package sibyl.module.logging

import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.restrictTo
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import sibyl.commands.SibylCommand

class LogCommand(
    private val loggingModule: LoggingModule
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
            loggingModule.getLogs(causeMessage.gateway, numberOfLines, skipCommands = skipCommands)
        }
        messages.forEach { record ->
            echo(loggingModule.messageFormat(record, loggingModule.dtFormat))
        }
    }
}