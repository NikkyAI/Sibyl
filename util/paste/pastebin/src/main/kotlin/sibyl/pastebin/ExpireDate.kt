package sibyl.pastebin

enum class ExpireDate(val code: String) {
    NEVER("N"),
    TEN_MINUTES("10M"),
    ONE_HOUR("1H"),
    ONE_DAY("1D"),
    ONE_WEEK("1W"),
    TWO_WEEKS("2W"),
    ONE_MONTH("1M"),
    SIX_MONTHS("6M"),
    ONE_YEAR("1Y")
}
