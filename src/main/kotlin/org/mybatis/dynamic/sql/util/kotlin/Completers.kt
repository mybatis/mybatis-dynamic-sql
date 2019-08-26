package org.mybatis.dynamic.sql.util.kotlin

import org.mybatis.dynamic.sql.delete.DeleteDSL
import org.mybatis.dynamic.sql.delete.DeleteModel
import org.mybatis.dynamic.sql.select.CountDSL
import org.mybatis.dynamic.sql.select.QueryExpressionDSL
import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.update.UpdateDSL
import org.mybatis.dynamic.sql.update.UpdateModel
import org.mybatis.dynamic.sql.util.Buildable

typealias CountCompleter = CountDSL<SelectModel>.() -> Buildable<SelectModel>
typealias DeleteCompleter = DeleteDSL<DeleteModel>.() -> Buildable<DeleteModel>
typealias SelectCompleter = QueryExpressionDSL<SelectModel>.() -> Buildable<SelectModel>
typealias UpdateCompleter = UpdateDSL<UpdateModel>.() -> Buildable<UpdateModel>
