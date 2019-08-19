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
import org.mybatis.dynamic.sql.delete.DeleteDSL
import org.mybatis.dynamic.sql.delete.DeleteModel
import org.mybatis.dynamic.sql.select.CompletableQuery
import org.mybatis.dynamic.sql.select.SelectModel
import org.mybatis.dynamic.sql.update.UpdateDSL
import org.mybatis.dynamic.sql.update.UpdateModel
import org.mybatis.dynamic.sql.util.Buildable
import org.mybatis.dynamic.sql.util.mybatis3.MyBatis3Utils
import org.mybatis.dynamic.sql.util.mybatis3.kotlin.insert
import org.mybatis.dynamic.sql.util.mybatis3.kotlin.insertMultiple

fun PersonMapper.count(completer: CompletableQuery<SelectModel>.() -> Buildable<SelectModel>) =
        MyBatis3Utils.count(this::count, Person, completer)

fun PersonMapper.delete(completer: DeleteDSL<DeleteModel>.() -> Buildable<DeleteModel>) =
        MyBatis3Utils.deleteFrom(this::delete, Person, completer)

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

private val selectList = arrayOf(id.`as`("A_ID"), firstName, lastName, birthDate, employed, occupation, addressId)

fun PersonMapper.selectOne(completer: CompletableQuery<SelectModel>.() -> Buildable<SelectModel>): PersonRecord? =
        MyBatis3Utils.selectOne(this::selectOne, selectList, Person, completer)

fun PersonMapper.select(completer: CompletableQuery<SelectModel>.() -> Buildable<SelectModel>): List<PersonRecord> =
        MyBatis3Utils.selectList(this::selectMany, selectList, Person, completer)

fun PersonMapper.selectDistinct(completer: CompletableQuery<SelectModel>.() -> Buildable<SelectModel>): List<PersonRecord> =
        MyBatis3Utils.selectDistinct(this::selectMany, selectList, Person, completer)

fun PersonMapper.selectByPrimaryKey(id_: Int) =
        selectOne {
            where(id, isEqualTo(id_))
        }

fun PersonMapper.update(completer: UpdateDSL<UpdateModel>.() -> Buildable<UpdateModel>) =
        MyBatis3Utils.update(this::update, Person, completer)

fun UpdateDSL<UpdateModel>.setAll(record: PersonRecord) =
        apply {
            set(id).equalTo(record::id)
            set(firstName).equalTo(record::firstName)
            set(lastName).equalTo(record::lastName)
            set(birthDate).equalTo(record::birthDate)
            set(employed).equalTo(record::employed)
            set(occupation).equalTo(record::occupation)
            set(addressId).equalTo(record::addressId)
        }

fun UpdateDSL<UpdateModel>.setSelective(record: PersonRecord) =
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
