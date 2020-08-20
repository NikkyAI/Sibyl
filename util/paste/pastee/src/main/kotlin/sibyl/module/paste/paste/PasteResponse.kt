package sibyl.module.paste.paste

import kotlinx.serialization.Serializable

@Serializable
data class PasteResponse(
    val id: String,
    val link: String,
    val success: Boolean
)