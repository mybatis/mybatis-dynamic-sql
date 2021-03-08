/*
 *    Copyright 2016-2021 the original author or authors.
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
package examples.kotlin.spring.canonical

import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.util.kotlin.column
import java.util.Date

object PersonDynamicSqlSupport {
    object Person : SqlTable("Person") {
        val id = column<Int>("id")
        val firstName = column<String>("first_name")
        val lastName = column<LastName>("last_name") {
            withParameterTypeConverter(lastNameConverter)
        }
        val birthDate = column<Date>("birth_date")
        val employed = column<Boolean>("employed") {
            withParameterTypeConverter(booleanToStringConverter)
        }
        val occupation = column<String>("occupation")
        val addressId = column<Int>("address_id")
    }
}
