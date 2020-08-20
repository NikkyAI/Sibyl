package sibyl.module.paste.syntax

import kotlinx.serialization.Serializable

@Serializable
data class Syntax(
    val id: Int,
    val short: String,
    val name: String
)