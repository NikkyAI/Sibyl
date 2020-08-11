package modules

import api.ApiMessage
import com.github.ajalt.clikt.core.CliktCommand
import commands.SibylCommand

abstract class SibylModule(
    val name: String,
    val commandPrefix: String = "!"
) {
    abstract val commands: List<SibylCommand>

    /**
     * function that receives every message
     *
     * @return signifies if the message is consumed and not processed by other modules
     */
    open fun process(message: ApiMessage): Boolean {
        return false
    }

    // TODO pre check interceptor (can check for permissions on all commands from a module)
}