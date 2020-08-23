package sibyl.module.reminders

import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.*
import org.joda.time.Instant
import org.joda.time.LocalDateTime
import sibyl.commands.SibylCommand
import sibyl.db.Reminders

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