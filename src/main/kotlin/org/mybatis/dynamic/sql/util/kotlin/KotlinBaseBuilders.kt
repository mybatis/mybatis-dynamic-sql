/**
 *    Copyright 2016-2019 the original author or authors.
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
import org.mybatis.dynamic.sql.util.Buildable
import org.mybatis.dynamic.sql.where.AbstractWhereDSL

abstract class KotlinBaseBuilder<M, W : AbstractWhereDSL<W>, B : KotlinBaseBuilder<M, W, B>> : Buildable<M> {
    fun <T> where(column: BindableColumn<T>, condition: VisitableCondition<T>): B {
        getWhere().where(column, condition)
        return getThis()
    }

    fun <T> where(column: BindableColumn<T>, condition: VisitableCondition<T>, collect: CriteriaReceiver): B {
        getWhere().where(column, condition, collect)
        return getThis()
    }

    fun applyWhere(whereApplier: WhereApplier): B {
        getWhere().applyWhere(whereApplier)
        return getThis()
    }

    fun <T> and(column: BindableColumn<T>, condition: VisitableCondition<T>): B {
        getWhere().and(column, condition)
        return getThis()
    }

    fun <T> and(column: BindableColumn<T>, condition: VisitableCondition<T>, collect: CriteriaReceiver): B {
        getWhere().and(column, condition, collect)
        return getThis()
    }

    fun <T> or(column: BindableColumn<T>, condition: VisitableCondition<T>): B {
        getWhere().or(column, condition)
        return getThis()
    }

    fun <T> or(column: BindableColumn<T>, condition: VisitableCondition<T>, collect: CriteriaReceiver): B {
        getWhere().or(column, condition, collect)
        return getThis()
    }

    protected abstract fun getWhere(): W
    protected abstract fun getThis(): B
}

abstract class KotlinBaseJoiningBuilder<T : AbstractQueryExpressionDSL<T, SelectModel>, W: AbstractWhereDSL<W>, B : KotlinBaseJoiningBuilder<T, W, B>>(private val dsl: AbstractQueryExpressionDSL<T, SelectModel>) :
    KotlinBaseBuilder<SelectModel, W, B>() {

    fun join(table: SqlTable, receiver: JoinReceiver) =
            apply {
                dsl.join(table, receiver)
            }

    fun join(table: SqlTable, alias: String, receiver: JoinReceiver) =
            apply {
                dsl.join(table, alias, receiver)
            }

    fun fullJoin(table: SqlTable, receiver: JoinReceiver) =
            apply {
                dsl.fullJoin(table, receiver)
            }

    fun fullJoin(table: SqlTable, alias: String, receiver: JoinReceiver) =
            apply {
                dsl.fullJoin(table, alias, receiver)
            }

    fun leftJoin(table: SqlTable, receiver: JoinReceiver) =
            apply {
                dsl.leftJoin(table, receiver)
            }

    fun leftJoin(table: SqlTable, alias: String, receiver: JoinReceiver) =
            apply {
                dsl.leftJoin(table, alias, receiver)
            }

    fun rightJoin(table: SqlTable, receiver: JoinReceiver) =
            apply {
                dsl.rightJoin(table, receiver)
            }

    fun rightJoin(table: SqlTable, alias: String, receiver: JoinReceiver) =
            apply {
                dsl.rightJoin(table, alias, receiver)
            }
}
