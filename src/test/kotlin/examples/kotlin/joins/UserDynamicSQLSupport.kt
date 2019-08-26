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

object UserDynamicSQLSupport {
    object User1 : SqlTable("User") {
        val userId = column<Int>("user_id", JDBCType.INTEGER)
        val userName = column<String>("user_name", JDBCType.VARCHAR)
        val parentId = column<Int>("parent_id", JDBCType.INTEGER)
    }

    object User2 : SqlTable("User") {
        val userId = column<Int>("user_id", JDBCType.INTEGER)
        val userName = column<String>("user_name", JDBCType.VARCHAR)
        val parentId = column<Int>("parent_id", JDBCType.INTEGER)
    }
}
