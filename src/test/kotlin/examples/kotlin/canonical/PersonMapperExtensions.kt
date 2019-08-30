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

import examples.kotlin.canonical.PersonDynamicSqlSupport.Person
import examples.kotlin.canonical.PersonDynamicSqlSupport.Person.addressId
import examples.kotlin.canonical.PersonDynamicSqlSupport.Person.birthDate
import examples.kotlin.canonical.PersonDynamicSqlSupport.Person.employed
import examples.kotlin.canonical.PersonDynamicSqlSupport.Person.firstName
import examples.kotlin.canonical.PersonDynamicSqlSupport.Person.id
import examples.kotlin.canonical.PersonDynamicSqlSupport.Person.lastName
import examples.kotlin.canonical.PersonDynamicSqlSupport.Person.occupation
import org.mybatis.dynamic.sql.SqlBuilder.isEqualTo
import org.mybatis.dynamic.sql.update.UpdateDSL
import org.mybatis.dynamic.sql.update.UpdateModel
import org.mybatis.dynamic.sql.util.kotlin.CountCompleter
import org.mybatis.dynamic.sql.util.kotlin.DeleteCompleter
import org.mybatis.dynamic.sql.util.kotlin.SelectCompleter
import org.mybatis.dynamic.sql.util.kotlin.UpdateCompleter
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.*

fun PersonMapper.count(completer: CountCompleter) =
    count(this::count, Person, completer)

fun PersonMapper.delete(completer: DeleteCompleter) =
    deleteFrom(this::delete, Person, completer)

fun PersonMapper.deleteByPrimaryKey(id_: Int) =
    delete {
        where(id, isEqualTo(id_))
    }

fun PersonMapper.insert(record: PersonRecord) =
    insert(this::insert, record, Person) {
        map(id).toProperty("id")
        map(firstName).toProperty("firstName")
        map(lastName).toProperty("lastName")
        map(birthDate).toProperty("birthDate")
        map(employed).toProperty("employed")
        map(occupation).toProperty("occupation")
        map(addressId).toProperty("addressId")
    }

fun PersonMapper.insertMultiple(vararg records: PersonRecord) =
    insertMultiple(records.toList())

fun PersonMapper.insertMultiple(records: Collection<PersonRecord>) =
    insertMultiple(this::insertMultiple, records, Person) {
        map(id).toProperty("id")
        map(firstName).toProperty("firstName")
        map(lastName).toProperty("lastName")
        map(birthDate).toProperty("birthDate")
        map(employed).toProperty("employed")
        map(occupation).toProperty("occupation")
        map(addressId).toProperty("addressId")
    }

fun PersonMapper.insertSelective(record: PersonRecord) =
    insert(this::insert, record, Person) {
        map(id).toPropertyWhenPresent("id", record::id)
        map(firstName).toPropertyWhenPresent("firstName", record::firstName)
        map(lastName).toPropertyWhenPresent("lastName", record::lastName)
        map(birthDate).toPropertyWhenPresent("birthDate", record::birthDate)
        map(employed).toPropertyWhenPresent("employed", record::employed)
        map(occupation).toPropertyWhenPresent("occupation", record::occupation)
        map(addressId).toPropertyWhenPresent("addressId", record::addressId)
    }

private val columnList = listOf(id.`as`("A_ID"), firstName, lastName, birthDate, employed, occupation, addressId)

fun PersonMapper.selectOne(completer: SelectCompleter) =
    selectOne(this::selectOne, columnList, Person, completer)

fun PersonMapper.select(completer: SelectCompleter) =
    selectList(this::selectMany, columnList, Person, completer)

fun PersonMapper.selectDistinct(completer: SelectCompleter) =
    selectDistinct(this::selectMany, columnList, Person, completer)

fun PersonMapper.selectByPrimaryKey(id_: Int) =
    selectOne {
        where(id, isEqualTo(id_))
    }

fun PersonMapper.update(completer: UpdateCompleter) =
    update(this::update, Person, completer)

fun UpdateDSL<UpdateModel>.updateAllColumns(record: PersonRecord) =
    apply {
        set(id).equalTo(record::id)
        set(firstName).equalTo(record::firstName)
        set(lastName).equalTo(record::lastName)
        set(birthDate).equalTo(record::birthDate)
        set(employed).equalTo(record::employed)
        set(occupation).equalTo(record::occupation)
        set(addressId).equalTo(record::addressId)
    }

fun UpdateDSL<UpdateModel>.updateSelectiveColumns(record: PersonRecord) =
    apply {
        set(id).equalToWhenPresent(record::id)
        set(firstName).equalToWhenPresent(record::firstName)
        set(lastName).equalToWhenPresent(record::lastName)
        set(birthDate).equalToWhenPresent(record::birthDate)
        set(employed).equalToWhenPresent(record::employed)
        set(occupation).equalToWhenPresent(record::occupation)
        set(addressId).equalToWhenPresent(record::addressId)
    }

fun PersonMapper.updateByPrimaryKey(record: PersonRecord) =
    update {
        set(firstName).equalTo(record::firstName)
        set(lastName).equalTo(record::lastName)
        set(birthDate).equalTo(record::birthDate)
        set(employed).equalTo(record::employed)
        set(occupation).equalTo(record::occupation)
        set(addressId).equalTo(record::addressId)
        where(id, isEqualTo(record::id))
    }

fun PersonMapper.updateByPrimaryKeySelective(record: PersonRecord) =
    update {
        set(firstName).equalToWhenPresent(record::firstName)
        set(lastName).equalToWhenPresent(record::lastName)
        set(birthDate).equalToWhenPresent(record::birthDate)
        set(employed).equalToWhenPresent(record::employed)
        set(occupation).equalToWhenPresent(record::occupation)
        set(addressId).equalToWhenPresent(record::addressId)
        where(id, isEqualTo(record::id))
    }
