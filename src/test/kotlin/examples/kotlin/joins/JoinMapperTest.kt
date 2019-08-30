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
package examples.kotlin.joins

import examples.kotlin.joins.ItemMasterDynamicSQLSupport.ItemMaster
import examples.kotlin.joins.OrderDetailDynamicSQLSupport.OrderDetail
import examples.kotlin.joins.OrderLineDynamicSQLSupport.OrderLine
import examples.kotlin.joins.OrderMasterDynamicSQLSupport.OrderMaster
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
import org.mybatis.dynamic.sql.util.kotlin.fullJoin
import org.mybatis.dynamic.sql.util.kotlin.join
import org.mybatis.dynamic.sql.util.kotlin.leftJoin
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.from
import org.mybatis.dynamic.sql.util.kotlin.rightJoin
import java.io.InputStreamReader
import java.sql.DriverManager

class JoinMapperTest {

    private fun newSession(): SqlSession {
        Class.forName(JDBC_DRIVER)
        val script = javaClass.getResourceAsStream("/examples/kotlin/joins/CreateJoinDB.sql")

        DriverManager.getConnection(JDBC_URL, "sa", "").use { connection ->
            val sr = ScriptRunner(connection)
            sr.setLogWriter(null)
            sr.runScript(InputStreamReader(script))
        }

        val ds = UnpooledDataSource(JDBC_DRIVER, JDBC_URL, "sa", "")
        val environment = Environment("test", JdbcTransactionFactory(), ds)
        val config = Configuration(environment)
        config.addMapper(JoinMapper::class.java)
        return SqlSessionFactoryBuilder().build(config).openSession()
    }

    @Test
    fun testSingleTableJoin() {
        newSession().use { session ->
            val mapper = session.getMapper(JoinMapper::class.java)

            val selectStatement = select(OrderMaster.orderId, OrderMaster.orderDate, OrderDetail.lineNumber,
                OrderDetail.description, OrderDetail.quantity).from(OrderMaster, "om") {
                join(OrderDetail, "od") {
                    on(OrderMaster.orderId, equalTo(OrderDetail.orderId))
                }
            }

            val expectedStatement = "select om.order_id, om.order_date, od.line_number, od.description, od.quantity" +
                " from OrderMaster om join OrderDetail od on om.order_id = od.order_id"
            assertThat(selectStatement.selectStatement).isEqualTo(expectedStatement)

            val rows = mapper.selectMany(selectStatement)

            assertThat(rows.size).isEqualTo(2)

            with(rows[0]) {
                assertThat(id).isEqualTo(1)
                assertThat(details?.size).isEqualTo(2)
                assertThat(details?.get(0)?.lineNumber).isEqualTo(1)
                assertThat(details?.get(1)?.lineNumber).isEqualTo(2)
            }

            with(rows[1]) {
                assertThat(id).isEqualTo(2)
                assertThat(details?.size).isEqualTo(1)
                assertThat(details?.get(0)?.lineNumber).isEqualTo(1)
            }
        }
    }

    @Test
    fun testCompoundJoin1() {
        // this is a nonsensical join, but it does test the "and" capability
        val selectStatement = select(OrderMaster.orderId, OrderMaster.orderDate, OrderDetail.lineNumber,
            OrderDetail.description, OrderDetail.quantity).from(OrderMaster, "om") {
            join(OrderDetail, "od") {
                on(OrderMaster.orderId, equalTo(OrderDetail.orderId))
                and(OrderMaster.orderId, equalTo(OrderDetail.orderId))
            }
        }

        val expectedStatement = "select om.order_id, om.order_date, od.line_number, od.description, od.quantity" +
            " from OrderMaster om join OrderDetail od on om.order_id = od.order_id and om.order_id = od.order_id"
        assertThat(selectStatement.selectStatement).isEqualTo(expectedStatement)
    }

    @Test
    fun testCompoundJoin2() {
        // this is a nonsensical join, but it does test the "and" capability
        val selectStatement = select(OrderMaster.orderId, OrderMaster.orderDate, OrderDetail.lineNumber,
            OrderDetail.description, OrderDetail.quantity).from(OrderMaster, "om") {
            join(OrderDetail, "od") {
                on(OrderMaster.orderId, equalTo(OrderDetail.orderId))
                and(OrderMaster.orderId, equalTo(OrderDetail.orderId))
            }
            where(OrderMaster.orderId, isEqualTo(1))
        }

        val expectedStatement = "select om.order_id, om.order_date, od.line_number, od.description, od.quantity" +
            " from OrderMaster om join OrderDetail od on om.order_id = od.order_id and om.order_id = od.order_id" +
            " where om.order_id = #{parameters.p1,jdbcType=INTEGER}"
        assertThat(selectStatement.selectStatement).isEqualTo(expectedStatement)
    }

