package sibyl.module.reminders

import com.github.ajalt.clikt.core.subcommands
import sibyl.commands.SibylCommand

class RemindCommand(remindersModule: RemindersModule) : SibylCommand(
    name = "remind",
    help = "commands that you will forget about"
) {
    init {
        subcommands(
            RemindInCommand(remindersModule)
        )
    }

    override fun run() {

    }

}