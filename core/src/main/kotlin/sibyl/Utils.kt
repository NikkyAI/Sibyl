package sibyl

import org.joda.time.DateTime
import sibyl.api.ApiMessage

fun String.removeBlankLines() = lineSequence().filter { it.isNotBlank() }.joinToString("\n")

fun String.asMessage() = ApiMessage(
    username = "Tester",
    text = this,
    gateway = "testgateway",
    timestamp = DateTime.now().toString(),
    channel = "api",
    userid = "testUser"
)

