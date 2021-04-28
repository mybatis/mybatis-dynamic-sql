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
import org.mybatis.dynamic.sql.util.kotlin.elements.column

object AddressDynamicSqlSupport {
    val address = Address()
    val id = address.id
    val streetAddress = address.streetAddress
    val city = address.city
    val state = address.state

    class Address : SqlTable("Address") {
        val id = column<Int>(name = "address_id")
        val streetAddress = column<String>(name = "street_address")
        val city = column<String>(name = "city")
        val state = column<String>(name = "state")
    }
}
