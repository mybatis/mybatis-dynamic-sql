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
package examples.kotlin.joins

import org.mybatis.dynamic.sql.SqlTable

import java.sql.JDBCType

object OrderLineDynamicSQLSupport {
    object OrderLine : SqlTable("OrderLine") {
        val orderId = column<Int>("order_id", JDBCType.INTEGER)
        val itemId = column<Int>("item_id", JDBCType.INTEGER)
        val lineNumber = column<Int>("line_number", JDBCType.INTEGER)
        val quantity = column<Int>("quantity", JDBCType.INTEGER)
    }
}
