package sibyl.commands

import sibyl.api.ApiMessage
import com.github.ajalt.clikt.core.*
import kotlinx.coroutines.channels.SendChannel
import mu.KotlinLogging
import org.joda.time.DateTime
import java.util.regex.Matcher
import java.util.regex.Pattern

private val logger = KotlinLogging.logger {}

fun String.asMessage() = ApiMessage(
    username = "Tester",
    text = this,
    gateway = "testgateway",
    timestamp = DateTime.now().toString(),
    channel = "api",
    userid = "testUser"
)

fun SibylCommand.runCommand(
    commandPrefix: String,
    message: ApiMessage,
    sendChannel: SendChannel<ApiMessage>,
    bufferConsole: BufferConsole = BufferConsole()
) {
    try {
        exec(
            commandPrefix = commandPrefix,
            message = message,
            sendChannel = sendChannel,
            customConsole = bufferConsole
        )
        logger.info { "execution finished\n" }
    } catch (e: PrintHelpMessage) {
        e.command.also { cmd ->
//            logger.info { "commandHelp: \n${cmd.commandHelp}" }
//            logger.info { "getFormattedHelp(): \n${cmd.getFormattedHelp()}" }
            bufferConsole.stdOutBuilder.appendln(cmd.getFormattedHelp().trim())
        }
    } catch (e: IllegalArgumentException) {
        logger.catching(e)
    } catch (e: UsageError) {
        logger.warn { e.helpMessage() }
        logger.warn { e.localizedMessage }
        bufferConsole.stdOutBuilder.appendln(e.helpMessage())
        logger.catching(e)
    } catch (e: ProgramResult) {
        logger.info(e) { "statusCode: ${e.statusCode}" }
    } catch (e: PrintMessage) {
        logger.info(e) { "message: ${e.message}" }
    } catch (e: CliktError) {
        logger.catching(e)
    } catch (e: Abort) {
        logger.error(e) { "Abort" }
    }
}

private val SHELL_PATTERN = Pattern.compile("\"(\\\"|[^\"])*?\"|[^ ]+", Pattern.MULTILINE or Pattern.CASE_INSENSITIVE)

fun String.shellSplit(): List<String> {
    if (isEmpty()) {
        return listOf()
    }
    val trimmedCmd = trim { it <= ' ' }
    val matcher: Matcher = SHELL_PATTERN.matcher(trimmedCmd)
    val matches: MutableList<String> = mutableListOf()
    while (matcher.find()) {
        matches.add(matcher.group())
    }
    return matches.toList()
}