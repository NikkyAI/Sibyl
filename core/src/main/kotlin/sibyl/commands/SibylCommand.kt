package sibyl.commands

import sibyl.api.ApiMessage
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.PrintHelpMessage
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.output.CliktConsole
import com.github.ajalt.clikt.parameters.options.eagerOption
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import kotlinx.coroutines.channels.SendChannel
import mu.KotlinLogging
import sibyl.modules.core.HelpCommand

abstract class SibylCommand(
    help: String = "",
    epilog: String = "",
    name: String? = null,
    invokeWithoutSubcommand: Boolean = true,
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
        private val logger = KotlinLogging.logger {}
    }

    private lateinit var messageContext: ApiMessage
    private lateinit var sendChannelContext: SendChannel<ApiMessage>

    val message: ApiMessage
        get() =
            (currentContext.findRoot().command as? SibylCommand)?.messageContext ?: messageContext
            ?: error("cannot find message context")
    val sendChannel: SendChannel<ApiMessage>
        get() =
            (currentContext.findRoot().command as? SibylCommand)?.sendChannelContext ?: sendChannelContext
            ?: error("cannot find sendchannel")

    init {
        eagerOption("-H", "--HELP", hidden = true) {
            logger.info { "setting verbose" }
            (currentContext.helpFormatter as ShortHelpFormatter).verbose = true
            throw PrintHelpMessage(this@SibylCommand)
        }
        eagerOption("-h", "--help", help = "shows command usage") {
            throw PrintHelpMessage(this@SibylCommand)
        }
    }

    internal fun exec(
        commandPrefix: String,
        message: ApiMessage,
        sendChannel: SendChannel<ApiMessage>,
        customConsole: CliktConsole
    ) {
        context {
            console = customConsole
            helpFormatter = ShortHelpFormatter(verbose = false)
        }

        messageContext = message
        sendChannelContext = sendChannel
        val argv = message.text.substringAfter(commandPrefix + commandName).shellSplit()
        logger.info { "executing: $commandPrefix$commandName $argv" }
        parse(argv)
    }

//    override fun run() {
//        // find message context on root command
//        logger.info { "message: $message" }
//        run(
//            message = message,
//            sendChannel = sendChannel
//        )
//    }

//    abstract fun run(message: ApiMessage, sendChannel: SendChannel<ApiMessage>)
}