    @Test
    fun testMultipleTableJoinWithWhereClause() {
        newSession().use { session ->
            val mapper = session.getMapper(JoinMapper::class.java)

            val selectStatement = select(OrderMaster.orderId, OrderMaster.orderDate, OrderLine.lineNumber,
                ItemMaster.description, OrderLine.quantity).from(OrderMaster, "om") {
                join(OrderLine, "ol") {
                    on(OrderMaster.orderId, equalTo(OrderLine.orderId))
                }
                join(ItemMaster, "im") {
                    on(OrderLine.itemId, equalTo(ItemMaster.itemId))
                }
                where(OrderMaster.orderId, isEqualTo(2))
            }

            val expectedStatement = "select om.order_id, om.order_date, ol.line_number, im.description, ol.quantity" +
                " from OrderMaster om join OrderLine ol" +
                " on om.order_id = ol.order_id join ItemMaster im on ol.item_id = im.item_id" +
                " where om.order_id = #{parameters.p1,jdbcType=INTEGER}"
            assertThat(selectStatement.selectStatement).isEqualTo(expectedStatement)

            val rows = mapper.selectMany(selectStatement)

            assertThat(rows.size).isEqualTo(1)
            with(rows[0]) {
                assertThat(id).isEqualTo(2)
                assertThat(details?.size).isEqualTo(2)
                assertThat(details?.get(0)?.lineNumber).isEqualTo(1)
                assertThat(details?.get(1)?.lineNumber).isEqualTo(2)
            }
        }
    }

    @Test
    fun testFullJoinWithAliases() {
        newSession().use { session ->
            val mapper = session.getMapper(JoinMapper::class.java)

            val selectStatement = select(OrderLine.orderId, OrderLine.quantity, ItemMaster.itemId,
                ItemMaster.description).from(OrderMaster, "om") {
                join(OrderLine, "ol") {
                    on(OrderMaster.orderId, equalTo(OrderLine.orderId))
                }
                fullJoin(ItemMaster, "im") {
                    on(OrderLine.itemId, equalTo(ItemMaster.itemId))
                }
                orderBy(OrderLine.orderId, ItemMaster.itemId)
            }

            val expectedStatement = "select ol.order_id, ol.quantity, im.item_id, im.description" +
                " from OrderMaster om join OrderLine ol on om.order_id = ol.order_id" +
                " full join ItemMaster im on ol.item_id = im.item_id" +
                " order by order_id, item_id"

            assertThat(selectStatement.selectStatement).isEqualTo(expectedStatement)

            val rows = mapper.generalSelect(selectStatement)

            assertThat(rows.size).isEqualTo(6)

            with(rows[0]) {
                assertThat(get("ORDER_ID")).isNull()
                assertThat(get("QUANTITY")).isNull()
                assertThat(get("DESCRIPTION")).isEqualTo("Catcher Glove")
                assertThat(get("ITEM_ID")).isEqualTo(55)
            }

            with(rows[3]) {
                assertThat(get("ORDER_ID")).isEqualTo(2)
                assertThat(get("QUANTITY")).isEqualTo(6)
                assertThat(get("DESCRIPTION")).isNull()
                assertThat(get("ITEM_ID")).isNull()
            }

            with(rows[5]) {
                assertThat(get("ORDER_ID")).isEqualTo(2)
                assertThat(get("QUANTITY")).isEqualTo(1)
                assertThat(get("DESCRIPTION")).isEqualTo("Outfield Glove")
                assertThat(get("ITEM_ID")).isEqualTo(44)
            }
        }
    }

