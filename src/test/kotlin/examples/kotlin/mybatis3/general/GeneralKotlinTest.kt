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
package examples.kotlin.mybatis3.general

import examples.kotlin.mybatis3.canonical.AddressDynamicSqlSupport.address
import examples.kotlin.mybatis3.canonical.LastName
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.addressId
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.birthDate
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.employed
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.firstName
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.id
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.lastName
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.occupation
import examples.kotlin.mybatis3.canonical.PersonMapper
import examples.kotlin.mybatis3.canonical.PersonMapperTest
import examples.kotlin.mybatis3.canonical.PersonRecord
import examples.kotlin.mybatis3.canonical.PersonWithAddressMapper
import examples.kotlin.mybatis3.canonical.YesNoTypeHandler
import examples.kotlin.mybatis3.canonical.select
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource
import org.apache.ibatis.jdbc.ScriptRunner
import org.apache.ibatis.mapping.Environment
import org.apache.ibatis.session.Configuration
import org.apache.ibatis.session.SqlSession
import org.apache.ibatis.session.SqlSessionFactoryBuilder
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import org.mybatis.dynamic.sql.util.Messages
import org.mybatis.dynamic.sql.util.kotlin.KInvalidSQLException
import org.mybatis.dynamic.sql.util.kotlin.elements.`as`
import org.mybatis.dynamic.sql.util.kotlin.elements.count
import org.mybatis.dynamic.sql.util.kotlin.elements.insert
import org.mybatis.dynamic.sql.util.kotlin.elements.insertMultiple
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.count
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countDistinct
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.deleteFrom
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.into
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.select
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.selectDistinct
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.update
import java.io.InputStreamReader
import java.sql.DriverManager
import java.util.Date

@Suppress("LargeClass")
class GeneralKotlinTest {
    private fun newSession(): SqlSession {
        Class.forName(PersonMapperTest.JDBC_DRIVER)
        val script = javaClass.getResourceAsStream("/examples/kotlin/mybatis3/CreateSimpleDB.sql")
        DriverManager.getConnection(PersonMapperTest.JDBC_URL, "sa", "").use { connection ->
            val sr = ScriptRunner(connection)
            sr.setLogWriter(null)
            sr.runScript(InputStreamReader(script))
        }

        val ds = UnpooledDataSource(PersonMapperTest.JDBC_DRIVER, PersonMapperTest.JDBC_URL, "sa", "")
        val environment = Environment("test", JdbcTransactionFactory(), ds)
        val config = Configuration(environment)
        config.typeHandlerRegistry.register(YesNoTypeHandler::class.java)
        config.addMapper(PersonMapper::class.java)
        config.addMapper(PersonWithAddressMapper::class.java)
        return SqlSessionFactoryBuilder().build(config).openSession()
    }

    @Test
    fun testRawCount() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val countStatement = countFrom(person) {
                where { id isLessThan 4 }
            }

            assertThat(countStatement.selectStatement).isEqualTo(
                "select count(*) from Person where id < #{parameters.p1,jdbcType=INTEGER}"
            )

            val rows = mapper.count(countStatement)

