package org.mybatis.dynamic.sql.util.kotlin

import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.select.QueryExpressionDSL
import org.mybatis.dynamic.sql.select.SelectModel

// These functions are intended for use in a Join mapper where a join is setup before the remainder of the query is completed

fun QueryExpressionDSL.FromGatherer<SelectModel>.fromJoining(table: SqlTable,
                                                             enhance: QueryExpressionDSL<SelectModel>.() -> QueryExpressionDSL<SelectModel>) =
        enhance(from(table))

fun QueryExpressionDSL.FromGatherer<SelectModel>.fromJoining(table: SqlTable, alias: String,
                                                             enhance: QueryExpressionDSL<SelectModel>.() -> QueryExpressionDSL<SelectModel>) =
        enhance(from(table, alias))
