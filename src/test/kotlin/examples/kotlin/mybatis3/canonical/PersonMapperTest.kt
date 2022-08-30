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

import examples.kotlin.mybatis3.canonical.AddressDynamicSqlSupport.address
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.addressId
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.birthDate
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.employed
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.firstName
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.id
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.lastName
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.occupation
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource
import org.apache.ibatis.jdbc.ScriptRunner
import org.apache.ibatis.mapping.Environment
import org.apache.ibatis.session.Configuration
import org.apache.ibatis.session.ExecutorType
import org.apache.ibatis.session.SqlSession
import org.apache.ibatis.session.SqlSessionFactoryBuilder
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mybatis.dynamic.sql.util.kotlin.elements.add
import org.mybatis.dynamic.sql.util.kotlin.elements.constant
import org.mybatis.dynamic.sql.util.kotlin.elements.isIn
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.insertInto
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.insertSelect
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.select
import java.io.InputStreamReader
import java.sql.DriverManager
import java.util.*

class PersonMapperTest {
    private fun newSession(executorType: ExecutorType = ExecutorType.REUSE): SqlSession {
        Class.forName(JDBC_DRIVER)
        val script = javaClass.getResourceAsStream("/examples/kotlin/mybatis3/CreateSimpleDB.sql")
        DriverManager.getConnection(JDBC_URL, "sa", "").use { connection ->
            val sr = ScriptRunner(connection)
            sr.setLogWriter(null)
            sr.runScript(InputStreamReader(script!!))
        }

        val ds = UnpooledDataSource(JDBC_DRIVER, JDBC_URL, "sa", "")
        val environment = Environment("test", JdbcTransactionFactory(), ds)
        val config = Configuration(environment)
        config.typeHandlerRegistry.register(YesNoTypeHandler::class.java)
        config.addMapper(PersonMapper::class.java)
        config.addMapper(PersonWithAddressMapper::class.java)
        config.addMapper(AddressMapper::class.java)
        return SqlSessionFactoryBuilder().build(config).openSession(executorType)
    }

    @Test
    fun testSelect() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val rows = mapper.select {
                where { id isEqualTo 1 }
                or { occupation.isNull() }
            }

