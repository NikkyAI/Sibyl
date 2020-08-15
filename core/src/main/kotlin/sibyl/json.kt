package sibyl

import kotlinx.serialization.json.Json

val jsonSerializerPretty = Json {
    prettyPrint = true
    encodeDefaults = false
}
val jsonSerializer = Json {
    prettyPrint = false
    encodeDefaults = false
}