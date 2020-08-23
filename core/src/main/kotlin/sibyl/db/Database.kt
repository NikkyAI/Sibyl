package sibyl.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import mu.KotlinLogging
import org.flywaydb.core.Flyway
import sibyl.config.ConfigUtil
import java.io.File
import javax.sql.DataSource

object Database {
    private val logger = KotlinLogging.logger {}
    /**
     * this function initializes [dataSourceForSchema]
     */
    fun dataSourceForSchema(schema: String): HikariDataSource {
        val config = ConfigUtil.load(File("database.json"), DatabaseConfig.serializer()) {
            DatabaseConfig("localhost")
        }

        logger.info { "creating database connection for schema $schema" }

        return HikariDataSource(
            HikariConfig().apply {
                jdbcUrl = "jdbc:postgresql://${config.host}:${config.port}/${config.database}"
                username = config.user
                password = config.password
                this.schema = schema
                addDataSourceProperty( "cachePrepStmts" , "true" )
                addDataSourceProperty( "prepStmtCacheSize" , "250" )
                addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" )
                addDataSourceProperty( "stringtype" , "unspecified" )
            }
        )
    }

    private fun configureFlyway(dataSource: DataSource): Flyway = Flyway.configure()
        .baselineVersion("0")
        .schemas(dataSource.connection.schema) // get schema from connection
        .locations("classpath:migrations/${dataSource.connection.schema}")
        .dataSource(dataSource)
        .load()

    fun flywayCleanBaseline(dataSource: DataSource) {
        val flyway = configureFlyway(dataSource)

        flyway.clean()
        flyway.baseline()
    }

    fun flywayMigrate(dataSource: DataSource) {
        val flyway = configureFlyway(dataSource)

        flyway.migrate()
        flyway.validate()
    }

    fun flywayInfo(dataSource: DataSource) {
        val flyway = configureFlyway(dataSource)

        flyway.info()
    }

    fun flywayValidate(dataSource: DataSource) {
        val flyway = configureFlyway(dataSource)

        flyway.validate()
    }
}