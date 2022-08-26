/*
 *    Copyright 2016-2021 the original author or authors.
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

object UserDynamicSQLSupport {
    val user = User()
    val userId = user.userId
    val userName = user.userName
    val parentId = user.parentId

    class User : AliasableSqlTable<User>("User", ::User) {
        val userId = column<Int>(name = "user_id", jdbcType = JDBCType.INTEGER)
        val userName = column<String>(name = "user_name", jdbcType = JDBCType.VARCHAR)
        val parentId = column<Int>(name = "parent_id", jdbcType = JDBCType.INTEGER)
    }
}
