package sibyl.module.accounts

import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.arguments.validate
import com.github.ajalt.clikt.parameters.options.eagerOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.joda.time.LocalDateTime
import org.postgresql.util.PSQLException
import sibyl.commands.SibylCommand
import sibyl.db.Accounts
import sibyl.db.ConnectRequests
import sibyl.db.PlatformAccount

class AccountCommand(val module: AccountsModule) : SibylCommand(
    name = "account",
    help = "manage and link different platforms",
    invokeWithoutSubcommand = false
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    init {
        subcommands(
            Info(module),
            WhoIs(module),
            Register(module),
            Connect(module)
        )
    }

    override fun run() {}

    class Info(val module: AccountsModule) : SibylCommand(
        name = "info",
        help = "display info about your account"
    ) {
        override fun run() {
            runBlocking {
                withContext(Dispatchers.IO) {
                    val myAccount = module.identifyAccount(causeMessage)
                    if (myAccount == null) {
                        echo("not registered, create a account with `${module.commandPrefix}account register [NAME]` or request to connect with `!account connect request [ACCOUNT]")
                        return@withContext
                    }
                    val info = module.accountInfo(myAccount)!!
                    val platforms = module.db.platformQueries.selectAllForAccount(account = myAccount)
                        .executeAsList()
                        .joinToString { "`${it.userid} @ ${it.platform}`" }
                    echo("@$myAccount registered ${info.registeredAt} connected to: $platforms")
                }
            }
        }
    }

    class WhoIs(val module: AccountsModule) : SibylCommand(
        name = "whois",
        help = "display account id"
    ) {
        private val username by argument("USERNAME").convert { username ->
            username.toLowerCase()
        }

        override fun run() {
            runBlocking {
                val lastSeenResults = module.db.lastSeenQueries.select(
                    platform = causeMessage.platform,
                    username = username
                ).executeAsList()
                if (lastSeenResults.isEmpty()) {
                    echo("username '$username' was not found in recently active chatters")
                    return@runBlocking
                }
                lastSeenResults.forEach { lastSeen ->
                    val account = module.identifyAccount(lastSeen.platform, lastSeen.userid) ?: "not registered"

                    echo("${lastSeen.username}: $account")
                }
            }
        }
    }

    class Register(val module: AccountsModule) : SibylCommand(
        name = "register",
        help = "register a new account"
    ) {

        private val newAccountId by argument("NAME").validate { name ->
            require(name.matches("[A-Za-z_0-9]{2,16}".toRegex())) { "name must contain only alphanumerical characters length 2..16" }
        }

        override fun run() {
            runBlocking {
                val existingAccount = module.identifyAccount(causeMessage)
                if (existingAccount != null) {
                    echo("you already have a account $existingAccount")
                    return@runBlocking
                }
                val conflictingAccount = module.db.accountQueries.select(
                    newAccountId
                ).executeAsOneOrNull()
                if (conflictingAccount != null) {
                    echo("there is already a account '${newAccountId}' registered")
                    return@runBlocking
                }
                module.db.accountQueries.insert(
                    Accounts(
                        id = newAccountId,
                        registeredAt = LocalDateTime.now()
                    )
                )
                // link current platform to it
                module.db.platformQueries.insert(
                    PlatformAccount(
                        platform = causeMessage.platform,
                        userid = causeMessage.userid,
                        account = newAccountId
                    )
                )
                echo("@$newAccountId: registered account")
            }

        }
    }

    class Connect(module: AccountsModule) : SibylCommand(
        name = "connect",
        help = "connect different platforms together",
        invokeWithoutSubcommand = false
    ) {
        init {
            subcommands(
                Request(module),
                Info(module),
                Cancel(module),
                Confirm(module),
                Deny(module)
            )
        }

        override fun run() {
        }

        class Request(val module: AccountsModule) : SibylCommand(
            name = "request",
            help = "request to connect a platform account"
        ) {
            private val account by argument("ACCOUNT").validate { account ->
                val accountResult = module.db.accountQueries.select(account).executeAsOneOrNull()
                require(accountResult != null) {
                    "account $account does not exist"
                }
            }

            override fun run() {
                // TODO: check if a request exists already

                val existingRequest =  module.db.connectRequestQueries.select(
                    platform = causeMessage.platform,
                    username = causeMessage.username.toLowerCase(),
                    account = account
                ).executeAsOneOrNull()

                if(existingRequest != null) {
                    echo("request exists already..")
                } else {

                    try {
                        module.db.connectRequestQueries.insert(
                            ConnectRequests(
                                fromPlatform = causeMessage.platform,
                                fromUsername = causeMessage.username.toLowerCase(),
                                fromUserid = causeMessage.userid,
                                requestedAt = LocalDateTime.now(),
                                account = account
                            )
                        )
                    } catch (e: PSQLException) {
                        logger.error(e) { "failure during SQL insert" }
                        throw e
                    }
                }

                val platforms = module.platformsForAccount(account)

                echo("@$account please confirm by sending `!account connect confirm ${causeMessage.username.toLowerCase()} ${causeMessage.platform}` on ($platforms)")
            }
        }

        class Info(val module: AccountsModule) : SibylCommand(
            name = "info",
            help = "list open requests"
        ) {
            override fun run() {
                runBlocking {
                    withContext(Dispatchers.IO) {
                        val myAccount = module.identifyAccount(causeMessage)

                        if (myAccount == null) {
                            val requests = module.db.connectRequestQueries.selectByRequester(
                                platform = causeMessage.platform,
                                userid = causeMessage.userid
                            ).executeAsList()
                            when {
                                requests.isEmpty() -> {
                                    echo("@${causeMessage.username}: no open requests")
                                }
                                requests.size == 1 -> {
                                    val request = requests[0]

                                    val platforms = module.platformsForAccount(request.account)
                                    echo("@${causeMessage.username}: `!account confirm|deny ${request.fromUsername} ${request.fromPlatform}` on ($platforms)")
                                }
                                requests.size > 1 -> {
                                    echo("@${causeMessage.username}: ${requests.size} open requests")
                                    requests.forEach { request ->
                                        echo("> @${request.account}: `!account confirm|deny ${request.fromUsername} ${request.fromPlatform}`")
                                    }
                                }
                            }
                        } else {
                            val requests = module.db.connectRequestQueries.selectAll(myAccount).executeAsList()
                            when {
                                requests.isEmpty() -> {
                                    echo("@$myAccount: no open requests")
                                }
                                requests.size == 1 -> {
                                    val request = requests[0]
                                    val platforms = module.platformsForAccount(myAccount)
                                    echo("@${request.account}: `!account confirm|deny ${request.fromUsername} ${request.fromPlatform}` on ($platforms)")
                                }
                                requests.size > 1 -> {
                                    val existingPlatformAccounts = module.db.platformQueries.selectAllForAccount(
                                        account = myAccount
                                    ).executeAsList()
                                    val platforms = existingPlatformAccounts.joinToString(",") {
                                        "`${it.platform}`"
                                    }
                                    echo("@$myAccount: ${requests.size} open requests, respond to them on ($platforms)")
                                    requests.forEach { request ->
                                        echo("> `!account confirm|deny ${request.fromUsername} ${request.fromPlatform}`")
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }

        class Cancel(val module: AccountsModule) : SibylCommand(
            name = "cancel",
            help = "cancel connection requests"
        ) {
            private val account by argument("ACCOUNT").validate { account ->
                val accountResult = module.db.accountQueries.select(account).executeAsOneOrNull()
                require(accountResult != null) {
                    "account $account does not exist"
                }
            }

            override fun run() {
                val request = module.db.connectRequestQueries.select(
                    platform = causeMessage.platform,
                    username = causeMessage.username.toLowerCase(),
                    account = account
                ).executeAsOneOrNull()
                if (request == null) {
                    echo("no connection request found")
                    return
                }
                module.db.connectRequestQueries.delete(
                    platform = request.fromPlatform,
                    username = request.fromUsername,
                    account = account
                )
            }
        }

        class Confirm(val module: AccountsModule) : SibylCommand(
            name = "confirm",
            help = "confirm connecting a platform account"
        ) {
            private val username by argument("USERNAME").convert { it.toLowerCase() }
            private val platform by argument("PLATFORM")
            override fun run() {
                runBlocking {
                    withContext(Dispatchers.IO) {
                        val myAccount = module.identifyAccount(causeMessage)
                        if (myAccount == null) {
                            echo("not registered, create a account with `${module.commandPrefix}account register [NAME]` or request to connect with `!account connect request [ACCOUNT]`")
                            return@withContext
                        }
                        val request = module.db.connectRequestQueries.select(
                            platform = platform,
                            username = username,
                            account = myAccount
                        ).executeAsOneOrNull()
                        if (request == null) {
                            echo("no connection request found")
                            return@withContext
                        }
                        module.db.platformQueries.insert(
                            PlatformAccount(
                                platform = request.fromPlatform,
                                userid = request.fromUserid,
                                account = myAccount
                            )
                        )
                        module.db.connectRequestQueries.delete(
                            platform = platform,
                            username = username,
                            account = myAccount
                        )
                        echo("@$myAccount you added `$username @ $platform` to your account")
                    }
                }
            }
        }

        class Deny(val module: AccountsModule) : SibylCommand(
            name = "deny",
            help = "deny connection requests"
        ) {
            init {
                eagerOption("--all") {
                    runBlocking {
                        withContext(Dispatchers.IO) {
                            val myAccount = module.identifyAccount(causeMessage)
                            if (myAccount == null) {
                                echo("not registered, create a account with `${module.commandPrefix}account register [NAME]` or request to connect with `!account connect request [ACCOUNT]")
                                return@withContext
                            }

                            val requests =
                                module.db.connectRequestQueries.selectAll(account = myAccount).executeAsList()
                            module.db.connectRequestQueries.deleteAll(account = myAccount)

                            echo("@$myAccount: deleted ${requests.size} requests")
                        }
                    }
                }
            }

            private val username by argument("USERNAME").convert { it.toLowerCase() }
            private val platform by argument("PLATFORM")
            override fun run() {
                runBlocking {
                    withContext(Dispatchers.IO) {
                        val myAccount = module.identifyAccount(causeMessage)
                        if (myAccount == null) {
                            echo("not registered, create a account with `${module.commandPrefix}account register [NAME]` or request to connect with `!account connect request [ACCOUNT]")
                            return@withContext
                        }

                        val request = module.db.connectRequestQueries.select(
                            platform = platform,
                            username = username,
                            account = myAccount
                        ).executeAsOneOrNull()

                        if (request == null) {
                            echo("no request found to delete")
                            return@withContext
                        }
                        module.db.connectRequestQueries.delete(
                            platform = platform,
                            username = username,
                            account = myAccount
                        )
                    }
                }
            }
        }
    }

}