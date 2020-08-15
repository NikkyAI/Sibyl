import mu.KotlinLogging
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import sibyl.*
import sibyl.api.ApiMessage
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.random.nextInt

class RoleplayModule(
    val dtFormat: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"),
    val messageFormat: (ApiMessage, DateTimeFormatter) -> String = { message, dtf ->
        val prefix = "${message.timestamp.toString(dtf)} <${message.username} ${message.userid}> "
        prefix + message.text.withIndent("", " ".repeat(prefix.length))
    }
) : SibylModule("log") {
    companion object {
        private val logger = KotlinLogging.logger {}

        val AFTER_COMMAND = Stage("AFTER_COMMANDS", 4)

        const val EMPTY = ""
        const val DETAIL_ENABLE = true
        const val DETAIL_MAX_DICE_PER_ROLL = 8
        const val DETAIL_MAX_TOTAL_DICE = 16
    }

    private val diceRegex = "\\b(\\d+)d(\\d+)(?:([v><])(\\d+))?(( )?[+-]\\d+)?\\b".toRegex()

    override fun MessageProcessor.setup() {
        registerIncomingInterceptor(AFTER_COMMAND, ::processDicerolls)
    }

    override fun start() {
    }

    suspend fun processDicerolls(message: ApiMessage, stage: Stage): ApiMessage {
        val matches = diceRegex.findAll(message.text).toList()

        // Prepare and validate some stuff.
        val rolls = mutableListOf<Roll>()
        var totalDice: Int = 0

        matches.forEach { match ->
            val dice  = match.groupValues[1].toInt()
            val sides = match.groupValues[2].toInt()
            val drop  = if(match.groupValues[3] == "v") match.groupValues[4].toInt() else null // TODO: nullable here
            val min   = if(match.groupValues[3] == ">") match.groupValues[4].toInt() else null
            val max   = if(match.groupValues[3] == "<") match.groupValues[4].toInt() else null
            val diceOffset  = (if((match.groupValues[5] != EMPTY) && (match.groupValues[6] == EMPTY)) match.groupValues[5].toInt() else 0)
            val totalOffset = if(match.groupValues[6] != EMPTY) match.groupValues[5].replace(" ", "").toInt() else 0

            totalDice += (if(dice <= 8) dice else 1)
            rolls += Roll(
                match = match.groupValues[0],
                dice = dice,
                sides = sides,
                drop = drop,
                min = min,
                max = max,
                diceOffset = diceOffset,
                totalOffset = totalOffset
            )
        }

        // Calculate each roll (1d20 1d20 1d20 => 3 rolls).
        for (roll in rolls) {
//            val (match, dice, sides, drop, min, max, diceOffset, totalOffset) = roll
            val dice = roll.dice
            val sides = roll.sides
            val drop = roll.drop
            val min = roll.min
            val max = roll.max
            val diceOffset = roll.diceOffset

            roll.detail = (DETAIL_ENABLE && (dice > 1) &&
                    (dice <= DETAIL_MAX_DICE_PER_ROLL) &&
                    (totalDice <= DETAIL_MAX_TOTAL_DICE))

            // Build an array of the individual dice roll results (5d20 => 5 results).

            val results = (0 until dice).map { i -> Random.nextInt(1..sides) + diceOffset }.toMutableList()

                    // Modify dice roll requirements affecting the result of the roll (but not roll.diceRolls).
            if (drop != null) {
//                results = results.sorted()
//                results = results.drop(drop)
//                val toDrop = results.sorted().take(drop)
//                results -= toDrop
                repeat(drop) {
                    val lowest = results.min()
                    if(lowest != null) results -= lowest
                }
            }

            if (min != null) results.retainAll { result -> result > min }
            if (max != null) results.retainAll { result -> result < max }

            // Calculate the result and average.
            roll.results = results
        }

        // Put the response together.
        val totalResult = rolls.sumBy { it.result }
        val str = rolls.joinToString(" [+] ", "", "") { roll ->
            val match = roll.match
            val detail = roll.detail
            val result = roll.result
            val results = roll.results
            val average = roll.average

            val detailList = if (detail) {
                results.takeIf { it.isNotEmpty() }
                    ?.joinToString(", ", " (", ")")
                    ?: " (none)"
            } else ""
            val s = "$match = $result$detailList" +
                    if (results.size > 1) " ~ ${(average * 10).roundToInt() / 10.0}" else ""
            s
        }
        val response = if(matches.size > 1) "$str [=] $totalResult" else str

        sendMessage(
            ApiMessage(
                gateway = message.gateway,
                text = response
            ),
            stage
        )

        return message
    }

    data class Roll(
        val match: String,
        val dice: Int,
        val sides: Int,
        val drop: Int?,
        val min: Int?,
        val max: Int?,
        val diceOffset: Int,
        val totalOffset: Int
    ) {
        var detail: Boolean = false
        var results: List<Int> = listOf()

        val result: Int get() = results.sum() + totalOffset
        val average: Double get() = results.average()
    }
}