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
package examples.kotlin.mybatis3.mariadb

import config.TestContainersConfiguration
import examples.kotlin.mybatis3.mariadb.KItemsDynamicSQLSupport.amount
import examples.kotlin.mybatis3.mariadb.KItemsDynamicSQLSupport.id
import examples.kotlin.mybatis3.mariadb.KItemsDynamicSQLSupport.items
import examples.kotlin.mybatis3.mariadb.KItemsDynamicSQLSupport.description
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource
import org.apache.ibatis.mapping.Environment
import org.apache.ibatis.session.Configuration
import org.apache.ibatis.session.SqlSession
import org.apache.ibatis.session.SqlSessionFactory
import org.apache.ibatis.session.SqlSessionFactoryBuilder
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mybatis.dynamic.sql.util.kotlin.elements.add
import org.mybatis.dynamic.sql.util.kotlin.elements.constant
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.deleteFrom
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.select
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.update
import org.mybatis.dynamic.sql.util.mybatis3.CommonDeleteMapper
import org.mybatis.dynamic.sql.util.mybatis3.CommonSelectMapper
import org.mybatis.dynamic.sql.util.mybatis3.CommonUpdateMapper
import org.testcontainers.containers.MariaDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KMariaDBTest {

    private lateinit var sqlSessionFactory: SqlSessionFactory

    @BeforeAll
    fun setUp() {
        val dataSource = UnpooledDataSource(
            mariadb.driverClassName,
            mariadb.jdbcUrl,
            mariadb.username,
            mariadb.password
        )
        val environment = Environment("test", JdbcTransactionFactory(), dataSource)
        with(Configuration(environment)) {
            addMapper(CommonDeleteMapper::class.java)
            addMapper(CommonSelectMapper::class.java)
            addMapper(CommonUpdateMapper::class.java)
            sqlSessionFactory = SqlSessionFactoryBuilder().build(this)
        }
    }

    private fun newSession(): SqlSession = sqlSessionFactory.openSession()

    @Test
    fun smokeTest() {
        newSession().use { session ->
            val mapper = session.getMapper(CommonSelectMapper::class.java)

            val selectStatement = select(id, description) {
                from(items)
            }

            val rows = mapper.selectManyMappedRows(selectStatement)
            assertThat(rows).hasSize(20)
        }
    }

    @Test
    fun testDeleteWithLimit() {
        newSession().use { session ->
            val mapper = session.getMapper(CommonDeleteMapper::class.java)

            val deleteStatement = deleteFrom(items) {
                limit(4)
            }

            assertThat(deleteStatement.deleteStatement).isEqualTo("delete from items limit #{parameters.p1}")

            val rows = mapper.delete(deleteStatement)
            assertThat(rows).isEqualTo(4)
        }
    }

    @Test
    fun testDeleteWithOrderBy() {
        newSession().use { session ->
            val mapper = session.getMapper(CommonDeleteMapper::class.java)

            val deleteStatement = deleteFrom(items) {
                orderBy(id)
            }

            assertThat(deleteStatement.deleteStatement).isEqualTo("delete from items order by id")

            val rows = mapper.delete(deleteStatement)
            assertThat(rows).isEqualTo(20)
        }
    }

    @Test
    fun testUpdateWithLimit() {
        newSession().use { session ->
            val mapper = session.getMapper(CommonUpdateMapper::class.java)

            val updateStatement = update(items) {
                set(amount) equalTo add(amount, constant<Int>("100"))
                limit(4)
            }

            assertThat(updateStatement.updateStatement)
                .isEqualTo("update items set amount = (amount + 100) limit #{parameters.p1}")

            val rows = mapper.update(updateStatement)
            assertThat(rows).isEqualTo(4)
        }
    }

    @Test
    fun testUpdateWithOrderBy() {
        newSession().use { session ->
            val mapper = session.getMapper(CommonUpdateMapper::class.java)

            val updateStatement = update(items) {
                set(amount) equalTo add(amount, constant<Int>("100"))
                orderBy(id)
            }

            assertThat(updateStatement.updateStatement)
                .isEqualTo("update items set amount = (amount + 100) order by id")

            val rows = mapper.update(updateStatement)
            assertThat(rows).isEqualTo(20)
        }
    }

    companion object {
        @Container
        private val mariadb = MariaDBContainer(TestContainersConfiguration.MARIADB_LATEST)
            .withInitScript("examples/mariadb/CreateDB.sql")
    }
}
