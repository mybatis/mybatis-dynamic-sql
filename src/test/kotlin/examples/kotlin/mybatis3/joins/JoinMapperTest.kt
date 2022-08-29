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
import examples.kotlin.mybatis3.joins.OrderDetailDynamicSQLSupport.orderDetail
import examples.kotlin.mybatis3.joins.OrderLineDynamicSQLSupport.orderLine
import examples.kotlin.mybatis3.joins.OrderMasterDynamicSQLSupport.orderMaster
import examples.kotlin.mybatis3.joins.UserDynamicSQLSupport.user
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource
import org.apache.ibatis.jdbc.ScriptRunner
import org.apache.ibatis.mapping.Environment
import org.apache.ibatis.session.Configuration
import org.apache.ibatis.session.SqlSession
import org.apache.ibatis.session.SqlSessionFactoryBuilder
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.assertj.core.api.Assertions.entry
import org.junit.jupiter.api.Test
import org.mybatis.dynamic.sql.util.Messages
import org.mybatis.dynamic.sql.util.kotlin.KInvalidSQLException
import org.mybatis.dynamic.sql.util.kotlin.elements.equalTo
import org.mybatis.dynamic.sql.util.kotlin.elements.invoke
import org.mybatis.dynamic.sql.util.kotlin.elements.max
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.select
import java.io.InputStreamReader
import java.sql.DriverManager

@Suppress("LargeClass")
class JoinMapperTest {

