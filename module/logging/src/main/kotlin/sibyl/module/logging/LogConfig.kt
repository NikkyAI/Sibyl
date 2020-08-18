package sibyl.module.logging

import kotlinx.serialization.Serializable
import sibyl.config.WithSchema

@Serializable
data class LogConfig(
    val dateFormat: String? = null
) : WithSchema {
    override val `$schema`: String? = null
}
