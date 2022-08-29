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
package examples.joins;

import static examples.joins.ItemMasterDynamicSQLSupport.itemMaster;
import static examples.joins.OrderDetailDynamicSQLSupport.*;
import static examples.joins.OrderLineDynamicSQLSupport.orderLine;
import static examples.joins.OrderMasterDynamicSQLSupport.orderDate;
import static examples.joins.OrderMasterDynamicSQLSupport.orderMaster;
import static examples.joins.UserDynamicSQLSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.exception.DuplicateTableAliasException;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.QueryExpressionDSL;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.Messages;
import org.mybatis.dynamic.sql.util.mybatis3.CommonSelectMapper;

class JoinMapperTest {

    private static final String JDBC_URL = "jdbc:hsqldb:mem:aname";
    private static final String JDBC_DRIVER = "org.hsqldb.jdbcDriver";

    private SqlSessionFactory sqlSessionFactory;

    @BeforeEach
    void setup() throws Exception {
        Class.forName(JDBC_DRIVER);
        InputStream is = getClass().getResourceAsStream("/examples/joins/CreateJoinDB.sql");
        assert is != null;
        try (Connection connection = DriverManager.getConnection(JDBC_URL, "sa", "")) {
            ScriptRunner sr = new ScriptRunner(connection);
            sr.setLogWriter(null);
            sr.runScript(new InputStreamReader(is));
        }

        UnpooledDataSource ds = new UnpooledDataSource(JDBC_DRIVER, JDBC_URL, "sa", "");
        Environment environment = new Environment("test", new JdbcTransactionFactory(), ds);
        Configuration config = new Configuration(environment);
        config.addMapper(JoinMapper.class);
        config.addMapper(CommonSelectMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(config);
    }

    @Test
    void testSingleTableJoin1() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            JoinMapper mapper = session.getMapper(JoinMapper.class);

            SelectStatementProvider selectStatement = select(orderMaster.orderId, orderDate, orderDetail.lineNumber, orderDetail.description, orderDetail.quantity)
                    .from(orderMaster, "om")
                    .join(orderDetail, "od").on(orderMaster.orderId, equalTo(orderDetail.orderId))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "select om.order_id, om.order_date, od.line_number, od.description, od.quantity"
                    + " from OrderMaster om join OrderDetail od on om.order_id = od.order_id";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatement);

            List<OrderMaster> rows = mapper.selectMany(selectStatement);

            assertThat(rows).hasSize(2);
            OrderMaster orderMaster = rows.get(0);
            assertThat(orderMaster.getId()).isEqualTo(1);
            assertThat(orderMaster.getDetails()).hasSize(2);
            OrderDetail orderDetail = orderMaster.getDetails().get(0);
            assertThat(orderDetail.getLineNumber()).isEqualTo(1);
            orderDetail = orderMaster.getDetails().get(1);
            assertThat(orderDetail.getLineNumber()).isEqualTo(2);

