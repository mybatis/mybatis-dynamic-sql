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
package examples.kotlin.general

import examples.kotlin.canonical.*
import examples.kotlin.canonical.AddressDynamicSqlSupport.Address
import examples.kotlin.canonical.PersonDynamicSqlSupport.Person
import examples.kotlin.canonical.PersonDynamicSqlSupport.Person.addressId
import examples.kotlin.canonical.PersonDynamicSqlSupport.Person.birthDate
import examples.kotlin.canonical.PersonDynamicSqlSupport.Person.employed
import examples.kotlin.canonical.PersonDynamicSqlSupport.Person.firstName
import examples.kotlin.canonical.PersonDynamicSqlSupport.Person.id
import examples.kotlin.canonical.PersonDynamicSqlSupport.Person.lastName
import examples.kotlin.canonical.PersonDynamicSqlSupport.Person.occupation
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource
import org.apache.ibatis.jdbc.ScriptRunner
import org.apache.ibatis.mapping.Environment
import org.apache.ibatis.session.Configuration
import org.apache.ibatis.session.SqlSession
import org.apache.ibatis.session.SqlSessionFactoryBuilder
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mybatis.dynamic.sql.SqlBuilder.*
import org.mybatis.dynamic.sql.util.kotlin.*
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.deleteFrom
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.from
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.update
import java.io.InputStreamReader
import java.sql.DriverManager

