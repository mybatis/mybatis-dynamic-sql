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
import org.mybatis.dynamic.sql.util.kotlin.elements.invoke
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.deleteFrom
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.select
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.update
import org.mybatis.dynamic.sql.util.mybatis3.CommonDeleteMapper
import org.mybatis.dynamic.sql.util.mybatis3.CommonSelectMapper
import org.mybatis.dynamic.sql.util.mybatis3.CommonUpdateMapper
import java.io.InputStreamReader
import java.sql.DriverManager

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ExistsTest {
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
            addMapper(CommonDeleteMapper::class.java)
            addMapper(CommonUpdateMapper::class.java)
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
                where {
                    exists {
                        select(orderLine.allColumns()) {
                            from(orderLine, "ol")
                            where { orderLine.itemId isEqualTo "im"(itemMaster.itemId) }
                        }
                    }
                }
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
    fun testExistsPropagatedAliases() {
        newSession().use { session ->
            val mapper = session.getMapper(CommonSelectMapper::class.java)

            val selectStatement = select(itemMaster.allColumns()) {
                from(itemMaster, "im")
                where {
                    exists {
                        select(orderLine.allColumns()) {
                            from(orderLine, "ol")
                            where { orderLine.itemId isEqualTo itemMaster.itemId }
                        }
                    }
                }
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
                where {
                    not {
                        exists {
                            select(orderLine.allColumns()) {
                                from(orderLine, "ol")
                                where { orderLine.itemId isEqualTo "im"(itemMaster.itemId) }
                            }
                        }
                    }
                }
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
    fun testNotExistsNewNot() {
        newSession().use { session ->
            val mapper = session.getMapper(CommonSelectMapper::class.java)

            val selectStatement = select(itemMaster.allColumns()) {
                from(itemMaster, "im")
                where {
                    not {
                        exists {
                            select(orderLine.allColumns()) {
                                from(orderLine, "ol")
                                where { orderLine.itemId isEqualTo "im"(itemMaster.itemId) }
                            }
                        }
                    }
                }
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
    fun testPropagateTableAliasToExists() {
        newSession().use { session ->
            val mapper = session.getMapper(CommonSelectMapper::class.java)

            val selectStatement = select(itemMaster.allColumns()) {
                from(itemMaster, "im")
                where {
                    not {
                        exists {
                            select(orderLine.allColumns()) {
                                from(orderLine, "ol")
                                where { orderLine.itemId isEqualTo itemMaster.itemId }
                            }
                        }
                    }
                }
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
                where { itemMaster.itemId isEqualTo 22 }
                and {
                    exists {
                        select(orderLine.allColumns()) {
                            from(orderLine, "ol")
                            where { orderLine.itemId isEqualTo "im"(itemMaster.itemId) }
                        }
                    }
                }
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
                where { itemMaster.itemId isEqualTo 22 }
                and {
                    exists {
                        select(orderLine.allColumns()) {
                            from(orderLine, "ol")
                            where { orderLine.itemId isEqualTo "im"(itemMaster.itemId) }
                        }
                    }
                    and { itemMaster.itemId isGreaterThan 2 }
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
                where {
                    itemMaster.itemId isEqualTo 22
                    and {
                        exists {
                            select(orderLine.allColumns()) {
                                from(orderLine, "ol")
                                where { orderLine.itemId isEqualTo "im"(itemMaster.itemId) }
                            }
                        }
                    }
                    and { itemMaster.itemId isGreaterThan 2 }
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
                where {
                    itemMaster.itemId isEqualTo 22
                    and {
                        exists {
                            select(orderLine.allColumns()) {
                                from(orderLine, "ol")
                                where { orderLine.itemId isEqualTo "im"(itemMaster.itemId) }
                            }
                        }
                        and { itemMaster.itemId isGreaterThan 2 }
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
                where { itemMaster.itemId isEqualTo 22 }
                or {
                    exists {
                        select(orderLine.allColumns()) {
                            from(orderLine, "ol")
                            where { orderLine.itemId isEqualTo "im"(itemMaster.itemId) }
                        }
                    }
                }
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
                where { itemMaster.itemId isEqualTo 22 }
                or {
                    exists {
                        select(orderLine.allColumns()) {
                            from(orderLine, "ol")
                            where { orderLine.itemId isEqualTo "im"(itemMaster.itemId) }
                        }
                    }
                    and { itemMaster.itemId isGreaterThan 2 }
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
                where {
                    itemMaster.itemId isEqualTo 22
                    or {
                        exists {
                            select(orderLine.allColumns()) {
                                from(orderLine, "ol")
                                where { orderLine.itemId isEqualTo "im"(itemMaster.itemId) }
                            }
                        }
                    }
                    and { itemMaster.itemId isGreaterThan 2 }
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
                where {
                    itemMaster.itemId isEqualTo 22
                    or {
                        exists {
                            select(orderLine.allColumns()) {
                                from(orderLine, "ol")
                                where { orderLine.itemId isEqualTo "im"(itemMaster.itemId) }
                            }
                        }
                        and { itemMaster.itemId isGreaterThan 2 }
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
                where {
                    exists {
                        select(orderLine.allColumns()) {
                            from(orderLine, "ol")
                            where { orderLine.itemId isEqualTo "im"(itemMaster.itemId) }
                        }
                    }
                    or { itemMaster.itemId isEqualTo 22 }
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
                where {
                    exists {
                        select(orderLine.allColumns()) {
                            from(orderLine, "ol")
                            where { orderLine.itemId isEqualTo "im"(itemMaster.itemId) }
                        }
                    }
                    and { itemMaster.itemId isEqualTo 22 }
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
    fun testDeleteWithHardAlias() {
        newSession().use { session ->
            val mapper = session.getMapper(CommonDeleteMapper::class.java)
            val im = itemMaster.withAlias("im")
            val deleteStatement = deleteFrom(im) {
                where {
                    not {
                        exists {
                            select(orderLine.allColumns()) {
                                from(orderLine, "ol")
                                where{orderLine.itemId isEqualTo im.itemId}
                            }
                        }
                    }
                }
            }

            val expectedStatement = "delete from ItemMaster im where not exists " +
                    "(select ol.* from OrderLine ol where ol.item_id = im.item_id)"

            assertThat(deleteStatement.deleteStatement).isEqualTo(expectedStatement)
            val rows = mapper.delete(deleteStatement)
            assertThat(rows).isEqualTo(1)
        }
    }

    @Test
    fun testDeleteWithSoftAlias() {
        newSession().use { session ->
            val mapper = session.getMapper(CommonDeleteMapper::class.java)
            val deleteStatement = deleteFrom(itemMaster, "im") {
                where {
                    not {
                        exists {
                            select(orderLine.allColumns()) {
                                from(orderLine, "ol")
                                where{orderLine.itemId isEqualTo itemMaster.itemId}
                            }
                        }
                    }
                }
            }

            val expectedStatement = "delete from ItemMaster im where not exists " +
                    "(select ol.* from OrderLine ol where ol.item_id = im.item_id)"

            assertThat(deleteStatement.deleteStatement).isEqualTo(expectedStatement)
            val rows = mapper.delete(deleteStatement)
            assertThat(rows).isEqualTo(1)
        }
    }

    @Test
    fun testUpdateWithHardAlias() {
        newSession().use { session ->
            val mapper = session.getMapper(CommonUpdateMapper::class.java)
            val im = itemMaster.withAlias("im")
            val updateStatement = update(im) {
                set(im.description) equalTo "No Orders"
                where {
                    not {
                        exists {
                            select(orderLine.allColumns()) {
                                from(orderLine, "ol")
                                where{orderLine.itemId isEqualTo im.itemId}
                            }
                        }
                    }
                }
            }

            val expectedStatement = "update ItemMaster im " +
                    "set im.description = #{parameters.p1,jdbcType=VARCHAR} " +
                    "where not exists (select ol.* from OrderLine ol where ol.item_id = im.item_id)"

            assertThat(updateStatement.updateStatement).isEqualTo(expectedStatement)
            val rows = mapper.update(updateStatement)
            assertThat(rows).isEqualTo(1)
        }
    }

    @Test
    fun testUpdateWithSoftAlias() {
        newSession().use { session ->
            val mapper = session.getMapper(CommonUpdateMapper::class.java)
            val updateStatement = update(itemMaster, "im") {
                set(itemMaster.description) equalTo "No Orders"
                where {
                    not {
                        exists {
                            select(orderLine.allColumns()) {
                                from(orderLine, "ol")
                                where{orderLine.itemId isEqualTo itemMaster.itemId}
                            }
                        }
                    }
                }
            }

            val expectedStatement = "update ItemMaster im " +
                    "set im.description = #{parameters.p1,jdbcType=VARCHAR} " +
                    "where not exists (select ol.* from OrderLine ol where ol.item_id = im.item_id)"

            assertThat(updateStatement.updateStatement).isEqualTo(expectedStatement)
            val rows = mapper.update(updateStatement)
            assertThat(rows).isEqualTo(1)
        }
    }

    companion object {
        const val JDBC_URL = "jdbc:hsqldb:mem:aname"
        const val JDBC_DRIVER = "org.hsqldb.jdbcDriver"
    }
}
