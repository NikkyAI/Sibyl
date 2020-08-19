package sibyl.db

import org.jooq.*
import org.jooq.impl.DSL

class JOOQBuilder<R : Record?>(val ctx: DSLContext, val table: Table<R>,
                               val condition: Condition? = null) {
    fun where(c: Condition) = if (condition == null) {
        JOOQBuilder(ctx, table, c)
    } else {
        JOOQBuilder(ctx, table, condition.and(c))
    }

    fun select() = ctx.selectFrom(table).where(condition)
    fun <T1>select(f1: SelectField<T1>) = ctx.select(f1).where(condition)
    fun <T1,T2>select(f1: SelectField<T1>,f2: SelectField<T2>) = ctx.select(f1,f2).where(condition)
    fun <T1,T2,T3>select(f1: SelectField<T1>,f2: SelectField<T2>,f3: SelectField<T3>) =
        ctx.select(f1,f2,f3).where(condition)
    fun <T1,T2,T3,T4>select(f1: SelectField<T1>,f2: SelectField<T2>,f3: SelectField<T3>,f4: SelectField<T4>) =
        ctx.select(f1,f2,f3,f4).where(condition)

    fun delete() = ctx.deleteFrom(table).where(condition)

    fun update(): JOOQUpdateBuilder<R> =
        JOOQUpdateBuilder(ctx.update(table), condition ?: DSL.trueCondition())
}

class JOOQUpdateBuilder<R : Record?>(val ctx: UpdateSetFirstStep<R>,
                                     val condition: Condition) {
    operator fun <T> set(field: Field<T>?, value: T): JOOQUpdateMoreBuilder<R> {
        return JOOQUpdateMoreBuilder(ctx.set(field, value), condition)
    }
}


class JOOQUpdateMoreBuilder<R : Record?>(val ctx: UpdateSetMoreStep<R>,
                                     val condition: Condition) {
    operator fun <T> set(field: Field<T>?, value: T): JOOQUpdateMoreBuilder<R> {
        return JOOQUpdateMoreBuilder(ctx.set(field, value), condition)
    }

    fun done(): UpdateConditionStep<R> = ctx.where(condition)

    fun execute() = done().execute()
}