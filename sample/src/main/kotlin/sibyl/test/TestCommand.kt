package sibyl.test

import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.restrictTo
import sibyl.commands.SibylCommand
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}
class TestCommand : SibylCommand(
    name = "test",
    help = "tests stuff",
    invokeWithoutSubcommand = false
) {
    init {
        subcommands(Subcommand(), MessageCommand(), OptionCommand())
    }
    //    val numberArg by argument().int()
    override fun run() {
        val subcommand = currentContext.invokedSubcommand
        if (subcommand == null) {
            logger.info { "invoked without a subcommand" }
            echo("test")
        } else {
            logger.info { "about to run ${subcommand.commandName}" }
        }
    }

    class Subcommand : SibylCommand(
        name = "sub",
        help = "subs stuff"
    ) {
        val numberArg by argument().int()
        override fun run() {
            logger.info { "issuing messages" }
            echo("sub $numberArg")
        }
    }

    class MessageCommand : SibylCommand(
        name = "msg",
        help = "messages stuff"
    ) {
        override fun run() {
            logger.info { "issuing messages" }
            echo("message")
            echo("message2")
        }
    }

    class OptionCommand : SibylCommand(
        name = "opt",
        help = "options stuff"
    ) {
        val a by option("-t", "--test", "--test2").int().restrictTo(0, 20).required()
        val b by option("-b", metavar = "number").int()
        val c by option("-c", metavar = "number", hidden = true).int()
        val d by option("-d", metavar = "number").int().multiple()
        val e by option("-e").flag()
        val f by option("-f").counted()
        val g by option(help = "multiline\nhelp\nstring").switch("-g" to "G", "--gg" to "GG")
        override fun run() {
            logger.info { "issuing messages" }
            echo("option $a")
        }
    }
}
