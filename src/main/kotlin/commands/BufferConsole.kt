package commands

import com.github.ajalt.clikt.output.CliktConsole
import mu.KotlinLogging

class BufferConsole(
    val stdOutBuilder: StringBuilder = StringBuilder(),
    val stdErrBuilder: StringBuilder = StringBuilder()
) : CliktConsole {
    private val logger = KotlinLogging.logger {}
    override fun promptForLine(prompt: String, hideInput: Boolean): String? {
        TODO("interactivity is not implemented yet")
//        stdOutBUilder.append(prompt)
//        return if (hideInput) throw IllegalStateException("cannot do password prompts")
//        else readLine() // TODO: somehow match input from user on matterbridge
    }

    override fun print(text: String, error: Boolean) {
        // TODO: send to matterbridge
        logger.info { "print('${text.replace("\n", "\\n")}', $error)" }
        if (error) stdErrBuilder.append(text)
        else stdOutBuilder.append(text)
    }

//    override val lineSeparator: String get() = ""
    override val lineSeparator: String get() = "\n"
}