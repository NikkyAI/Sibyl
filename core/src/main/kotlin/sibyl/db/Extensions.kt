package sibyl.db

import org.jooq.*
import org.jooq.impl.DSL
import javax.sql.DataSource

fun <R> UpdateResultStep<Record1<R>>.fetchNullable(): R? = fetchOptional().orElse(null)?.component1()
fun <R : Record> UpdateResultStep<R>.fetchNullable(): R? = fetchOptional().orElse(null)

fun <R> SelectConditionStep<Record1<R>>.fetchNullable(): R? = fetchOptional().orElse(null)?.component1()
fun <R : Record> SelectConditionStep<R>.fetchNullable(): R? = fetchOptional().orElse(null)

fun <R> DataSource.runTransaction(builder: (context: DSLContext) -> R): R {
    return try {
        DSL.using(this, SQLDialect.POSTGRES).transactionResult { transactionContext ->
            builder(DSL.using(transactionContext))
        }
    } catch(e: Exception) {
        e.printStackTrace()
        throw e
    }
}

suspend fun <R> DataSource.runOperation(builder: suspend (context: DSLContext) -> R): R {
    return try {
        builder(DSL.using(this, SQLDialect.POSTGRES))
    } catch(e: Exception) {
        e.printStackTrace()
        throw e
    }
}