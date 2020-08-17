package sibyl.pastebin

/**
 * @param content api_paste_code
 * @param userKey api_user_key
 * @param name api_paste_name
 * @param format api_paste_format
 * @param visibility api_paste_private
 * @param expire api_expire_date
 */
data class PastebinRequest(
    val content: String,
    val userKey: String? = null,
    val name: String? = null,
    val format: String? = null,
    val visibility: PasteVisibility = PasteVisibility.HIDDEN,
    val expire: ExpireDate = ExpireDate.ONE_MONTH
)
