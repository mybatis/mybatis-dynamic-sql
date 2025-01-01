/*
 *    Copyright 2016-2025 the original author or authors.
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
package examples.kotlin.mybatis3.column.comparison

import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.util.kotlin.elements.column
import java.sql.JDBCType

object ColumnComparisonDynamicSqlSupport {
    val columnComparison = ColumnComparison()
    val number1 = columnComparison.number1
    val number2 = columnComparison.number2

    val columnList = listOf(number1, number2)

    class ColumnComparison : SqlTable("ColumnComparison") {
        val number1 = column<Int>(name = "number1", jdbcType = JDBCType.INTEGER)
        val number2 = column<Int>(name = "number2", jdbcType = JDBCType.INTEGER)
    }
}
