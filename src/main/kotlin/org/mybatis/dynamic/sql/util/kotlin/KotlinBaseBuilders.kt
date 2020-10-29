/*
 *    Copyright 2016-2020 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.dynamic.sql.util.kotlin

import org.mybatis.dynamic.sql.BindableColumn
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.VisitableCondition
import org.mybatis.dynamic.sql.select.AbstractQueryExpressionDSL
import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.where.AbstractWhereDSL

@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
@DslMarker
annotation class MyBatisDslMarker

@MyBatisDslMarker
@Suppress("TooManyFunctions")
abstract class KotlinBaseBuilder<W : AbstractWhereDSL<W>, B : KotlinBaseBuilder<W, B>> {
    fun <T> where(column: BindableColumn<T>, condition: VisitableCondition<T>): B =
        applySelf {
            getWhere().where(column, condition)
        }

    fun <T> where(column: BindableColumn<T>, condition: VisitableCondition<T>, collect: CriteriaReceiver): B =
        applySelf {
            getWhere().where(column, condition, collect)
        }

    fun applyWhere(whereApplier: WhereApplier): B =
        applySelf {
            getWhere().applyWhere(whereApplier)
        }

    fun <T> and(column: BindableColumn<T>, condition: VisitableCondition<T>): B =
        applySelf {
            getWhere().and(column, condition)
        }

    fun <T> and(column: BindableColumn<T>, condition: VisitableCondition<T>, collect: CriteriaReceiver): B =
        applySelf {
            getWhere().and(column, condition, collect)
        }

    fun <T> or(column: BindableColumn<T>, condition: VisitableCondition<T>): B =
        applySelf {
            getWhere().or(column, condition)
        }

    fun <T> or(column: BindableColumn<T>, condition: VisitableCondition<T>, collect: CriteriaReceiver): B =
        applySelf {
            getWhere().or(column, condition, collect)
        }

    fun allRows() = self()

    protected fun applySelf(block: B.() -> Unit): B =
        self().apply { block() }

    protected abstract fun self(): B

    protected abstract fun getWhere(): W
}

abstract class KotlinBaseJoiningBuilder<T : AbstractQueryExpressionDSL<T, SelectModel>, W : AbstractWhereDSL<W>,
        B : KotlinBaseJoiningBuilder<T, W, B>> : KotlinBaseBuilder<W, B>() {

    fun join(table: SqlTable, receiver: JoinReceiver): B =
        applySelf {
            getDsl().join(table, receiver)
        }

    fun join(table: SqlTable, alias: String, receiver: JoinReceiver): B =
        applySelf {
            getDsl().join(table, alias, receiver)
        }

    fun fullJoin(table: SqlTable, receiver: JoinReceiver): B =
        applySelf {
            getDsl().fullJoin(table, receiver)
        }

    fun fullJoin(table: SqlTable, alias: String, receiver: JoinReceiver): B =
        applySelf {
            getDsl().fullJoin(table, alias, receiver)
        }

    fun leftJoin(table: SqlTable, receiver: JoinReceiver): B =
        applySelf {
            getDsl().leftJoin(table, receiver)
        }

    fun leftJoin(table: SqlTable, alias: String, receiver: JoinReceiver): B =
        applySelf {
            getDsl().leftJoin(table, alias, receiver)
        }

    fun rightJoin(table: SqlTable, receiver: JoinReceiver): B =
        applySelf {
            getDsl().rightJoin(table, receiver)
        }

    fun rightJoin(table: SqlTable, alias: String, receiver: JoinReceiver): B =
        applySelf {
            getDsl().rightJoin(table, alias, receiver)
        }

    protected abstract fun getDsl(): AbstractQueryExpressionDSL<T, SelectModel>
}
