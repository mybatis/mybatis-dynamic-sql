/*
 *    Copyright 2016-2022 the original author or authors.
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

import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.addressId
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.birthDate
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.employed
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.firstName
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.id
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.lastName
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.occupation
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
import org.mybatis.dynamic.sql.BasicColumn
import org.mybatis.dynamic.sql.util.kotlin.CountCompleter
import org.mybatis.dynamic.sql.util.kotlin.DeleteCompleter
import org.mybatis.dynamic.sql.util.kotlin.GeneralInsertCompleter
import org.mybatis.dynamic.sql.util.kotlin.InsertSelectCompleter
import org.mybatis.dynamic.sql.util.kotlin.KotlinUpdateBuilder
import org.mybatis.dynamic.sql.util.kotlin.SelectCompleter
import org.mybatis.dynamic.sql.util.kotlin.UpdateCompleter
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.count
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countDistinct
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.deleteFrom
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.insert
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.insertBatch
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.insertInto
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.insertMultiple
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.insertSelect
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.selectDistinct
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.selectList
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.selectOne
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.update

fun PersonMapper.count(column: BasicColumn, completer: CountCompleter) =
    count(this::count, column, person, completer)

fun PersonMapper.countDistinct(column: BasicColumn, completer: CountCompleter) =
    countDistinct(this::count, column, person, completer)

fun PersonMapper.count(completer: CountCompleter) =
    countFrom(this::count, person, completer)

fun PersonMapper.delete(completer: DeleteCompleter) =
    deleteFrom(this::delete, person, completer)

fun PersonMapper.deleteByPrimaryKey(id_: Int) =
    delete {
        where { id isEqualTo id_ }
    }

fun PersonMapper.insert(record: PersonRecord) =
    insert(this::insert, record, person) {
        map(id).toProperty("id")
        map(firstName).toProperty("firstName")
        map(lastName).toProperty("lastName")
        map(birthDate).toProperty("birthDate")
        map(employed).toProperty("employed")
        map(occupation).toProperty("occupation")
        map(addressId).toProperty("addressId")
    }

fun PersonMapper.generalInsert(completer: GeneralInsertCompleter) =
    insertInto(this::generalInsert, person, completer)

fun PersonMapper.insertSelect(completer: InsertSelectCompleter) =
    insertSelect(this::insertSelect, person, completer)

fun PersonMapper.insertBatch(vararg records: PersonRecord): List<Int> =
    insertBatch(records.toList())

fun PersonMapper.insertBatch(records: Collection<PersonRecord>): List<Int> =
    insertBatch(this::insert, records, person) {
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
    insertMultiple(this::insertMultiple, records, person) {
        map(id).toProperty("id")
        map(firstName).toProperty("firstName")
        map(lastName).toProperty("lastName")
        map(birthDate).toProperty("birthDate")
        map(employed).toProperty("employed")
        map(occupation).toProperty("occupation")
        map(addressId).toProperty("addressId")
    }

fun PersonMapper.insertSelective(record: PersonRecord) =
    insert(this::insert, record, person) {
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
    selectOne(this::selectOne, columnList, person, completer)

fun PersonMapper.select(completer: SelectCompleter) =
    selectList(this::selectMany, columnList, person, completer)

fun PersonMapper.selectDistinct(completer: SelectCompleter) =
    selectDistinct(this::selectMany, columnList, person, completer)

fun PersonMapper.selectByPrimaryKey(id_: Int) =
    selectOne {
        where { id isEqualTo id_ }
    }

fun PersonMapper.update(completer: UpdateCompleter) =
    update(this::update, person, completer)

fun KotlinUpdateBuilder.updateAllColumns(record: PersonRecord) =
    apply {
        set(id) equalToOrNull record::id
        set(firstName) equalToOrNull record::firstName
        set(lastName) equalToOrNull record::lastName
        set(birthDate) equalToOrNull record::birthDate
        set(employed) equalToOrNull record::employed
        set(occupation) equalToOrNull record::occupation
        set(addressId) equalToOrNull record::addressId
    }

fun KotlinUpdateBuilder.updateSelectiveColumns(record: PersonRecord) =
    apply {
        set(id) equalToWhenPresent record::id
        set(firstName) equalToWhenPresent record::firstName
        set(lastName) equalToWhenPresent record::lastName
        set(birthDate) equalToWhenPresent record::birthDate
        set(employed) equalToWhenPresent record::employed
        set(occupation) equalToWhenPresent record::occupation
        set(addressId) equalToWhenPresent record::addressId
    }

fun PersonMapper.updateByPrimaryKey(record: PersonRecord) =
    update {
        set(firstName) equalToOrNull record::firstName
        set(lastName) equalToOrNull record::lastName
        set(birthDate) equalToOrNull record::birthDate
        set(employed) equalToOrNull record::employed
        set(occupation) equalToOrNull record::occupation
        set(addressId) equalToOrNull record::addressId
        where { id isEqualTo record.id!! }
    }

fun PersonMapper.updateByPrimaryKeySelective(record: PersonRecord) =
    update {
        set(firstName) equalToWhenPresent record::firstName
        set(lastName) equalToWhenPresent record::lastName
        set(birthDate) equalToWhenPresent record::birthDate
        set(employed) equalToWhenPresent record::employed
        set(occupation) equalToWhenPresent record::occupation
        set(addressId) equalToWhenPresent record::addressId
        where { id isEqualTo record.id!! }
    }
