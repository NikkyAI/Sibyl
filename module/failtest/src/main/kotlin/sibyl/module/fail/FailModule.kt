package sibyl.module.fail

import mu.KotlinLogging
import sibyl.MessageProcessor
import sibyl.SibylModule

class FailModule() : SibylModule("log", "logs messages and allows you to retrieve them") {
    companion object {
        private val logger = KotlinLogging.logger {}
    }


    override fun MessageProcessor.install() {
        error("failed to load")
    }
}