class GeneralKotlinTest {
    private fun newSession(): SqlSession {
        Class.forName(PersonMapperTest.JDBC_DRIVER)
        val script = javaClass.getResourceAsStream("/examples/kotlin/CreateSimpleDB.sql")
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
    fun testRawDelete1() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val deleteStatement = deleteFrom(Person) {
                where(id, isLessThan(4))
            }

            assertThat(deleteStatement.deleteStatement).isEqualTo("delete from Person" +
                " where id < #{parameters.p1,jdbcType=INTEGER}")

            val rows = mapper.delete(deleteStatement)

            assertThat(rows).isEqualTo(3)
        }
    }

    @Test
    fun testRawDelete2() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val deleteStatement = deleteFrom(Person) {
                where(id, isLessThan(4))
                and(occupation, isNotNull())
            }

            assertThat(deleteStatement.deleteStatement).isEqualTo("delete from Person" +
                " where id < #{parameters.p1,jdbcType=INTEGER} and occupation is not null")

            val rows = mapper.delete(deleteStatement)

            assertThat(rows).isEqualTo(2)
        }
    }

    @Test
    fun testRawDelete3() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val deleteStatement = deleteFrom(Person) {
                where(id, isLessThan(4))
                or(occupation, isNotNull())
            }

            assertThat(deleteStatement.deleteStatement).isEqualTo("delete from Person" +
                " where id < #{parameters.p1,jdbcType=INTEGER} or occupation is not null")

            val rows = mapper.delete(deleteStatement)

            assertThat(rows).isEqualTo(5)
        }
    }

    @Test
    fun testRawDelete4() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val deleteStatement = deleteFrom(Person) {
                where(id, isLessThan(4)) {
                    or(occupation, isNotNull())
                }
                and(employed, isEqualTo(true))
            }

            val expected = "delete from Person" +
                " where (id < #{parameters.p1,jdbcType=INTEGER} or occupation is not null)" +
                " and employed =" +
                " #{parameters.p2,jdbcType=VARCHAR,typeHandler=examples.kotlin.canonical.YesNoTypeHandler}"

            assertThat(deleteStatement.deleteStatement).isEqualTo(expected)

            val rows = mapper.delete(deleteStatement)

            assertThat(rows).isEqualTo(4)
        }
    }

    @Test
    fun testRawDelete5() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val deleteStatement = deleteFrom(Person) {
                where(id, isLessThan(4))
                or(occupation, isNotNull()) {
                    and(employed, isEqualTo(true))
                }
            }

            val expected = "delete from Person" +
                " where id < #{parameters.p1,jdbcType=INTEGER} or (occupation is not null" +
                " and employed =" +
                " #{parameters.p2,jdbcType=VARCHAR,typeHandler=examples.kotlin.canonical.YesNoTypeHandler})"

            assertThat(deleteStatement.deleteStatement).isEqualTo(expected)

            val rows = mapper.delete(deleteStatement)

            assertThat(rows).isEqualTo(5)
        }
    }

    @Test
    fun testRawDelete6() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val deleteStatement = deleteFrom(Person) {
                where(id, isLessThan(4))
                and(occupation, isNotNull()) {
                    and(employed, isEqualTo(true))
                }
            }

            val expected = "delete from Person where id < #{parameters.p1,jdbcType=INTEGER}" +
                " and (occupation is not null and" +
                " employed =" +
                " #{parameters.p2,jdbcType=VARCHAR,typeHandler=examples.kotlin.canonical.YesNoTypeHandler})"

            assertThat(deleteStatement.deleteStatement).isEqualTo(expected)

            val rows = mapper.delete(deleteStatement)

            assertThat(rows).isEqualTo(2)
        }
    }

    @Test
    fun testRawSelect() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val selectStatement = select(id.`as`("A_ID"), firstName, lastName, birthDate, employed, occupation,
                addressId).from(Person) {
                where(id, isLessThan(4)) {
                    and(occupation, isNotNull())
                }
                and(occupation, isNotNull())
                orderBy(id)
                limit(3)
            }

            val rows = mapper.selectMany(selectStatement)

            assertThat(rows.size).isEqualTo(2)
            with(rows[0]) {
                assertThat(id).isEqualTo(1)
                assertThat(firstName).isEqualTo("Fred")
                assertThat(lastName?.name).isEqualTo("Flintstone")
                assertThat(birthDate).isNotNull()
                assertThat(employed).isTrue()
                assertThat(occupation).isEqualTo("Brontosaurus Operator")
                assertThat(addressId).isEqualTo(1)
            }
        }
    }

    @Test
    fun testRawSelectWithJoin() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonWithAddressMapper::class.java)

            val selectStatement = select(id.`as`("A_ID"), firstName, lastName, birthDate, employed, occupation,
                Address.id, Address.streetAddress, Address.city, Address.state)
                .from(Person) {
                    join(Address) {
                        on(addressId, equalTo(Address.id))
                    }
                    where(id, isLessThan(4))
                    orderBy(id)
                    limit(3)
                }

            val rows = mapper.selectMany(selectStatement)

            assertThat(rows.size).isEqualTo(3)
            with(rows[0]) {
                assertThat(id).isEqualTo(1)
                assertThat(firstName).isEqualTo("Fred")
                assertThat(lastName?.name).isEqualTo("Flintstone")
                assertThat(birthDate).isNotNull()
                assertThat(employed).isTrue()
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

            val selectStatement = select(id.`as`("A_ID"), firstName, lastName, birthDate, employed, occupation,
                Address.id, Address.streetAddress, Address.city, Address.state)
                .from(Person) {
                    join(Address) {
                        on(addressId, equalTo(Address.id))
                    }
                    where(id, isLessThan(5))
                    and(id, isLessThan(4)) {
                        and(id, isLessThan(3)) {
                            and(id, isLessThan(2))
                        }
                    }
                }

            val rows = mapper.selectMany(selectStatement)

            assertThat(rows.size).isEqualTo(1)
            with(rows[0]) {
                assertThat(id).isEqualTo(1)
                assertThat(firstName).isEqualTo("Fred")
                assertThat(lastName?.name).isEqualTo("Flintstone")
                assertThat(birthDate).isNotNull()
                assertThat(employed).isTrue()
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

            val selectStatement = select(id.`as`("A_ID"), firstName, lastName, birthDate, employed, occupation,
                Address.id, Address.streetAddress, Address.city, Address.state).from(Person) {
                join(Address) {
                    on(addressId, equalTo(Address.id))
                }
                where(id, isEqualTo(5))
                or(id, isEqualTo(4)) {
                    or(id, isEqualTo(3)) {
                        or(id, isEqualTo(2))
                    }
                }
                orderBy(id)
                limit(3)
            }

            val rows = mapper.selectMany(selectStatement)

            assertThat(rows.size).isEqualTo(3)
            with(rows[2]) {
                assertThat(id).isEqualTo(4)
                assertThat(firstName).isEqualTo("Barney")
                assertThat(lastName?.name).isEqualTo("Rubble")
                assertThat(birthDate).isNotNull()
                assertThat(employed).isTrue()
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

            val selectStatement = select(id.`as`("A_ID"), firstName, lastName, birthDate, employed, occupation,
                addressId).from(Person) {
                where(id, isLessThan(5))
                and(id, isLessThan(4)) {
                    and(id, isLessThan(3)) {
                        and(id, isLessThan(2))
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
                " order by id limit #{parameters._limit}"

            assertThat(selectStatement.selectStatement).isEqualTo(expected)

            val rows = mapper.selectMany(selectStatement)

            assertThat(rows.size).isEqualTo(1)
            with(rows[0]) {
                assertThat(id).isEqualTo(1)
                assertThat(firstName).isEqualTo("Fred")
                assertThat(lastName?.name).isEqualTo("Flintstone")
                assertThat(birthDate).isNotNull()
                assertThat(employed).isTrue()
                assertThat(occupation).isEqualTo("Brontosaurus Operator")
                assertThat(addressId).isEqualTo(1)
            }
        }
    }

    @Test
    fun testRawSelectWithComplexWhere2() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val selectStatement = select(id.`as`("A_ID"), firstName, lastName, birthDate, employed, occupation,
                addressId).from(Person) {
                where(id, isEqualTo(5))
                or(id, isEqualTo(4)) {
                    or(id, isEqualTo(3)) {
                        or(id, isEqualTo(2))
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
                " order by id limit #{parameters._limit}"

            assertThat(selectStatement.selectStatement).isEqualTo(expected)

            val rows = mapper.selectMany(selectStatement)

            assertThat(rows.size).isEqualTo(3)
            with(rows[2]) {
                assertThat(id).isEqualTo(4)
                assertThat(firstName).isEqualTo("Barney")
                assertThat(lastName?.name).isEqualTo("Rubble")
                assertThat(birthDate).isNotNull()
                assertThat(employed).isTrue()
                assertThat(occupation).isEqualTo("Brontosaurus Operator")
                assertThat(addressId).isEqualTo(2)
            }
        }
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

            assertThat(rows.size).isEqualTo(3)
            with(rows[0]) {
                assertThat(id).isEqualTo(3)
                assertThat(firstName).isEqualTo("Pebbles")
                assertThat(lastName?.name).isEqualTo("Flintstone")
                assertThat(birthDate).isNotNull()
                assertThat(employed).isFalse()
                assertThat(occupation).isNull()
                assertThat(addressId).isEqualTo(1)
            }
        }
    }

    @Test
    fun testRawUpdate1() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val updateStatement = update(Person) {
                set(firstName).equalTo("Sam")
                where(firstName, isEqualTo("Fred"))
            }

            assertThat(updateStatement.updateStatement).isEqualTo("update Person" +
                " set first_name = #{parameters.p1,jdbcType=VARCHAR}" +
                " where first_name = #{parameters.p2,jdbcType=VARCHAR}")

            val rows = mapper.update(updateStatement)

            assertThat(rows).isEqualTo(1)
        }
    }

    @Test
    fun testRawUpdate2() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val updateStatement = update(Person) {
                set(firstName).equalTo("Sam")
                where(firstName, isEqualTo("Fred")) {
                    or(id, isGreaterThan(3))
                }
            }

            assertThat(updateStatement.updateStatement).isEqualTo("update Person" +
                " set first_name = #{parameters.p1,jdbcType=VARCHAR}" +
                " where (first_name = #{parameters.p2,jdbcType=VARCHAR} or id > #{parameters.p3,jdbcType=INTEGER})")

            val rows = mapper.update(updateStatement)

            assertThat(rows).isEqualTo(4)
        }
    }

    @Test
    fun testRawUpdate3() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val updateStatement = update(Person) {
                set(firstName).equalTo("Sam")
                where(firstName, isEqualTo("Fred"))
                or(id, isEqualTo(5)) {
                    or(id, isEqualTo(6))
                }
            }

            assertThat(updateStatement.updateStatement).isEqualTo("update Person" +
                " set first_name = #{parameters.p1,jdbcType=VARCHAR}" +
                " where first_name = #{parameters.p2,jdbcType=VARCHAR}" +
                " or (id = #{parameters.p3,jdbcType=INTEGER} or id = #{parameters.p4,jdbcType=INTEGER})")

            val rows = mapper.update(updateStatement)

            assertThat(rows).isEqualTo(3)
        }
    }

    @Test
    fun testRawUpdate4() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val updateStatement = update(Person) {
                set(firstName).equalTo("Sam")
                where(firstName, isEqualTo("Fred"))
                and(id, isEqualTo(1)) {
                    or(id, isEqualTo(6))
                }
            }

            assertThat(updateStatement.updateStatement).isEqualTo("update Person" +
                " set first_name = #{parameters.p1,jdbcType=VARCHAR}" +
                " where first_name = #{parameters.p2,jdbcType=VARCHAR}" +
                " and (id = #{parameters.p3,jdbcType=INTEGER} or id = #{parameters.p4,jdbcType=INTEGER})")

            val rows = mapper.update(updateStatement)

            assertThat(rows).isEqualTo(1)
        }
    }

    @Test
    fun testRawUpdate5() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val updateStatement = update(Person) {
                set(firstName).equalTo("Sam")
                where(firstName, isEqualTo("Fred"))
                or(id, isEqualTo(3))
            }

            assertThat(updateStatement.updateStatement).isEqualTo("update Person" +
                " set first_name = #{parameters.p1,jdbcType=VARCHAR}" +
                " where first_name = #{parameters.p2,jdbcType=VARCHAR}" +
                " or id = #{parameters.p3,jdbcType=INTEGER}")

            val rows = mapper.update(updateStatement)

            assertThat(rows).isEqualTo(2)
        }
    }
}
