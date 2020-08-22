package sibyl.module.reminders

import com.github.ajalt.clikt.parameters.arguments.*
import org.joda.time.Instant
import org.joda.time.LocalDateTime
import sibyl.commands.SibylCommand
import sibyl.db.Reminders

class RemindCommand(val remindersModule: RemindersModule) : SibylCommand(
    name = "in",
    help = "reminds you after a specified duration in the same channel"
) {
    val period by argument("PERIOD").convert { string ->
        TimeUtil.parsePeriod(string)
    }.validate {  period ->
        require(period.toDurationFrom(Instant.EPOCH).millis > 0) {
            "duration must be positive"
        }
    }
    val message by argument("MESSAGE").multiple().transformAll { args ->
        args.joinToString(" ")
    }
    override fun run() {
        val id = remindersModule.reminders.remindersQueries.selectMaxId().executeAsOneOrNull() ?: 0 // TODO: remove hack when SERIAL is fixed
        remindersModule.addReminder(
            Reminders(
                id = id+1,
                message = message,
                username = causeMessage.username,
                userid = causeMessage.userid,
                gateway = causeMessage.gateway,
                target = LocalDateTime.now() + period, //).toString(ISODateTimeFormat.basicDateTimeNoMillis()),
                requestedAt = LocalDateTime.now(), //.toString(ISODateTimeFormat.basicDateTimeNoMillis()),
                fulfilledAt = null
            )
        )

    }

}