package api

import jsonSerializer
import kotlinx.serialization.Serializable

/**
 * Created by nikky on 07/05/18.
 *
 * @author Nikky
 * @version 1.0
 */
@Serializable
data class ApiMessage(
    var username: String = "",
    var text: String = "",
    var gateway: String = "",
    var timestamp: String = "",
    var channel: String = "",
    var userid: String = "",
    var avatar: String = "",
    var account: String = "",
    var protocol: String = "",
    var event: String = "",
    var id: String = "",
    var parent_id: String = "",
    var Extra: Map<String, String>? = null
) {
    override fun toString(): String {
        return jsonSerializer.stringify(serializer(), this)
    }

    companion object {
        const val API_CONNECTED = "api_connected"
        const val USER_ACTION = "user_action"
        const val JOIN_LEAVE = "join_leave"
    }
}
