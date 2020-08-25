package sibyl

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

fun String.removeBlankLines() = lineSequence().filter(String::isNotBlank).joinToString("\n")

fun String.withIndent(indent: String, subsequentIndent: String = indent) =
    indent + lines().joinToString("\n$subsequentIndent")

@OptIn(ExperimentalContracts::class)
inline fun <T> T.transformIf(condition: Boolean, block: (T) -> T?): T? {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    return if(condition) block(this) else this
}

@OptIn(ExperimentalContracts::class)
inline fun <T> T.consumeIf(condition: Boolean, block: (T) -> Unit): T? {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    return if(condition) {
        block(this)
        null
    } else {
        this
    }
}