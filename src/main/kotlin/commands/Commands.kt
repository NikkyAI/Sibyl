package commands

import api.ApiMessage
import com.github.ajalt.clikt.core.*
import modules.test.TestCommand
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
    message: ApiMessage
) {
    val bufferConsole = BufferConsole()
    try {
        exec(
            commandPrefix = commandPrefix,
            message = message,
            customConsole = bufferConsole
        )
        logger.info { "execution finished\n" }
    } catch (e: PrintHelpMessage) {
        e.command.also { cmd ->
            logger.info { "commandHelp: \n${cmd.commandHelp}" }
            logger.info { "getFormattedHelp(): \n${cmd.getFormattedHelp()}" }
        }
    } catch (e: IllegalArgumentException) {
        logger.catching(e)
    } catch (e: UsageError) {
        logger.warn { e.helpMessage() }
        logger.warn { e.localizedMessage }
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

    logger.info { "stdout: ${bufferConsole.stdOutBuilder}" }
    logger.info { "stderr: ${bufferConsole.stdErrBuilder}" }
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