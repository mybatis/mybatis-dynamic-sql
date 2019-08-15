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

import examples.kotlin.*
import examples.kotlin.AddressDynamicSqlSupport.Address
import examples.kotlin.PersonDynamicSqlSupport.Person
import examples.kotlin.PersonDynamicSqlSupport.Person.addressId
import examples.kotlin.PersonDynamicSqlSupport.Person.birthDate
import examples.kotlin.PersonDynamicSqlSupport.Person.employed
import examples.kotlin.PersonDynamicSqlSupport.Person.firstName
import examples.kotlin.PersonDynamicSqlSupport.Person.id
import examples.kotlin.PersonDynamicSqlSupport.Person.lastName
import examples.kotlin.PersonDynamicSqlSupport.Person.occupation
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
import org.mybatis.dynamic.sql.delete.and
import org.mybatis.dynamic.sql.delete.or
import org.mybatis.dynamic.sql.delete.where
import org.mybatis.dynamic.sql.util.mybatis3.kotlin.allRows
import org.mybatis.dynamic.sql.util.mybatis3.kotlin.deleteFrom
import org.mybatis.dynamic.sql.util.mybatis3.kotlin.from
import org.mybatis.dynamic.sql.util.mybatis3.kotlin.on
import java.io.InputStreamReader
import java.sql.DriverManager

class GeneralKotlinTest {
    private fun newSession(): SqlSession {
        Class.forName(KotlinTest.JDBC_DRIVER)
        val script = javaClass.getResourceAsStream("/examples/kotlin/CreateSimpleDB.sql")
        DriverManager.getConnection(KotlinTest.JDBC_URL, "sa", "").use { connection ->
            val sr = ScriptRunner(connection)
            sr.setLogWriter(null)
            sr.runScript(InputStreamReader(script))
        }

        val ds = UnpooledDataSource(KotlinTest.JDBC_DRIVER, KotlinTest.JDBC_URL, "sa", "")
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

            assertThat(deleteStatement.deleteStatement).isEqualTo("delete from Person where id < #{parameters.p1,jdbcType=INTEGER}")

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

            assertThat(deleteStatement.deleteStatement).isEqualTo("delete from Person where id < #{parameters.p1,jdbcType=INTEGER} and occupation is not null")

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

            assertThat(deleteStatement.deleteStatement).isEqualTo("delete from Person where id < #{parameters.p1,jdbcType=INTEGER} or occupation is not null")

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

            val expected = "delete from Person where (id < #{parameters.p1,jdbcType=INTEGER} or occupation is not null)" +
                    " and employed = #{parameters.p2,jdbcType=VARCHAR,typeHandler=examples.kotlin.YesNoTypeHandler}"

            assertThat(deleteStatement.deleteStatement).isEqualTo(expected)

            val rows = mapper.delete(deleteStatement)

            assertThat(rows).isEqualTo(4)
        }
    }

    @Test
    fun testRawSelect() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val selectStatement = select(id.`as`("A_ID"), firstName, lastName, birthDate, employed, occupation, addressId)
                    .from(Person) {
                        where(id, isLessThan(4), and(occupation, isNotNull())).and(occupation, isNotNull())
                        orderBy(id)
                        limit(3)
                    }

            val rows = mapper.selectMany(selectStatement)

            assertThat(rows.size).isEqualTo(2)
            assertThat(rows[0].id).isEqualTo(1)
            assertThat(rows[0].firstName).isEqualTo("Fred")
            assertThat(rows[0].lastName).isEqualTo("Flintstone")
            assertThat(rows[0].birthDate).isNotNull()
            assertThat(rows[0].employed).isTrue()
            assertThat(rows[0].occupation).isEqualTo("Brontosaurus Operator")
            assertThat(rows[0].addressId).isEqualTo(1)
        }
    }

    @Test
    fun testRawSelectWithJoin() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)


            val selectStatement = select(id.`as`("A_ID"), firstName, lastName, birthDate, employed, occupation, addressId)
                    .from(Person).join(Address).on(addressId, equalTo(Address.id)) {
                        where(id, isLessThan(4))
                        orderBy(id)
                        limit(3)
                    }

            val rows = mapper.selectMany(selectStatement)

            assertThat(rows.size).isEqualTo(3)
            assertThat(rows[0].id).isEqualTo(1)
            assertThat(rows[0].firstName).isEqualTo("Fred")
            assertThat(rows[0].lastName).isEqualTo("Flintstone")
            assertThat(rows[0].birthDate).isNotNull()
            assertThat(rows[0].employed).isTrue()
            assertThat(rows[0].occupation).isEqualTo("Brontosaurus Operator")
            assertThat(rows[0].addressId).isEqualTo(1)
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
            assertThat(rows[0].id).isEqualTo(3)
            assertThat(rows[0].firstName).isEqualTo("Pebbles")
            assertThat(rows[0].lastName).isEqualTo("Flintstone")
            assertThat(rows[0].birthDate).isNotNull()
            assertThat(rows[0].employed).isFalse()
            assertThat(rows[0].occupation).isNull()
            assertThat(rows[0].addressId).isEqualTo(1)
        }
    }
}
