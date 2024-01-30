/*
 *    Copyright 2016-2024 the original author or authors.
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

import org.mybatis.dynamic.sql.AliasableSqlTable
import org.mybatis.dynamic.sql.util.kotlin.elements.column
import java.sql.JDBCType

object ItemMasterDynamicSQLSupport {
    val itemMaster = ItemMaster()
    val itemId = itemMaster.itemId
    val description = itemMaster.description

    class ItemMaster : AliasableSqlTable<ItemMaster>("ItemMaster", ::ItemMaster) {
        val itemId = column<Int>(name = "item_id", jdbcType = JDBCType.INTEGER)
        val description = column<String>(name = "description", jdbcType = JDBCType.VARCHAR)
    }
}
