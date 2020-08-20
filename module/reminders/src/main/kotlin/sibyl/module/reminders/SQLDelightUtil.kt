package sibyl.module.reminders

import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.asJdbcDriver
import org.joda.time.DateTime
import sibyl.db.Database
import sibyl.db.Reminders
import sibyl.db.RemindersDatabase

object SQLDelightUtil  {
    val reminders by lazy {
        val timestampAdapter = object : ColumnAdapter<DateTime, String> {
            override fun decode(databaseValue: String) = DateTime.parse(databaseValue)
            override fun encode(value: DateTime) = value.toString()
        }

        val dataSource = Database.dataSourceForSchema("sibyl-reminders")
        val driver: SqlDriver = dataSource.asJdbcDriver()

        RemindersDatabase(
            driver = driver,
            remindersAdapter = Reminders.Adapter(
                timestampAdapter = timestampAdapter
            )
        )
    }
}