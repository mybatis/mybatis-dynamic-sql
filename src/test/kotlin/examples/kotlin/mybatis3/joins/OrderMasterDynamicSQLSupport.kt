/*
 *    Copyright 2016-2023 the original author or authors.
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
import java.util.Date

object OrderMasterDynamicSQLSupport {
    val orderMaster = OrderMaster()
    val orderId = orderMaster.orderId
    val orderDate = orderMaster.orderDate

    class OrderMaster : SqlTable("OrderMaster") {
        val orderId = column<Int>(name = "order_id", jdbcType = JDBCType.INTEGER)
        val orderDate = column<Date>(name = "order_date", jdbcType = JDBCType.DATE)
    }
}
