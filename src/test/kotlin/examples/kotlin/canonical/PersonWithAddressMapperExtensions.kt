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
package examples.kotlin.canonical

import examples.kotlin.canonical.AddressDynamicSqlSupport.Address
import examples.kotlin.canonical.PersonDynamicSqlSupport.Person
import examples.kotlin.canonical.PersonDynamicSqlSupport.Person.birthDate
import examples.kotlin.canonical.PersonDynamicSqlSupport.Person.employed
import examples.kotlin.canonical.PersonDynamicSqlSupport.Person.firstName
import examples.kotlin.canonical.PersonDynamicSqlSupport.Person.id
import examples.kotlin.canonical.PersonDynamicSqlSupport.Person.lastName
import examples.kotlin.canonical.PersonDynamicSqlSupport.Person.occupation
import org.mybatis.dynamic.sql.SqlBuilder.*
import org.mybatis.dynamic.sql.util.kotlin.SelectCompleter
import org.mybatis.dynamic.sql.util.kotlin.from
import org.mybatis.dynamic.sql.util.kotlin.fullJoin
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.selectList
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.selectOne

private val columnList = listOf(id.`as`("A_ID"), firstName, lastName, birthDate, employed, occupation, Address.id,
    Address.streetAddress, Address.city, Address.state)

fun PersonWithAddressMapper.selectOne(completer: SelectCompleter): PersonWithAddress? {
    val start = select(columnList).from(Person) {
        fullJoin(Address) {
            on(Person.addressId, equalTo(Address.id))
        }
    }

    return selectOne(this::selectOne, start, completer)
}

fun PersonWithAddressMapper.select(completer: SelectCompleter): List<PersonWithAddress> {
    val start = select(columnList).from(Person, "p") {
        fullJoin(Address) {
            on(Person.addressId, equalTo(Address.id))
        }
    }
    return selectList(this::selectMany, start, completer)
}

fun PersonWithAddressMapper.selectByPrimaryKey(id_: Int) =
    selectOne {
        where(id, isEqualTo(id_))
    }
