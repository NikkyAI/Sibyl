package sibyl.module.fail

import mu.KotlinLogging
import sibyl.MessageProcessor
import sibyl.SibylModule

class FailOnLoadModule() : SibylModule("failonload", "tests failure on install") {
    companion object {
        private val logger = KotlinLogging.logger {}
    }


    override fun MessageProcessor.install() {
        error("failed to load")
    }
}
