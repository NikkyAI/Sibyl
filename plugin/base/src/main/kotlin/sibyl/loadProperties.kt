package sibyl

import org.gradle.api.Project
import java.io.File
import java.util.*

fun Project.loadProperties(vararg files: File): Properties {
    return Properties().apply {
        files.forEach { file ->
            if(file.exists()) {
                file.bufferedReader().use{
                    load(it)
                }
            } else {
                logger.lifecycle("file: $file does nto exist")
            }
        }
    }
}
