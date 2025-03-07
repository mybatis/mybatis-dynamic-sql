/*
 *    Copyright 2016-2025 the original author or authors.
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
import examples.kotlin.mybatis3.TestUtils
import examples.kotlin.mybatis3.mariadb.KItemsDynamicSQLSupport.amount
import examples.kotlin.mybatis3.mariadb.KItemsDynamicSQLSupport.id
import examples.kotlin.mybatis3.mariadb.KItemsDynamicSQLSupport.items
import examples.kotlin.mybatis3.mariadb.KItemsDynamicSQLSupport.description
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource
import org.apache.ibatis.session.SqlSessionFactory
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
import java.util.*

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KMariaDBTest {

    private lateinit var sqlSessionFactory: SqlSessionFactory

    @BeforeAll
    fun setUp() {
        sqlSessionFactory = TestUtils.buildSqlSessionFactory {
            withDataSource(UnpooledDataSource(
                mariadb.driverClassName,
                mariadb.jdbcUrl,
                mariadb.username,
                mariadb.password
            ))
            withMapper(CommonDeleteMapper::class)
            withMapper(CommonSelectMapper::class)
            withMapper(CommonUpdateMapper::class)
        }
    }

    @Test
    fun smokeTest() {
        sqlSessionFactory.openSession().use { session ->
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
        sqlSessionFactory.openSession().use { session ->
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
        sqlSessionFactory.openSession().use { session ->
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
        sqlSessionFactory.openSession().use { session ->
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
        sqlSessionFactory.openSession().use { session ->
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

    @Test
    fun testIsLikeEscape() {
        sqlSessionFactory.openSession().use { session ->
            val mapper = session.getMapper(CommonSelectMapper::class.java)
            val selectStatement = select(id, description) {
                from(items)
                where {
                    description(KIsLikeEscape.isLike("Item 1%", '#'))
                }
            }

            assertThat(selectStatement.selectStatement).isEqualTo("select id, description from items where description like #{parameters.p1,jdbcType=VARCHAR} ESCAPE '#'")
            assertThat(selectStatement.parameters).containsEntry("p1", "Item 1%")

            val rows = mapper.selectManyMappedRows(selectStatement)
            assertThat(rows).hasSize(11)
        }
    }

    @Test
    fun testIsLikeEscapeNoEscapeCharacter() {
        val selectStatement = select(id, description) {
            from(items)
            where {
                description(KIsLikeEscape.isLike("%fred%"))
            }
        }

        assertThat(selectStatement.selectStatement).isEqualTo("select id, description from items where description like #{parameters.p1,jdbcType=VARCHAR}")
        assertThat(selectStatement.parameters).containsEntry("p1", "%fred%")
    }

    @Test
    fun testIsLikeEscapeMap() {
        val selectStatement = select(id, description) {
            from(items)
            where {
                description(KIsLikeEscape.isLike("%fred%", '#').map { s -> s.uppercase(Locale.getDefault()) })
            }
        }

        assertThat(selectStatement.selectStatement).isEqualTo("select id, description from items where description like #{parameters.p1,jdbcType=VARCHAR} ESCAPE '#'")
        assertThat(selectStatement.parameters).containsEntry("p1", "%FRED%")
    }

    @Test
    fun testIsLikeEscapeFilter() {
        val selectStatement = select(id, description) {
            from(items)
            where {
                description(KIsLikeEscape.isLike("%fred%", '#').filter { _ -> false })
            }
            configureStatement { isNonRenderingWhereClauseAllowed = true }
        }

        assertThat(selectStatement.selectStatement).isEqualTo("select id, description from items")
        assertThat(selectStatement.parameters).isEmpty()
    }

    @Test
    fun testIsLikeEscapeFilterMapFilter() {
        val selectStatement = select(id, description) {
            from(items)
            where {
                description(KIsLikeEscape.isLike("%fred%", '#')
                    .filter { _ -> true }
                    .map { s -> s.uppercase(Locale.getDefault()) }
                    .filter{_ -> false })
            }
            configureStatement { isNonRenderingWhereClauseAllowed = true }
        }

        assertThat(selectStatement.selectStatement).isEqualTo("select id, description from items")
        assertThat(selectStatement.parameters).isEmpty()
    }

    companion object {
        @Container
        private val mariadb = MariaDBContainer(TestContainersConfiguration.MARIADB_LATEST)
            .withInitScript("examples/mariadb/CreateDB.sql")
    }
}
