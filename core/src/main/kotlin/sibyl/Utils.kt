package sibyl

import org.joda.time.DateTime
import sibyl.api.ApiMessage

fun String.removeBlankLines() = lineSequence().filter(String::isNotBlank).joinToString("\n")

fun String.withIndent(indent: String, subsequentIndent: String = indent) =
    indent + lines().joinToString("\n$subsequentIndent")

@Deprecated("don't use")
fun String.asMessage() = ApiMessage(
    username = "Tester",
    text = this,
    gateway = "testgateway",
    timestamp = DateTime.now(),
    channel = "api",
    userid = "testUser"
)

