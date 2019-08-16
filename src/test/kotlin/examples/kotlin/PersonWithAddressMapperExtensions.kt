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
package examples.kotlin

import examples.kotlin.AddressDynamicSqlSupport.Address
import examples.kotlin.AddressDynamicSqlSupport.Address.city
import examples.kotlin.AddressDynamicSqlSupport.Address.state
import examples.kotlin.AddressDynamicSqlSupport.Address.streetAddress
import examples.kotlin.PersonDynamicSqlSupport.Person
import examples.kotlin.PersonDynamicSqlSupport.Person.addressId
import examples.kotlin.PersonDynamicSqlSupport.Person.birthDate
import examples.kotlin.PersonDynamicSqlSupport.Person.employed
import examples.kotlin.PersonDynamicSqlSupport.Person.firstName
import examples.kotlin.PersonDynamicSqlSupport.Person.lastName
import examples.kotlin.PersonDynamicSqlSupport.Person.occupation
import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.SqlBuilder
import org.mybatis.dynamic.sql.SqlBuilder.equalTo
import org.mybatis.dynamic.sql.render.RenderingStrategy
import org.mybatis.dynamic.sql.select.CompletableQuery
import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.util.Buildable

private fun selectList(): Array<BasicColumn> =
        arrayOf(Person.id, firstName, lastName, birthDate, employed, occupation,
                Address.id.`as`("address_id"), streetAddress, city, state)

fun PersonWithAddressMapper.select(helper: CompletableQuery<SelectModel>.() -> Buildable<SelectModel>): List<PersonWithAddress> {
    val dsl = SqlBuilder.select(*selectList()).from(Person).join(Address).on(addressId, equalTo(Address.id))
    helper(dsl)
    return selectMany(dsl.build().render(RenderingStrategy.MYBATIS3))
}
