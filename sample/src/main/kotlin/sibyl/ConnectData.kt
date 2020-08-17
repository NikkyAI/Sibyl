package sibyl

import kotlinx.serialization.Serializable

@Serializable
data class ConnectData(
    val controlGateway: String,
    val host: String,
    val token: String? = null,
    val port: Int = 4242
)