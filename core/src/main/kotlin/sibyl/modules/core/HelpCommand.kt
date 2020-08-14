package sibyl.modules.core

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.PrintHelpMessage
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.arguments.transformAll
import com.github.ajalt.clikt.parameters.options.eagerOption
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import sibyl.MessageProcessor
import sibyl.api.ApiMessage
import sibyl.commands.SibylCommand
import kotlinx.coroutines.channels.SendChannel
import mu.KotlinLogging
import sibyl.commands.ShortHelpFormatter

class HelpCommand(private val messageProcessor: MessageProcessor) : SibylCommand(
    name = "help",
    help = "helps you, hopefully",
    invokeWithoutSubcommand = true
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    val command by argument("COMMANDS").multiple(required = true).transformAll { commands ->
        val rootCommand: CliktCommand = messageProcessor.modules.flatMap { it.commands }
            .find {
                it.commandName == commands.first()
            } ?: error("cannot find command ${commands.first()}")
//        val rootCommand = currentContext.findRoot().command
        commands.drop(1).fold(rootCommand) { cmd, commandName ->
            cmd.registeredSubcommands().firstOrNull { it.commandName.equals(commandName, ignoreCase = true) }
                ?: fail("cannot find command $commandName")
        } as SibylCommand
    }

//    init {
//        eagerOption("-v", "--verbose") {
//
//        }
//    }

//    val verbose by option("-v", "--verbose").flag("-V", default = false)

    override fun run() {
//        if(command is HelpCommand) {
//            context {
//                helpFormatter = shortHelpFormatter
//            }
//        }
//        logger.info { "verbose: $verbose" }
//        shortHelpFormatter.verbose = verbose

        throw PrintHelpMessage(command = command, error = false)
    }

}