            assertThat(rows).isEqualTo(3)
        }
    }

    @Test
    fun testRawCountAllRows() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val countStatement = countFrom(person) {
                allRows()
            }

            assertThat(countStatement.selectStatement).isEqualTo("select count(*) from Person")

            val rows = mapper.count(countStatement)

            assertThat(rows).isEqualTo(6)
        }
    }

    @Test
    fun testRawCountColumn() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val countStatement = count(lastName) {
                from(person)
                where { id isLessThan 4 }
            }

            assertThat(countStatement.selectStatement).isEqualTo(
                "select count(last_name) from Person where id < #{parameters.p1,jdbcType=INTEGER}"
            )

            val rows = mapper.count(countStatement)

            assertThat(rows).isEqualTo(3)
        }
    }

    @Test
    fun testRawCountDistinctColumn() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val countStatement = countDistinct(lastName) {
                from(person)
                where { id isLessThan 4 }
            }

            assertThat(countStatement.selectStatement).isEqualTo(
                "select count(distinct last_name) from Person where id < #{parameters.p1,jdbcType=INTEGER}"
            )

            val rows = mapper.count(countStatement)

            assertThat(rows).isEqualTo(1)
        }
    }

    @Test
    fun testRawDelete1() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val deleteStatement = deleteFrom(person) {
                where { id isLessThan 4 }
            }

            assertThat(deleteStatement.deleteStatement).isEqualTo(
                "delete from Person where id < #{parameters.p1,jdbcType=INTEGER}"
            )

            val rows = mapper.delete(deleteStatement)

            assertThat(rows).isEqualTo(3)
        }
    }

    @Test
    fun testRawDelete2() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val deleteStatement = deleteFrom(person) {
                where { id isLessThan 4 }
                and { occupation.isNotNull() }
            }

            assertThat(deleteStatement.deleteStatement).isEqualTo(
                "delete from Person where id < #{parameters.p1,jdbcType=INTEGER} and occupation is not null"
            )

            val rows = mapper.delete(deleteStatement)

            assertThat(rows).isEqualTo(2)
        }
    }

    @Test
    fun testRawDelete3() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val deleteStatement = deleteFrom(person) {
                where { id isLessThan 4 }
                or { occupation.isNotNull() }
            }

            assertThat(deleteStatement.deleteStatement).isEqualTo(
                "delete from Person where id < #{parameters.p1,jdbcType=INTEGER} or occupation is not null"
            )

            val rows = mapper.delete(deleteStatement)

            assertThat(rows).isEqualTo(5)
        }
    }

    @Test
    fun testRawDelete4() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val deleteStatement = deleteFrom(person) {
                where {
                    id isLessThan 4
                    or { occupation.isNotNull() }
                }
                and { employed isEqualTo true }
            }

            val expected = "delete from Person " +
                "where (id < #{parameters.p1,jdbcType=INTEGER} or occupation is not null) " +
                "and employed = " +
                "#{parameters.p2,jdbcType=VARCHAR,typeHandler=examples.kotlin.mybatis3.canonical.YesNoTypeHandler}"

            assertThat(deleteStatement.deleteStatement).isEqualTo(expected)

            val rows = mapper.delete(deleteStatement)

            assertThat(rows).isEqualTo(4)
        }
    }

    @Test
    fun testRawDelete5() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val deleteStatement = deleteFrom(person) {
                where { id isLessThan 4 }
                or {
                    occupation.isNotNull()
                    and { employed isEqualTo true }
                }
            }

            val expected = "delete from Person" +
                " where id < #{parameters.p1,jdbcType=INTEGER} or (occupation is not null" +
                " and employed =" +
                " #{parameters.p2,jdbcType=VARCHAR,typeHandler=examples.kotlin.mybatis3.canonical.YesNoTypeHandler})"

            assertThat(deleteStatement.deleteStatement).isEqualTo(expected)

            val rows = mapper.delete(deleteStatement)

            assertThat(rows).isEqualTo(5)
        }
    }

    @Test
    fun testRawDelete6() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val deleteStatement = deleteFrom(person) {
                where { id isLessThan 4 }
                and {
                    occupation.isNotNull()
                    and { employed isEqualTo true }
                }
            }

            val expected = "delete from Person where id < #{parameters.p1,jdbcType=INTEGER}" +
                " and (occupation is not null and" +
                " employed =" +
                " #{parameters.p2,jdbcType=VARCHAR,typeHandler=examples.kotlin.mybatis3.canonical.YesNoTypeHandler})"

            assertThat(deleteStatement.deleteStatement).isEqualTo(expected)

            val rows = mapper.delete(deleteStatement)

            assertThat(rows).isEqualTo(2)
        }
    }

    @Test
    fun testDeprecatedInsert() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val record = PersonRecord(100, "Joe", LastName("Jones"), Date(), true, "Developer", 1)

            val insertStatement = insert(record).into(person) {
                map(id).toProperty("id")
                map(firstName).toProperty("firstName")
                map(lastName).toProperty("lastName")
                map(birthDate).toProperty("birthDate")
                map(employed).toProperty("employed")
                map(occupation).toProperty("occupation")
                map(addressId).toProperty("addressId")
            }

            val expected =
                "insert into Person (id, first_name, last_name, birth_date, employed, occupation, address_id) " +
                    "values " +
                    "(#{record.id,jdbcType=INTEGER}, #{record.firstName,jdbcType=VARCHAR}, " +
                    "#{record.lastName,jdbcType=VARCHAR," +
                    "typeHandler=examples.kotlin.mybatis3.canonical.LastNameTypeHandler}, " +
                    "#{record.birthDate,jdbcType=DATE}, " +
                    "#{record.employed,jdbcType=VARCHAR," +
                    "typeHandler=examples.kotlin.mybatis3.canonical.YesNoTypeHandler}, " +
                    "#{record.occupation,jdbcType=VARCHAR}, #{record.addressId,jdbcType=INTEGER})"

            assertThat(insertStatement.insertStatement).isEqualTo(expected)

            val rows = mapper.insert(insertStatement)
            assertThat(rows).isEqualTo(1)
        }
    }

    @Test
    fun testDeprecatedInsertMultiple() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val record1 = PersonRecord(100, "Joe", LastName("Jones"), Date(), true, "Developer", 1)
            val record2 = PersonRecord(101, "Sarah", LastName("Smith"), Date(), true, "Architect", 2)

            val insertStatement =
                insertMultiple(listOf(record1, record2)).into(person) {
                    map(id).toProperty("id")
                    map(firstName).toProperty("firstName")
                    map(lastName).toProperty("lastName")
                    map(birthDate).toProperty("birthDate")
                    map(employed).toProperty("employed")
                    map(occupation).toProperty("occupation")
                    map(addressId).toProperty("addressId")
                }

            val expected =
                "insert into Person (id, first_name, last_name, birth_date, employed, occupation, address_id)" +
                    " values" +
                    " (#{records[0].id,jdbcType=INTEGER}," +
                    " #{records[0].firstName,jdbcType=VARCHAR}," +
                    " #{records[0].lastName,jdbcType=VARCHAR," +
                    "typeHandler=examples.kotlin.mybatis3.canonical.LastNameTypeHandler}," +
                    " #{records[0].birthDate,jdbcType=DATE}," +
                    " #{records[0].employed,jdbcType=VARCHAR," +
                    "typeHandler=examples.kotlin.mybatis3.canonical.YesNoTypeHandler}," +
                    " #{records[0].occupation,jdbcType=VARCHAR}," +
                    " #{records[0].addressId,jdbcType=INTEGER})" +
                    ", (#{records[1].id,jdbcType=INTEGER}," +
                    " #{records[1].firstName,jdbcType=VARCHAR}," +
                    " #{records[1].lastName,jdbcType=VARCHAR," +
                    "typeHandler=examples.kotlin.mybatis3.canonical.LastNameTypeHandler}," +
                    " #{records[1].birthDate,jdbcType=DATE}," +
                    " #{records[1].employed,jdbcType=VARCHAR," +
                    "typeHandler=examples.kotlin.mybatis3.canonical.YesNoTypeHandler}," +
                    " #{records[1].occupation,jdbcType=VARCHAR}," +
                    " #{records[1].addressId,jdbcType=INTEGER})"

            assertThat(insertStatement.insertStatement).isEqualTo(expected)

            val rows = mapper.insertMultiple(insertStatement)

            assertThat(rows).isEqualTo(2)
        }
    }

    @Test
    fun testRawSelectDistinct() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val selectStatement = selectDistinct(
                id `as` "A_ID", firstName, lastName, birthDate, employed, occupation,
                addressId
            ) {
                from(person)
                where {
                    id isLessThan 4
                    and { occupation.isNotNull() }
                }
                and { occupation.isNotNull() }
                orderBy(id)
                limit(3)
            }

            val rows = mapper.selectMany(selectStatement)

            assertThat(rows).hasSize(2)
            with(rows[0]) {
                assertThat(id).isEqualTo(1)
                assertThat(firstName).isEqualTo("Fred")
                assertThat(lastName?.name).isEqualTo("Flintstone")
                assertThat(birthDate).isNotNull
                assertThat(employed).isTrue
                assertThat(occupation).isEqualTo("Brontosaurus Operator")
                assertThat(addressId).isEqualTo(1)
            }
        }
    }

    @Test
    fun testRawSelect() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val selectStatement = select(
                id `as` "A_ID", firstName, lastName, birthDate, employed, occupation,
                addressId
            ) {
                from(person)
                where {
                    id  isLessThan 4
                    and { occupation.isNotNull() }
                }
                and { occupation.isNotNull() }
                orderBy(id)
                limit(3)
            }

            val rows = mapper.selectMany(selectStatement)

            assertThat(rows).hasSize(2)
            with(rows[0]) {
                assertThat(id).isEqualTo(1)
                assertThat(firstName).isEqualTo("Fred")
                assertThat(lastName?.name).isEqualTo("Flintstone")
                assertThat(birthDate).isNotNull
                assertThat(employed).isTrue
                assertThat(occupation).isEqualTo("Brontosaurus Operator")
                assertThat(addressId).isEqualTo(1)
            }
        }
    }

    @Test
    fun testRawSelectWithJoin() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonWithAddressMapper::class.java)

            val selectStatement = select(
                id `as` "A_ID", firstName, lastName, birthDate, employed, occupation,
                address.id, address.streetAddress, address.city, address.state
            ) {
                from(person)
                join(address) {
                    on(addressId) equalTo address.id
                }
                where { id isLessThan 4 }
                orderBy(id)
                limit(3)
            }

            val rows = mapper.selectMany(selectStatement)

            assertThat(rows).hasSize(3)
            with(rows[0]) {
                assertThat(id).isEqualTo(1)
                assertThat(firstName).isEqualTo("Fred")
                assertThat(lastName?.name).isEqualTo("Flintstone")
                assertThat(birthDate).isNotNull
                assertThat(employed).isTrue
                assertThat(occupation).isEqualTo("Brontosaurus Operator")
                assertThat(address?.id).isEqualTo(1)
                assertThat(address?.streetAddress).isEqualTo("123 Main Street")
                assertThat(address?.city).isEqualTo("Bedrock")
                assertThat(address?.state).isEqualTo("IN")
            }
        }
    }

    @Test
    fun testRawSelectWithJoinAndComplexWhere1() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonWithAddressMapper::class.java)

            val selectStatement = select(
                id `as` "A_ID", firstName, lastName, birthDate, employed, occupation,
                address.id, address.streetAddress, address.city, address.state
            ) {
                from(person)
                join(address) {
                    on(addressId) equalTo address.id
                }
                where { id isLessThan 5 }
                and {
                    id isLessThan 4
                    and {
                        id isLessThan 3
                        and { id isLessThan 2 }
                    }
                }
            }

            val rows = mapper.selectMany(selectStatement)

            assertThat(rows).hasSize(1)
            with(rows[0]) {
                assertThat(id).isEqualTo(1)
                assertThat(firstName).isEqualTo("Fred")
                assertThat(lastName?.name).isEqualTo("Flintstone")
                assertThat(birthDate).isNotNull
                assertThat(employed).isTrue
                assertThat(occupation).isEqualTo("Brontosaurus Operator")
                assertThat(address?.id).isEqualTo(1)
                assertThat(address?.streetAddress).isEqualTo("123 Main Street")
                assertThat(address?.city).isEqualTo("Bedrock")
                assertThat(address?.state).isEqualTo("IN")
            }
        }
    }

    @Test
    fun testRawSelectWithJoinAndComplexWhere2() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonWithAddressMapper::class.java)

            val selectStatement = select(
                id `as` "A_ID", firstName, lastName, birthDate, employed, occupation,
                address.id, address.streetAddress, address.city, address.state
            ) {
                from(person)
                join(address) {
                    on(addressId) equalTo address.id
                }
                where { id isEqualTo 5 }
                or {
                    id  isEqualTo 4
                    or {
                        id isEqualTo 3
                        or { id isEqualTo 2 }
                    }
                }
                orderBy(id)
                limit(3)
            }

            val rows = mapper.selectMany(selectStatement)

            assertThat(rows).hasSize(3)
            with(rows[2]) {
                assertThat(id).isEqualTo(4)
                assertThat(firstName).isEqualTo("Barney")
                assertThat(lastName?.name).isEqualTo("Rubble")
                assertThat(birthDate).isNotNull
                assertThat(employed).isTrue
                assertThat(occupation).isEqualTo("Brontosaurus Operator")
                assertThat(address?.id).isEqualTo(2)
                assertThat(address?.streetAddress).isEqualTo("456 Main Street")
                assertThat(address?.city).isEqualTo("Bedrock")
                assertThat(address?.state).isEqualTo("IN")
            }
        }
    }

    @Test
    fun testRawSelectWithComplexWhere1() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val selectStatement = select(
                id `as` "A_ID", firstName, lastName, birthDate, employed, occupation,
                addressId
            ) {
                from(person)
                where { id isLessThan 5 }
                and {
                    id isLessThan 4
                    and {
                        id isLessThan 3
                        and { id isLessThan 2 }
                    }
                }
                orderBy(id)
                limit(3)
            }

            val expected = "select id as A_ID, first_name, last_name, birth_date, employed, occupation, address_id" +
                " from Person" +
                " where id < #{parameters.p1,jdbcType=INTEGER}" +
                " and (id < #{parameters.p2,jdbcType=INTEGER}" +
                " and (id < #{parameters.p3,jdbcType=INTEGER} and id < #{parameters.p4,jdbcType=INTEGER}))" +
                " order by id limit #{parameters.p5}"

            assertThat(selectStatement.selectStatement).isEqualTo(expected)

            val rows = mapper.selectMany(selectStatement)

            assertThat(rows).hasSize(1)
            with(rows[0]) {
                assertThat(id).isEqualTo(1)
                assertThat(firstName).isEqualTo("Fred")
                assertThat(lastName?.name).isEqualTo("Flintstone")
                assertThat(birthDate).isNotNull
                assertThat(employed).isTrue
                assertThat(occupation).isEqualTo("Brontosaurus Operator")
                assertThat(addressId).isEqualTo(1)
            }
        }
    }

    @Test
    fun testRawSelectWithComplexWhere2() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val selectStatement = select(
                id `as` "A_ID", firstName, lastName, birthDate, employed, occupation,
                addressId
            ) {
                from(person)
                where { id isEqualTo 5 }
                or {
                    id isEqualTo 4
                    or {
                        id isEqualTo 3
                        or { id isEqualTo 2 }
                    }
                }
                orderBy(id)
                limit(3)
            }

            val expected = "select id as A_ID, first_name, last_name, birth_date, employed, occupation, address_id" +
                " from Person" +
                " where id = #{parameters.p1,jdbcType=INTEGER}" +
                " or (id = #{parameters.p2,jdbcType=INTEGER}" +
                " or (id = #{parameters.p3,jdbcType=INTEGER} or id = #{parameters.p4,jdbcType=INTEGER}))" +
                " order by id limit #{parameters.p5}"

            assertThat(selectStatement.selectStatement).isEqualTo(expected)

            val rows = mapper.selectMany(selectStatement)

            assertThat(rows).hasSize(3)
            with(rows[2]) {
                assertThat(id).isEqualTo(4)
                assertThat(firstName).isEqualTo("Barney")
                assertThat(lastName?.name).isEqualTo("Rubble")
                assertThat(birthDate).isNotNull
                assertThat(employed).isTrue
                assertThat(occupation).isEqualTo("Brontosaurus Operator")
                assertThat(addressId).isEqualTo(2)
            }
        }
    }

    @Test
    fun testRawSelectWithoutFrom() {
        assertThatExceptionOfType(KInvalidSQLException::class.java).isThrownBy {
            select(id `as` "A_ID", firstName, lastName, birthDate, employed, occupation, addressId) {
                where { id isEqualTo 5 }
                or {
                    id isEqualTo 4
                    or {
                        id isEqualTo 3
                        or { id isEqualTo 2 }
                    }
                }
                orderBy(id)
                limit(3)
            }
        }.withMessage(Messages.getString("ERROR.27")) //$NON-NLS-1$
    }

    @Test
    fun testRawCountWithoutFrom() {
        assertThatExceptionOfType(KInvalidSQLException::class.java).isThrownBy {
            count(id) {
                where { id isEqualTo 5 }
                or {
                    id isEqualTo 4
                    or {
                        id isEqualTo 3
                        or { id isEqualTo 2 }
                    }
                }
            }
        }.withMessage(Messages.getString("ERROR.24")) //$NON-NLS-1$
    }

    @Test
    fun testSelect() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val rows = mapper.select {
                allRows()
                orderBy(id)
                limit(3)
                offset(2)
            }

            assertThat(rows).hasSize(3)
            with(rows[0]) {
                assertThat(id).isEqualTo(3)
                assertThat(firstName).isEqualTo("Pebbles")
                assertThat(lastName?.name).isEqualTo("Flintstone")
                assertThat(birthDate).isNotNull
                assertThat(employed).isFalse
                assertThat(occupation).isNull()
                assertThat(addressId).isEqualTo(1)
            }
        }
    }

    @Test
    fun testSelectWithFetchFirst() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val rows = mapper.select {
                allRows()
                orderBy(id)
                offset(2)
                fetchFirst(3)
            }

            assertThat(rows).hasSize(3)
            with(rows[0]) {
                assertThat(id).isEqualTo(3)
                assertThat(firstName).isEqualTo("Pebbles")
                assertThat(lastName?.name).isEqualTo("Flintstone")
                assertThat(birthDate).isNotNull
                assertThat(employed).isFalse
                assertThat(occupation).isNull()
                assertThat(addressId).isEqualTo(1)
            }
        }
    }

    @Test
    fun testRawSelectWithGroupBy() {

        val ss = select(lastName, count()) {
            from(person)
            where { firstName isNotEqualTo "Bamm Bamm" }
            groupBy(lastName)
            orderBy(lastName)
        }

        val expected = "select last_name, count(*) from Person " +
            "where first_name <> #{parameters.p1,jdbcType=VARCHAR} " +
            "group by last_name " +
            "order by last_name"

        assertThat(ss.selectStatement).isEqualTo(expected)
    }

    @Test
    fun testRawUpdate1() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val updateStatement = update(person) {
                set(firstName) equalTo  "Sam"
                where { firstName isEqualTo "Fred" }
            }

            assertThat(updateStatement.updateStatement).isEqualTo(
                "update Person" +
                    " set first_name = #{parameters.p1,jdbcType=VARCHAR}" +
                    " where first_name = #{parameters.p2,jdbcType=VARCHAR}"
            )

            val rows = mapper.update(updateStatement)

            assertThat(rows).isEqualTo(1)
        }
    }

    @Test
    fun testRawUpdate2() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val updateStatement = update(person) {
                set(firstName) equalTo "Sam"
                where {
                    firstName isEqualTo "Fred"
                    or { id isGreaterThan 3 }
                }
            }

            assertThat(updateStatement.updateStatement).isEqualTo(
                "update Person" +
                    " set first_name = #{parameters.p1,jdbcType=VARCHAR}" +
                    " where (first_name = #{parameters.p2,jdbcType=VARCHAR} or id > #{parameters.p3,jdbcType=INTEGER})"
            )

            val rows = mapper.update(updateStatement)

            assertThat(rows).isEqualTo(4)
        }
    }

    @Test
    fun testRawUpdate3() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val updateStatement = update(person) {
                set(firstName) equalTo "Sam"
                where { firstName isEqualTo "Fred" }
                or {
                    id isEqualTo 5
                    or { id isEqualTo 6 }
                }
            }

            assertThat(updateStatement.updateStatement).isEqualTo(
                "update Person" +
                    " set first_name = #{parameters.p1,jdbcType=VARCHAR}" +
                    " where first_name = #{parameters.p2,jdbcType=VARCHAR}" +
                    " or (id = #{parameters.p3,jdbcType=INTEGER} or id = #{parameters.p4,jdbcType=INTEGER})"
            )

            val rows = mapper.update(updateStatement)

            assertThat(rows).isEqualTo(3)
        }
    }

    @Test
    fun testRawUpdate4() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val updateStatement = update(person) {
                set(firstName) equalTo "Sam"
                where { firstName isEqualTo "Fred" }
                and {
                    id isEqualTo 1
                    or { id isEqualTo 6 }
                }
            }

            assertThat(updateStatement.updateStatement).isEqualTo(
                "update Person" +
                    " set first_name = #{parameters.p1,jdbcType=VARCHAR}" +
                    " where first_name = #{parameters.p2,jdbcType=VARCHAR}" +
                    " and (id = #{parameters.p3,jdbcType=INTEGER} or id = #{parameters.p4,jdbcType=INTEGER})"
            )

            val rows = mapper.update(updateStatement)

            assertThat(rows).isEqualTo(1)
        }
    }

    @Test
    fun testRawUpdate5() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val updateStatement = update(person) {
                set(firstName) equalTo  "Sam"
                where { firstName isEqualTo "Fred" }
                or { id isEqualTo 3 }
            }

            assertThat(updateStatement.updateStatement).isEqualTo(
                "update Person" +
                    " set first_name = #{parameters.p1,jdbcType=VARCHAR}" +
                    " where first_name = #{parameters.p2,jdbcType=VARCHAR}" +
                    " or id = #{parameters.p3,jdbcType=INTEGER}"
            )

            val rows = mapper.update(updateStatement)

            assertThat(rows).isEqualTo(2)
        }
    }
}
