import mu.KotlinLogging
import sibyl.MessageProcessor
import sibyl.SibylModule
import sibyl.Stage
import sibyl.api.ApiMessage
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.random.nextInt

class RoleplayModule : SibylModule("roleplay", "throw dice and other things, only dice are implemented so far") {
    companion object {
        private val logger = KotlinLogging.logger {}

        const val EMPTY = ""
        const val DETAIL_ENABLE = true
        const val DETAIL_MAX_DICE_PER_ROLL = 8
        const val DETAIL_MAX_TOTAL_DICE = 16
    }

    private val diceRegex = "\\b(\\d+)d(\\d+)(?:([v><])(\\d+))?(( )?[+-]\\d+)?\\b".toRegex()

    override fun MessageProcessor.setup() {
        registerIncomingInterceptor(Stage.POST_COMMAND, ::processDiceRolls)
    }

    private suspend fun processDiceRolls(message: ApiMessage, stage: Stage): ApiMessage {
        val matches = diceRegex.findAll(message.text).toList()

        // Prepare and validate some stuff.

        val rolls = matches.map { match ->
            Roll(
                match = match.groupValues[0],
                dice = match.groupValues[1].toInt(),
                sides = match.groupValues[2].toInt(),
                drop = if (match.groupValues[3] == "v") match.groupValues[4].toInt() else null,
                min = if (match.groupValues[3] == ">") match.groupValues[4].toInt() else null,
                max = if (match.groupValues[3] == "<") match.groupValues[4].toInt() else null,
                diceOffset = if ((match.groupValues[5] != EMPTY) && (match.groupValues[6] == EMPTY)) match.groupValues[5].toInt() else 0,
                totalOffset = if (match.groupValues[6] != EMPTY) match.groupValues[5].replace(" ", "").toInt() else 0
            )
        }

        val totalDiceDetail = rolls.sumBy { roll ->
            if (roll.dice <= DETAIL_MAX_DICE_PER_ROLL) roll.dice else 0
        }

        // Calculate each roll (1d20 1d20 1d20 => 3 rolls).
        for (roll in rolls) {
            with(roll) {
                detail = (DETAIL_ENABLE && (dice > 1) &&
                        (dice <= DETAIL_MAX_DICE_PER_ROLL) &&
                        (totalDiceDetail <= DETAIL_MAX_TOTAL_DICE))

                // Build a list of the individual dice roll results (5d20 => 5 results).
                val resultList = MutableList(dice) { _ -> Random.nextInt(1..sides) + diceOffset }

                // Modify dice roll requirements affecting the result of the roll (but not roll.diceRolls).
                if (drop != null) {
                    repeat(drop) {
                        val lowest = resultList.min()
                        if (lowest != null) resultList -= lowest
                    }
                }

                if (min != null) resultList.retainAll { result -> result > min }
                if (max != null) resultList.retainAll { result -> result < max }

                // Calculate the result and average.
                results = resultList.toList()
            }
        }

        // Put the response together.
        val totalResult = rolls.sumBy { it.result }
        val str = rolls.joinToString(" [+] ", "", "") { roll ->
            with(roll) {
                val detailList = if (detail) {
                    results.takeIf { it.isNotEmpty() }
                        ?.joinToString(", ", " (", ")")
                        ?: " (none)"
                } else ""
                val averageString = if (results.size > 1) " ~ ${(average * 10).roundToInt() / 10.0}" else ""
                "$match = $result$detailList$averageString"
            }
        }
        val response = if (matches.size > 1) "$str [=] $totalResult" else str

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