            assertThat(rows).hasSize(3)
        }
    }

    @Test
    fun testSelectAll() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val rows = mapper.select { allRows() }

            assertThat(rows).hasSize(6)
            assertThat(rows[0].id).isEqualTo(1)
            assertThat(rows[5].id).isEqualTo(6)
        }
    }

    @Test
    fun testSelectAllOrdered() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val rows = mapper.select {
                allRows()
                orderBy(lastName.descending(), firstName.descending())
            }

            assertThat(rows).hasSize(6)
            assertThat(rows[0].id).isEqualTo(5)
            assertThat(rows[5].id).isEqualTo(1)
        }
    }

    @Test
    fun testSelectDistinct() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val rows = mapper.selectDistinct {
                where { id isGreaterThan 1 }
                or { occupation.isNull() }
            }

            assertThat(rows).hasSize(5)
        }
    }

    @Test
    fun testSelectWithTypeHandler() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val rows = mapper.select {
                where { employed isEqualTo false }
                orderBy(id)
            }

            assertThat(rows).hasSize(2)
            assertThat(rows[0].id).isEqualTo(3)
            assertThat(rows[1].id).isEqualTo(6)
        }
    }

    @Test
    fun testSelectByPrimaryKeyWithMissingRecord() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val record = mapper.selectByPrimaryKey(300)
            assertThat(record).isNull()
        }
    }

    @Test
    fun testFirstNameIn() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val rows = mapper.select {
                where { firstName.isIn("Fred", "Barney") }
            }

            assertThat(rows).hasSize(2)
            assertThat(rows[0].lastName?.name).isEqualTo("Flintstone")
            assertThat(rows[1].lastName?.name).isEqualTo("Rubble")
        }
    }

    @Test
    fun testFirstNameNotIn() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val rows = mapper.select {
                where { not { firstName (isIn("Fred", "Barney")) }}
            }

            assertThat(rows).hasSize(4)
            assertThat(rows[0].firstName).isEqualTo("Wilma")
            assertThat(rows[1].firstName).isEqualTo("Pebbles")
        }
    }

    @Test
    fun testDelete() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val rows = mapper.delete {
                where { occupation.isNull() }
            }
            assertThat(rows).isEqualTo(2)
        }
    }

    @Test
    fun testDeleteAll() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val rows = mapper.delete { allRows() }

            assertThat(rows).isEqualTo(6)
        }
    }

    @Test
    fun testDeleteByPrimaryKey() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val rows = mapper.deleteByPrimaryKey(2)

            assertThat(rows).isEqualTo(1)
        }
    }

    @Test
    fun testInsert() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val record = PersonRecord(100, "Joe", LastName("Jones"), Date(), true, "Developer", 1)

            val rows = mapper.insert(record)
            assertThat(rows).isEqualTo(1)
        }
    }

    @Test
    fun testGeneralInsert() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val rows = mapper.generalInsert {
                set(id) toValue 100
                set(firstName) toValue "Joe"
                set(lastName) toValue LastName("Jones")
                set(employed) toValue true
                set(occupation) toValue "Developer"
                set(addressId) toValue 1
                set(birthDate) toValue Date()
            }

            assertThat(rows).isEqualTo(1)
        }
    }

    @Test
    fun testInsertSelect() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val rows = mapper.insertSelect {
                columns(id, firstName, lastName, employed, occupation, addressId, birthDate)
                select(add(id, constant<Int>("100")), firstName, lastName, employed, occupation, addressId, birthDate) {
                    from(person)
                    orderBy(id)
                }
            }

            assertThat(rows).isEqualTo(6)
        }
    }

    @Test
    fun testDeprecatedInsertSelect() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val insertStatement = insertSelect(person) {
                columns(id, firstName, lastName, employed, occupation, addressId, birthDate)
                select(add(id, constant<Int>("100")), firstName, lastName, employed, occupation, addressId, birthDate) {
                    from(person)
                    orderBy(id)
                }
            }

            val expected = "insert into Person " +
                    "(id, first_name, last_name, employed, occupation, address_id, birth_date) " +
                    "select (id + 100), first_name, last_name, employed, occupation, address_id, birth_date " +
                    "from Person order by id"

            assertThat(insertStatement.insertStatement).isEqualTo(expected)

            val rows = mapper.insertSelect(insertStatement)

            assertThat(rows).isEqualTo(6)
        }
    }

    @Test
    fun testInsertBatch() {
        newSession(ExecutorType.BATCH).use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val record1 = PersonRecord(100, "Joe", LastName("Jones"), Date(), true, "Developer", 1)
            val record2 = PersonRecord(101, "Sarah", LastName("Smith"), Date(), true, "Architect", 2)

            mapper.insertBatch(record1, record2)

            val batchResults = mapper.flush()
            assertThat(batchResults).hasSize(1)
            assertThat(batchResults.flatMap { it.updateCounts.asList() }.sum()).isEqualTo(2)
        }
    }

    @Test
    fun testInsertMultiple() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val record1 = PersonRecord(100, "Joe", LastName("Jones"), Date(), true, "Developer", 1)
            val record2 = PersonRecord(101, "Sarah", LastName("Smith"), Date(), true, "Architect", 2)

            val rows = mapper.insertMultiple(record1, record2)
            assertThat(rows).isEqualTo(2)
        }
    }

    @Test
    fun testInsertSelective() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val record = PersonRecord(100, "Joe", LastName("Jones"), Date(), false, null, 1)

            val rows = mapper.insertSelective(record)
            assertThat(rows).isEqualTo(1)
        }
    }

    @Test
    fun testUpdateByPrimaryKey() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val record = PersonRecord(100, "Joe", LastName("Jones"), Date(), true, "Developer", 1)

            var rows = mapper.insert(record)
            assertThat(rows).isEqualTo(1)

            rows = mapper.update {
                set(occupation) equalTo  "Programmer"
                where { id isEqualTo 100 }
            }
            assertThat(rows).isEqualTo(1)

            val newRecord = mapper.selectByPrimaryKey(100)
            assertThat(newRecord?.occupation).isEqualTo("Programmer")
        }
    }

    @Test
    fun testUpdateByPrimaryKeySelective() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val record = PersonRecord(100, "Joe", LastName("Jones"), Date(), true, "Developer", 1)

            var rows = mapper.insert(record)
            assertThat(rows).isEqualTo(1)

            rows = mapper.update {
                set(occupation) equalTo "Programmer"
                where { id isEqualTo 100 }
            }
            assertThat(rows).isEqualTo(1)

            val newRecord = mapper.selectByPrimaryKey(100)
            assertThat(newRecord?.occupation).isEqualTo("Programmer")
            assertThat(newRecord?.firstName).isEqualTo("Joe")
        }
    }

    @Test
    fun testUpdate() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val record = PersonRecord(100, "Joe", LastName("Jones"), Date(), true, "Developer", 1)

            var rows = mapper.insert(record)
            assertThat(rows).isEqualTo(1)

            rows = mapper.update {
                set(occupation) equalTo "Programmer"
                where { id isEqualTo 100 }
                and { firstName isEqualTo "Joe" }
            }

            assertThat(rows).isEqualTo(1)

            val newRecord = mapper.selectByPrimaryKey(100)
            assertThat(newRecord?.occupation).isEqualTo("Programmer")
        }
    }

    @Test
    fun testUpdateOneField() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val record = PersonRecord(100, "Joe", LastName("Jones"), Date(), true, "Developer", 1)

            var rows = mapper.insert(record)
            assertThat(rows).isEqualTo(1)

            rows = mapper.update {
                set(occupation) equalTo "Programmer"
                where { id isEqualTo 100 }
            }

            assertThat(rows).isEqualTo(1)

            val newRecord = mapper.selectByPrimaryKey(100)
            assertThat(newRecord?.occupation).isEqualTo("Programmer")
        }
    }

    @Test
    fun testUpdateOneFieldInAllRows() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val record = PersonRecord(100, "Joe", LastName("Jones"), Date(), true, "Developer", 1)

            var rows = mapper.insert(record)
            assertThat(rows).isEqualTo(1)

            rows = mapper.update {
                set(occupation) equalTo "Programmer"
            }

            assertThat(rows).isEqualTo(7)

            val newRecord = mapper.selectByPrimaryKey(100)
            assertThat(newRecord?.occupation).isEqualTo("Programmer")
        }
    }

    @Test
    fun testUpdateAll() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val record = PersonRecord(100, "Joe", LastName("Jones"), Date(), true, "Developer", 1)

            var rows = mapper.insert(record)
            assertThat(rows).isEqualTo(1)

            rows = mapper.update {
                set(occupation) equalTo "Programmer"
            }

            assertThat(rows).isEqualTo(7)

            val newRecord = mapper.selectByPrimaryKey(100)
            assertThat(newRecord?.occupation).isEqualTo("Programmer")
        }
    }

    @Test
    fun testUpdateSelective() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val record = PersonRecord(100, "Joe", LastName("Jones"), Date(), true, "Developer", 1)

            var rows = mapper.insert(record)
            assertThat(rows).isEqualTo(1)

            rows = mapper.update {
                set(occupation) equalTo "Programmer"
                where { id isEqualTo 100 }
            }

            assertThat(rows).isEqualTo(1)

            val newRecord = mapper.selectByPrimaryKey(100)
            assertThat(newRecord?.occupation).isEqualTo("Programmer")
        }
    }

    @Test
    fun testCount1() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val rows = mapper.count {
                where {
                    occupation.isNull()
                    and { employed.isFalse() }
                }
            }

            assertThat(rows).isEqualTo(2L)
        }
    }

    @Test
    fun testCount2() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val rows = mapper.count {
                where { employed.isTrue() }
                and { occupation isEqualTo "Brontosaurus Operator" }
            }

            assertThat(rows).isEqualTo(2L)
        }
    }

    @Test
    fun testCount3() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val rows = mapper.count {
                where {
                    id isEqualTo 1
                    or { id isEqualTo 2 }
                }
            }

            assertThat(rows).isEqualTo(2L)
        }
    }

    @Test
    fun testCount4() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val rows = mapper.count {
                where { id isEqualTo 1 }
                or {
                    id isEqualTo 2
                    or { id isEqualTo 3 }
                }
            }

            assertThat(rows).isEqualTo(3L)
        }
    }

    @Test
    fun testCount5() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val rows = mapper.count {
                where { id isLessThan 5 }
                and {
                    id isLessThan 3
                    and { id isEqualTo 1 }
                }
            }

            assertThat(rows).isEqualTo(1L)
        }
    }

    @Test
    fun testCountAll() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val rows = mapper.count { allRows() }

            assertThat(rows).isEqualTo(6L)
        }
    }

    @Test
    fun testCountLastName() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val rows = mapper.count(lastName) { allRows() }

            assertThat(rows).isEqualTo(6L)
        }
    }

    @Test
    fun testCountDistinctLastName() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val rows = mapper.countDistinct(lastName) { allRows() }

            assertThat(rows).isEqualTo(2L)
        }
    }

    @Test
    fun testTypeHandledLike() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val rows = mapper.select {
                where { lastName isLike LastName("Fl%") }
                orderBy(id)
            }

            assertThat(rows).hasSize(3)
            assertThat(rows[0].firstName).isEqualTo("Fred")
        }
    }

    @Test
    fun testTypeHandledNotLike() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val rows = mapper.select {
                where { lastName isNotLike LastName("Fl%") }
                orderBy(id)
            }

            assertThat(rows).hasSize(3)
            assertThat(rows[0].firstName).isEqualTo("Barney")
        }
    }

    @Test
    fun testJoinAllRows() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonWithAddressMapper::class.java)

            val records = mapper.select {
                allRows()
                orderBy(id)
            }

            assertThat(records).hasSize(6)
            with(records[0]) {
                assertThat(id).isEqualTo(1)
                assertThat(employed).isTrue
                assertThat(firstName).isEqualTo("Fred")
                assertThat(lastName).isEqualTo(LastName("Flintstone"))
                assertThat(occupation).isEqualTo("Brontosaurus Operator")
                assertThat(birthDate).isNotNull
                assertThat(address?.id).isEqualTo(1)
                assertThat(address?.streetAddress).isEqualTo("123 Main Street")
                assertThat(address?.city).isEqualTo("Bedrock")
                assertThat(address?.state).isEqualTo("IN")
                assertThat(address?.addressType).isEqualTo(AddressType.HOME)
            }

            with(records[4]) {
                assertThat(address?.addressType).isEqualTo(AddressType.BUSINESS)
            }
        }
    }

    @Test
    fun testJoinOneRow() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonWithAddressMapper::class.java)

            val records = mapper.select {
                where { id isEqualTo 1 }
            }

            assertThat(records).hasSize(1)
            with(records[0]) {
                assertThat(id).isEqualTo(1)
                assertThat(employed).isTrue
                assertThat(firstName).isEqualTo("Fred")
                assertThat(lastName).isEqualTo(LastName("Flintstone"))
                assertThat(occupation).isEqualTo("Brontosaurus Operator")
                assertThat(birthDate).isNotNull
                assertThat(address?.id).isEqualTo(1)
                assertThat(address?.streetAddress).isEqualTo("123 Main Street")
                assertThat(address?.city).isEqualTo("Bedrock")
                assertThat(address?.state).isEqualTo("IN")
                assertThat(address?.addressType).isEqualTo(AddressType.HOME)
            }
        }
    }

    @Test
    fun testJoinDistinct() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonWithAddressMapper::class.java)

            val records = mapper.selectDistinct {
                where { id isEqualTo 1 }
            }

            assertThat(records).hasSize(1)
            with(records[0]) {
                assertThat(id).isEqualTo(1)
                assertThat(employed).isTrue
                assertThat(firstName).isEqualTo("Fred")
                assertThat(lastName).isEqualTo(LastName("Flintstone"))
                assertThat(occupation).isEqualTo("Brontosaurus Operator")
                assertThat(birthDate).isNotNull
                assertThat(address?.id).isEqualTo(1)
                assertThat(address?.streetAddress).isEqualTo("123 Main Street")
                assertThat(address?.city).isEqualTo("Bedrock")
                assertThat(address?.state).isEqualTo("IN")
                assertThat(address?.addressType).isEqualTo(AddressType.HOME)
            }
        }
    }

    @Test
    fun testJoinPrimaryKey() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonWithAddressMapper::class.java)

            val record = mapper.selectByPrimaryKey(1)

            assertThat(record).isNotNull
            with(record!!) {
                assertThat(id).isEqualTo(1)
                assertThat(employed).isTrue
                assertThat(firstName).isEqualTo("Fred")
                assertThat(lastName).isEqualTo(LastName("Flintstone"))
                assertThat(occupation).isEqualTo("Brontosaurus Operator")
                assertThat(birthDate).isNotNull
                assertThat(address?.id).isEqualTo(1)
                assertThat(address?.streetAddress).isEqualTo("123 Main Street")
                assertThat(address?.city).isEqualTo("Bedrock")
                assertThat(address?.state).isEqualTo("IN")
                assertThat(address?.addressType).isEqualTo(AddressType.HOME)
            }
        }
    }

    @Test
    fun testJoinPrimaryKeyInvalidRecord() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonWithAddressMapper::class.java)

            val record = mapper.selectByPrimaryKey(55)

            assertThat(record).isNull()
        }
    }

    @Test
    fun testWithEnumOrdinalTypeHandler() {
        newSession().use { session ->
            val mapper: AddressMapper = session.getMapper(AddressMapper::class.java)

            val insertStatement = insertInto(address) {
                set(address.id) toValue 4
                set(address.streetAddress) toValue "987 Elm Street"
                set(address.city) toValue "Mayberry"
                set(address.state) toValue "NC"
                set(address.addressType) toValue AddressType.HOME
            }

            val rows = mapper.generalInsert(insertStatement)
            assertThat(rows).isEqualTo(1)

            val selectStatement = select(address.addressType) {
                from(address)
                where { address.id isEqualTo 4 }
            }

            val type = mapper.selectOptionalInteger(selectStatement)

            assertThat(type).hasValueSatisfying { assertThat(it).isEqualTo(0) }
        }
    }

    companion object {
        const val JDBC_URL = "jdbc:hsqldb:mem:aname"
        const val JDBC_DRIVER = "org.hsqldb.jdbcDriver"
    }
}
