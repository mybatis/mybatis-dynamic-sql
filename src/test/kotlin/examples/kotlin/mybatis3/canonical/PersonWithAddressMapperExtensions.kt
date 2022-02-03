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
package examples.kotlin.mybatis3.canonical

import examples.kotlin.mybatis3.canonical.AddressDynamicSqlSupport.address
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.birthDate
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.employed
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.firstName
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.id
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.lastName
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.occupation
import org.mybatis.dynamic.sql.util.kotlin.SelectCompleter
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.select
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.selectDistinct

private val columnList = listOf(id.`as`("A_ID"), firstName, lastName, birthDate,
    employed, occupation, address.id, address.streetAddress, address.city, address.state, address.addressType
)

fun PersonWithAddressMapper.selectOne(completer: SelectCompleter): PersonWithAddress? =
    select(columnList) {
        from(person)
        fullJoin(address) {
            on(person.addressId) equalTo address.id
        }
        completer()
    }.run(this::selectOne)

fun PersonWithAddressMapper.select(completer: SelectCompleter): List<PersonWithAddress> =
    select(columnList) {
        from(person, "p")
        fullJoin(address) {
            on(person.addressId) equalTo address.id
        }
        completer()
    }.run(this::selectMany)

fun PersonWithAddressMapper.selectDistinct(completer: SelectCompleter): List<PersonWithAddress> =
    selectDistinct(columnList) {
        from(person)
        fullJoin(address) {
            on(person.addressId) equalTo address.id
        }
        completer()
    }.run(this::selectMany)

fun PersonWithAddressMapper.selectByPrimaryKey(id_: Int) =
    selectOne {
        where { id isEqualTo id_ }
    }
