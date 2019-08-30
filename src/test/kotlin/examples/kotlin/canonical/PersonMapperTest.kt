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
import org.mybatis.dynamic.sql.util.kotlin.allRows
import org.mybatis.dynamic.sql.util.kotlin.and
import org.mybatis.dynamic.sql.util.kotlin.or
import org.mybatis.dynamic.sql.util.kotlin.where
import java.io.InputStreamReader
import java.sql.DriverManager
import java.util.*

class PersonMapperTest {
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
                or(occupation, isNull())
            }

            assertThat(rows.size).isEqualTo(3)
        }
    }

    @Test
    fun testSelectAll() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val rows = mapper.select { allRows() }

            assertThat(rows.size).isEqualTo(6)
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

            assertThat(rows.size).isEqualTo(6)
            assertThat(rows[0].id).isEqualTo(5)
            assertThat(rows[5].id).isEqualTo(1)
        }
    }

    @Test
    fun testSelectDistinct() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val rows = mapper.selectDistinct {
                where(id, isGreaterThan(1))
                or(occupation, isNull())
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
                orderBy(id)
            }

            assertThat(rows.size).isEqualTo(2)
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
                where(firstName, isIn("Fred", "Barney"))
            }

            assertThat(rows.size).isEqualTo(2)
            assertThat(rows[0].lastName?.name).isEqualTo("Flintstone")
            assertThat(rows[1].lastName?.name).isEqualTo("Rubble")
        }
    }

    @Test
    fun testDelete() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val rows = mapper.delete {
                where(occupation, isNull())
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

            record.occupation = "Programmer"
            rows = mapper.updateByPrimaryKey(record)
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

            val updateRecord = PersonRecord(id = 100, occupation = "Programmer")
            rows = mapper.updateByPrimaryKeySelective(updateRecord)
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

            record.occupation = "Programmer"

            rows = mapper.update {
                updateAllColumns(record)
                where(id, isEqualTo(100))
                and(firstName, isEqualTo("Joe"))
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
                set(occupation).equalTo("Programmer")
                where(id, isEqualTo(100))
            }

            assertThat(rows).isEqualTo(1)

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

            val updateRecord = PersonRecord(occupation = "Programmer")

            rows = mapper.update {
                updateSelectiveColumns(updateRecord)
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

            val updateRecord = PersonRecord(occupation = "Programmer")

            rows = mapper.update {
                updateSelectiveColumns(updateRecord)
                where(id, isEqualTo(100))
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
                where(occupation, isNull()) {
                    and(employed, isFalse())
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
                where(employed, isTrue())
                and(occupation, isEqualTo("Brontosaurus Operator"))
            }

            assertThat(rows).isEqualTo(2L)
        }
    }

    @Test
    fun testCount3() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val rows = mapper.count {
                where(id, isEqualTo(1))
                or(id, isEqualTo(2))
            }

            assertThat(rows).isEqualTo(2L)
        }
    }

    @Test
    fun testCount4() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val rows = mapper.count {
                where(id, isEqualTo(1))
                or(id, isEqualTo(2)) {
                    or(id, isEqualTo(3))
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
                where(id, isLessThan(5))
                and(id, isLessThan(3)) {
                    and(id, isEqualTo(1))
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
    fun testTypeHandledLike() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val rows = mapper.select {
                where(lastName, isLike(LastName("Fl%")))
                orderBy(id)
            }

            assertThat(rows.size).isEqualTo(3)
            assertThat(rows[0].firstName).isEqualTo("Fred")
        }
    }

    @Test
    fun testTypeHandledNotLike() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val rows = mapper.select {
                where(lastName, isNotLike(LastName("Fl%")))
                orderBy(id)
            }

            assertThat(rows.size).isEqualTo(3)
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

            assertThat(records.size).isEqualTo(6L)
            with(records[0]) {
                assertThat(id).isEqualTo(1)
                assertThat(employed).isTrue()
                assertThat(firstName).isEqualTo("Fred")
                assertThat(lastName).isEqualTo(LastName("Flintstone"))
                assertThat(occupation).isEqualTo("Brontosaurus Operator")
                assertThat(birthDate).isNotNull()
                assertThat(address?.id).isEqualTo(1)
                assertThat(address?.streetAddress).isEqualTo("123 Main Street")
                assertThat(address?.city).isEqualTo("Bedrock")
                assertThat(address?.state).isEqualTo("IN")
            }
        }
    }

    @Test
    fun testJoinOneRow() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonWithAddressMapper::class.java)

            val records = mapper.select {
                where(id, isEqualTo(1))
            }

            assertThat(records.size).isEqualTo(1L)
            with(records[0]) {
                assertThat(id).isEqualTo(1)
                assertThat(employed).isTrue()
                assertThat(firstName).isEqualTo("Fred")
                assertThat(lastName).isEqualTo(LastName("Flintstone"))
                assertThat(occupation).isEqualTo("Brontosaurus Operator")
                assertThat(birthDate).isNotNull()
                assertThat(address?.id).isEqualTo(1)
                assertThat(address?.streetAddress).isEqualTo("123 Main Street")
                assertThat(address?.city).isEqualTo("Bedrock")
                assertThat(address?.state).isEqualTo("IN")
            }
        }
    }

    @Test
    fun testJoinPrimaryKey() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonWithAddressMapper::class.java)

            val record = mapper.selectByPrimaryKey(1)

            assertThat(record).isNotNull()
            with(record!!) {
                assertThat(id).isEqualTo(1)
                assertThat(employed).isTrue()
                assertThat(firstName).isEqualTo("Fred")
                assertThat(lastName).isEqualTo(LastName("Flintstone"))
                assertThat(occupation).isEqualTo("Brontosaurus Operator")
                assertThat(birthDate).isNotNull()
                assertThat(address?.id).isEqualTo(1)
                assertThat(address?.streetAddress).isEqualTo("123 Main Street")
                assertThat(address?.city).isEqualTo("Bedrock")
                assertThat(address?.state).isEqualTo("IN")
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

    companion object {
        const val JDBC_URL = "jdbc:hsqldb:mem:aname"
        const val JDBC_DRIVER = "org.hsqldb.jdbcDriver"
    }
}
