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
package examples.kotlin.mybatis3.joins

import examples.kotlin.mybatis3.joins.ItemMasterDynamicSQLSupport.itemMaster
import examples.kotlin.mybatis3.joins.OrderLineDynamicSQLSupport.orderLine
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource
import org.apache.ibatis.jdbc.ScriptRunner
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
import org.mybatis.dynamic.sql.util.kotlin.elements.exists
import org.mybatis.dynamic.sql.util.kotlin.elements.isEqualTo
import org.mybatis.dynamic.sql.util.kotlin.elements.isGreaterThan
import org.mybatis.dynamic.sql.util.kotlin.elements.notExists
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.select
import org.mybatis.dynamic.sql.util.mybatis3.CommonSelectMapper
import java.io.InputStreamReader
import java.sql.DriverManager

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DeprecatedWhereDSLTest {
    private lateinit var sqlSessionFactory: SqlSessionFactory

    @BeforeAll
    fun setup() {
        Class.forName(JDBC_DRIVER)
        val script = javaClass.getResourceAsStream("/examples/kotlin/mybatis3/joins/CreateJoinDB.sql")
        DriverManager.getConnection(JDBC_URL, "sa", "").use { connection ->
            val sr = ScriptRunner(connection)
            sr.setLogWriter(null)
            sr.runScript(InputStreamReader(script!!))
        }

        val dataSource = UnpooledDataSource(JDBC_DRIVER, JDBC_URL, "sa", "")
        val environment = Environment("test", JdbcTransactionFactory(), dataSource)
        with(Configuration(environment)) {
            addMapper(CommonSelectMapper::class.java)
            sqlSessionFactory = SqlSessionFactoryBuilder().build(this)
        }
    }

    private fun newSession(): SqlSession = sqlSessionFactory.openSession()

    @Test
    fun testExists() {
        newSession().use { session ->
            val mapper = session.getMapper(CommonSelectMapper::class.java)

            val selectStatement = select(itemMaster.allColumns()) {
                from(itemMaster, "im")
                where(
                    exists {
                        select(orderLine.allColumns()) {
                            from(orderLine, "ol")
                            where(orderLine.itemId, isEqualTo(itemMaster.itemId.qualifiedWith("im")))
                        }
                    }
                )
                orderBy(itemMaster.itemId)
            }

            val expectedStatement: String = "select im.* from ItemMaster im" +
                " where exists (select ol.* from OrderLine ol where ol.item_id = im.item_id)" +
                " order by item_id"
            assertThat(selectStatement.selectStatement).isEqualTo(expectedStatement)

            val rows = mapper.selectManyMappedRows(selectStatement)
            assertThat(rows).hasSize(3)

            with(rows[0]) {
                assertThat(this).containsEntry("ITEM_ID", 22)
                assertThat(this).containsEntry("DESCRIPTION", "Helmet")
            }

            with(rows[1]) {
                assertThat(this).containsEntry("ITEM_ID", 33)
                assertThat(this).containsEntry("DESCRIPTION", "First Base Glove")
            }

            with(rows[2]) {
                assertThat(this).containsEntry("ITEM_ID", 44)
                assertThat(this).containsEntry("DESCRIPTION", "Outfield Glove")
            }
        }
    }

    @Test
    fun testNotExists() {
        newSession().use { session ->
            val mapper = session.getMapper(CommonSelectMapper::class.java)

            val selectStatement = select(itemMaster.allColumns()) {
                from(itemMaster, "im")
                where(
                    notExists {
                        select(orderLine.allColumns()) {
                            from(orderLine, "ol")
                            where(orderLine.itemId, isEqualTo(itemMaster.itemId.qualifiedWith("im")))
                        }
                    }
                )
                orderBy(itemMaster.itemId)
            }

            val expectedStatement: String = "select im.* from ItemMaster im" +
                " where not exists (select ol.* from OrderLine ol where ol.item_id = im.item_id)" +
                " order by item_id"
            assertThat(selectStatement.selectStatement).isEqualTo(expectedStatement)

            val rows = mapper.selectManyMappedRows(selectStatement)
            assertThat(rows).hasSize(1)

            with(rows[0]) {
                assertThat(this).containsEntry("ITEM_ID", 55)
                assertThat(this).containsEntry("DESCRIPTION", "Catcher Glove")
            }
        }
    }

    @Test
    fun testAndExists() {
        newSession().use { session ->
            val mapper = session.getMapper(CommonSelectMapper::class.java)

            val selectStatement = select(itemMaster.allColumns()) {
                from(itemMaster, "im")
                where(itemMaster.itemId, isEqualTo(22))
                and(
                    exists {
                        select(orderLine.allColumns()) {
                            from(orderLine, "ol")
                            where(orderLine.itemId, isEqualTo(itemMaster.itemId.qualifiedWith("im")))
                        }
                    }
                )
                orderBy(itemMaster.itemId)
            }

            val expectedStatement = "select im.* from ItemMaster im" +
                " where im.item_id = #{parameters.p1,jdbcType=INTEGER}" +
                " and exists (select ol.* from OrderLine ol where ol.item_id = im.item_id)" +
                " order by item_id"

            assertThat(selectStatement.selectStatement).isEqualTo(expectedStatement)

            val rows = mapper.selectManyMappedRows(selectStatement)
            assertThat(rows).hasSize(1)

            with(rows[0]) {
                assertThat(this).containsEntry("ITEM_ID", 22)
                assertThat(this).containsEntry("DESCRIPTION", "Helmet")
            }
        }
    }

    @Test
    fun testAndExistsAnd() {
        newSession().use { session ->
            val mapper = session.getMapper(CommonSelectMapper::class.java)

            val selectStatement = select(itemMaster.allColumns()) {
                from(itemMaster, "im")
                where(itemMaster.itemId, isEqualTo(22))
                and(
                    exists {
                        select(orderLine.allColumns()) {
                            from(orderLine, "ol")
                            where(orderLine.itemId, isEqualTo(itemMaster.itemId.qualifiedWith("im")))
                        }
                    }
                ) {
                    and(itemMaster.itemId, isGreaterThan(2))
                }
                orderBy(itemMaster.itemId)
            }

            val expectedStatement = "select im.* from ItemMaster im" +
                " where im.item_id = #{parameters.p1,jdbcType=INTEGER}" +
                " and (exists (select ol.* from OrderLine ol where ol.item_id = im.item_id)" +
                " and im.item_id > #{parameters.p2,jdbcType=INTEGER})" +
                " order by item_id"
            assertThat(selectStatement.selectStatement).isEqualTo(expectedStatement)

            val rows = mapper.selectManyMappedRows(selectStatement)
            assertThat(rows).hasSize(1)

            with(rows[0]) {
                assertThat(this).containsEntry("ITEM_ID", 22)
                assertThat(this).containsEntry("DESCRIPTION", "Helmet")
            }
        }
    }

    @Test
    fun testAndExistsAnd2() {
        newSession().use { session ->
            val mapper = session.getMapper(CommonSelectMapper::class.java)

            val selectStatement = select(itemMaster.allColumns()) {
                from(itemMaster, "im")
                where(itemMaster.itemId, isEqualTo(22)) {
                    and(
                        exists {
                            select(orderLine.allColumns()) {
                                from(orderLine, "ol")
                                where(orderLine.itemId, isEqualTo(itemMaster.itemId.qualifiedWith("im")))
                            }
                        }
                    )
                    and(itemMaster.itemId, isGreaterThan(2))
                }
                orderBy(itemMaster.itemId)
            }

            val expectedStatement = "select im.* from ItemMaster im" +
                " where (im.item_id = #{parameters.p1,jdbcType=INTEGER}" +
                " and exists (select ol.* from OrderLine ol where ol.item_id = im.item_id)" +
                " and im.item_id > #{parameters.p2,jdbcType=INTEGER})" +
                " order by item_id"
            assertThat(selectStatement.selectStatement).isEqualTo(expectedStatement)

            val rows = mapper.selectManyMappedRows(selectStatement)
            assertThat(rows).hasSize(1)

            with(rows[0]) {
                assertThat(this).containsEntry("ITEM_ID", 22)
                assertThat(this).containsEntry("DESCRIPTION", "Helmet")
            }
        }
    }

    @Test
    fun testAndExistsAnd3() {
        newSession().use { session ->
            val mapper = session.getMapper(CommonSelectMapper::class.java)

            val selectStatement = select(itemMaster.allColumns()) {
                from(itemMaster, "im")
                where(itemMaster.itemId, isEqualTo(22)) {
                    and(
                        exists {
                            select(orderLine.allColumns()) {
                                from(orderLine, "ol")
                                where(orderLine.itemId, isEqualTo(itemMaster.itemId.qualifiedWith("im")))
                            }
                        }
                    ) {
                        and(itemMaster.itemId, isGreaterThan(2))
                    }
                }
                orderBy(itemMaster.itemId)
            }

            val expectedStatement = "select im.* from ItemMaster im" +
                " where (im.item_id = #{parameters.p1,jdbcType=INTEGER}" +
                " and (exists (select ol.* from OrderLine ol where ol.item_id = im.item_id)" +
                " and im.item_id > #{parameters.p2,jdbcType=INTEGER}))" +
                " order by item_id"
            assertThat(selectStatement.selectStatement).isEqualTo(expectedStatement)

            val rows = mapper.selectManyMappedRows(selectStatement)
            assertThat(rows).hasSize(1)

            with(rows[0]) {
                assertThat(this).containsEntry("ITEM_ID", 22)
                assertThat(this).containsEntry("DESCRIPTION", "Helmet")
            }
        }
    }

    @Test
    fun testOrExists() {
        newSession().use { session ->
            val mapper = session.getMapper(CommonSelectMapper::class.java)

            val selectStatement = select(itemMaster.allColumns()) {
                from(itemMaster, "im")
                where(itemMaster.itemId, isEqualTo(22))
                or(
                    exists {
                        select(orderLine.allColumns()) {
                            from(orderLine, "ol")
                            where(orderLine.itemId, isEqualTo(itemMaster.itemId.qualifiedWith("im")))
                        }
                    }
                )
                orderBy(itemMaster.itemId)
            }

            val expectedStatement = "select im.* from ItemMaster im" +
                " where im.item_id = #{parameters.p1,jdbcType=INTEGER}" +
                " or exists (select ol.* from OrderLine ol where ol.item_id = im.item_id)" +
                " order by item_id"
            assertThat(selectStatement.selectStatement).isEqualTo(expectedStatement)

            val rows = mapper.selectManyMappedRows(selectStatement)
            assertThat(rows).hasSize(3)

            with(rows[0]) {
                assertThat(this).containsEntry("ITEM_ID", 22)
                assertThat(this).containsEntry("DESCRIPTION", "Helmet")
            }

            with(rows[1]) {
                assertThat(this).containsEntry("ITEM_ID", 33)
                assertThat(this).containsEntry("DESCRIPTION", "First Base Glove")
            }

            with(rows[2]) {
                assertThat(this).containsEntry("ITEM_ID", 44)
                assertThat(this).containsEntry("DESCRIPTION", "Outfield Glove")
            }
        }
    }

    @Test
    fun testOrExistsAnd() {
        newSession().use { session ->
            val mapper = session.getMapper(CommonSelectMapper::class.java)

            val selectStatement = select(itemMaster.allColumns()) {
                from(itemMaster, "im")
                where(itemMaster.itemId, isEqualTo(22))
                or(
                    exists {
                        select(orderLine.allColumns()) {
                            from(orderLine, "ol")
                            where(
                                orderLine.itemId,
                                isEqualTo(
                                    itemMaster.itemId.qualifiedWith("im")
                                )
                            )
                        }
                    }
                ) {
                    and(itemMaster.itemId, isGreaterThan(2))
                }
                orderBy(itemMaster.itemId)
            }

            val expectedStatement = "select im.* from ItemMaster im" +
                " where im.item_id = #{parameters.p1,jdbcType=INTEGER}" +
                " or (exists (select ol.* from OrderLine ol where ol.item_id = im.item_id)" +
                " and im.item_id > #{parameters.p2,jdbcType=INTEGER})" +
                " order by item_id"
            assertThat(selectStatement.selectStatement).isEqualTo(expectedStatement)

            val rows = mapper.selectManyMappedRows(selectStatement)
            assertThat(rows).hasSize(3)

            with(rows[0]) {
                assertThat(this).containsEntry("ITEM_ID", 22)
                assertThat(this).containsEntry("DESCRIPTION", "Helmet")
            }

            with(rows[1]) {
                assertThat(this).containsEntry("ITEM_ID", 33)
                assertThat(this).containsEntry("DESCRIPTION", "First Base Glove")
            }

            with(rows[2]) {
                assertThat(this).containsEntry("ITEM_ID", 44)
                assertThat(this).containsEntry("DESCRIPTION", "Outfield Glove")
            }
        }
    }

    @Test
    fun testOrExistsAnd2() {
        newSession().use { session ->
            val mapper = session.getMapper(CommonSelectMapper::class.java)

            val selectStatement = select(itemMaster.allColumns()) {
                from(itemMaster, "im")
                where(itemMaster.itemId, isEqualTo(22)) {
                    or(
                        exists {
                            select(orderLine.allColumns()) {
                                from(orderLine, "ol")
                                where(orderLine.itemId, isEqualTo(itemMaster.itemId.qualifiedWith("im")))
                            }
                        }
                    )
                    and(itemMaster.itemId, isGreaterThan(2))
                }
                orderBy(itemMaster.itemId)
            }

            val expectedStatement = "select im.* from ItemMaster im" +
                " where (im.item_id = #{parameters.p1,jdbcType=INTEGER}" +
                " or exists (select ol.* from OrderLine ol where ol.item_id = im.item_id)" +
                " and im.item_id > #{parameters.p2,jdbcType=INTEGER})" +
                " order by item_id"
            assertThat(selectStatement.selectStatement).isEqualTo(expectedStatement)

            val rows = mapper.selectManyMappedRows(selectStatement)
            assertThat(rows).hasSize(3)

            with(rows[0]) {
                assertThat(this).containsEntry("ITEM_ID", 22)
                assertThat(this).containsEntry("DESCRIPTION", "Helmet")
            }

            with(rows[1]) {
                assertThat(this).containsEntry("ITEM_ID", 33)
                assertThat(this).containsEntry("DESCRIPTION", "First Base Glove")
            }

            with(rows[2]) {
                assertThat(this).containsEntry("ITEM_ID", 44)
                assertThat(this).containsEntry("DESCRIPTION", "Outfield Glove")
            }
        }
    }

    @Test
    fun testOrExistsAnd3() {
        newSession().use { session ->
            val mapper = session.getMapper(CommonSelectMapper::class.java)

            val selectStatement = select(itemMaster.allColumns()) {
                from(itemMaster, "im")
                where(itemMaster.itemId, isEqualTo(22)) {
                    or(
                        exists {
                            select(orderLine.allColumns()) {
                                from(orderLine, "ol")
                                where(orderLine.itemId, isEqualTo(itemMaster.itemId.qualifiedWith("im")))
                            }
                        }
                    ) {
                        and(itemMaster.itemId, isGreaterThan(2))
                    }
                }
                orderBy(itemMaster.itemId)
            }

            val expectedStatement = "select im.* from ItemMaster im" +
                " where (im.item_id = #{parameters.p1,jdbcType=INTEGER}" +
                " or (exists (select ol.* from OrderLine ol where ol.item_id = im.item_id)" +
                " and im.item_id > #{parameters.p2,jdbcType=INTEGER}))" +
                " order by item_id"
            assertThat(selectStatement.selectStatement).isEqualTo(expectedStatement)

            val rows = mapper.selectManyMappedRows(selectStatement)
            assertThat(rows).hasSize(3)

            with(rows[0]) {
                assertThat(this).containsEntry("ITEM_ID", 22)
                assertThat(this).containsEntry("DESCRIPTION", "Helmet")
            }

            with(rows[1]) {
                assertThat(this).containsEntry("ITEM_ID", 33)
                assertThat(this).containsEntry("DESCRIPTION", "First Base Glove")
            }

            with(rows[2]) {
                assertThat(this).containsEntry("ITEM_ID", 44)
                assertThat(this).containsEntry("DESCRIPTION", "Outfield Glove")
            }
        }
    }

    @Test
    fun testWhereExistsOr() {
        newSession().use { session ->
            val mapper = session.getMapper(CommonSelectMapper::class.java)

            val selectStatement = select(itemMaster.allColumns()) {
                from(itemMaster, "im")
                where(
                    exists {
                        select(orderLine.allColumns()) {
                            from(orderLine, "ol")
                            where(orderLine.itemId, isEqualTo(itemMaster.itemId.qualifiedWith("im")))
                        }
                    }
                ) {
                    or(itemMaster.itemId, isEqualTo(22))
                }
                orderBy(itemMaster.itemId)
            }

            val expectedStatement = "select im.* from ItemMaster im" +
                " where (exists (select ol.* from OrderLine ol where ol.item_id = im.item_id)" +
                " or im.item_id = #{parameters.p1,jdbcType=INTEGER})" +
                " order by item_id"
            assertThat(selectStatement.selectStatement).isEqualTo(expectedStatement)

            val rows = mapper.selectManyMappedRows(selectStatement)
            assertThat(rows).hasSize(3)

            with(rows[0]) {
                assertThat(this).containsEntry("ITEM_ID", 22)
                assertThat(this).containsEntry("DESCRIPTION", "Helmet")
            }

            with(rows[1]) {
                assertThat(this).containsEntry("ITEM_ID", 33)
                assertThat(this).containsEntry("DESCRIPTION", "First Base Glove")
            }

            with(rows[2]) {
                assertThat(this).containsEntry("ITEM_ID", 44)
                assertThat(this).containsEntry("DESCRIPTION", "Outfield Glove")
            }
        }
    }

    @Test
    fun testWhereExistsAnd() {
        newSession().use { session ->
            val mapper = session.getMapper(CommonSelectMapper::class.java)

            val selectStatement = select(itemMaster.allColumns()) {
                from(itemMaster, "im")
                where(
                    exists {
                        select(orderLine.allColumns()) {
                            from(orderLine, "ol")
                            where(orderLine.itemId, isEqualTo(itemMaster.itemId.qualifiedWith("im")))
                        }
                    }
                ) {
                    and(itemMaster.itemId, isEqualTo(22))
                }
                orderBy(itemMaster.itemId)
            }

            val expectedStatement = "select im.* from ItemMaster im" +
                " where (exists (select ol.* from OrderLine ol where ol.item_id = im.item_id)" +
                " and im.item_id = #{parameters.p1,jdbcType=INTEGER})" +
                " order by item_id"
            assertThat(selectStatement.selectStatement).isEqualTo(expectedStatement)

            val rows = mapper.selectManyMappedRows(selectStatement)
            assertThat(rows).hasSize(1)

            with(rows[0]) {
                assertThat(this).containsEntry("ITEM_ID", 22)
                assertThat(this).containsEntry("DESCRIPTION", "Helmet")
            }
        }
    }

    @Test
    fun testWhereAnd() {
        newSession().use { session ->
            val mapper = session.getMapper(CommonSelectMapper::class.java)

            val selectStatement = select(itemMaster.allColumns()) {
                from(itemMaster)
                where(itemMaster.itemId, isGreaterThan(3))
                and(itemMaster.itemId, isGreaterThan(4))
                orderBy(itemMaster.itemId)
            }

            val expectedStatement = "select * from ItemMaster" +
                    " where item_id > #{parameters.p1,jdbcType=INTEGER}" +
                    " and item_id > #{parameters.p2,jdbcType=INTEGER}" +
                    " order by item_id"
            assertThat(selectStatement.selectStatement).isEqualTo(expectedStatement)

            val rows = mapper.selectManyMappedRows(selectStatement)
            assertThat(rows).hasSize(4)

            with(rows[0]) {
                assertThat(this).containsEntry("ITEM_ID", 22)
                assertThat(this).containsEntry("DESCRIPTION", "Helmet")
            }
        }
    }

    @Test
    fun testWhereAndAnd() {
        newSession().use { session ->
            val mapper = session.getMapper(CommonSelectMapper::class.java)

            val selectStatement = select(itemMaster.allColumns()) {
                from(itemMaster)
                where(itemMaster.itemId, isGreaterThan(3))
                and(itemMaster.itemId, isGreaterThan(4)) {
                    and(itemMaster.itemId, isGreaterThan(5))
                }
                orderBy(itemMaster.itemId)
            }

            val expectedStatement = "select * from ItemMaster" +
                    " where item_id > #{parameters.p1,jdbcType=INTEGER}" +
                    " and (item_id > #{parameters.p2,jdbcType=INTEGER}" +
                    " and item_id > #{parameters.p3,jdbcType=INTEGER})" +
                    " order by item_id"
            assertThat(selectStatement.selectStatement).isEqualTo(expectedStatement)

            val rows = mapper.selectManyMappedRows(selectStatement)
            assertThat(rows).hasSize(4)

            with(rows[0]) {
                assertThat(this).containsEntry("ITEM_ID", 22)
                assertThat(this).containsEntry("DESCRIPTION", "Helmet")
            }
        }
    }

    @Test
    fun testWhereOr() {
        newSession().use { session ->
            val mapper = session.getMapper(CommonSelectMapper::class.java)

            val selectStatement = select(itemMaster.allColumns()) {
                from(itemMaster)
                where(itemMaster.itemId, isEqualTo(22))
                or(itemMaster.itemId, isEqualTo(33))
                orderBy(itemMaster.itemId)
            }

            val expectedStatement = "select * from ItemMaster" +
                    " where item_id = #{parameters.p1,jdbcType=INTEGER}" +
                    " or item_id = #{parameters.p2,jdbcType=INTEGER}" +
                    " order by item_id"
            assertThat(selectStatement.selectStatement).isEqualTo(expectedStatement)

            val rows = mapper.selectManyMappedRows(selectStatement)
            assertThat(rows).hasSize(2)

            with(rows[0]) {
                assertThat(this).containsEntry("ITEM_ID", 22)
                assertThat(this).containsEntry("DESCRIPTION", "Helmet")
            }
        }
    }

    @Test
    fun testWhereOrOr() {
        newSession().use { session ->
            val mapper = session.getMapper(CommonSelectMapper::class.java)

            val selectStatement = select(itemMaster.allColumns()) {
                from(itemMaster)
                where(itemMaster.itemId, isEqualTo(22))
                or(itemMaster.itemId, isEqualTo(33)) {
                    or(itemMaster.itemId, isEqualTo(44))
                }
                orderBy(itemMaster.itemId)
            }

            val expectedStatement = "select * from ItemMaster" +
                    " where item_id = #{parameters.p1,jdbcType=INTEGER}" +
                    " or (item_id = #{parameters.p2,jdbcType=INTEGER}" +
                    " or item_id = #{parameters.p3,jdbcType=INTEGER})" +
                    " order by item_id"
            assertThat(selectStatement.selectStatement).isEqualTo(expectedStatement)

            val rows = mapper.selectManyMappedRows(selectStatement)
            assertThat(rows).hasSize(3)

            with(rows[0]) {
                assertThat(this).containsEntry("ITEM_ID", 22)
                assertThat(this).containsEntry("DESCRIPTION", "Helmet")
            }
        }
    }

    companion object {
        const val JDBC_URL = "jdbc:hsqldb:mem:aname"
        const val JDBC_DRIVER = "org.hsqldb.jdbcDriver"
    }
}
