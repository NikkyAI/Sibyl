package sibyl.module.reminders

import com.github.ajalt.clikt.parameters.arguments.*
import mu.KotlinLogging
import org.joda.time.LocalDateTime
import sibyl.commands.SibylCommand
import sibyl.db.Reminders

class RemindInCommand(private val remindersModule: RemindersModule) : SibylCommand(
    name = "in",
    help = "reminds you after a specified duration in the same channel"
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }
    val period by argument("PERIOD").convert { string ->
        TimeUtil.parsePeriod(string)
    }.validate { period ->
        require(period.toStandardSeconds().seconds > 0) {
            "duration must be positive"
        }
    }
    val message by argument("MESSAGE").multiple().transformAll { args ->
        args.joinToString(" ")
    }
    override fun run() {
        val id = remindersModule.reminders.remindersQueries.selectMaxId().executeAsOneOrNull() ?: 0 // TODO: remove hack when SERIAL is fixed

        val targetDateTime = LocalDateTime.now() + period
        remindersModule.addReminder(
            Reminders(
                id = id+1,
                message = message,
                username = causeMessage.username,
                userid = causeMessage.userid,
                gateway = causeMessage.gateway,
                target = targetDateTime,
                requestedAt = LocalDateTime.now(),
                fulfilledAt = null
            )
        )
        echo("scheduled reminder at $targetDateTime")

    }

}