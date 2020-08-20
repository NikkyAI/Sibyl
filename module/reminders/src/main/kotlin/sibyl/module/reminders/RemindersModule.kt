package sibyl.module.reminders

import sibyl.MessageProcessor
import sibyl.SibylModule
import sibyl.commands.SibylCommand

class RemindersModule() : SibylModule("reminders") {
    override val commands: List<SibylCommand> = listOf(

    )

    override fun MessageProcessor.setup() {

    }
}