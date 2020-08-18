package sibyl.config

import com.github.ricky12awesome.jss.stringifyToSchema
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import java.io.File

object ConfigUtil {
    private val jsonSerializer = Json {
        encodeDefaults = true
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    fun <T : WithSchema> load(
        file: File,
        serializer: KSerializer<T>,
        json: Json = jsonSerializer,
        defaultGenerator: () -> T
    ): T {
        val serializedSchema = json.stringifyToSchema(serializer)
        val jsonObj = if (file.exists()) {
            val textContent = file.readText()
            json.parseToJsonElement(textContent)
        } else {
            json.encodeToJsonElement(serializer, defaultGenerator())
        }.jsonObject

        val config = json.decodeFromJsonElement(serializer, jsonObj)

        val schemaFile = config.`$schema`
            ?.let { path ->
                file.absoluteFile.parentFile.resolve(path).takeIf {
                    it.startsWith(file.absoluteFile.parentFile) // only accept paths that are siblings of the current file
                }
            }
            ?: file.absoluteFile.parentFile.resolve("./schemas/" + file.nameWithoutExtension + ".schema.json")
        schemaFile.absoluteFile.parentFile.mkdirs()
        schemaFile.writeText(
            serializedSchema
        )
        val relativeSchema = "./" + schemaFile.toRelativeString(file.absoluteFile.parentFile).replace('\\', '/')

        file.writeText(
            json.encodeToString(
                JsonObject.serializer(),
                JsonObject(
                    mapOf("\$schema" to JsonPrimitive(relativeSchema)) + jsonObj.toMap().minus("\$schema")
                )
            )
        )

        return config
    }
}