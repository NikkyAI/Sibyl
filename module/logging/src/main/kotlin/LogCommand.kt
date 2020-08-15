import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.restrictTo
import mu.KotlinLogging
import sibyl.commands.SibylCommand
import java.io.File

class LogCommand(val logsFolder: File) : SibylCommand(
    name = "log",
    help = "replay log"
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }
    val numberOfLines by argument("LINES").int().restrictTo(min = 1)
    val skipCommands by option("--skip-commands").flag(default = false)
    override fun run() {
        val logFile = logsFolder.resolve(message.gateway + ".log")
        var countdown = numberOfLines + 1
        val allLines = logFile.readLines().asSequence().filter {  line ->
            if(skipCommands) {
                !line.startsWith("!")
            } else true
        }
        val logLines = logFile.readLines().takeLastWhile { line ->
            if(!line.startsWith(" ")) {
                countdown--
            }
            countdown > 0
        }
        logLines.forEach { line ->
            echo(line)
        }
    }
}