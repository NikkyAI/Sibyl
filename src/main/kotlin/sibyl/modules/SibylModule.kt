package sibyl.modules

import sibyl.api.ApiMessage
import sibyl.commands.SibylCommand

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