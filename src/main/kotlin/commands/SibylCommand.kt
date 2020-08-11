package commands

import api.ApiMessage
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.output.CliktConsole
import mu.KotlinLogging

abstract class SibylCommand(
    help: String = "",
    epilog: String = "",
    name: String? = null,
    invokeWithoutSubcommand: Boolean = false,
    printHelpOnEmptyArgs: Boolean = false,
    helpTags: Map<String, String> = emptyMap(),
    autoCompleteEnvvar: String? = "",
    allowMultipleSubcommands: Boolean = false,
    treatUnknownOptionsAsArgs: Boolean = false
) : CliktCommand(
    help = help,
    epilog = epilog,
    name = name,
    invokeWithoutSubcommand = invokeWithoutSubcommand,
    printHelpOnEmptyArgs = printHelpOnEmptyArgs,
    helpTags = helpTags,
    autoCompleteEnvvar = autoCompleteEnvvar,
    allowMultipleSubcommands = allowMultipleSubcommands,
    treatUnknownOptionsAsArgs = treatUnknownOptionsAsArgs
) {
    companion object {
        private val logger = KotlinLogging.logger{}
    }
    private lateinit var messageContext: ApiMessage
    private val shortHelpFormatter = ShortHelpFormatter(verbose = false)
    var verboseHelp: Boolean
        get() = shortHelpFormatter.verbose
        set(value) {
            shortHelpFormatter.verbose = value
        }

    init {
        addHelp()
    }

    private fun addHelp() {
        logger.debug { "adding help subcommand" }
        if(this !is HelpCommand) {
            subcommands(HelpCommand()) // subcommand here breaks shit
        }
    }

    internal fun exec(commandPrefix: String, message: ApiMessage, customConsole: CliktConsole) {
        context {
            helpFormatter = shortHelpFormatter
            console = customConsole
        }
        messageContext = message
        val argv = message.text.substringAfter(commandPrefix + commandName).shellSplit()
        logger.info { "executing: $commandPrefix$commandName $argv" }
        parse(argv)
    }

    override fun run() {
        // find message context on root command
        val message = (currentContext.findRoot().command as? SibylCommand)?.messageContext ?: messageContext
        logger.info { "message: $message" }
        run(message)
    }

    abstract fun run(message: ApiMessage)
}