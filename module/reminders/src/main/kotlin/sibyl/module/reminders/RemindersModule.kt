package sibyl.module.reminders

import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.sqlite.driver.asJdbcDriver
import kotlinx.coroutines.*
import mu.KotlinLogging
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.ISODateTimeFormat
import sibyl.MessageProcessor
import sibyl.SibylModule
import sibyl.api.ApiMessage
import sibyl.commands.SibylCommand
import sibyl.db.*

class RemindersModule : SibylModule("reminders") {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override val commands: List<SibylCommand> = listOf(
        RemindCommand(this)
    )

    val db by lazy {
        val timestampAdapter = object : ColumnAdapter<LocalDateTime, String> {
            val timestampWriteFormat = ISODateTimeFormat.dateTimeNoMillis() // DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
            val timestampReadFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
            override fun decode(databaseValue: String) = LocalDateTime.parse(databaseValue, timestampReadFormat)
            override fun encode(value: LocalDateTime) = value.toString(timestampWriteFormat)
        }

        val dataSource = Database.dataSourceForSchema(SCHEMA_NAME_REMINDERS)
        Database.flywayMigrate(dataSource)
        Database.flywayValidate(dataSource)

        RemindersDatabase(
            driver = dataSource.asJdbcDriver(),
            remindersAdapter = Reminders.Adapter(
                targetAdapter = timestampAdapter,
                requestedAtAdapter = timestampAdapter,
                fulfilledAtAdapter = timestampAdapter
            )
        )
    }

    override fun MessageProcessor.install() {
        val r = db
        logger.info { "loaded data source with $r" }
    }

    private lateinit var job: Job

    override suspend fun start() {
//        logger.info { "pulling unfulfilled messages" }
//        reminders.remindersQueries.selectAll().executeAsList().forEach {
//            logger.info { "reminder: $it" }
//        }

        job = GlobalScope.launch(Dispatchers.Unconfined) {
            while (true) {
                delay(10000)
                checkForReminder()
            }
        }

    }

    private suspend fun checkForReminder() {
        withContext(Dispatchers.IO) {
            val unfulfilled = db.remindersQueries.selectUnfulfilled(LocalDateTime.now()).executeAsList()

            for (reminder in unfulfilled) {
//                val targetTimestamp = LocalDateTime.parse(reminder.target, timestampFormat)
                if (reminder.target.isAfter(LocalDateTime.now())) continue

                sendMessage(
                    ApiMessage(
                        text = "@${reminder.username} : ${reminder.message}",
                        gateway = reminder.gateway,
                        username = "Reminder"
                    ),
                    stage = null,
                    fromCommand = null
                )

                db.remindersQueries.setFulFilled(
                    LocalDateTime.now(), reminder.id
                )
            }
        }
    }

}