            orderMaster = rows.get(1);
            assertThat(orderMaster.getId()).isEqualTo(2);
            assertThat(orderMaster.getDetails()).hasSize(1);
            orderDetail = orderMaster.getDetails().get(0);
            assertThat(orderDetail.getLineNumber()).isEqualTo(1);
        }
    }

    @Test
    void testSingleTableJoin2() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            JoinMapper mapper = session.getMapper(JoinMapper.class);

            SelectStatementProvider selectStatement = select(orderMaster.orderId, orderDate, orderDetail.lineNumber, orderDetail.description, orderDetail.quantity)
                    .from(orderMaster, "om")
                    .join(orderDetail, "od", on(orderMaster.orderId, equalTo(orderDetail.orderId)))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "select om.order_id, om.order_date, od.line_number, od.description, od.quantity"
                    + " from OrderMaster om join OrderDetail od on om.order_id = od.order_id";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatement);

            List<OrderMaster> rows = mapper.selectMany(selectStatement);

            assertThat(rows).hasSize(2);
            OrderMaster orderMaster = rows.get(0);
            assertThat(orderMaster.getId()).isEqualTo(1);
            assertThat(orderMaster.getDetails()).hasSize(2);
            OrderDetail orderDetail = orderMaster.getDetails().get(0);
            assertThat(orderDetail.getLineNumber()).isEqualTo(1);
            orderDetail = orderMaster.getDetails().get(1);
            assertThat(orderDetail.getLineNumber()).isEqualTo(2);

            orderMaster = rows.get(1);
            assertThat(orderMaster.getId()).isEqualTo(2);
            assertThat(orderMaster.getDetails()).hasSize(1);
            orderDetail = orderMaster.getDetails().get(0);
            assertThat(orderDetail.getLineNumber()).isEqualTo(1);
        }
    }

    @Test
    void testCompoundJoin1() {
        // this is a nonsensical join, but it does test the "and" capability
        SelectStatementProvider selectStatement = select(orderMaster.orderId, orderDate, orderDetail.lineNumber, orderDetail.description, orderDetail.quantity)
                .from(orderMaster, "om")
                .join(orderDetail, "od").on(orderMaster.orderId, equalTo(orderDetail.orderId), and(orderMaster.orderId, equalTo(orderDetail.orderId)))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expectedStatement = "select om.order_id, om.order_date, od.line_number, od.description, od.quantity"
                + " from OrderMaster om join OrderDetail od on om.order_id = od.order_id and om.order_id = od.order_id";
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatement);
    }

    @Test
    void testCompoundJoin2() {
        // this is a nonsensical join, but it does test the "and" capability
        SelectStatementProvider selectStatement = select(orderMaster.orderId, orderDate, orderDetail.lineNumber, orderDetail.description, orderDetail.quantity)
                .from(orderMaster, "om")
                .join(orderDetail, "od").on(orderMaster.orderId, equalTo(orderDetail.orderId))
                .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
                .and(orderMaster.orderId, equalTo(orderDetail.orderId))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expectedStatement = "select om.order_id, om.order_date, od.line_number, od.description, od.quantity"
                + " from OrderMaster om join OrderDetail od on om.order_id = od.order_id and om.order_id = od.order_id";
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatement);
    }

    @Test
    void testCompoundJoin3() {
        // this is a nonsensical join, but it does test the "and" capability
        SelectStatementProvider selectStatement = select(orderMaster.orderId, orderDate, orderDetail.lineNumber, orderDetail.description, orderDetail.quantity)
                .from(orderMaster, "om")
                .join(orderDetail, "od", on(orderMaster.orderId, equalTo(orderDetail.orderId)), and(orderMaster.orderId, equalTo(orderDetail.orderId)))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expectedStatement = "select om.order_id, om.order_date, od.line_number, od.description, od.quantity"
                + " from OrderMaster om join OrderDetail od on om.order_id = od.order_id and om.order_id = od.order_id";
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatement);
    }

    @Test
    void testCompoundJoin4() {
        // this is a nonsensical join, but it does test the "and" capability
        SelectStatementProvider selectStatement = select(orderMaster.orderId, orderDate, orderDetail.lineNumber, orderDetail.description, orderDetail.quantity)
                .from(orderMaster, "om")
                .leftJoin(orderDetail, "od", on(orderMaster.orderId, equalTo(orderDetail.orderId)), and(orderMaster.orderId, equalTo(orderDetail.orderId)))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expectedStatement = "select om.order_id, om.order_date, od.line_number, od.description, od.quantity"
                + " from OrderMaster om left join OrderDetail od on om.order_id = od.order_id and om.order_id = od.order_id";
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatement);
    }

    @Test
    void testCompoundJoin5() {
        // this is a nonsensical join, but it does test the "and" capability
        SelectStatementProvider selectStatement = select(orderMaster.orderId, orderDate, orderDetail.lineNumber, orderDetail.description, orderDetail.quantity)
                .from(orderMaster, "om")
                .rightJoin(orderDetail, "od", on(orderMaster.orderId, equalTo(orderDetail.orderId)), and(orderMaster.orderId, equalTo(orderDetail.orderId)))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expectedStatement = "select om.order_id, om.order_date, od.line_number, od.description, od.quantity"
                + " from OrderMaster om right join OrderDetail od on om.order_id = od.order_id and om.order_id = od.order_id";
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatement);
    }

    @Test
    void testCompoundJoin6() {
        // this is a nonsensical join, but it does test the "and" capability
        SelectStatementProvider selectStatement = select(orderMaster.orderId, orderDate, orderDetail.lineNumber, orderDetail.description, orderDetail.quantity)
                .from(orderMaster, "om")
                .fullJoin(orderDetail, "od", on(orderMaster.orderId, equalTo(orderDetail.orderId)), and(orderMaster.orderId, equalTo(orderDetail.orderId)))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expectedStatement = "select om.order_id, om.order_date, od.line_number, od.description, od.quantity"
                + " from OrderMaster om full join OrderDetail od on om.order_id = od.order_id and om.order_id = od.order_id";
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatement);
    }

    @Test
    void testMultipleTableJoinWithWhereClause() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            JoinMapper mapper = session.getMapper(JoinMapper.class);

            SelectStatementProvider selectStatement = select(orderMaster.orderId, orderDate, orderLine.lineNumber, itemMaster.description, orderLine.quantity)
                    .from(orderMaster, "om")
                    .join(orderLine, "ol").on(orderMaster.orderId, equalTo(orderLine.orderId))
                    .join(itemMaster, "im").on(orderLine.itemId, equalTo(itemMaster.itemId))
                    .where(orderMaster.orderId, isEqualTo(2))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "select om.order_id, om.order_date, ol.line_number, im.description, ol.quantity"
                    + " from OrderMaster om join OrderLine ol on om.order_id = ol.order_id join ItemMaster im on ol.item_id = im.item_id"
                    + " where om.order_id = #{parameters.p1,jdbcType=INTEGER}";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatement);

            List<OrderMaster> rows = mapper.selectMany(selectStatement);

            assertThat(rows).hasSize(1);
            OrderMaster orderMaster = rows.get(0);
            assertThat(orderMaster.getId()).isEqualTo(2);
            assertThat(orderMaster.getDetails()).hasSize(2);
            OrderDetail orderDetail = orderMaster.getDetails().get(0);
            assertThat(orderDetail.getLineNumber()).isEqualTo(1);
            orderDetail = orderMaster.getDetails().get(1);
            assertThat(orderDetail.getLineNumber()).isEqualTo(2);
        }
    }

    @Test
    void testMultipleTableJoinWithApplyWhere() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            JoinMapper mapper = session.getMapper(JoinMapper.class);

            SelectStatementProvider selectStatement = select(orderMaster.orderId, orderDate, orderLine.lineNumber, itemMaster.description, orderLine.quantity)
                    .from(orderMaster, "om")
                    .join(orderLine, "ol").on(orderMaster.orderId, equalTo(orderLine.orderId))
                    .join(itemMaster, "im").on(orderLine.itemId, equalTo(itemMaster.itemId))
                    .applyWhere(d -> d.where(orderMaster.orderId, isEqualTo(2)))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "select om.order_id, om.order_date, ol.line_number, im.description, ol.quantity"
                    + " from OrderMaster om join OrderLine ol on om.order_id = ol.order_id join ItemMaster im on ol.item_id = im.item_id"
                    + " where om.order_id = #{parameters.p1,jdbcType=INTEGER}";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatement);

            List<OrderMaster> rows = mapper.selectMany(selectStatement);

            assertThat(rows).hasSize(1);
            OrderMaster orderMaster = rows.get(0);
            assertThat(orderMaster.getId()).isEqualTo(2);
            assertThat(orderMaster.getDetails()).hasSize(2);
            OrderDetail orderDetail = orderMaster.getDetails().get(0);
            assertThat(orderDetail.getLineNumber()).isEqualTo(1);
            orderDetail = orderMaster.getDetails().get(1);
            assertThat(orderDetail.getLineNumber()).isEqualTo(2);
        }
    }

    @Test
    void testMultipleTableJoinWithComplexWhereClause() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            JoinMapper mapper = session.getMapper(JoinMapper.class);

            SelectStatementProvider selectStatement = select(orderMaster.orderId, orderDate, orderLine.lineNumber, itemMaster.description, orderLine.quantity)
                    .from(orderMaster, "om")
                    .join(orderLine, "ol").on(orderMaster.orderId, equalTo(orderLine.orderId))
                    .join(itemMaster, "im").on(orderLine.itemId, equalTo(itemMaster.itemId))
                    .where(orderMaster.orderId, isEqualTo(2), and(orderLine.lineNumber, isEqualTo(2)))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "select om.order_id, om.order_date, ol.line_number, im.description, ol.quantity"
                    + " from OrderMaster om join OrderLine ol on om.order_id = ol.order_id join ItemMaster im on ol.item_id = im.item_id"
                    + " where (om.order_id = #{parameters.p1,jdbcType=INTEGER} and ol.line_number = #{parameters.p2,jdbcType=INTEGER})";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatement);

            List<OrderMaster> rows = mapper.selectMany(selectStatement);

            assertThat(rows).hasSize(1);
            OrderMaster orderMaster = rows.get(0);
            assertThat(orderMaster.getId()).isEqualTo(2);
            assertThat(orderMaster.getDetails()).hasSize(1);
            OrderDetail orderDetail = orderMaster.getDetails().get(0);
            assertThat(orderDetail.getLineNumber()).isEqualTo(2);
        }
    }

    @Test
    void testMultipleTableJoinWithOrderBy() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            JoinMapper mapper = session.getMapper(JoinMapper.class);

            SelectStatementProvider selectStatement = select(orderMaster.orderId, orderDate, orderLine.lineNumber, itemMaster.description, orderLine.quantity)
                    .from(orderMaster, "om")
                    .join(orderLine, "ol").on(orderMaster.orderId, equalTo(orderLine.orderId))
                    .join(itemMaster, "im").on(orderLine.itemId, equalTo(itemMaster.itemId))
                    .orderBy(orderMaster.orderId)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "select om.order_id, om.order_date, ol.line_number, im.description, ol.quantity"
                    + " from OrderMaster om join OrderLine ol on om.order_id = ol.order_id join ItemMaster im on ol.item_id = im.item_id"
                    + " order by order_id";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatement);

            List<OrderMaster> rows = mapper.selectMany(selectStatement);

            assertThat(rows).hasSize(2);
            OrderMaster orderMaster = rows.get(0);
            assertThat(orderMaster.getId()).isEqualTo(1);
            assertThat(orderMaster.getDetails()).hasSize(1);
            OrderDetail orderDetail = orderMaster.getDetails().get(0);
            assertThat(orderDetail.getLineNumber()).isEqualTo(1);

            orderMaster = rows.get(1);
            assertThat(orderMaster.getId()).isEqualTo(2);
            assertThat(orderMaster.getDetails()).hasSize(2);
            orderDetail = orderMaster.getDetails().get(0);
            assertThat(orderDetail.getLineNumber()).isEqualTo(1);
            orderDetail = orderMaster.getDetails().get(1);
            assertThat(orderDetail.getLineNumber()).isEqualTo(2);
        }
    }

    @Test
    void testMultipleTableJoinNoAliasWithOrderBy() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            JoinMapper mapper = session.getMapper(JoinMapper.class);

            SelectStatementProvider selectStatement = select(orderMaster.orderId, orderDate, orderLine.lineNumber, itemMaster.description, orderLine.quantity)
                    .from(orderMaster)
                    .join(orderLine).on(orderMaster.orderId, equalTo(orderLine.orderId))
                    .join(itemMaster).on(orderLine.itemId, equalTo(itemMaster.itemId))
                    .where(orderMaster.orderId, isEqualTo(2))
                    .orderBy(orderMaster.orderId)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "select OrderMaster.order_id, OrderMaster.order_date, OrderLine.line_number, ItemMaster.description, OrderLine.quantity"
                    + " from OrderMaster join OrderLine on OrderMaster.order_id = OrderLine.order_id join ItemMaster on OrderLine.item_id = ItemMaster.item_id"
                    + " where OrderMaster.order_id = #{parameters.p1,jdbcType=INTEGER}"
                    + " order by order_id";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatement);

            List<OrderMaster> rows = mapper.selectMany(selectStatement);

            assertThat(rows).hasSize(1);
            OrderMaster orderMaster = rows.get(0);
            assertThat(orderMaster.getId()).isEqualTo(2);
            assertThat(orderMaster.getDetails()).hasSize(2);
            OrderDetail orderDetail = orderMaster.getDetails().get(0);
            assertThat(orderDetail.getLineNumber()).isEqualTo(1);
            orderDetail = orderMaster.getDetails().get(1);
            assertThat(orderDetail.getLineNumber()).isEqualTo(2);
        }
    }

    @Test
    void testRightJoin() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(orderLine.orderId, orderLine.quantity, itemMaster.itemId, itemMaster.description)
                    .from(orderLine, "ol")
                    .rightJoin(itemMaster, "im").on(orderLine.itemId, equalTo(itemMaster.itemId))
                    .orderBy(itemMaster.itemId)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "select ol.order_id, ol.quantity, im.item_id, im.description"
                    + " from OrderLine ol right join ItemMaster im on ol.item_id = im.item_id"
                    + " order by item_id";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatement);

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);

            assertThat(rows).hasSize(5);
            Map<String, Object> row = rows.get(2);
            assertThat(row).containsEntry("ORDER_ID", 1);
            assertThat(row).containsEntry("QUANTITY", 1);
            assertThat(row).containsEntry("DESCRIPTION", "First Base Glove");
            assertThat(row).containsEntry("ITEM_ID", 33);

            row = rows.get(4);
            assertThat(row).doesNotContainKey("ORDER_ID");
            assertThat(row).doesNotContainKey("QUANTITY");
            assertThat(row).containsEntry("DESCRIPTION", "Catcher Glove");
            assertThat(row).containsEntry("ITEM_ID", 55);
        }
    }

    @Test
    void testRightJoin2() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(orderLine.orderId, orderLine.quantity, itemMaster.itemId, itemMaster.description)
                    .from(orderMaster, "om")
                    .join(orderLine, "ol").on(orderMaster.orderId, equalTo(orderLine.orderId))
                    .rightJoin(itemMaster, "im").on(orderLine.itemId, equalTo(itemMaster.itemId))
                    .orderBy(orderLine.orderId, itemMaster.itemId)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "select ol.order_id, ol.quantity, im.item_id, im.description"
                    + " from OrderMaster om join OrderLine ol on om.order_id = ol.order_id"
                    + " right join ItemMaster im on ol.item_id = im.item_id"
                    + " order by order_id, item_id";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatement);

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);

            assertThat(rows).hasSize(5);
            Map<String, Object> row = rows.get(0);
            assertThat(row).doesNotContainKey("ORDER_ID");
            assertThat(row).doesNotContainKey("QUANTITY");
            assertThat(row).containsEntry("DESCRIPTION", "Catcher Glove");
            assertThat(row).containsEntry("ITEM_ID", 55);

            row = rows.get(4);
            assertThat(row).containsEntry("ORDER_ID", 2);
            assertThat(row).containsEntry("QUANTITY", 1);
            assertThat(row).containsEntry("DESCRIPTION", "Outfield Glove");
            assertThat(row).containsEntry("ITEM_ID", 44);
        }
    }

    @Test
    void testRightJoin3() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(orderLine.orderId, orderLine.quantity, itemMaster.itemId, itemMaster.description)
                    .from(orderMaster, "om")
                    .join(orderLine, "ol", on(orderMaster.orderId, equalTo(orderLine.orderId)))
                    .rightJoin(itemMaster, "im", on(orderLine.itemId, equalTo(itemMaster.itemId)))
                    .orderBy(orderLine.orderId, itemMaster.itemId)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "select ol.order_id, ol.quantity, im.item_id, im.description"
                    + " from OrderMaster om join OrderLine ol on om.order_id = ol.order_id"
                    + " right join ItemMaster im on ol.item_id = im.item_id"
                    + " order by order_id, item_id";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatement);

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);

            assertThat(rows).hasSize(5);
            Map<String, Object> row = rows.get(0);
            assertThat(row).doesNotContainKey("ORDER_ID");
            assertThat(row).doesNotContainKey("QUANTITY");
            assertThat(row).containsEntry("DESCRIPTION", "Catcher Glove");
            assertThat(row).containsEntry("ITEM_ID", 55);

            row = rows.get(4);
            assertThat(row).containsEntry("ORDER_ID", 2);
            assertThat(row).containsEntry("QUANTITY", 1);
            assertThat(row).containsEntry("DESCRIPTION", "Outfield Glove");
            assertThat(row).containsEntry("ITEM_ID", 44);
        }
    }

    @Test
    void testRightJoinNoAliases() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(orderLine.orderId, orderLine.quantity, itemMaster.itemId, itemMaster.description)
                    .from(orderMaster)
                    .join(orderLine).on(orderMaster.orderId, equalTo(orderLine.orderId))
                    .rightJoin(itemMaster).on(orderLine.itemId, equalTo(itemMaster.itemId))
                    .orderBy(orderLine.orderId, itemMaster.itemId)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "select OrderLine.order_id, OrderLine.quantity, ItemMaster.item_id, ItemMaster.description"
                    + " from OrderMaster join OrderLine on OrderMaster.order_id = OrderLine.order_id"
                    + " right join ItemMaster on OrderLine.item_id = ItemMaster.item_id"
                    + " order by order_id, item_id";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatement);

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);

            assertThat(rows).hasSize(5);
            Map<String, Object> row = rows.get(0);
            assertThat(row).doesNotContainKey("ORDER_ID");
            assertThat(row).doesNotContainKey("QUANTITY");
            assertThat(row).containsEntry("DESCRIPTION", "Catcher Glove");
            assertThat(row).containsEntry("ITEM_ID", 55);

            row = rows.get(4);
            assertThat(row).containsEntry("ORDER_ID", 2);
            assertThat(row).containsEntry("QUANTITY", 1);
            assertThat(row).containsEntry("DESCRIPTION", "Outfield Glove");
            assertThat(row).containsEntry("ITEM_ID", 44);
        }
    }

    @Test
    void testLeftJoin() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(orderLine.orderId, orderLine.quantity, itemMaster.itemId, itemMaster.description)
                    .from(itemMaster, "im")
                    .leftJoin(orderLine, "ol").on(orderLine.itemId, equalTo(itemMaster.itemId))
                    .orderBy(itemMaster.itemId)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "select ol.order_id, ol.quantity, im.item_id, im.description"
                    + " from ItemMaster im left join OrderLine ol on ol.item_id = im.item_id"
                    + " order by item_id";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatement);

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);

            assertThat(rows).hasSize(5);
            Map<String, Object> row = rows.get(2);
            assertThat(row).containsEntry("ORDER_ID", 1);
            assertThat(row).containsEntry("QUANTITY", 1);
            assertThat(row).containsEntry("DESCRIPTION", "First Base Glove");
            assertThat(row).containsEntry("ITEM_ID", 33);

            row = rows.get(4);
            assertThat(row).doesNotContainKey("ORDER_ID");
            assertThat(row).doesNotContainKey("QUANTITY");
            assertThat(row).containsEntry("DESCRIPTION", "Catcher Glove");
            assertThat(row).containsEntry("ITEM_ID", 55);
        }
    }

    @Test
    void testLeftJoin2() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(orderLine.orderId, orderLine.quantity, itemMaster.itemId, itemMaster.description)
                    .from(orderMaster, "om")
                    .join(orderLine, "ol").on(orderMaster.orderId, equalTo(orderLine.orderId))
                    .leftJoin(itemMaster, "im").on(orderLine.itemId, equalTo(itemMaster.itemId))
                    .orderBy(orderLine.orderId, itemMaster.itemId)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "select ol.order_id, ol.quantity, im.item_id, im.description"
                    + " from OrderMaster om join OrderLine ol on om.order_id = ol.order_id"
                    + " left join ItemMaster im on ol.item_id = im.item_id"
                    + " order by order_id, item_id";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatement);

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);

            assertThat(rows).hasSize(5);
            Map<String, Object> row = rows.get(2);
            assertThat(row).containsEntry("ORDER_ID", 2);
            assertThat(row).containsEntry("QUANTITY", 6);
            assertThat(row).doesNotContainKey("DESCRIPTION");
            assertThat(row).doesNotContainKey("ITEM_ID");

            row = rows.get(4);
            assertThat(row).containsEntry("ORDER_ID", 2);
            assertThat(row).containsEntry("QUANTITY", 1);
            assertThat(row).containsEntry("DESCRIPTION", "Outfield Glove");
            assertThat(row).containsEntry("ITEM_ID", 44);
        }
    }

    @Test
    void testLeftJoin3() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(orderLine.orderId, orderLine.quantity, itemMaster.itemId, itemMaster.description)
                    .from(orderMaster, "om")
                    .join(orderLine, "ol", on(orderMaster.orderId, equalTo(orderLine.orderId)))
                    .leftJoin(itemMaster, "im", on(orderLine.itemId, equalTo(itemMaster.itemId)))
                    .orderBy(orderLine.orderId, itemMaster.itemId)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "select ol.order_id, ol.quantity, im.item_id, im.description"
                    + " from OrderMaster om join OrderLine ol on om.order_id = ol.order_id"
                    + " left join ItemMaster im on ol.item_id = im.item_id"
                    + " order by order_id, item_id";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatement);

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);

            assertThat(rows).hasSize(5);
            Map<String, Object> row = rows.get(2);
            assertThat(row).containsEntry("ORDER_ID", 2);
            assertThat(row).containsEntry("QUANTITY", 6);
            assertThat(row).doesNotContainKey("DESCRIPTION");
            assertThat(row).doesNotContainKey("ITEM_ID");

            row = rows.get(4);
            assertThat(row).containsEntry("ORDER_ID", 2);
            assertThat(row).containsEntry("QUANTITY", 1);
            assertThat(row).containsEntry("DESCRIPTION", "Outfield Glove");
            assertThat(row).containsEntry("ITEM_ID", 44);
        }
    }

    @Test
    void testLeftJoinNoAliases() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(orderLine.orderId, orderLine.quantity, itemMaster.itemId, itemMaster.description)
                    .from(orderMaster)
                    .join(orderLine).on(orderMaster.orderId, equalTo(orderLine.orderId))
                    .leftJoin(itemMaster).on(orderLine.itemId, equalTo(itemMaster.itemId))
                    .orderBy(orderLine.orderId, itemMaster.itemId)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "select OrderLine.order_id, OrderLine.quantity, ItemMaster.item_id, ItemMaster.description"
                    + " from OrderMaster join OrderLine on OrderMaster.order_id = OrderLine.order_id"
                    + " left join ItemMaster on OrderLine.item_id = ItemMaster.item_id"
                    + " order by order_id, item_id";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatement);

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);

            assertThat(rows).hasSize(5);
            Map<String, Object> row = rows.get(2);
            assertThat(row).containsEntry("ORDER_ID", 2);
            assertThat(row).containsEntry("QUANTITY", 6);
            assertThat(row).doesNotContainKey("DESCRIPTION");
            assertThat(row).doesNotContainKey("ITEM_ID");

            row = rows.get(4);
            assertThat(row).containsEntry("ORDER_ID", 2);
            assertThat(row).containsEntry("QUANTITY", 1);
            assertThat(row).containsEntry("DESCRIPTION", "Outfield Glove");
            assertThat(row).containsEntry("ITEM_ID", 44);
        }
    }

    @Test
    void testFullJoin() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(orderLine.orderId, orderLine.quantity, orderLine.itemId.as("ol_itemid"), itemMaster.itemId.as("im_itemid"), itemMaster.description)
                    .from(itemMaster, "im")
                    .fullJoin(orderLine, "ol").on(itemMaster.itemId, equalTo(orderLine.itemId))
                    .orderBy(orderLine.orderId, sortColumn("im_itemid"))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "select ol.order_id, ol.quantity, ol.item_id as ol_itemid, im.item_id as im_itemid, im.description"
                    + " from ItemMaster im full join OrderLine ol on im.item_id = ol.item_id"
                    + " order by order_id, im_itemid";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatement);

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);

            assertThat(rows).hasSize(6);
            Map<String, Object> row = rows.get(0);
            assertThat(row).doesNotContainKey("ORDER_ID");
            assertThat(row).doesNotContainKey("QUANTITY");
            assertThat(row).containsEntry("DESCRIPTION", "Catcher Glove");
            assertThat(row).containsEntry("IM_ITEMID", 55);

            row = rows.get(2);
            assertThat(row).containsEntry("ORDER_ID", 1);
            assertThat(row).containsEntry("QUANTITY", 1);
            assertThat(row).containsEntry("DESCRIPTION", "First Base Glove");
            assertThat(row).containsEntry("IM_ITEMID", 33);

            row = rows.get(3);
            assertThat(row).containsEntry("ORDER_ID", 2);
            assertThat(row).containsEntry("QUANTITY", 6);
            assertThat(row).containsEntry("OL_ITEMID", 66);
            assertThat(row).doesNotContainKey("DESCRIPTION");
            assertThat(row).doesNotContainKey("IM_ITEMID");
        }
    }

    @Test
    void testFullJoin2() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(orderLine.orderId, orderLine.quantity, itemMaster.itemId, itemMaster.description)
                    .from(orderMaster, "om")
                    .join(orderLine, "ol").on(orderMaster.orderId, equalTo(orderLine.orderId))
                    .fullJoin(itemMaster, "im").on(orderLine.itemId, equalTo(itemMaster.itemId))
                    .orderBy(orderLine.orderId, itemMaster.itemId)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "select ol.order_id, ol.quantity, im.item_id, im.description"
                    + " from OrderMaster om join OrderLine ol on om.order_id = ol.order_id"
                    + " full join ItemMaster im on ol.item_id = im.item_id"
                    + " order by order_id, item_id";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatement);

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);

            assertThat(rows).hasSize(6);
            Map<String, Object> row = rows.get(0);
            assertThat(row).doesNotContainKey("ORDER_ID");
            assertThat(row).doesNotContainKey("QUANTITY");
            assertThat(row).containsEntry("DESCRIPTION", "Catcher Glove");
            assertThat(row).containsEntry("ITEM_ID", 55);

            row = rows.get(3);
            assertThat(row).containsEntry("ORDER_ID", 2);
            assertThat(row).containsEntry("QUANTITY", 6);
            assertThat(row).doesNotContainKey("DESCRIPTION");
            assertThat(row).doesNotContainKey("ITEM_ID");

            row = rows.get(5);
            assertThat(row).containsEntry("ORDER_ID", 2);
            assertThat(row).containsEntry("QUANTITY", 1);
            assertThat(row).containsEntry("DESCRIPTION", "Outfield Glove");
            assertThat(row).containsEntry("ITEM_ID", 44);
        }
    }

    @Test
    void testFullJoin3() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(orderLine.orderId, orderLine.quantity, itemMaster.itemId, itemMaster.description)
                    .from(orderMaster, "om")
                    .join(orderLine, "ol", on(orderMaster.orderId, equalTo(orderLine.orderId)))
                    .fullJoin(itemMaster, "im", on(orderLine.itemId, equalTo(itemMaster.itemId)))
                    .orderBy(orderLine.orderId, itemMaster.itemId)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "select ol.order_id, ol.quantity, im.item_id, im.description"
                    + " from OrderMaster om join OrderLine ol on om.order_id = ol.order_id"
                    + " full join ItemMaster im on ol.item_id = im.item_id"
                    + " order by order_id, item_id";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatement);

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);

            assertThat(rows).hasSize(6);
            Map<String, Object> row = rows.get(0);
            assertThat(row).doesNotContainKey("ORDER_ID");
            assertThat(row).doesNotContainKey("QUANTITY");
            assertThat(row).containsEntry("DESCRIPTION", "Catcher Glove");
            assertThat(row).containsEntry("ITEM_ID", 55);

            row = rows.get(3);
            assertThat(row).containsEntry("ORDER_ID", 2);
            assertThat(row).containsEntry("QUANTITY", 6);
            assertThat(row).doesNotContainKey("DESCRIPTION");
            assertThat(row).doesNotContainKey("ITEM_ID");

            row = rows.get(5);
            assertThat(row).containsEntry("ORDER_ID", 2);
            assertThat(row).containsEntry("QUANTITY", 1);
            assertThat(row).containsEntry("DESCRIPTION", "Outfield Glove");
            assertThat(row).containsEntry("ITEM_ID", 44);
        }
    }

    @Test
    void testFullJoin4() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(orderLine.orderId, orderLine.quantity, itemMaster.description)
                    .from(orderMaster, "om")
                    .join(orderLine, "ol", on(orderMaster.orderId, equalTo(orderLine.orderId)))
                    .fullJoin(itemMaster, "im", on(orderLine.itemId, equalTo(itemMaster.itemId)))
                    .orderBy(orderLine.orderId, sortColumn("im", itemMaster.itemId))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "select ol.order_id, ol.quantity, im.description"
                    + " from OrderMaster om join OrderLine ol on om.order_id = ol.order_id"
                    + " full join ItemMaster im on ol.item_id = im.item_id"
                    + " order by order_id, im.item_id";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatement);

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);

            assertThat(rows).hasSize(6);
            Map<String, Object> row = rows.get(0);
            assertThat(row).doesNotContainKey("ORDER_ID");
            assertThat(row).doesNotContainKey("QUANTITY");
            assertThat(row).containsEntry("DESCRIPTION", "Catcher Glove");

            row = rows.get(3);
            assertThat(row).containsEntry("ORDER_ID", 2);
            assertThat(row).containsEntry("QUANTITY", 6);
            assertThat(row).doesNotContainKey("DESCRIPTION");

            row = rows.get(5);
            assertThat(row).containsEntry("ORDER_ID", 2);
            assertThat(row).containsEntry("QUANTITY", 1);
            assertThat(row).containsEntry("DESCRIPTION", "Outfield Glove");
        }
    }

    @Test
    void testFullJoin5() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(orderLine.orderId, orderLine.quantity, itemMaster.description)
                    .from(orderMaster, "om")
                    .join(orderLine, "ol", on(orderMaster.orderId, equalTo(orderLine.orderId)))
                    .fullJoin(itemMaster, "im", on(orderLine.itemId, equalTo(itemMaster.itemId)))
                    .orderBy(orderLine.orderId, sortColumn("im", itemMaster.itemId).descending())
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "select ol.order_id, ol.quantity, im.description"
                    + " from OrderMaster om join OrderLine ol on om.order_id = ol.order_id"
                    + " full join ItemMaster im on ol.item_id = im.item_id"
                    + " order by order_id, im.item_id DESC";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatement);

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);

            assertThat(rows).hasSize(6);
            Map<String, Object> row = rows.get(0);
            assertThat(row).doesNotContainKey("ORDER_ID");
            assertThat(row).doesNotContainKey("QUANTITY");
            assertThat(row).containsEntry("DESCRIPTION", "Catcher Glove");

            row = rows.get(3);
            assertThat(row).containsEntry("ORDER_ID", 2);
            assertThat(row).containsEntry("QUANTITY", 6);
            assertThat(row).doesNotContainKey("DESCRIPTION");

            row = rows.get(5);
            assertThat(row).containsEntry("ORDER_ID", 2);
            assertThat(row).containsEntry("QUANTITY", 1);
            assertThat(row).containsEntry("DESCRIPTION", "Helmet");
        }
    }

    @Test
    void testFullJoinNoAliases() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(orderLine.orderId, orderLine.quantity, itemMaster.itemId, itemMaster.description)
                    .from(orderMaster)
                    .join(orderLine).on(orderMaster.orderId, equalTo(orderLine.orderId))
                    .fullJoin(itemMaster).on(orderLine.itemId, equalTo(itemMaster.itemId))
                    .orderBy(orderLine.orderId, itemMaster.itemId)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "select OrderLine.order_id, OrderLine.quantity, ItemMaster.item_id, ItemMaster.description"
                    + " from OrderMaster join OrderLine on OrderMaster.order_id = OrderLine.order_id"
                    + " full join ItemMaster on OrderLine.item_id = ItemMaster.item_id"
                    + " order by order_id, item_id";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatement);

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);

            assertThat(rows).hasSize(6);
            Map<String, Object> row = rows.get(0);
            assertThat(row).doesNotContainKey("ORDER_ID");
            assertThat(row).doesNotContainKey("QUANTITY");
            assertThat(row).containsEntry("DESCRIPTION", "Catcher Glove");
            assertThat(row).containsEntry("ITEM_ID", 55);

            row = rows.get(3);
            assertThat(row).containsEntry("ORDER_ID", 2);
            assertThat(row).containsEntry("QUANTITY", 6);
            assertThat(row).doesNotContainKey("DESCRIPTION");
            assertThat(row).doesNotContainKey("ITEM_ID");

            row = rows.get(5);
            assertThat(row).containsEntry("ORDER_ID", 2);
            assertThat(row).containsEntry("QUANTITY", 1);
            assertThat(row).containsEntry("DESCRIPTION", "Outfield Glove");
            assertThat(row).containsEntry("ITEM_ID", 44);
        }
    }

    @Test
    void testSelf() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            JoinMapper mapper = session.getMapper(JoinMapper.class);

            // create second table instance for self-join
            UserDynamicSQLSupport.User user2 = new UserDynamicSQLSupport.User();

            // get Bamm Bamm's parent - should be Barney
            SelectStatementProvider selectStatement = select(user.userId, user.userName, user.parentId)
                    .from(user, "u1")
                    .join(user2, "u2").on(user.userId, equalTo(user2.parentId))
                    .where(user2.userId, isEqualTo(4))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "select u1.user_id, u1.user_name, u1.parent_id"
                    + " from User u1 join User u2 on u1.user_id = u2.parent_id"
                    + " where u2.user_id = #{parameters.p1,jdbcType=INTEGER}";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatement);

            List<User> rows = mapper.selectUsers(selectStatement);

            assertThat(rows).hasSize(1);
            User row = rows.get(0);
            assertThat(row.getUserId()).isEqualTo(2);
            assertThat(row.getUserName()).isEqualTo("Barney");
            assertThat(row.getParentId()).isNull();
        }
    }

    @Test
    void testSelfWithDuplicateAlias() {
        QueryExpressionDSL<SelectModel> dsl = select(user.userId, user.userName, user.parentId)
                .from(user, "u1");

        assertThatExceptionOfType(DuplicateTableAliasException.class).isThrownBy(() -> dsl.join(user, "u2"))
                .withMessage(Messages.getString("ERROR.1", user.tableNameAtRuntime(), "u2", "u1"));
    }

    @Test
    void testSelfWithNewAlias() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            JoinMapper mapper = session.getMapper(JoinMapper.class);

            // create second table instance for self-join
            UserDynamicSQLSupport.User user2 = user.withAlias("u2");

            // get Bamm Bamm's parent - should be Barney
            SelectStatementProvider selectStatement = select(user.userId, user.userName, user.parentId)
                    .from(user)
                    .join(user2).on(user.userId, equalTo(user2.parentId))
                    .where(user2.userId, isEqualTo(4))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "select User.user_id, User.user_name, User.parent_id"
                    + " from User join User u2 on User.user_id = u2.parent_id"
                    + " where u2.user_id = #{parameters.p1,jdbcType=INTEGER}";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatement);

            List<User> rows = mapper.selectUsers(selectStatement);

            assertThat(rows).hasSize(1);
            User row = rows.get(0);
            assertThat(row.getUserId()).isEqualTo(2);
            assertThat(row.getUserName()).isEqualTo("Barney");
            assertThat(row.getParentId()).isNull();
        }
    }

    @Test
    void testSelfWithNewAliasAndOverride() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            JoinMapper mapper = session.getMapper(JoinMapper.class);

            // create second table instance for self-join
            UserDynamicSQLSupport.User user2 = user.withAlias("other_user");

            // get Bamm Bamm's parent - should be Barney
            SelectStatementProvider selectStatement = select(user.userId, user.userName, user.parentId)
                    .from(user, "u1")
                    .join(user2, "u2").on(user.userId, equalTo(user2.parentId))
                    .where(user2.userId, isEqualTo(4))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "select u1.user_id, u1.user_name, u1.parent_id"
                    + " from User u1 join User u2 on u1.user_id = u2.parent_id"
                    + " where u2.user_id = #{parameters.p1,jdbcType=INTEGER}";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatement);

            List<User> rows = mapper.selectUsers(selectStatement);

            assertThat(rows).hasSize(1);
            User row = rows.get(0);
            assertThat(row.getUserId()).isEqualTo(2);
            assertThat(row.getUserName()).isEqualTo("Barney");
            assertThat(row.getParentId()).isNull();
        }
    }

    @Test
    void testLimitAndOffsetAfterJoin() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(orderLine.orderId, orderLine.quantity, itemMaster.itemId, itemMaster.description)
                    .from(itemMaster, "im")
                    .leftJoin(orderLine, "ol").on(orderLine.itemId, equalTo(itemMaster.itemId))
                    .limit(2)
                    .offset(1)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "select ol.order_id, ol.quantity, im.item_id, im.description"
                    + " from ItemMaster im left join OrderLine ol on ol.item_id = im.item_id"
                    + " limit #{parameters.p1} offset #{parameters.p2}";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatement);

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);

            assertThat(rows).hasSize(2);
            Map<String, Object> row = rows.get(0);
            assertThat(row).containsEntry("ORDER_ID", 2);
            assertThat(row).containsEntry("QUANTITY", 1);
            assertThat(row).containsEntry("DESCRIPTION", "Helmet");
            assertThat(row).containsEntry("ITEM_ID", 22);

            row = rows.get(1);
            assertThat(row).containsEntry("ORDER_ID", 1);
            assertThat(row).containsEntry("QUANTITY", 1);
            assertThat(row).containsEntry("DESCRIPTION", "First Base Glove");
            assertThat(row).containsEntry("ITEM_ID", 33);
        }
    }

    @Test
    void testLimitOnlyAfterJoin() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(orderLine.orderId, orderLine.quantity, itemMaster.itemId, itemMaster.description)
                    .from(itemMaster, "im")
                    .leftJoin(orderLine, "ol").on(orderLine.itemId, equalTo(itemMaster.itemId))
                    .limit(2)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "select ol.order_id, ol.quantity, im.item_id, im.description"
                    + " from ItemMaster im left join OrderLine ol on ol.item_id = im.item_id"
                    + " limit #{parameters.p1}";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatement);

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);

            assertThat(rows).hasSize(2);
            Map<String, Object> row = rows.get(0);
            assertThat(row).containsEntry("ORDER_ID", 1);
            assertThat(row).containsEntry("QUANTITY", 1);
            assertThat(row).containsEntry("DESCRIPTION", "Helmet");
            assertThat(row).containsEntry("ITEM_ID", 22);

            row = rows.get(1);
            assertThat(row).containsEntry("ORDER_ID", 2);
            assertThat(row).containsEntry("QUANTITY", 1);
            assertThat(row).containsEntry("DESCRIPTION", "Helmet");
            assertThat(row).containsEntry("ITEM_ID", 22);
        }
    }

    @Test
    void testOffsetOnlyAfterJoin() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(orderLine.orderId, orderLine.quantity, itemMaster.itemId, itemMaster.description)
                    .from(itemMaster, "im")
                    .leftJoin(orderLine, "ol").on(orderLine.itemId, equalTo(itemMaster.itemId))
                    .offset(2)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "select ol.order_id, ol.quantity, im.item_id, im.description"
                    + " from ItemMaster im left join OrderLine ol on ol.item_id = im.item_id"
                    + " offset #{parameters.p1} rows";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatement);

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);

            assertThat(rows).hasSize(3);
            Map<String, Object> row = rows.get(0);
            assertThat(row).containsEntry("ORDER_ID", 1);
            assertThat(row).containsEntry("QUANTITY", 1);
            assertThat(row).containsEntry("DESCRIPTION", "First Base Glove");
            assertThat(row).containsEntry("ITEM_ID", 33);

            row = rows.get(1);
            assertThat(row).containsEntry("ORDER_ID", 2);
            assertThat(row).containsEntry("QUANTITY", 1);
            assertThat(row).containsEntry("DESCRIPTION", "Outfield Glove");
            assertThat(row).containsEntry("ITEM_ID", 44);
        }
    }

    @Test
    void testOffsetAndFetchFirstAfterJoin() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(orderLine.orderId, orderLine.quantity, itemMaster.itemId, itemMaster.description)
                    .from(itemMaster, "im")
                    .leftJoin(orderLine, "ol").on(orderLine.itemId, equalTo(itemMaster.itemId))
                    .offset(1)
                    .fetchFirst(2).rowsOnly()
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "select ol.order_id, ol.quantity, im.item_id, im.description"
                    + " from ItemMaster im left join OrderLine ol on ol.item_id = im.item_id"
                    + " offset #{parameters.p1} rows fetch first #{parameters.p2} rows only";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatement);

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);

            assertThat(rows).hasSize(2);
            Map<String, Object> row = rows.get(0);
            assertThat(row).containsEntry("ORDER_ID", 2);
            assertThat(row).containsEntry("QUANTITY", 1);
            assertThat(row).containsEntry("DESCRIPTION", "Helmet");
            assertThat(row).containsEntry("ITEM_ID", 22);

            row = rows.get(1);
            assertThat(row).containsEntry("ORDER_ID", 1);
            assertThat(row).containsEntry("QUANTITY", 1);
            assertThat(row).containsEntry("DESCRIPTION", "First Base Glove");
            assertThat(row).containsEntry("ITEM_ID", 33);
        }
    }

    @Test
    void testFetchFirstOnlyAfterJoin() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(orderLine.orderId, orderLine.quantity, itemMaster.itemId, itemMaster.description)
                    .from(itemMaster, "im")
                    .leftJoin(orderLine, "ol").on(orderLine.itemId, equalTo(itemMaster.itemId))
                    .fetchFirst(2).rowsOnly()
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "select ol.order_id, ol.quantity, im.item_id, im.description"
                    + " from ItemMaster im left join OrderLine ol on ol.item_id = im.item_id"
                    + " fetch first #{parameters.p1} rows only";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatement);

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);

            assertThat(rows).hasSize(2);
            Map<String, Object> row = rows.get(0);
            assertThat(row).containsEntry("ORDER_ID", 1);
            assertThat(row).containsEntry("QUANTITY", 1);
            assertThat(row).containsEntry("DESCRIPTION", "Helmet");
            assertThat(row).containsEntry("ITEM_ID", 22);

            row = rows.get(1);
            assertThat(row).containsEntry("ORDER_ID", 2);
            assertThat(row).containsEntry("QUANTITY", 1);
            assertThat(row).containsEntry("DESCRIPTION", "Helmet");
            assertThat(row).containsEntry("ITEM_ID", 22);
        }
    }
}
