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
import org.mybatis.dynamic.sql.delete.allRows
import org.mybatis.dynamic.sql.delete.or
import org.mybatis.dynamic.sql.render.RenderingStrategy
import org.mybatis.dynamic.sql.select.SelectDSL
import org.mybatis.dynamic.sql.util.mybatis3.kotlin.allRows
import org.mybatis.dynamic.sql.util.mybatis3.kotlin.allRowsOrderedBy
import java.io.InputStreamReader
import java.sql.DriverManager
import java.util.*

internal class KotlinTest {

    private fun newSession(): SqlSession {
        Class.forName(JDBC_DRIVER)
        val script = javaClass.getResourceAsStream("/examples/kotlin/CreateSimpleDB.sql")
        DriverManager.getConnection(JDBC_URL, "sa", "").use { connection ->
            val sr = ScriptRunner(connection)
            sr.setLogWriter(null)
            sr.runScript(InputStreamReader(script))
        }

        val ds = UnpooledDataSource(JDBC_DRIVER, JDBC_URL, "sa", "")
        val environment = Environment("test", JdbcTransactionFactory(), ds)
        val config = Configuration(environment)
        config.typeHandlerRegistry.register(YesNoTypeHandler::class.java)
        config.addMapper(PersonMapper::class.java)
        config.addMapper(PersonWithAddressMapper::class.java)
        return SqlSessionFactoryBuilder().build(config).openSession()
    }

    @Test
    fun testSelect() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val rows = mapper.select {
                where(id, isEqualTo(1))
                        .or(occupation, isNull())
                orderBy(id)
            }

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
    fun testSelectAllRows() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val rows = mapper.select { allRows() }

