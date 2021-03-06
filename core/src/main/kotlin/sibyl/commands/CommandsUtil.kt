package sibyl.commands

import sibyl.api.ApiMessage
import com.github.ajalt.clikt.core.*
import mu.KotlinLogging
import sibyl.removeBlankLines
import java.util.regex.Matcher
import java.util.regex.Pattern

private val logger = KotlinLogging.logger {}

fun SibylCommand.runCommand(
    commandPrefix: String,
    message: ApiMessage,
//    sendChannel: SendChannel<ApiMessage>,
    bufferConsole: BufferConsole = BufferConsole()
) {
    try {
        exec(
            commandPrefix = commandPrefix,
            message = message,
//            sendMessage = sendChannel,
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
        bufferConsole.stdOutBuilder.appendln("Error: " + e.localizedMessage)
    } catch (e: UsageError) {
        logger.warn { e.helpMessage() }
        logger.warn { e.localizedMessage }
        bufferConsole.stdOutBuilder.appendln(e.helpMessage().removeBlankLines())
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