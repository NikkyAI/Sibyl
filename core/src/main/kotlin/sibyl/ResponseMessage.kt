package sibyl

import sibyl.api.ApiMessage
import sibyl.commands.SibylCommand

data class ResponseMessage(
    val message: ApiMessage,
    val from: SibylModule,
    val fromCommand: SibylCommand?
)