import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.restrictTo
import sibyl.commands.SibylCommand

class LogCommand : SibylCommand(
    help = "replay log"
) {
    val numberOfLines by argument("LINES").int().restrictTo(min = 1)
    override fun run() {
        echo("TODO: implement spitting out logs")
    }
}