package sibyl.module.reminders

import org.joda.time.DateTime
import org.joda.time.LocalDateTime
import org.joda.time.Period
import org.joda.time.format.PeriodFormat
import org.joda.time.format.PeriodFormatter
import org.joda.time.format.PeriodFormatterBuilder

object TimeUtil {
    val formatter: PeriodFormatter = PeriodFormatterBuilder()
        .appendYears().appendSuffix("Y")
        .appendMonths().appendSuffix("M")
        .appendDays().appendSuffix("d")
        .appendHours().appendSuffix("h")
        .appendMinutes().appendSuffix("m")
        .appendSeconds().appendSuffix("s")
        .toFormatter()

    fun parsePeriod(str: String): Period = formatter.parsePeriod(str).normalizedStandard()

    @JvmStatic
    fun main(args: Array<String>) {
        val tests = listOf(
            "2d5h30m",
            "2d5h",
            "3Y2d5h",
        )

        println(formatter)

        tests.forEach {  periodStr ->
            val period = parsePeriod(periodStr)
            println("period: $period")
            println("period: ${PeriodFormat.getDefault().print(period)}")
        }

        println(LocalDateTime.now().toString())
    }
}