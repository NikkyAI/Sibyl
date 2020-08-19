package sibyl.db

import com.github.ricky12awesome.jss.JsonSchema
import kotlinx.serialization.Serializable
import sibyl.config.WithSchema

@Serializable
data class DatabaseConfig (
    val host: String,
    @JsonSchema.IntRange(1, 65535)
    val port: Int = 5432,
    val database: String = "sibyl",
    val user: String = "postgres",
    val password: String = "postgres"
): WithSchema {
    override val `$schema`: String? = null
}