            assertThat(rows.size).isEqualTo(6)
        }
    }

    @Test
    fun testSelectAllRowsWithOrder() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val rows = mapper.select { allRowsOrderedBy(firstName, lastName) }

            assertThat(rows.size).isEqualTo(6)
            assertThat(rows[0].firstName).isEqualTo("Bamm Bamm")
        }
    }

    @Test
    fun testSelectAllRowsWithOrderNoColumns() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val rows = mapper.select { allRowsOrderedBy() }

            assertThat(rows.size).isEqualTo(6)
            assertThat(rows[0].firstName).isEqualTo("Fred")
        }
    }

    @Test
    fun testSelectByPrimaryKeyNoRecord() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val row = mapper.selectByPrimaryKey(22)

            assertThat(row?.id).isNull()
            assertThat(row).isNull()
        }
    }

    @Test
    fun testSelectDistinct() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val rows = mapper.selectDistinct {
                where(id, isGreaterThan(1))
                        .or(occupation, isNull())
            }

            assertThat(rows.size).isEqualTo(5)
        }
    }

    @Test
    fun testSelectWithTypeHandler() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val rows = mapper.select {
                where(employed, isEqualTo(false))
                        .orderBy(id)
            }

            assertThat(rows.size).isEqualTo(2)
            assertThat(rows[0].id).isEqualTo(3)
            assertThat(rows[1].id).isEqualTo(6)
        }
    }

    @Test
    fun testFirstNameIn() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val rows = mapper.select { where(firstName, isIn("Fred", "Barney")) }

            assertThat(rows.size).isEqualTo(2)
            assertThat(rows[0].lastName).isEqualTo("Flintstone")
            assertThat(rows[1].lastName).isEqualTo("Rubble")
        }
    }

    @Test
    fun testDelete() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)
            val rows = mapper.delete {
                where(occupation, isNull())
                or(firstName, isEqualTo("Fred"))
            }
            assertThat(rows).isEqualTo(3)
        }
    }

    @Test
    fun testDeleteAllRows() {
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
            val record = PersonRecord(10, "Joe", "Jones", Date(), true, "Developer", 22)

            val rows = mapper.insert(record)
            assertThat(rows).isEqualTo(1)
        }
    }

    @Test
    fun testInsertMultiple() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val rows = mapper.insertMultiple(
                    PersonRecord(10, "Joe", "Jones", Date(), true, "Developer", 22),
                    PersonRecord(11, "Sam", "Smith", Date(), true, "Architect", 23))
            assertThat(rows).isEqualTo(2)
        }
    }

    @Test
    fun testInsertWithNull() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)
            val record = PersonRecord(100, "Joe", "Jones", Date(), false, null, 22)

            val rows = mapper.insert(record)
            assertThat(rows).isEqualTo(1)
        }
    }

    @Test
    fun testUpdateByPrimaryKey() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)
            val record = PersonRecord(100, "Joe", "Jones", Date(), true, "Developer", 22)

            var rows = mapper.insert(record)
            assertThat(rows).isEqualTo(1)

            val updateRecord = record.copy(occupation = "Programmer")
            rows = mapper.updateByPrimaryKey(updateRecord)
            assertThat(rows).isEqualTo(1)

            val newRecord = mapper.selectByPrimaryKey(100)
            assertThat(newRecord?.occupation).isEqualTo("Programmer")
        }
    }

    @Test
    fun testUpdateByPrimaryKeySelective() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)
            val record = PersonRecord(100, "Joe", "Jones", Date(), true, "Developer", 22)

            var rows = mapper.insert(record)
            assertThat(rows).isEqualTo(1)

            val updateRecord = PersonRecord(id = 100, occupation = "Programmer")
            rows = mapper.updateByPrimaryKeySelective(updateRecord)
            assertThat(rows).isEqualTo(1)

            val newRecord = mapper.selectByPrimaryKey(100)
            assertThat(newRecord?.occupation).isEqualTo("Programmer")
            assertThat(newRecord?.firstName).isEqualTo("Joe")

        }
    }

    @Test
    fun testUpdateByPrimaryKeySelectiveWithCopy() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)
            val record = PersonRecord(100, "Joe", "Jones", Date(), true, "Developer", 22)

            var rows = mapper.insert(record)
            assertThat(rows).isEqualTo(1)

            val updateRecord = PersonRecord(record.id, record.firstName, record.lastName, record.birthDate, record.employed, "Programmer", record.addressId)
            rows = mapper.updateByPrimaryKeySelective(updateRecord)
            assertThat(rows).isEqualTo(1)

            val newRecord = mapper.selectByPrimaryKey(100)
            assertThat(newRecord?.occupation).isEqualTo("Programmer")
            assertThat(newRecord?.firstName).isEqualTo("Joe")
        }
    }

    @Test
    fun testUpdateWithNulls() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)
            val record = PersonRecord(100, "Joe", "Jones", Date(), true, "Developer", 22)

            var rows = mapper.insert(record)
            assertThat(rows).isEqualTo(1)

            val updateStatement = update(Person)
                    .set(occupation).equalToNull()
                    .set(employed).equalTo(false)
                    .where(id, isEqualTo(100))
                    .build()
                    .render(RenderingStrategy.MYBATIS3)

            rows = mapper.update(updateStatement)
            assertThat(rows).isEqualTo(1)

            val newRecord = mapper.selectByPrimaryKey(100)
            assertThat(newRecord?.occupation).isNull()
            assertThat(newRecord?.employed).isEqualTo(false)
            assertThat(newRecord?.firstName).isEqualTo("Joe")
        }
    }

    @Test
    fun testUpdate() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)
            val record = PersonRecord(100, "Joe", "Jones", Date(), true, "Developer", 22)

            var rows = mapper.insert(record)
            assertThat(rows).isEqualTo(1)

            val updateRecord = record.copy(occupation = "Programmer")
            rows = mapper.update {
                setAll(updateRecord)
                where(id, isEqualTo(100))
                        .and(firstName, isEqualTo("Joe"))
            }

            assertThat(rows).isEqualTo(1)

            val newRecord = mapper.selectByPrimaryKey(100)
            assertThat(newRecord?.occupation).isEqualTo("Programmer")
        }
    }

    @Test
    fun testUpdateAllRows() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)
            val record = PersonRecord(100, "Joe", "Jones", Date(), true, "Developer", 22)

            var rows = mapper.insert(record)
            assertThat(rows).isEqualTo(1)

            val updateRecord = PersonRecord(occupation = "Programmer")

            rows = mapper.update {
                setSelective(updateRecord)
            }

            assertThat(rows).isEqualTo(7)

            val newRecord = mapper.selectByPrimaryKey(100)
            assertThat(newRecord?.occupation).isEqualTo("Programmer")
        }
    }

    @Test
    fun testCount() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)
            val rows = mapper.count { where(occupation, isNull()) }

            assertThat(rows).isEqualTo(2)
        }
    }

    @Test
    fun testCountAll() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)
            val rows = mapper.count { allRows() }

            assertThat(rows).isEqualTo(6)
        }
    }

    @Test
    fun testJoin() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonWithAddressMapper::class.java)

            val selectStatement = SelectDSL.select(id, firstName, lastName, birthDate, employed, occupation,
                    Address.id.`as`("address_id"), streetAddress, city, state)
                    .from(Person, "p").leftJoin(Address, "a").on(addressId, equalTo(Address.id))
                    .where(id, isEqualTo(1))
                    .build()
                    .render(RenderingStrategy.MYBATIS3)

            val expected = "select p.id, p.first_name, p.last_name, p.birth_date, p.employed, p.occupation," +
                    " a.id as address_id, a.street_address, a.city, a.state" +
                    " from Person p left join Address a on p.address_id = a.id" +
                    " where p.id = #{parameters.p1,jdbcType=INTEGER}"

            assertThat(selectStatement.selectStatement).isEqualTo(expected)
            val rows = mapper.selectMany(selectStatement)

            assertThat(rows.size).isEqualTo(1)
            assertThat(rows[0].id).isEqualTo(1)
            assertThat(rows[0].firstName).isEqualTo("Fred")
            assertThat(rows[0].lastName).isEqualTo("Flintstone")
            assertThat(rows[0].birthDate).isNotNull()
            assertThat(rows[0].employed).isTrue()
            assertThat(rows[0].occupation).isEqualTo("Brontosaurus Operator")
            assertThat(rows[0].address?.id).isEqualTo(1)
            assertThat(rows[0].address?.streetAddress).isEqualTo("123 Main Street")
            assertThat(rows[0].address?.city).isEqualTo("Bedrock")
            assertThat(rows[0].address?.state).isEqualTo("IN")
        }
    }

    @Test
    fun testJoinHelperFunction() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonWithAddressMapper::class.java)

            val rows = mapper.select {
                where(id, isEqualTo(1))
            }

            assertThat(rows.size).isEqualTo(1)
            assertThat(rows[0].id).isEqualTo(1)
            assertThat(rows[0].firstName).isEqualTo("Fred")
            assertThat(rows[0].lastName).isEqualTo("Flintstone")
            assertThat(rows[0].birthDate).isNotNull()
            assertThat(rows[0].employed).isTrue()
            assertThat(rows[0].occupation).isEqualTo("Brontosaurus Operator")
            assertThat(rows[0].address?.id).isEqualTo(1)
            assertThat(rows[0].address?.streetAddress).isEqualTo("123 Main Street")
            assertThat(rows[0].address?.city).isEqualTo("Bedrock")
            assertThat(rows[0].address?.state).isEqualTo("IN")
        }
    }

    companion object {
        const val JDBC_URL = "jdbc:hsqldb:mem:aname"
        const val JDBC_DRIVER = "org.hsqldb.jdbcDriver"
    }
}