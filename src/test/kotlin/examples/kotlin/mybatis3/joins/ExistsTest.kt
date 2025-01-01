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
package examples.kotlin.mybatis3.joins

import examples.kotlin.mybatis3.TestUtils
import examples.kotlin.mybatis3.joins.ItemMasterDynamicSQLSupport.itemMaster
import examples.kotlin.mybatis3.joins.OrderLineDynamicSQLSupport.orderLine
import org.apache.ibatis.session.SqlSessionFactory
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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ExistsTest {
    private lateinit var sqlSessionFactory: SqlSessionFactory

    @BeforeAll
    fun setup() {
        sqlSessionFactory = TestUtils.buildSqlSessionFactory {
            withInitializationScript("/examples/kotlin/mybatis3/joins/CreateJoinDB.sql")
            withMapper(CommonSelectMapper::class)
            withMapper(CommonDeleteMapper::class)
            withMapper(CommonUpdateMapper::class)
        }
    }

    @Test
    fun testExists() {
        sqlSessionFactory.openSession().use { session ->
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
        sqlSessionFactory.openSession().use { session ->
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
        sqlSessionFactory.openSession().use { session ->
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
        sqlSessionFactory.openSession().use { session ->
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
        sqlSessionFactory.openSession().use { session ->
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
        sqlSessionFactory.openSession().use { session ->
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
        sqlSessionFactory.openSession().use { session ->
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
        sqlSessionFactory.openSession().use { session ->
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
                " where im.item_id = #{parameters.p1,jdbcType=INTEGER}" +
                " and exists (select ol.* from OrderLine ol where ol.item_id = im.item_id)" +
                " and im.item_id > #{parameters.p2,jdbcType=INTEGER}" +
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
        sqlSessionFactory.openSession().use { session ->
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
    fun testOrExists() {
        sqlSessionFactory.openSession().use { session ->
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
        sqlSessionFactory.openSession().use { session ->
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
        sqlSessionFactory.openSession().use { session ->
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
                " where im.item_id = #{parameters.p1,jdbcType=INTEGER}" +
                " or exists (select ol.* from OrderLine ol where ol.item_id = im.item_id)" +
                " and im.item_id > #{parameters.p2,jdbcType=INTEGER}" +
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
        sqlSessionFactory.openSession().use { session ->
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
    fun testWhereExistsOr() {
        sqlSessionFactory.openSession().use { session ->
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
                " where exists (select ol.* from OrderLine ol where ol.item_id = im.item_id)" +
                " or im.item_id = #{parameters.p1,jdbcType=INTEGER}" +
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
        sqlSessionFactory.openSession().use { session ->
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
                " where exists (select ol.* from OrderLine ol where ol.item_id = im.item_id)" +
                " and im.item_id = #{parameters.p1,jdbcType=INTEGER}" +
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
        sqlSessionFactory.openSession().use { session ->
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
        sqlSessionFactory.openSession().use { session ->
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
        sqlSessionFactory.openSession().use { session ->
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
        sqlSessionFactory.openSession().use { session ->
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
}
