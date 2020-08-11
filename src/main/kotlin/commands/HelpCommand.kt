package commands

import api.ApiMessage
import com.github.ajalt.clikt.core.PrintHelpMessage
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.arguments.transformAll
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}
class HelpCommand : SibylCommand(
    name = "help",
    help = "show help"
) {
    val command by argument("COMMANDS").multiple(required = false).transformAll { commands ->
        val rootCommand = currentContext.findRoot().command
        commands.fold(rootCommand) { cmd, commandName ->
            cmd.registeredSubcommands().firstOrNull { it.commandName.equals(commandName, ignoreCase = true) }
                ?: fail("cannot find command $commandName")
        } as SibylCommand
    }

    val verbose by option("-v", "--verbose").flag("-V", default = false)

    override fun run(message: ApiMessage) {
        command.verboseHelp = verbose

        throw PrintHelpMessage(command = command, error = false)
    }
}