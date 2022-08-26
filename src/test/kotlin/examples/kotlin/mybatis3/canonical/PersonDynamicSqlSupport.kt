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
package examples.kotlin.mybatis3.canonical

import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.util.kotlin.elements.column
import java.sql.JDBCType
import java.util.Date

object PersonDynamicSqlSupport {
    val person = Person()
    val id = person.id
    val firstName = person.firstName
    val lastName = person.lastName
    val birthDate = person.birthDate
    val employed = person.employed
    val occupation = person.occupation
    val addressId = person.addressId

    class Person : SqlTable("Person") {
        val id = column<Int>(name = "id", jdbcType = JDBCType.INTEGER)
        val firstName = column<String>(name = "first_name", jdbcType = JDBCType.VARCHAR)
        val lastName = column<LastName>(
            name = "last_name",
            jdbcType = JDBCType.VARCHAR,
            typeHandler = "examples.kotlin.mybatis3.canonical.LastNameTypeHandler"
        )
        val birthDate = column<Date>(name = "birth_date", jdbcType = JDBCType.DATE)
        val employed = column<Boolean>(
            name = "employed",
            JDBCType.VARCHAR,
            typeHandler = "examples.kotlin.mybatis3.canonical.YesNoTypeHandler"
        )
        val occupation = column<String>(name = "occupation", jdbcType = JDBCType.VARCHAR)
        val addressId = column<Int>(name = "address_id", jdbcType = JDBCType.INTEGER)
    }
}
