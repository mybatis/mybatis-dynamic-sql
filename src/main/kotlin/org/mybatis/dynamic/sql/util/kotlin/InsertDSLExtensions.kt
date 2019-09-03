package org.mybatis.dynamic.sql.util.kotlin

import org.mybatis.dynamic.sql.insert.InsertDSL
import org.mybatis.dynamic.sql.insert.MultiRowInsertDSL

typealias InsertCompleter<T> = InsertDSL<T>.() -> InsertDSL<T>
typealias MultiRowInsertCompleter<T> = MultiRowInsertDSL<T>.() -> MultiRowInsertDSL<T>
