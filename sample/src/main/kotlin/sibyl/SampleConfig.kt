package sibyl

import com.github.ricky12awesome.jss.JsonSchema
import kotlinx.serialization.Serializable
import sibyl.config.WithSchema

@Serializable
data class SampleConfig(
    val host: String,
    val token: String? = null,
    @JsonSchema.IntRange(0, 65535)
    val port: Int = 4242
) : WithSchema {
    override val `$schema`: String? = null
}
