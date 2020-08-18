package sibyl.core

import kotlinx.serialization.Serializable
import sibyl.config.WithSchema

@Serializable
data class CoreConfig(
    val controlGateway: String? = null
): WithSchema {
    override val `$schema`: String? = null
}