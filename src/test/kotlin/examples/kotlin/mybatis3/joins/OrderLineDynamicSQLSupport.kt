/*
 *    Copyright 2016-2022 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package examples.kotlin.mybatis3.joins

import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.util.kotlin.elements.column
import java.sql.JDBCType

object OrderLineDynamicSQLSupport {
    val orderLine = OrderLine()
    val orderId = orderLine.orderId
    val itemId = orderLine.itemId
    val lineNumber = orderLine.lineNumber
    val quantity = orderLine.quantity

    class OrderLine : SqlTable("OrderLine") {
        val orderId = column<Int>(name = "order_id", jdbcType = JDBCType.INTEGER)
        val itemId = column<Int>(name = "item_id", jdbcType = JDBCType.INTEGER)
        val lineNumber = column<Int>(name = "line_number", jdbcType = JDBCType.INTEGER)
        val quantity = column<Int>(name = "quantity", jdbcType = JDBCType.INTEGER)
    }
}
