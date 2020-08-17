package sibyl.pastee.paste

import kotlinx.serialization.Serializable

/**
 * Created by nikky on 09/07/18.
 * @author Nikky
 */

@Serializable
data class Paste(
    val encrypted: Boolean = false,
    val description: String = "",
    val sections: List<PasteSection>
)