    private fun newSession(): SqlSession {
        Class.forName(JDBC_DRIVER)
        val script = javaClass.getResourceAsStream("/examples/kotlin/mybatis3/joins/CreateJoinDB.sql")

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

            val selectStatement = select(
                orderMaster.orderId, orderMaster.orderDate,
                orderDetail.lineNumber, orderDetail.description, orderDetail.quantity
            ) {
                from(orderMaster, "om")
                join(orderDetail, "od") {
                    on(orderMaster.orderId) equalTo orderDetail.orderId
                }
            }

            val expectedStatement = "select om.order_id, om.order_date, od.line_number, od.description, od.quantity" +
                " from OrderMaster om join OrderDetail od on om.order_id = od.order_id"

            assertThat(selectStatement.selectStatement).isEqualTo(expectedStatement)

            val rows = mapper.selectMany(selectStatement)

            assertThat(rows).hasSize(2)

            with(rows[0]) {
                assertThat(id).isEqualTo(1)
                assertThat(details).hasSize(2)
                assertThat(details?.get(0)?.lineNumber).isEqualTo(1)
                assertThat(details?.get(1)?.lineNumber).isEqualTo(2)
            }

            with(rows[1]) {
                assertThat(id).isEqualTo(2)
                assertThat(details).hasSize(1)
                assertThat(details?.get(0)?.lineNumber).isEqualTo(1)
            }
        }
    }

    @Test
    fun testCompoundJoin1() {
        // this is a nonsensical join, but it does test the "and" capability
        val selectStatement = select(
            orderMaster.orderId, orderMaster.orderDate, orderDetail.lineNumber,
            orderDetail.description, orderDetail.quantity
        ) {
            from(orderMaster, "om")
            join(orderDetail, "od") {
                on(orderMaster.orderId) equalTo orderDetail.orderId
                and(orderMaster.orderId) equalTo orderDetail.orderId
            }
        }

        val expectedStatement = "select om.order_id, om.order_date, od.line_number, od.description, od.quantity" +
            " from OrderMaster om join OrderDetail od on om.order_id = od.order_id and om.order_id = od.order_id"
        assertThat(selectStatement.selectStatement).isEqualTo(expectedStatement)
    }

    @Test
    fun testCompoundJoin2() {
        // this is a nonsensical join, but it does test the "and" capability
        val selectStatement = select(
            orderMaster.orderId, orderMaster.orderDate, orderDetail.lineNumber,
            orderDetail.description, orderDetail.quantity
        ) {
            from(orderMaster, "om")
            join(orderDetail, "od") {
                on(orderMaster.orderId) equalTo orderDetail.orderId
                and(orderMaster.orderId) equalTo orderDetail.orderId
            }
            where { orderMaster.orderId isEqualTo 1 }
        }

        val expectedStatement = "select om.order_id, om.order_date, od.line_number, od.description, od.quantity" +
            " from OrderMaster om join OrderDetail od on om.order_id = od.order_id and om.order_id = od.order_id" +
            " where om.order_id = #{parameters.p1,jdbcType=INTEGER}"
        assertThat(selectStatement.selectStatement).isEqualTo(expectedStatement)
    }

    @Test
    fun testDeprecatedJoin() {
        // this is a nonsensical join, but it does test the "and" capability
        val selectStatement = select(
            orderMaster.orderId, orderMaster.orderDate, orderDetail.lineNumber,
            orderDetail.description, orderDetail.quantity
        ) {
            from(orderMaster, "om")
            join(orderDetail, "od") {
                on(orderMaster.orderId, equalTo(orderDetail.orderId))
                and(orderMaster.orderId, equalTo(orderDetail.orderId))
            }
            where { orderMaster.orderId isEqualTo 1 }
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

            val selectStatement = select(
                orderMaster.orderId, orderMaster.orderDate, orderLine.lineNumber,
                itemMaster.description, orderLine.quantity
            ) {
                from(orderMaster, "om")
                join(orderLine, "ol") {
                    on(orderMaster.orderId) equalTo orderLine.orderId
                }
                join(itemMaster, "im") {
                    on(orderLine.itemId) equalTo itemMaster.itemId
                }
                where { orderMaster.orderId isEqualTo 2 }
            }

            val expectedStatement = "select om.order_id, om.order_date, ol.line_number, im.description, ol.quantity" +
                " from OrderMaster om join OrderLine ol" +
                " on om.order_id = ol.order_id join ItemMaster im on ol.item_id = im.item_id" +
                " where om.order_id = #{parameters.p1,jdbcType=INTEGER}"
            assertThat(selectStatement.selectStatement).isEqualTo(expectedStatement)

            val rows = mapper.selectMany(selectStatement)

            assertThat(rows).hasSize(1)
            with(rows[0]) {
                assertThat(id).isEqualTo(2)
                assertThat(details).hasSize(2)
                assertThat(details?.get(0)?.lineNumber).isEqualTo(1)
                assertThat(details?.get(1)?.lineNumber).isEqualTo(2)
            }
        }
    }

    @Test
    fun testFullJoinWithAliases() {
        newSession().use { session ->
            val mapper = session.getMapper(JoinMapper::class.java)

            val selectStatement = select(
                orderLine.orderId, orderLine.quantity, itemMaster.itemId, itemMaster.description
            ) {
                from(orderMaster, "om")
                join(orderLine, "ol") {
                    on(orderMaster.orderId) equalTo orderLine.orderId
                }
                fullJoin(itemMaster, "im") {
                    on(orderLine.itemId) equalTo itemMaster.itemId
                }
                orderBy(orderLine.orderId, itemMaster.itemId)
            }

            val expectedStatement = "select ol.order_id, ol.quantity, im.item_id, im.description" +
                " from OrderMaster om join OrderLine ol on om.order_id = ol.order_id" +
                " full join ItemMaster im on ol.item_id = im.item_id" +
                " order by order_id, item_id"

            assertThat(selectStatement.selectStatement).isEqualTo(expectedStatement)

            data class OrderDetail(val itemId: Int?, val orderId: Int?, val quantity: Int?, val description: String?)

            val rows = mapper.selectMany(selectStatement) {
                OrderDetail(
                    it["ITEM_ID"] as Int?,
                    it["ORDER_ID"] as Int?,
                    it["QUANTITY"] as Int?,
                    it["DESCRIPTION"] as String?
                )
            }

            assertThat(rows).hasSize(6)

            with(rows[0]) {
                assertThat(itemId).isEqualTo(55)
                assertThat(orderId).isNull()
                assertThat(quantity).isNull()
                assertThat(description).isEqualTo("Catcher Glove")
            }

            with(rows[3]) {
                assertThat(itemId).isNull()
                assertThat(orderId).isEqualTo(2)
                assertThat(quantity).isEqualTo(6)
                assertThat(description).isNull()
            }

            with(rows[5]) {
                assertThat(itemId).isEqualTo(44)
                assertThat(orderId).isEqualTo(2)
                assertThat(quantity).isEqualTo(1)
                assertThat(description).isEqualTo("Outfield Glove")
            }
        }
    }

    @Test
    @Suppress("LongMethod")
    fun testFullJoinWithSubQuery() {
        newSession().use { session ->
            val mapper = session.getMapper(JoinMapper::class.java)

            val selectStatement = select(
                "ol"(orderLine.orderId), orderLine.quantity, "im"(itemMaster.itemId),
                itemMaster.description
            ) {
                from {
                    select(orderMaster.allColumns()) {
                        from(orderMaster)
                    }
                    + "om"
                }
                join(
                    subQuery = {
                        select(orderLine.allColumns()) {
                            from(orderLine)
                        }
                        + "ol"
                    },
                    joinCriteria = {
                        on("om"(orderMaster.orderId)) equalTo
                                "ol"(orderLine.orderId)
                    }
                )
                fullJoin(
                    {
                        select(itemMaster.allColumns()) {
                            from(itemMaster)
                        }
                        + "im"
                    }
                ) {
                    on("ol"(orderLine.itemId)) equalTo
                            "im"(itemMaster.itemId)
                }
                orderBy(orderLine.orderId, itemMaster.itemId)
            }

            val expectedStatement = "select ol.order_id, quantity, im.item_id, description" +
                " from (select * from OrderMaster) om" +
                " join (select * from OrderLine) ol on om.order_id = ol.order_id" +
                " full join (select * from ItemMaster) im on ol.item_id = im.item_id" +
                " order by order_id, item_id"

            assertThat(selectStatement.selectStatement).isEqualTo(expectedStatement)

            data class OrderDetail(val itemId: Int?, val orderId: Int?, val quantity: Int?, val description: String?)

            val rows = mapper.selectMany(selectStatement) {
                OrderDetail(
                    it["ITEM_ID"] as Int?,
                    it["ORDER_ID"] as Int?,
                    it["QUANTITY"] as Int?,
                    it["DESCRIPTION"] as String?
                )
            }

            assertThat(rows).hasSize(6)

            with(rows[0]) {
                assertThat(itemId).isEqualTo(55)
                assertThat(orderId).isNull()
                assertThat(quantity).isNull()
                assertThat(description).isEqualTo("Catcher Glove")
            }

            with(rows[3]) {
                assertThat(itemId).isNull()
                assertThat(orderId).isEqualTo(2)
                assertThat(quantity).isEqualTo(6)
                assertThat(description).isNull()
            }

            with(rows[5]) {
                assertThat(itemId).isEqualTo(44)
                assertThat(orderId).isEqualTo(2)
                assertThat(quantity).isEqualTo(1)
                assertThat(description).isEqualTo("Outfield Glove")
            }
        }
    }

    @Test
    fun testFullJoinWithoutAliases() {
        newSession().use { session ->
            val mapper = session.getMapper(JoinMapper::class.java)

            val selectStatement = select(
                orderLine.orderId, orderLine.quantity, itemMaster.itemId, itemMaster.description
            ) {
                from(orderMaster, "om")
                join(orderLine, "ol") {
                    on(orderMaster.orderId) equalTo orderLine.orderId
                }
                fullJoin(itemMaster) {
                    on(orderLine.itemId) equalTo itemMaster.itemId
                }
                orderBy(orderLine.orderId, itemMaster.itemId)
            }

            val expectedStatement = "select ol.order_id, ol.quantity, ItemMaster.item_id, ItemMaster.description" +
                " from OrderMaster om join OrderLine ol on om.order_id = ol.order_id" +
                " full join ItemMaster on ol.item_id = ItemMaster.item_id" +
                " order by order_id, item_id"

            assertThat(selectStatement.selectStatement).isEqualTo(expectedStatement)

            val rows = mapper.selectManyMappedRows(selectStatement)

            assertThat(rows).hasSize(6)

            assertThat(rows[0]).containsExactly(
                entry("DESCRIPTION", "Catcher Glove"),
                entry("ITEM_ID", 55)
            )

            assertThat(rows[3]).containsExactly(
                entry("ORDER_ID", 2),
                entry("QUANTITY", 6)
            )

            assertThat(rows[5]).containsExactly(
                entry("ORDER_ID", 2),
                entry("QUANTITY", 1),
                entry("DESCRIPTION", "Outfield Glove"),
                entry("ITEM_ID", 44)
            )
        }
    }

    @Test
    fun testLeftJoinWithAliases() {
        newSession().use { session ->
            val mapper = session.getMapper(JoinMapper::class.java)

            val selectStatement = select(
                orderLine.orderId, orderLine.quantity, itemMaster.itemId, itemMaster.description
            ) {
                from(orderMaster, "om")
                join(orderLine, "ol") {
                    on(orderMaster.orderId) equalTo orderLine.orderId
                }
                leftJoin(itemMaster, "im") {
                    on(orderLine.itemId) equalTo itemMaster.itemId
                }
                orderBy(orderLine.orderId, itemMaster.itemId)
            }

            val expectedStatement = "select ol.order_id, ol.quantity, im.item_id, im.description" +
                " from OrderMaster om join OrderLine ol on om.order_id = ol.order_id" +
                " left join ItemMaster im on ol.item_id = im.item_id" +
                " order by order_id, item_id"

            assertThat(selectStatement.selectStatement).isEqualTo(expectedStatement)

            val rows = mapper.selectManyMappedRows(selectStatement)

            assertThat(rows).hasSize(5)

            assertThat(rows[2]).containsExactly(
                entry("ORDER_ID", 2),
                entry("QUANTITY", 6)
            )

            assertThat(rows[4]).containsExactly(
                entry("ORDER_ID", 2),
                entry("QUANTITY", 1),
                entry("DESCRIPTION", "Outfield Glove"),
                entry("ITEM_ID", 44)
            )
        }
    }

    @Test
    fun testLeftJoinWithSubQuery() {
        newSession().use { session ->
            val mapper = session.getMapper(JoinMapper::class.java)

            val selectStatement = select(
                orderLine.orderId, orderLine.quantity, "im"(itemMaster.itemId),
                itemMaster.description
            ) {
                from(orderMaster, "om")
                join(orderLine, "ol") {
                    on(orderMaster.orderId) equalTo orderLine.orderId
                }
                leftJoin(
                    {
                        select(itemMaster.allColumns()) {
                            from(itemMaster)
                        }
                        + "im"
                    }
                ) {
                    on(orderLine.itemId) equalTo "im"(itemMaster.itemId)
                }
                orderBy(orderLine.orderId, itemMaster.itemId)
            }

            val expectedStatement = "select ol.order_id, ol.quantity, im.item_id, description" +
                " from OrderMaster om join OrderLine ol on om.order_id = ol.order_id" +
                " left join (select * from ItemMaster) im on ol.item_id = im.item_id" +
                " order by order_id, item_id"

            assertThat(selectStatement.selectStatement).isEqualTo(expectedStatement)

            val rows = mapper.selectManyMappedRows(selectStatement)

            assertThat(rows).hasSize(5)

            assertThat(rows[2]).containsExactly(
                entry("ORDER_ID", 2),
                entry("QUANTITY", 6)
            )

            assertThat(rows[4]).containsExactly(
                entry("ORDER_ID", 2),
                entry("QUANTITY", 1),
                entry("DESCRIPTION", "Outfield Glove"),
                entry("ITEM_ID", 44)
            )
        }
    }

    @Test
    fun testLeftJoinWithoutAliases() {
        newSession().use { session ->
            val mapper = session.getMapper(JoinMapper::class.java)

            val selectStatement = select(
                orderLine.orderId, orderLine.quantity, itemMaster.itemId, itemMaster.description
            ) {
                from(orderMaster, "om")
                join(orderLine, "ol") {
                    on(orderMaster.orderId) equalTo orderLine.orderId
                }
                leftJoin(itemMaster) {
                    on(orderLine.itemId)  equalTo itemMaster.itemId
                }
                orderBy(orderLine.orderId, itemMaster.itemId)
            }

            val expectedStatement = "select ol.order_id, ol.quantity, ItemMaster.item_id, ItemMaster.description" +
                " from OrderMaster om join OrderLine ol on om.order_id = ol.order_id" +
                " left join ItemMaster on ol.item_id = ItemMaster.item_id" +
                " order by order_id, item_id"

            assertThat(selectStatement.selectStatement).isEqualTo(expectedStatement)

            val rows = mapper.selectManyMappedRows(selectStatement)

            assertThat(rows).hasSize(5)

            assertThat(rows[2]).containsExactly(
                entry("ORDER_ID", 2),
                entry("QUANTITY", 6)
            )

            assertThat(rows[4]).containsExactly(
                entry("ORDER_ID", 2),
                entry("QUANTITY", 1),
                entry("DESCRIPTION", "Outfield Glove"),
                entry("ITEM_ID", 44)
            )
        }
    }

    @Test
    fun testRightJoinWithAliases() {
        newSession().use { session ->
            val mapper = session.getMapper(JoinMapper::class.java)

            val selectStatement = select(
                orderLine.orderId, orderLine.quantity, itemMaster.itemId, itemMaster.description
            ) {
                from(orderMaster, "om")
                join(orderLine, "ol") {
                    on(orderMaster.orderId) equalTo orderLine.orderId
                }
                rightJoin(itemMaster, "im") {
                    on(orderLine.itemId) equalTo itemMaster.itemId
                }
                orderBy(orderLine.orderId, itemMaster.itemId)
            }

            val expectedStatement = "select ol.order_id, ol.quantity, im.item_id, im.description" +
                " from OrderMaster om join OrderLine ol on om.order_id = ol.order_id" +
                " right join ItemMaster im on ol.item_id = im.item_id" +
                " order by order_id, item_id"

            assertThat(selectStatement.selectStatement).isEqualTo(expectedStatement)

            val rows = mapper.selectManyMappedRows(selectStatement)

            assertThat(rows).hasSize(5)

            assertThat(rows[0]).containsExactly(
                entry("DESCRIPTION", "Catcher Glove"),
                entry("ITEM_ID", 55)
            )

            assertThat(rows[4]).containsExactly(
                entry("ORDER_ID", 2),
                entry("QUANTITY", 1),
                entry("DESCRIPTION", "Outfield Glove"),
                entry("ITEM_ID", 44)
            )
        }
    }

    @Test
    fun testRightJoinWithSubQuery() {
        newSession().use { session ->
            val mapper = session.getMapper(JoinMapper::class.java)

            val selectStatement = select(
                orderLine.orderId, orderLine.quantity,
                "im"(itemMaster.itemId), itemMaster.description
            ) {
                from(orderMaster, "om")
                join(orderLine, "ol") {
                    on(orderMaster.orderId) equalTo orderLine.orderId
                }
                rightJoin(
                    {
                        select(itemMaster.allColumns()) {
                            from(itemMaster)
                        }
                        + "im"
                    }
                ) {
                    on(orderLine.itemId) equalTo "im"(itemMaster.itemId)
                }
                orderBy(orderLine.orderId, itemMaster.itemId)
            }

            val expectedStatement = "select ol.order_id, ol.quantity, im.item_id, description" +
                " from OrderMaster om join OrderLine ol on om.order_id = ol.order_id" +
                " right join (select * from ItemMaster) im on ol.item_id = im.item_id" +
                " order by order_id, item_id"

            assertThat(selectStatement.selectStatement).isEqualTo(expectedStatement)

            val rows = mapper.selectManyMappedRows(selectStatement)

            assertThat(rows).hasSize(5)

            assertThat(rows[0]).containsExactly(
                entry("DESCRIPTION", "Catcher Glove"),
                entry("ITEM_ID", 55)
            )

            assertThat(rows[4]).containsExactly(
                entry("ORDER_ID", 2),
                entry("QUANTITY", 1),
                entry("DESCRIPTION", "Outfield Glove"),
                entry("ITEM_ID", 44)
            )
        }
    }

    @Test
    fun testRightJoinWithoutAliases() {
        newSession().use { session ->
            val mapper = session.getMapper(JoinMapper::class.java)

            val selectStatement = select(
                orderLine.orderId, orderLine.quantity, itemMaster.itemId, itemMaster.description
            ) {
                from(orderMaster, "om")
                join(orderLine, "ol") {
                    on(orderMaster.orderId) equalTo orderLine.orderId
                }
                rightJoin(itemMaster) {
                    on(orderLine.itemId) equalTo itemMaster.itemId
                }
                orderBy(orderLine.orderId, itemMaster.itemId)
            }

            val expectedStatement = "select ol.order_id, ol.quantity, ItemMaster.item_id, ItemMaster.description" +
                " from OrderMaster om join OrderLine ol on om.order_id = ol.order_id" +
                " right join ItemMaster on ol.item_id = ItemMaster.item_id" +
                " order by order_id, item_id"

            assertThat(selectStatement.selectStatement).isEqualTo(expectedStatement)

            val rows = mapper.selectManyMappedRows(selectStatement)

            assertThat(rows).hasSize(5)

            assertThat(rows[0]).containsExactly(
                entry("DESCRIPTION", "Catcher Glove"),
                entry("ITEM_ID", 55)
            )

            assertThat(rows[4]).containsExactly(
                entry("ORDER_ID", 2),
                entry("QUANTITY", 1),
                entry("DESCRIPTION", "Outfield Glove"),
                entry("ITEM_ID", 44)
            )
        }
    }

    @Test
    fun testSelf() {
        newSession().use { session ->
            val mapper = session.getMapper(JoinMapper::class.java)

            // create second table instance for self-join
            val user2 = UserDynamicSQLSupport.User()

            // get Bamm Bamm's parent - should be Barney
            val selectStatement = select(user.userId, user.userName, user.parentId) {
                from(user, "u1")
                join(user2, "u2") {
                    on(user.userId) equalTo user2.parentId
                }
                where { user2.userId isEqualTo 4 }
            }

            val expectedStatement = "select u1.user_id, u1.user_name, u1.parent_id" +
                    " from User u1 join User u2 on u1.user_id = u2.parent_id" +
                    " where u2.user_id = #{parameters.p1,jdbcType=INTEGER}"
            assertThat(selectStatement.selectStatement).isEqualTo(expectedStatement)
            val rows = mapper.selectManyMappedRows(selectStatement)

            assertThat(rows).hasSize(1)
            assertThat(rows[0]).containsExactly(
                entry("USER_ID", 2),
                entry("USER_NAME", "Barney"),
            )
        }
    }

    @Test
    fun testSelfWithNewAlias() {
        newSession().use { session ->
            val mapper = session.getMapper(JoinMapper::class.java)

            // create second table instance for self-join
            val user2 = user.withAlias("u2")

            // get Bamm Bamm's parent - should be Barney
            val selectStatement = select(user.userId, user.userName, user.parentId) {
                from(user)
                join(user2) {
                    on(user.userId) equalTo user2.parentId
                }
                where { user2.userId isEqualTo 4 }
            }

            val expectedStatement = "select User.user_id, User.user_name, User.parent_id" +
                    " from User join User u2 on User.user_id = u2.parent_id" +
                    " where u2.user_id = #{parameters.p1,jdbcType=INTEGER}"
            assertThat(selectStatement.selectStatement).isEqualTo(expectedStatement)

            val rows = mapper.selectManyMappedRows(selectStatement)
            assertThat(rows).hasSize(1)

            assertThat(rows[0]).containsExactly(
                entry("USER_ID", 2),
                entry("USER_NAME", "Barney"),
            )
        }
    }

    @Test
    fun testSelfWithNewAliasAndOverride() {
        newSession().use { session ->
            val mapper = session.getMapper(JoinMapper::class.java)

            // create second table instance for self-join
            val user2 = user.withAlias("other_user")

            // get Bamm Bamm's parent - should be Barney
            val selectStatement = select(user.userId, user.userName, user.parentId) {
                from(user, "u1")
                join(user2, "u2") {
                    on(user.userId) equalTo user2.parentId
                }
                where { user2.userId isEqualTo 4 }
            }

            val expectedStatement = "select u1.user_id, u1.user_name, u1.parent_id" +
                    " from User u1 join User u2 on u1.user_id = u2.parent_id" +
                    " where u2.user_id = #{parameters.p1,jdbcType=INTEGER}"
            assertThat(selectStatement.selectStatement).isEqualTo(expectedStatement)
            val rows = mapper.selectManyMappedRows(selectStatement)
            assertThat(rows).hasSize(1)

            assertThat(rows[0]).containsExactly(
                entry("USER_ID", 2),
                entry("USER_NAME", "Barney"),
            )
        }
    }

    @Test
    fun testJoinWithNoOnCondition() {
        // create second table instance for self-join
        val user2 = user.withAlias("other_user")

        assertThatExceptionOfType(KInvalidSQLException::class.java).isThrownBy {
            select(user.userId, user.userName, user.parentId) {
                from(user, "u1")
                join(user2, "u2") {
                    and(user.userId) equalTo user2.parentId
                }
                where { user2.userId isEqualTo 4 }
            }
        }.withMessage(Messages.getString("ERROR.22")) //$NON-NLS-1$
    }

    @Test
    fun testThatAliasesPropagateToSubQueryConditions() {
        newSession().use { session ->
            val mapper = session.getMapper(JoinMapper::class.java)

            val orderLine2 = OrderLineDynamicSQLSupport.OrderLine()

            val selectStatement = select(orderLine.orderId, orderLine.lineNumber) {
                from(orderLine, "ol")
                where {
                    orderLine.lineNumber isEqualTo  {
                        select(max(orderLine2.lineNumber)) {
                            from(orderLine2, "ol2")
                            where { orderLine2.orderId isEqualTo orderLine.orderId }
                        }
                    }
                }
                orderBy(orderLine.orderId)
            }

            val expectedStatement = "select ol.order_id, ol.line_number " +
                    "from OrderLine ol " +
                    "where ol.line_number = " +
                    "(select max(ol2.line_number) from OrderLine ol2 where ol2.order_id = ol.order_id) " +
                    "order by order_id"

            assertThat(selectStatement.selectStatement).isEqualTo(expectedStatement)

            val rows = mapper.selectManyMappedRows(selectStatement)

            assertThat(rows).hasSize(2)

            assertThat(rows[0]).containsOnly(
                entry("ORDER_ID", 1),
                entry("LINE_NUMBER", 2)
            )

            assertThat(rows[1]).containsOnly(
                entry("ORDER_ID", 2),
                entry("LINE_NUMBER", 3)
            )
        }
    }

    companion object {
        const val JDBC_URL = "jdbc:hsqldb:mem:aname"
        const val JDBC_DRIVER = "org.hsqldb.jdbcDriver"
    }
}
