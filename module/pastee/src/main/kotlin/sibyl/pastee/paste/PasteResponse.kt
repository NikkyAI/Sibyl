package sibyl.pastee.paste

import kotlinx.serialization.Serializable

@Serializable
data class PasteResponse(
    val id: String,
    val link: String,
    val success: Boolean
)