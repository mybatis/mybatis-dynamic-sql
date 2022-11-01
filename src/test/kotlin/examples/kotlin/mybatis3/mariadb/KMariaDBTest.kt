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
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.deleteFrom
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.select
import org.mybatis.dynamic.sql.util.mybatis3.CommonDeleteMapper
import org.mybatis.dynamic.sql.util.mybatis3.CommonSelectMapper
import org.testcontainers.containers.MariaDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KMariaDBTest {

    private lateinit var sqlSessionFactory: SqlSessionFactory

    @BeforeAll
    fun setUp() {
        val dataSource = UnpooledDataSource(
            container.driverClassName,
            container.jdbcUrl,
            container.username,
            container.password
        )
        val environment = Environment("test", JdbcTransactionFactory(), dataSource)
        with(Configuration()) {
            this.environment = environment
            addMapper(CommonDeleteMapper::class.java)
            addMapper(CommonSelectMapper::class.java)
            sqlSessionFactory = SqlSessionFactoryBuilder().build(this)
        }
    }

    private fun newSession(): SqlSession {
        return sqlSessionFactory.openSession()
    }

    @Test
    fun smokeTest() {
        newSession().use {
            val mapper = it.getMapper(CommonSelectMapper::class.java)

            val selectStatement = select(id, description) {
                from(items)
            }

            val rows = mapper.selectManyMappedRows(selectStatement)
            assertThat(rows).hasSize(20)
        }
    }

    @Test
    fun testDeleteWithLimit() {
        newSession().use {
            val mapper = it.getMapper(CommonDeleteMapper::class.java)

            val deleteStatement = deleteFrom(items) {
                limit(4)
            }

            assertThat(deleteStatement.deleteStatement).isEqualTo("delete from items limit #{parameters.p1}")

            val rows = mapper.delete(deleteStatement)
            assertThat(rows).isEqualTo(4)
        }
    }

    companion object {
        @Container
        private val container = MariaDBContainer(DockerImageName.parse("mariadb:10.9.3"))
            .withInitScript("examples/mariadb/CreateDB.sql")
    }
}
