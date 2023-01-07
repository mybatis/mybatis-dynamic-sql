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
package examples.kotlin.spring.canonical

import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.util.kotlin.elements.column
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
        val id = column<Int>(name = "id")
        val firstName = column<String>(name = "first_name")
        val lastName = column(
            name = "last_name",
            parameterTypeConverter = lastNameConverter
        )
        val birthDate = column<Date>(name = "birth_date")
        val employed = column(
            name = "employed",
            parameterTypeConverter = booleanToStringConverter
        )
        val occupation = column<String>(name = "occupation")
        val addressId = column<Int>(name = "address_id")
    }
}