    @Test
    fun testFullJoinWithoutAliases() {
        newSession().use { session ->
            val mapper = session.getMapper(JoinMapper::class.java)

            val selectStatement = select(OrderLine.orderId, OrderLine.quantity, ItemMaster.itemId,
                ItemMaster.description).from(OrderMaster, "om") {
                join(OrderLine, "ol") {
                    on(OrderMaster.orderId, equalTo(OrderLine.orderId))
                }
                fullJoin(ItemMaster) {
                    on(OrderLine.itemId, equalTo(ItemMaster.itemId))
                }
                orderBy(OrderLine.orderId, ItemMaster.itemId)
            }

            val expectedStatement = "select ol.order_id, ol.quantity, ItemMaster.item_id, ItemMaster.description" +
                " from OrderMaster om join OrderLine ol on om.order_id = ol.order_id" +
                " full join ItemMaster on ol.item_id = ItemMaster.item_id" +
                " order by order_id, item_id"

            assertThat(selectStatement.selectStatement).isEqualTo(expectedStatement)

            val rows = mapper.generalSelect(selectStatement)

            assertThat(rows.size).isEqualTo(6)

            with(rows[0]) {
                assertThat(get("ORDER_ID")).isNull()
                assertThat(get("QUANTITY")).isNull()
                assertThat(get("DESCRIPTION")).isEqualTo("Catcher Glove")
                assertThat(get("ITEM_ID")).isEqualTo(55)
            }

            with(rows[3]) {
                assertThat(get("ORDER_ID")).isEqualTo(2)
                assertThat(get("QUANTITY")).isEqualTo(6)
                assertThat(get("DESCRIPTION")).isNull()
                assertThat(get("ITEM_ID")).isNull()
            }

            with(rows[5]) {
                assertThat(get("ORDER_ID")).isEqualTo(2)
                assertThat(get("QUANTITY")).isEqualTo(1)
                assertThat(get("DESCRIPTION")).isEqualTo("Outfield Glove")
                assertThat(get("ITEM_ID")).isEqualTo(44)
            }
        }
    }

    @Test
    fun testLeftJoinWithAliases() {
        newSession().use { session ->
            val mapper = session.getMapper(JoinMapper::class.java)

            val selectStatement = select(OrderLine.orderId, OrderLine.quantity, ItemMaster.itemId,
                ItemMaster.description).from(OrderMaster, "om") {
                join(OrderLine, "ol") {
                    on(OrderMaster.orderId, equalTo(OrderLine.orderId))
                }
                leftJoin(ItemMaster, "im") {
                    on(OrderLine.itemId, equalTo(ItemMaster.itemId))
                }
                orderBy(OrderLine.orderId, ItemMaster.itemId)
            }

            val expectedStatement = "select ol.order_id, ol.quantity, im.item_id, im.description" +
                " from OrderMaster om join OrderLine ol on om.order_id = ol.order_id" +
                " left join ItemMaster im on ol.item_id = im.item_id" +
                " order by order_id, item_id"

            assertThat(selectStatement.selectStatement).isEqualTo(expectedStatement)

            val rows = mapper.generalSelect(selectStatement)

            assertThat(rows.size).isEqualTo(5)

            with(rows[2]) {
                assertThat(get("ORDER_ID")).isEqualTo(2)
                assertThat(get("QUANTITY")).isEqualTo(6)
                assertThat(get("DESCRIPTION")).isNull()
                assertThat(get("ITEM_ID")).isNull()
            }

            with(rows[4]) {
                assertThat(get("ORDER_ID")).isEqualTo(2)
                assertThat(get("QUANTITY")).isEqualTo(1)
                assertThat(get("DESCRIPTION")).isEqualTo("Outfield Glove")
                assertThat(get("ITEM_ID")).isEqualTo(44)
            }
        }
    }

    @Test
    fun testLeftJoinWithoutAliases() {
        newSession().use { session ->
            val mapper = session.getMapper(JoinMapper::class.java)

            val selectStatement = select(OrderLine.orderId, OrderLine.quantity, ItemMaster.itemId,
                ItemMaster.description).from(OrderMaster, "om") {
                join(OrderLine, "ol") {
                    on(OrderMaster.orderId, equalTo(OrderLine.orderId))
                }
                leftJoin(ItemMaster) {
                    on(OrderLine.itemId, equalTo(ItemMaster.itemId))
                }
                orderBy(OrderLine.orderId, ItemMaster.itemId)
            }

            val expectedStatement = "select ol.order_id, ol.quantity, ItemMaster.item_id, ItemMaster.description" +
                " from OrderMaster om join OrderLine ol on om.order_id = ol.order_id" +
                " left join ItemMaster on ol.item_id = ItemMaster.item_id" +
                " order by order_id, item_id"

            assertThat(selectStatement.selectStatement).isEqualTo(expectedStatement)

            val rows = mapper.generalSelect(selectStatement)

            assertThat(rows.size).isEqualTo(5)

            with(rows[2]) {
                assertThat(get("ORDER_ID")).isEqualTo(2)
                assertThat(get("QUANTITY")).isEqualTo(6)
                assertThat(get("DESCRIPTION")).isNull()
                assertThat(get("ITEM_ID")).isNull()
            }

            with(rows[4]) {
                assertThat(get("ORDER_ID")).isEqualTo(2)
                assertThat(get("QUANTITY")).isEqualTo(1)
                assertThat(get("DESCRIPTION")).isEqualTo("Outfield Glove")
                assertThat(get("ITEM_ID")).isEqualTo(44)
            }
        }
    }

