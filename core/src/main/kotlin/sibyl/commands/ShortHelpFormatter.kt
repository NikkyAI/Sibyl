package sibyl.commands

import com.github.ajalt.clikt.output.CliktHelpFormatter
import com.github.ajalt.clikt.output.HelpFormatter
import mu.KotlinLogging

class ShortHelpFormatter(
    var verbose: Boolean = false,
    indent: String = "  ",
    width: Int? = null,
    maxWidth: Int = 200, // 78
    maxColWidth: Int? = null,
    usageTitle: String = "Usage:",
    optionsTitle: String = "Options:",
    argumentsTitle: String = "Arguments:",
    commandsTitle: String = "Commands:",
    optionsMetavar: String = "[OPTIONS]",
    commandMetavar: String = "COMMAND [ARGS]...",
    colSpacing: Int = 2,
    requiredOptionMarker: String? = "*",
    showDefaultValues: Boolean = false,
    showRequiredTag: Boolean = false
) : CliktHelpFormatter(
    indent = indent,
    width = width,
    maxWidth = maxWidth,
    maxColWidth = maxColWidth,
    usageTitle = usageTitle,
    optionsTitle = optionsTitle,
    argumentsTitle = argumentsTitle,
    commandsTitle = commandsTitle,
    optionsMetavar = optionsMetavar,
    commandMetavar = commandMetavar,
    colSpacing = colSpacing,
    requiredOptionMarker = requiredOptionMarker,
    showDefaultValues = showDefaultValues,
    showRequiredTag = showRequiredTag
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun StringBuilder.addUsage(
        parameters: List<HelpFormatter.ParameterHelp>,
        programName: String
    ) {
        val prog = "${renderSectionTitle(usageTitle)} $programName"
        val usage = buildString {
            if (parameters.any { it is HelpFormatter.ParameterHelp.Option }) {
                val optionsStr = buildString {
                    parameters
                        .filterIsInstance<HelpFormatter.ParameterHelp.Option>()
                        .forEach { option ->
                            logger.debug { "names: ${option.names}" }
                            logger.debug { "groupName: ${option.groupName}" }
                            logger.debug { "nvalues: ${option.nvalues}" }
                            logger.debug { "secondaryNames: ${option.secondaryNames}" }
                            logger.debug { "metavar: ${option.metavar}" }
                        }
                    val options = parameters.filterIsInstance<HelpFormatter.ParameterHelp.Option>()
                    val flagOptions = options.filter { it.nvalues == 0 }
//                    val longOptions = options.filter { it.names.first().startsWith("--") }
                    val valueOptions = options - flagOptions

                    append('[')
                    val optionParts = flagOptions.map { option ->
                        val prefix = if (HelpFormatter.Tags.REQUIRED in option.tags) {
                            requiredOptionMarker
                        } else ""
                        prefix + option.names.first() +
                                (option.secondaryNames.firstOrNull()?.let {
                                    "/$it"
                                } ?: "")
                    } + valueOptions.map { option ->
                        val prefix = if (HelpFormatter.Tags.REQUIRED in option.tags) {
                            requiredOptionMarker
                        } else ""
                        prefix + option.names.first() + " " + option.metavar
                    }
                    append(optionParts.joinToString("/"))
//                    if (flagOptions.isNotEmpty()) {
//                        val flagString = flagOptions.joinToString { option ->
//                            val prefix = if (HelpFormatter.Tags.REQUIRED in option.tags) {
//                                requiredOptionMarker + " "
//                            } else ""
//                            prefix + option.names.first() +
//                                    (option.secondaryNames.firstOrNull()?.let {
//                                        "/$it"
//                                    } ?: "")
//                        }
//                        append(flagString)
//
//                        if (valueOptions.isNotEmpty()) {
//                            append(", ")
//                        }
//                    }
//
//                    if (valueOptions.isNotEmpty()) {
//                        val valueOptionString = valueOptions.joinToString { option ->
//                            val prefix = if (HelpFormatter.Tags.REQUIRED in option.tags) {
//                                requiredOptionMarker + " "
//                            } else ""
//                            prefix + option.names.first() + " " + option.metavar
//                        }
//                        append(valueOptionString)
//                    }
                    append(']')
                }

                append(optionsStr)
            }

            parameters.filterIsInstance<HelpFormatter.ParameterHelp.Argument>().forEach {
                append(" ")
                if (!it.required) append("[")
                append(it.name)
                if (!it.required) append("]")
                if (it.repeatable) append("...")
            }

            if (parameters.any { it is HelpFormatter.ParameterHelp.Subcommand }) {
                val subCommandsStr= parameters
                    .filterIsInstance<HelpFormatter.ParameterHelp.Subcommand>()
                    .map { subCmd ->
                        subCmd.name
                    }
                    .joinToString("|")

                append(" [").append(subCommandsStr).append("]")
//                append(" ").append(commandMetavar)
            }
        }

        if (usage.isEmpty()) {
            append(prog)
        } else if (prog.graphemeLength >= width - 20) {
            append(prog).append("\n")
            val usageIndent = " ".repeat(minOf(width / 3, 11))
            append(usage)
//            usage.wrapText(this, width, usageIndent, usageIndent)
        } else {
            val usageIndent = " ".repeat(prog.length + 1)
            append(prog)
            append(" ")
            append(usage)
//            usage.wrapText(this, width, "$prog ", usageIndent)
        }
    }

    override fun formatHelp(
        prolog: String,
        epilog: String,
        parameters: List<HelpFormatter.ParameterHelp>,
        programName: String
    ): String {
        logger.info { "verbose: $verbose" }
        return if (verbose) {
            super.formatHelp(
                prolog = prolog,
                epilog = epilog,
                parameters = parameters,
                programName = programName
            )
        } else {
            val prefix = prolog.lines().firstOrNull()?.takeIf(String::isNotBlank)?.let {
                "$it | "
            } ?: ""
            prefix + super.formatUsage(
                parameters = parameters,
                programName = programName
            )
        }
    }


    override fun StringBuilder.addProlog(prolog: String) {
        if (prolog.isNotEmpty()) {
            appendln()
            append(prolog.withIndent("  "))
        }
    }

    override fun StringBuilder.addCommands(parameters: List<HelpFormatter.ParameterHelp>) {
        val commands = parameters.filterIsInstance<HelpFormatter.ParameterHelp.Subcommand>().map {
            DefinitionRow(renderSubcommandName(it.name), renderHelpText(it.help, it.tags))
        }
        if (commands.isNotEmpty()) {
            append("\n")
            section(commandsTitle)
            customAppendDefinitionList(commands)
        }
    }

//    override fun StringBuilder.addOptions(parameters: List<HelpFormatter.ParameterHelp>) {
//        val groupsByName = parameters.filterIsInstance<HelpFormatter.ParameterHelp.Group>().associateBy { it.name }
//        parameters.filterIsInstance<HelpFormatter.ParameterHelp.Option>()
//            .groupBy { it.groupName }
//            .toList()
//            .sortedBy { it.first == null }
//            .forEach { (title, params) ->
//                addOptionGroup(title?.let { "$it:" } ?: optionsTitle, groupsByName[title]?.help, params)
//            }
//    }

    override fun StringBuilder.addOptionGroup(
        title: String,
        help: String?,
        parameters: List<HelpFormatter.ParameterHelp.Option>
    ) {
        val options = parameters.map {
            val names = mutableListOf(joinNamesForOption(it.names))
            if (it.secondaryNames.isNotEmpty()) names += joinNamesForOption(it.secondaryNames)
            DefinitionRow(
                col1 = names.joinToString(" / ", postfix = optionMetavar(it)),
                col2 = renderHelpText(it.help, it.tags),
                marker = if (HelpFormatter.Tags.REQUIRED in it.tags) requiredOptionMarker else null
            )
        }
        if (options.isNotEmpty()) {
            append("\n")
            section(title)
            if (help != null) append("\n")
            if (help != null) {
                append("  ")
                append(help.withIndent("  "))
            }
            if (help != null) append("\n\n")
            customAppendDefinitionList(options)
        }
    }

    protected fun StringBuilder.customAppendDefinitionList(rows: List<DefinitionRow>) {
        if (rows.isEmpty()) return
        val firstWidth = measureFirstColumn(rows)
        for ((i, row) in rows.withIndex()) {
            val (col1, col2, marker) = row
            if (i > 0) append("\n")

            val firstIndent = when {
                marker.isNullOrEmpty() -> indent
                else -> marker + indent.drop(marker.graphemeLength).ifEmpty { " " }
            }
            val subsequentIndent = " ".repeat(firstIndent.graphemeLength + firstWidth + colSpacing)

            if (col2.isBlank()) {
                append(firstIndent).append(col1)
            } else {
                val initialIndent = if (col1.graphemeLength > maxColWidth) {
                    // If the first column is too wide, append it and start the second column on a new line
                    append(firstIndent).append(col1).append("\n")
                    subsequentIndent
                } else {
                    // If the first column fits, use it as the initial indent for wrapping
                    buildString {
                        append(firstIndent).append(col1)
                        // Pad the difference between this column's width and the table's first column width
                        repeat(firstWidth - col1.graphemeLength + colSpacing) { append(" ") }
                    }
                }

                append(col2.withIndent(initialIndent, subsequentIndent))
//                col2.wrapText(this, width, initialIndent, subsequentIndent)
            }
        }
    }

    fun String.withIndent(indent: String, subsequentIndent: String = indent) =
        indent + lines().joinToString("\n$subsequentIndent")

    private fun StringBuilder.section(title: String) {
        //append("\n")
        append(renderSectionTitle(title))
        append("\n")
    }

    private fun measureFirstColumn(rows: List<DefinitionRow>): Int =
        rows.maxBy { it.col1.graphemeLength }?.col1?.graphemeLength?.coerceAtMost(maxColWidth) ?: maxColWidth

}