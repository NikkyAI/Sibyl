package sibyl.haste

import kotlinx.serialization.Serializable

@Serializable
data class HasteServerResponse(
    val key: String
)