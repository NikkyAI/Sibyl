package sibyl.module.accounts

import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.sqlite.driver.asJdbcDriver
import kotlinx.coroutines.*
import mu.KotlinLogging
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.ISODateTimeFormat
import sibyl.MessageProcessor
import sibyl.SibylModule
import sibyl.Stage
import sibyl.api.ApiMessage
import sibyl.commands.SibylCommand
import sibyl.db.*

class AccountsModule : SibylModule(
    name = "accounts",
    description = "identifying users across platforms"
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override val commands: List<SibylCommand> = listOf(
        AccountCommand(this)
    )

    val db by lazy {
        val timestampAdapter = object : ColumnAdapter<LocalDateTime, String> {
            val timestampWriteFormat = ISODateTimeFormat.dateTimeNoMillis() // DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
            val timestampReadFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
            override fun decode(databaseValue: String) = LocalDateTime.parse(databaseValue, timestampReadFormat)
            override fun encode(value: LocalDateTime) = value.toString(timestampWriteFormat)
        }

        val dataSource = Database.dataSourceForSchema(SCHEMA_NAME_ACCOUNTS)
        Database.flywayMigrate(dataSource)
        Database.flywayValidate(dataSource)

        AccountsDatabase(
            driver = dataSource.asJdbcDriver(),
            lastSeenAdapter = LastSeen.Adapter(
                timestampAdapter = timestampAdapter
            ),
            connectRequestsAdapter = ConnectRequests.Adapter(
                requestedAtAdapter = timestampAdapter
            ),
            accountsAdapter = Accounts.Adapter(
                registeredAtAdapter = timestampAdapter
            )
        )
    }

    override fun MessageProcessor.install() {
        db.lastSeenQueries.selectAll().executeAsList()
        logger.info { "loaded data source" }

        registerIncomingInterceptor(Stage.PRE_FILTER, ::lastSeen)
    }

    override suspend fun start() {

    }

    private suspend fun lastSeen(message: ApiMessage, stage: Stage): ApiMessage {
        withContext(Dispatchers.IO) {
            if(message.username.isBlank() || message.userid.isBlank() || message.platform.isBlank()) return@withContext
            val existingEntry = db.lastSeenQueries.select(
                platform = message.platform,
                username = message.username.toLowerCase()
            ).executeAsOneOrNull()

            logger.info { "existing entry; $existingEntry" }

            if(existingEntry == null) {
                db.lastSeenQueries.insert(
                    username = message.username.toLowerCase(),
                    platform = message.platform,
                    userid = message.userid,
                    timestamp = message.timestamp.toLocalDateTime(),
                )
            } else {
                db.lastSeenQueries.update(
                    username = message.username.toLowerCase(),
                    platform = message.platform,
                    userid = message.userid,
                    timestamp = message.timestamp.toLocalDateTime(),
                )
            }
        }

        return message
    }

    suspend fun identifyAccount(message: ApiMessage): String? = identifyAccount(platform = message.platform, userid = message.userid)
    suspend fun identifyAccount(platform: String, userid: String): String? = withContext(Dispatchers.IO) {
        val platformEntry = db.platformQueries.select(platform = platform, userid = userid).executeAsOneOrNull()

        platformEntry?.account
    }
    suspend fun accountInfo(account: String)= withContext(Dispatchers.IO) {
        db.accountQueries.select(id = account).executeAsOneOrNull()
    }

    fun platformsForAccount(account: String): String {
        val existingPlatformAccounts = db.platformQueries.selectAllForAccount(
            account = account
        ).executeAsList()
       return existingPlatformAccounts.joinToString(", ") {
            "`${it.platform}`"
        }
    }

}