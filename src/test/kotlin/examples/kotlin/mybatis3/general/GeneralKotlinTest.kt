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

import examples.kotlin.mybatis3.TestUtils
import examples.kotlin.mybatis3.canonical.AddressDynamicSqlSupport.address
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.addressId
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.birthDate
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.employed
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.firstName
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.id
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.lastName
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.occupation
import examples.kotlin.mybatis3.canonical.PersonMapper
import examples.kotlin.mybatis3.canonical.PersonWithAddressMapper
import examples.kotlin.mybatis3.canonical.YesNoTypeHandler
import examples.kotlin.mybatis3.canonical.select
import org.apache.ibatis.session.SqlSessionFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mybatis.dynamic.sql.util.Messages
import org.mybatis.dynamic.sql.util.kotlin.KInvalidSQLException
import org.mybatis.dynamic.sql.util.kotlin.elements.`as`
import org.mybatis.dynamic.sql.util.kotlin.elements.count
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.count
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countDistinct
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.deleteFrom
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.select
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.selectDistinct
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.update

@Suppress("LargeClass")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GeneralKotlinTest {
    private lateinit var sqlSessionFactory: SqlSessionFactory

    @BeforeAll
    fun setup() {
        sqlSessionFactory = TestUtils.buildSqlSessionFactory {
            withInitializationScript("/examples/kotlin/mybatis3/CreateSimpleDB.sql")
            withTypeHandler(YesNoTypeHandler::class)
            withMapper(PersonMapper::class)
            withMapper(PersonWithAddressMapper::class)
        }
    }

    @Test
    fun testRawCount() {
        sqlSessionFactory.openSession().use { session ->
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
        sqlSessionFactory.openSession().use { session ->
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
        sqlSessionFactory.openSession().use { session ->
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
        sqlSessionFactory.openSession().use { session ->
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
        sqlSessionFactory.openSession().use { session ->
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
        sqlSessionFactory.openSession().use { session ->
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
        sqlSessionFactory.openSession().use { session ->
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
        sqlSessionFactory.openSession().use { session ->
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
        sqlSessionFactory.openSession().use { session ->
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
        sqlSessionFactory.openSession().use { session ->
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
    fun testRawSelectDistinct() {
        sqlSessionFactory.openSession().use { session ->
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
        sqlSessionFactory.openSession().use { session ->
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
        sqlSessionFactory.openSession().use { session ->
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
        sqlSessionFactory.openSession().use { session ->
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
        sqlSessionFactory.openSession().use { session ->
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
        sqlSessionFactory.openSession().use { session ->
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
        sqlSessionFactory.openSession().use { session ->
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
        sqlSessionFactory.openSession().use { session ->
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
        sqlSessionFactory.openSession().use { session ->
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
        sqlSessionFactory.openSession().use { session ->
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
        sqlSessionFactory.openSession().use { session ->
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
        sqlSessionFactory.openSession().use { session ->
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
        sqlSessionFactory.openSession().use { session ->
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
        sqlSessionFactory.openSession().use { session ->
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
