package sibyl.pastee.paste

import kotlinx.serialization.Serializable

@Serializable
data class PasteSection(
    val name: String = "",
    val syntax: String = "text",
    val contents: String
)