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
package examples.kotlin.mybatis3.sharding

import org.mybatis.dynamic.sql.AliasableSqlTable
import java.sql.JDBCType
import org.mybatis.dynamic.sql.util.kotlin.elements.column

object KTableCodesTableDynamicSQLSupport {
    val tableCodes = TableCodes()
    val id = tableCodes.id
    val description = tableCodes.description

    class TableCodes : AliasableSqlTable<TableCodes>("tableCodes_even", ::TableCodes) {
        val id = column<Int>(name = "id", jdbcType = JDBCType.INTEGER)
        val description = column<String>(name = "description", jdbcType = JDBCType.VARCHAR)
    }
}