    @Test
    fun testRightJoinWithAliases() {
        newSession().use { session ->
            val mapper = session.getMapper(JoinMapper::class.java)

            val selectStatement = select(OrderLine.orderId, OrderLine.quantity, ItemMaster.itemId,
                ItemMaster.description).from(OrderMaster, "om") {
                join(OrderLine, "ol") {
                    on(OrderMaster.orderId, equalTo(OrderLine.orderId))
                }
                rightJoin(ItemMaster, "im") {
                    on(OrderLine.itemId, equalTo(ItemMaster.itemId))
                }
                orderBy(OrderLine.orderId, ItemMaster.itemId)
            }

            val expectedStatement = "select ol.order_id, ol.quantity, im.item_id, im.description" +
                " from OrderMaster om join OrderLine ol on om.order_id = ol.order_id" +
                " right join ItemMaster im on ol.item_id = im.item_id" +
                " order by order_id, item_id"

            assertThat(selectStatement.selectStatement).isEqualTo(expectedStatement)

            val rows = mapper.generalSelect(selectStatement)

            assertThat(rows.size).isEqualTo(5)
            with(rows[0]) {
                assertThat(get("ORDER_ID")).isNull()
                assertThat(get("QUANTITY")).isNull()
                assertThat(get("DESCRIPTION")).isEqualTo("Catcher Glove")
                assertThat(get("ITEM_ID")).isEqualTo(55)
            }

            with(rows[4]) {
                assertThat(get("ORDER_ID")).isEqualTo(2)
                assertThat(get("QUANTITY")).isEqualTo(1)
                assertThat(get("DESCRIPTION")).isEqualTo("Outfield Glove")
                assertThat(get("ITEM_ID")).isEqualTo(44)
            }
        }
    }

    @Test
    fun testRightJoinWithoutAliases() {
        newSession().use { session ->
            val mapper = session.getMapper(JoinMapper::class.java)

            val selectStatement = select(OrderLine.orderId, OrderLine.quantity, ItemMaster.itemId,
                ItemMaster.description).from(OrderMaster, "om") {
                join(OrderLine, "ol") {
                    on(OrderMaster.orderId, equalTo(OrderLine.orderId))
                }
                rightJoin(ItemMaster) {
                    on(OrderLine.itemId, equalTo(ItemMaster.itemId))
                }
                orderBy(OrderLine.orderId, ItemMaster.itemId)
            }

            val expectedStatement = "select ol.order_id, ol.quantity, ItemMaster.item_id, ItemMaster.description" +
                " from OrderMaster om join OrderLine ol on om.order_id = ol.order_id" +
                " right join ItemMaster on ol.item_id = ItemMaster.item_id" +
                " order by order_id, item_id"

            assertThat(selectStatement.selectStatement).isEqualTo(expectedStatement)

            val rows = mapper.generalSelect(selectStatement)

            assertThat(rows.size).isEqualTo(5)
            with(rows[0]) {
                assertThat(get("ORDER_ID")).isNull()
                assertThat(get("QUANTITY")).isNull()
                assertThat(get("DESCRIPTION")).isEqualTo("Catcher Glove")
                assertThat(get("ITEM_ID")).isEqualTo(55)
            }

            with(rows[4]) {
                assertThat(get("ORDER_ID")).isEqualTo(2)
                assertThat(get("QUANTITY")).isEqualTo(1)
                assertThat(get("DESCRIPTION")).isEqualTo("Outfield Glove")
                assertThat(get("ITEM_ID")).isEqualTo(44)
            }
        }
    }

    companion object {
        const val JDBC_URL = "jdbc:hsqldb:mem:aname"
        const val JDBC_DRIVER = "org.hsqldb.jdbcDriver"
    }
}
