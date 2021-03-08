/*
 *    Copyright 2016-2021 the original author or authors.
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

import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.Person.id
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.Person.occupation
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource
import org.apache.ibatis.jdbc.ScriptRunner
import org.apache.ibatis.mapping.Environment
import org.apache.ibatis.session.Configuration
import org.apache.ibatis.session.SqlSession
import org.apache.ibatis.session.SqlSessionFactoryBuilder
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mybatis.dynamic.sql.util.kotlin.WhereApplier
import org.mybatis.dynamic.sql.util.kotlin.elements.isEqualTo
import org.mybatis.dynamic.sql.util.kotlin.elements.isNull
import java.io.InputStreamReader
import java.sql.DriverManager

class ReusableWhereTest {
    private fun newSession(): SqlSession {
        Class.forName(JDBC_DRIVER)
        val script = javaClass.getResourceAsStream("/examples/kotlin/mybatis3/CreateSimpleDB.sql")
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
    fun testCount() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val rows = mapper.count {
                applyWhere(commonWhere)
            }

            assertThat(rows).isEqualTo(3)
        }
    }

    @Test
    fun testDelete() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val rows = mapper.delete {
                applyWhere(commonWhere)
            }

            assertThat(rows).isEqualTo(3)
        }
    }

    @Test
    fun testSelect() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val rows = mapper.select {
                applyWhere(commonWhere)
                orderBy(id)
            }

            assertThat(rows).hasSize(3)
        }
    }

    @Test
    fun testUpdate() {
        newSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val rows = mapper.update {
                set(occupation).equalToStringConstant("worker")
                applyWhere(commonWhere)
            }

            assertThat(rows).isEqualTo(3)
        }
    }

    private val commonWhere: WhereApplier = {
        where(id, isEqualTo(1)).or(occupation, isNull<String>())
    }

    companion object {
        const val JDBC_URL = "jdbc:hsqldb:mem:aname"
        const val JDBC_DRIVER = "org.hsqldb.jdbcDriver"
    }
}
