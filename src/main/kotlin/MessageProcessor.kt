import api.ApiMessage
import commands.asMessage
import commands.runCommand
import modules.SibylModule
import modules.core.CoreModule
import modules.test.TestModule

class MessageProcessor {
    // TODO: make this funtion instead
    internal val modules: MutableList<SibylModule> = mutableListOf()

    init {
        addModule(CoreModule(this))
    }

    fun addModule(newModule: SibylModule) {
        modules.forEach { mod ->
            mod.commands.forEach { cmd ->
                newModule.commands.forEach { newCmd ->
                    require(newCmd.commandName != cmd.commandName) {
                        "duplicate command ${cmd.commandName}"
                    }
                }
            }
        }

        modules += newModule
    }

    // TODO: return value? any kind of response ?
    fun process(message: ApiMessage) {
        // TODO: create context from message

        // TODO: create response holder object
        // if response holder object contains reponse -> break
        // or allow multiple processors to respond to a single message ?

        modules@ for (module in modules) {
            if (message.text.startsWith(module.commandPrefix)) {
                val command = module.commands.find { cmd ->
//                    val regex = "^\\Q$\\E\\s".toRegex(RegexOption.IGNORE_CASE)
//                    message.text.matches(regex)
//                    cmd.commandName.equals(firstWord, ignoreCase = true)
                    message.text.startsWith("${module.commandPrefix}${cmd.commandName} ")
                }
                if (command != null) {
                    command.runCommand(
                        commandPrefix = module.commandPrefix,
                        message = message
                    )

                    // message was consumed by command
                    break@modules
                }
            }

            val consumed = module.process(message)
            if (consumed) break@modules
        }
    }
}


fun main(args: Array<String>) {
    val msgProcessor = MessageProcessor()
    msgProcessor.addModule(TestModule())
    listOf(
//        "!commands",
        "!test",
        "!test --help",
        "!test sub --help",
        "!test ",
        "!test sub 42",
        "!test msg",
        "!test opt --help",
        "!test opt -t 20",
        "!test opt --test 23",
        "!test help opt",
        "!test help opt -v",
        "!test help help",
        "!test help -v",
        "!commands",
        "!whoami",
        "!echo hello world"
    )
        .map {
            it.asMessage()
        }
        .forEach { msg ->
            msgProcessor.process(msg)
        }
}