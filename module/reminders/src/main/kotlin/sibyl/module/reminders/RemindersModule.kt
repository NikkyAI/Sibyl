package sibyl.module.reminders

import com.zaxxer.hikari.HikariDataSource
import mu.KotlinLogging
import sibyl.MessageProcessor
import sibyl.SibylModule
import sibyl.commands.SibylCommand
import sibyl.db.Database
import javax.sql.DataSource

class RemindersModule() : SibylModule("reminders") {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override val commands: List<SibylCommand> = listOf(

    )

    private lateinit var dataSource: HikariDataSource

    override fun MessageProcessor.setup() {
        dataSource = Database.dataSourceForSchema(schema = "sibyl-reminders")
        Database.flywayMigrate(dataSource)
        Database.flywayValidate(dataSource)
        logger.info { "loaded data source" }
    }
}