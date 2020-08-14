package sibyl

import kotlinx.serialization.json.Json

val jsonSerializer = Json {
    prettyPrint = true
    encodeDefaults = false
}