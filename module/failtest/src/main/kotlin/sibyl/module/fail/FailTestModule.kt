package sibyl.module.fail

import mu.KotlinLogging
import sibyl.*
import sibyl.api.ApiMessage

class FailTestModule() : SibylModule("failtest", "test errors in interceptors") {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun MessageProcessor.install() {
        registerIncomingInterceptor(Stage.FILTER) { message, stage ->
            message.transformIf(message.text.contains("@failtest_in")) { message ->
                error("failtest on $message")
            }
        }

        registerOutgoingInterceptor(Stage.FILTER) { response, stage ->
            response.transformIf(response.message.text.contains("@failtest_out")) { reponse ->
                error("failtest on ${response.message}")
            }
        }

        registerIncomingInterceptor(Stage.FILTER) { message, stage ->
            message.transformIf(message.text.contains("@failtest_out")) { message ->
                sendMessage(
                    ApiMessage(
                        username = "fail",
                        userid = "failtest",
                        text = "@failtest_out",
                        gateway = message.gateway
                    )
                )
                null
            }
        }
    }
}
