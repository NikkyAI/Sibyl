package sibyl.module.paste.syntax

import kotlinx.serialization.Serializable

@Serializable
data class SyntaxesResponse(
    val syntaxes: List<Syntax>,
    val success: Boolean
)

