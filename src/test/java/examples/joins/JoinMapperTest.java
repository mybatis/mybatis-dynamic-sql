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
package examples.joins;

import static examples.joins.ItemMasterDynamicSQLSupport.itemMaster;
import static examples.joins.OrderDetailDynamicSQLSupport.*;
import static examples.joins.OrderLineDynamicSQLSupport.orderLine;
import static examples.joins.OrderMasterDynamicSQLSupport.orderDate;
import static examples.joins.OrderMasterDynamicSQLSupport.orderMaster;
import static examples.joins.UserDynamicSQLSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
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
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;

public class JoinMapperTest {

    private static final String JDBC_URL = "jdbc:hsqldb:mem:aname";
    private static final String JDBC_DRIVER = "org.hsqldb.jdbcDriver";
    
    private SqlSessionFactory sqlSessionFactory;
    
    @BeforeEach
    public void setup() throws Exception {
        Class.forName(JDBC_DRIVER);
        InputStream is = getClass().getResourceAsStream("/examples/joins/CreateJoinDB.sql");
        try (Connection connection = DriverManager.getConnection(JDBC_URL, "sa", "")) {
            ScriptRunner sr = new ScriptRunner(connection);
            sr.setLogWriter(null);
            sr.runScript(new InputStreamReader(is));
        }
        
        UnpooledDataSource ds = new UnpooledDataSource(JDBC_DRIVER, JDBC_URL, "sa", "");
        Environment environment = new Environment("test", new JdbcTransactionFactory(), ds);
        Configuration config = new Configuration(environment);
        config.addMapper(JoinMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(config);
    }
    
    @Test
    public void testSingleTableJoin1() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            JoinMapper mapper = session.getMapper(JoinMapper.class);
            
            SelectStatementProvider selectStatement = select(orderMaster.orderId, orderDate, orderDetail.lineNumber, orderDetail.description, orderDetail.quantity)
                    .from(orderMaster, "om")
                    .join(orderDetail, "od").on(orderMaster.orderId, equalTo(orderDetail.orderId))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expectedStatment = "select om.order_id, om.order_date, od.line_number, od.description, od.quantity"
                    + " from OrderMaster om join OrderDetail od on om.order_id = od.order_id";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatment);
            
            List<OrderMaster> rows = mapper.selectMany(selectStatement);

            assertThat(rows.size()).isEqualTo(2);
            OrderMaster orderMaster = rows.get(0);
            assertThat(orderMaster.getId()).isEqualTo(1);
            assertThat(orderMaster.getDetails().size()).isEqualTo(2);
            OrderDetail orderDetail = orderMaster.getDetails().get(0);
            assertThat(orderDetail.getLineNumber()).isEqualTo(1);
            orderDetail = orderMaster.getDetails().get(1);
            assertThat(orderDetail.getLineNumber()).isEqualTo(2);

            orderMaster = rows.get(1);
            assertThat(orderMaster.getId()).isEqualTo(2);
            assertThat(orderMaster.getDetails().size()).isEqualTo(1);
            orderDetail = orderMaster.getDetails().get(0);
            assertThat(orderDetail.getLineNumber()).isEqualTo(1);
        }
    }
    
    @Test
    public void testSingleTableJoin2() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            JoinMapper mapper = session.getMapper(JoinMapper.class);
            
            SelectStatementProvider selectStatement = select(orderMaster.orderId, orderDate, orderDetail.lineNumber, orderDetail.description, orderDetail.quantity)
                    .from(orderMaster, "om")
                    .join(orderDetail, "od", on(orderMaster.orderId, equalTo(orderDetail.orderId)))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expectedStatment = "select om.order_id, om.order_date, od.line_number, od.description, od.quantity"
                    + " from OrderMaster om join OrderDetail od on om.order_id = od.order_id";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatment);
            
            List<OrderMaster> rows = mapper.selectMany(selectStatement);

            assertThat(rows.size()).isEqualTo(2);
            OrderMaster orderMaster = rows.get(0);
            assertThat(orderMaster.getId()).isEqualTo(1);
            assertThat(orderMaster.getDetails().size()).isEqualTo(2);
            OrderDetail orderDetail = orderMaster.getDetails().get(0);
            assertThat(orderDetail.getLineNumber()).isEqualTo(1);
            orderDetail = orderMaster.getDetails().get(1);
            assertThat(orderDetail.getLineNumber()).isEqualTo(2);

            orderMaster = rows.get(1);
            assertThat(orderMaster.getId()).isEqualTo(2);
            assertThat(orderMaster.getDetails().size()).isEqualTo(1);
            orderDetail = orderMaster.getDetails().get(0);
            assertThat(orderDetail.getLineNumber()).isEqualTo(1);
        }
    }
    
    @Test
    public void testCompoundJoin1() {
        // this is a nonsensical join, but it does test the "and" capability
        SelectStatementProvider selectStatement = select(orderMaster.orderId, orderDate, orderDetail.lineNumber, orderDetail.description, orderDetail.quantity)
                .from(orderMaster, "om")
                .join(orderDetail, "od").on(orderMaster.orderId, equalTo(orderDetail.orderId), and(orderMaster.orderId, equalTo(orderDetail.orderId)))
                .build()
                .render(RenderingStrategies.MYBATIS3);
        
        String expectedStatment = "select om.order_id, om.order_date, od.line_number, od.description, od.quantity"
                + " from OrderMaster om join OrderDetail od on om.order_id = od.order_id and om.order_id = od.order_id";
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatment);
    }

    @Test
    public void testCompoundJoin2() {
        // this is a nonsensical join, but it does test the "and" capability
        SelectStatementProvider selectStatement = select(orderMaster.orderId, orderDate, orderDetail.lineNumber, orderDetail.description, orderDetail.quantity)
                .from(orderMaster, "om")
                .join(orderDetail, "od").on(orderMaster.orderId, equalTo(orderDetail.orderId))
                .and(orderMaster.orderId, equalTo(orderDetail.orderId))
                .build()
                .render(RenderingStrategies.MYBATIS3);
        
        String expectedStatment = "select om.order_id, om.order_date, od.line_number, od.description, od.quantity"
                + " from OrderMaster om join OrderDetail od on om.order_id = od.order_id and om.order_id = od.order_id";
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatment);
    }
    
    @Test
    public void testCompoundJoin3() {
        // this is a nonsensical join, but it does test the "and" capability
        SelectStatementProvider selectStatement = select(orderMaster.orderId, orderDate, orderDetail.lineNumber, orderDetail.description, orderDetail.quantity)
                .from(orderMaster, "om")
                .join(orderDetail, "od", on(orderMaster.orderId, equalTo(orderDetail.orderId)), and(orderMaster.orderId, equalTo(orderDetail.orderId)))
                .build()
                .render(RenderingStrategies.MYBATIS3);
        
        String expectedStatment = "select om.order_id, om.order_date, od.line_number, od.description, od.quantity"
                + " from OrderMaster om join OrderDetail od on om.order_id = od.order_id and om.order_id = od.order_id";
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatment);
    }

    @Test
    public void testCompoundJoin4() {
        // this is a nonsensical join, but it does test the "and" capability
        SelectStatementProvider selectStatement = select(orderMaster.orderId, orderDate, orderDetail.lineNumber, orderDetail.description, orderDetail.quantity)
                .from(orderMaster, "om")
                .leftJoin(orderDetail, "od", on(orderMaster.orderId, equalTo(orderDetail.orderId)), and(orderMaster.orderId, equalTo(orderDetail.orderId)))
                .build()
                .render(RenderingStrategies.MYBATIS3);
        
        String expectedStatment = "select om.order_id, om.order_date, od.line_number, od.description, od.quantity"
                + " from OrderMaster om left join OrderDetail od on om.order_id = od.order_id and om.order_id = od.order_id";
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatment);
    }

    @Test
    public void testCompoundJoin5() {
        // this is a nonsensical join, but it does test the "and" capability
        SelectStatementProvider selectStatement = select(orderMaster.orderId, orderDate, orderDetail.lineNumber, orderDetail.description, orderDetail.quantity)
                .from(orderMaster, "om")
                .rightJoin(orderDetail, "od", on(orderMaster.orderId, equalTo(orderDetail.orderId)), and(orderMaster.orderId, equalTo(orderDetail.orderId)))
                .build()
                .render(RenderingStrategies.MYBATIS3);
        
        String expectedStatment = "select om.order_id, om.order_date, od.line_number, od.description, od.quantity"
                + " from OrderMaster om right join OrderDetail od on om.order_id = od.order_id and om.order_id = od.order_id";
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatment);
    }

    @Test
    public void testCompoundJoin6() {
        // this is a nonsensical join, but it does test the "and" capability
        SelectStatementProvider selectStatement = select(orderMaster.orderId, orderDate, orderDetail.lineNumber, orderDetail.description, orderDetail.quantity)
                .from(orderMaster, "om")
                .fullJoin(orderDetail, "od", on(orderMaster.orderId, equalTo(orderDetail.orderId)), and(orderMaster.orderId, equalTo(orderDetail.orderId)))
                .build()
                .render(RenderingStrategies.MYBATIS3);
        
        String expectedStatment = "select om.order_id, om.order_date, od.line_number, od.description, od.quantity"
                + " from OrderMaster om full join OrderDetail od on om.order_id = od.order_id and om.order_id = od.order_id";
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatment);
    }

    @Test
    public void testMultipleTableJoinWithWhereClause() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            JoinMapper mapper = session.getMapper(JoinMapper.class);
            
            SelectStatementProvider selectStatement = select(orderMaster.orderId, orderDate, orderLine.lineNumber, itemMaster.description, orderLine.quantity)
                    .from(orderMaster, "om")
                    .join(orderLine, "ol").on(orderMaster.orderId, equalTo(orderLine.orderId))
                    .join(itemMaster, "im").on(orderLine.itemId, equalTo(itemMaster.itemId))
                    .where(orderMaster.orderId, isEqualTo(2))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expectedStatment = "select om.order_id, om.order_date, ol.line_number, im.description, ol.quantity"
                    + " from OrderMaster om join OrderLine ol on om.order_id = ol.order_id join ItemMaster im on ol.item_id = im.item_id"
                    + " where om.order_id = #{parameters.p1,jdbcType=INTEGER}";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatment);
            
            List<OrderMaster> rows = mapper.selectMany(selectStatement);

            assertThat(rows.size()).isEqualTo(1);
            OrderMaster orderMaster = rows.get(0);
            assertThat(orderMaster.getId()).isEqualTo(2);
            assertThat(orderMaster.getDetails().size()).isEqualTo(2);
            OrderDetail orderDetail = orderMaster.getDetails().get(0);
            assertThat(orderDetail.getLineNumber()).isEqualTo(1);
            orderDetail = orderMaster.getDetails().get(1);
            assertThat(orderDetail.getLineNumber()).isEqualTo(2);
        }
    }

    @Test
    public void testMultipleTableJoinWithComplexWhereClause() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            JoinMapper mapper = session.getMapper(JoinMapper.class);
            
            SelectStatementProvider selectStatement = select(orderMaster.orderId, orderDate, orderLine.lineNumber, itemMaster.description, orderLine.quantity)
                    .from(orderMaster, "om")
                    .join(orderLine, "ol").on(orderMaster.orderId, equalTo(orderLine.orderId))
                    .join(itemMaster, "im").on(orderLine.itemId, equalTo(itemMaster.itemId))
                    .where(orderMaster.orderId, isEqualTo(2), and(orderLine.lineNumber, isEqualTo(2)))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expectedStatment = "select om.order_id, om.order_date, ol.line_number, im.description, ol.quantity"
                    + " from OrderMaster om join OrderLine ol on om.order_id = ol.order_id join ItemMaster im on ol.item_id = im.item_id"
                    + " where (om.order_id = #{parameters.p1,jdbcType=INTEGER} and ol.line_number = #{parameters.p2,jdbcType=INTEGER})";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatment);
            
            List<OrderMaster> rows = mapper.selectMany(selectStatement);

            assertThat(rows.size()).isEqualTo(1);
            OrderMaster orderMaster = rows.get(0);
            assertThat(orderMaster.getId()).isEqualTo(2);
            assertThat(orderMaster.getDetails().size()).isEqualTo(1);
            OrderDetail orderDetail = orderMaster.getDetails().get(0);
            assertThat(orderDetail.getLineNumber()).isEqualTo(2);
        }
    }

    @Test
    public void testMultipleTableJoinWithOrderBy() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            JoinMapper mapper = session.getMapper(JoinMapper.class);
            
            SelectStatementProvider selectStatement = select(orderMaster.orderId, orderDate, orderLine.lineNumber, itemMaster.description, orderLine.quantity)
                    .from(orderMaster, "om")
                    .join(orderLine, "ol").on(orderMaster.orderId, equalTo(orderLine.orderId))
                    .join(itemMaster, "im").on(orderLine.itemId, equalTo(itemMaster.itemId))
                    .orderBy(orderMaster.orderId)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expectedStatment = "select om.order_id, om.order_date, ol.line_number, im.description, ol.quantity"
                    + " from OrderMaster om join OrderLine ol on om.order_id = ol.order_id join ItemMaster im on ol.item_id = im.item_id"
                    + " order by order_id";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatment);
            
            List<OrderMaster> rows = mapper.selectMany(selectStatement);

            assertThat(rows.size()).isEqualTo(2);
            OrderMaster orderMaster = rows.get(0);
            assertThat(orderMaster.getId()).isEqualTo(1);
            assertThat(orderMaster.getDetails().size()).isEqualTo(1);
            OrderDetail orderDetail = orderMaster.getDetails().get(0);
            assertThat(orderDetail.getLineNumber()).isEqualTo(1);

            orderMaster = rows.get(1);
            assertThat(orderMaster.getId()).isEqualTo(2);
            assertThat(orderMaster.getDetails().size()).isEqualTo(2);
            orderDetail = orderMaster.getDetails().get(0);
            assertThat(orderDetail.getLineNumber()).isEqualTo(1);
            orderDetail = orderMaster.getDetails().get(1);
            assertThat(orderDetail.getLineNumber()).isEqualTo(2);
        }
    }

    @Test
    public void testMultibleTableJoinNoAliasWithOrderBy() {
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
            
            String expectedStatment = "select OrderMaster.order_id, OrderMaster.order_date, OrderLine.line_number, ItemMaster.description, OrderLine.quantity"
                    + " from OrderMaster join OrderLine on OrderMaster.order_id = OrderLine.order_id join ItemMaster on OrderLine.item_id = ItemMaster.item_id"
                    + " where OrderMaster.order_id = #{parameters.p1,jdbcType=INTEGER}"
                    + " order by order_id";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatment);
            
            List<OrderMaster> rows = mapper.selectMany(selectStatement);

            assertThat(rows.size()).isEqualTo(1);
            OrderMaster orderMaster = rows.get(0);
            assertThat(orderMaster.getId()).isEqualTo(2);
            assertThat(orderMaster.getDetails().size()).isEqualTo(2);
            OrderDetail orderDetail = orderMaster.getDetails().get(0);
            assertThat(orderDetail.getLineNumber()).isEqualTo(1);
            orderDetail = orderMaster.getDetails().get(1);
            assertThat(orderDetail.getLineNumber()).isEqualTo(2);
        }
    }
    
    @Test
    public void testRightJoin() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            JoinMapper mapper = session.getMapper(JoinMapper.class);
            
            SelectStatementProvider selectStatement = select(orderLine.orderId, orderLine.quantity, itemMaster.itemId, itemMaster.description)
                    .from(orderLine, "ol")
                    .rightJoin(itemMaster, "im").on(orderLine.itemId, equalTo(itemMaster.itemId))
                    .orderBy(itemMaster.itemId)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expectedStatment = "select ol.order_id, ol.quantity, im.item_id, im.description"
                    + " from OrderLine ol right join ItemMaster im on ol.item_id = im.item_id"
                    + " order by item_id";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatment);
            
            List<Map<String, Object>> rows = mapper.generalSelect(selectStatement);

            assertThat(rows.size()).isEqualTo(5);
            Map<String, Object> row = rows.get(2);
            assertThat(row.get("ORDER_ID")).isEqualTo(1);
            assertThat(row.get("QUANTITY")).isEqualTo(1);
            assertThat(row.get("DESCRIPTION")).isEqualTo("First Base Glove");
            assertThat(row.get("ITEM_ID")).isEqualTo(33);

            row = rows.get(4);
            assertThat(row.get("ORDER_ID")).isNull();
            assertThat(row.get("QUANTITY")).isNull();
            assertThat(row.get("DESCRIPTION")).isEqualTo("Catcher Glove");
            assertThat(row.get("ITEM_ID")).isEqualTo(55);
        }
    }

    @Test
    public void testRightJoin2() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            JoinMapper mapper = session.getMapper(JoinMapper.class);
            
            SelectStatementProvider selectStatement = select(orderLine.orderId, orderLine.quantity, itemMaster.itemId, itemMaster.description)
                    .from(orderMaster, "om")
                    .join(orderLine, "ol").on(orderMaster.orderId, equalTo(orderLine.orderId))
                    .rightJoin(itemMaster, "im").on(orderLine.itemId, equalTo(itemMaster.itemId))
                    .orderBy(orderLine.orderId, itemMaster.itemId)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expectedStatment = "select ol.order_id, ol.quantity, im.item_id, im.description"
                    + " from OrderMaster om join OrderLine ol on om.order_id = ol.order_id"
                    + " right join ItemMaster im on ol.item_id = im.item_id"
                    + " order by order_id, item_id";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatment);
            
            List<Map<String, Object>> rows = mapper.generalSelect(selectStatement);

            assertThat(rows.size()).isEqualTo(5);
            Map<String, Object> row = rows.get(0);
            assertThat(row.get("ORDER_ID")).isNull();
            assertThat(row.get("QUANTITY")).isNull();
            assertThat(row.get("DESCRIPTION")).isEqualTo("Catcher Glove");
            assertThat(row.get("ITEM_ID")).isEqualTo(55);

            row = rows.get(4);
            assertThat(row.get("ORDER_ID")).isEqualTo(2);
            assertThat(row.get("QUANTITY")).isEqualTo(1);
            assertThat(row.get("DESCRIPTION")).isEqualTo("Outfield Glove");
            assertThat(row.get("ITEM_ID")).isEqualTo(44);
        }
    }

    @Test
    public void testRightJoin3() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            JoinMapper mapper = session.getMapper(JoinMapper.class);
            
            SelectStatementProvider selectStatement = select(orderLine.orderId, orderLine.quantity, itemMaster.itemId, itemMaster.description)
                    .from(orderMaster, "om")
                    .join(orderLine, "ol", on(orderMaster.orderId, equalTo(orderLine.orderId)))
                    .rightJoin(itemMaster, "im", on(orderLine.itemId, equalTo(itemMaster.itemId)))
                    .orderBy(orderLine.orderId, itemMaster.itemId)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expectedStatment = "select ol.order_id, ol.quantity, im.item_id, im.description"
                    + " from OrderMaster om join OrderLine ol on om.order_id = ol.order_id"
                    + " right join ItemMaster im on ol.item_id = im.item_id"
                    + " order by order_id, item_id";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatment);
            
            List<Map<String, Object>> rows = mapper.generalSelect(selectStatement);

            assertThat(rows.size()).isEqualTo(5);
            Map<String, Object> row = rows.get(0);
            assertThat(row.get("ORDER_ID")).isNull();
            assertThat(row.get("QUANTITY")).isNull();
            assertThat(row.get("DESCRIPTION")).isEqualTo("Catcher Glove");
            assertThat(row.get("ITEM_ID")).isEqualTo(55);

            row = rows.get(4);
            assertThat(row.get("ORDER_ID")).isEqualTo(2);
            assertThat(row.get("QUANTITY")).isEqualTo(1);
            assertThat(row.get("DESCRIPTION")).isEqualTo("Outfield Glove");
            assertThat(row.get("ITEM_ID")).isEqualTo(44);
        }
    }

    @Test
    public void testRightJoinNoAliases() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            JoinMapper mapper = session.getMapper(JoinMapper.class);
            
            SelectStatementProvider selectStatement = select(orderLine.orderId, orderLine.quantity, itemMaster.itemId, itemMaster.description)
                    .from(orderMaster)
                    .join(orderLine).on(orderMaster.orderId, equalTo(orderLine.orderId))
                    .rightJoin(itemMaster).on(orderLine.itemId, equalTo(itemMaster.itemId))
                    .orderBy(orderLine.orderId, itemMaster.itemId)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expectedStatment = "select OrderLine.order_id, OrderLine.quantity, ItemMaster.item_id, ItemMaster.description"
                    + " from OrderMaster join OrderLine on OrderMaster.order_id = OrderLine.order_id"
                    + " right join ItemMaster on OrderLine.item_id = ItemMaster.item_id"
                    + " order by order_id, item_id";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatment);
            
            List<Map<String, Object>> rows = mapper.generalSelect(selectStatement);

            assertThat(rows.size()).isEqualTo(5);
            Map<String, Object> row = rows.get(0);
            assertThat(row.get("ORDER_ID")).isNull();
            assertThat(row.get("QUANTITY")).isNull();
            assertThat(row.get("DESCRIPTION")).isEqualTo("Catcher Glove");
            assertThat(row.get("ITEM_ID")).isEqualTo(55);

            row = rows.get(4);
            assertThat(row.get("ORDER_ID")).isEqualTo(2);
            assertThat(row.get("QUANTITY")).isEqualTo(1);
            assertThat(row.get("DESCRIPTION")).isEqualTo("Outfield Glove");
            assertThat(row.get("ITEM_ID")).isEqualTo(44);
        }
    }

    @Test
    public void testLeftJoin() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            JoinMapper mapper = session.getMapper(JoinMapper.class);
            
            SelectStatementProvider selectStatement = select(orderLine.orderId, orderLine.quantity, itemMaster.itemId, itemMaster.description)
                    .from(itemMaster, "im")
                    .leftJoin(orderLine, "ol").on(orderLine.itemId, equalTo(itemMaster.itemId))
                    .orderBy(itemMaster.itemId)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expectedStatment = "select ol.order_id, ol.quantity, im.item_id, im.description"
                    + " from ItemMaster im left join OrderLine ol on ol.item_id = im.item_id"
                    + " order by item_id";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatment);
            
            List<Map<String, Object>> rows = mapper.generalSelect(selectStatement);

            assertThat(rows.size()).isEqualTo(5);
            Map<String, Object> row = rows.get(2);
            assertThat(row.get("ORDER_ID")).isEqualTo(1);
            assertThat(row.get("QUANTITY")).isEqualTo(1);
            assertThat(row.get("DESCRIPTION")).isEqualTo("First Base Glove");
            assertThat(row.get("ITEM_ID")).isEqualTo(33);

            row = rows.get(4);
            assertThat(row.get("ORDER_ID")).isNull();
            assertThat(row.get("QUANTITY")).isNull();
            assertThat(row.get("DESCRIPTION")).isEqualTo("Catcher Glove");
            assertThat(row.get("ITEM_ID")).isEqualTo(55);
        }
    }

    @Test
    public void testLeftJoin2() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            JoinMapper mapper = session.getMapper(JoinMapper.class);
            
            SelectStatementProvider selectStatement = select(orderLine.orderId, orderLine.quantity, itemMaster.itemId, itemMaster.description)
                    .from(orderMaster, "om")
                    .join(orderLine, "ol").on(orderMaster.orderId, equalTo(orderLine.orderId))
                    .leftJoin(itemMaster, "im").on(orderLine.itemId, equalTo(itemMaster.itemId))
                    .orderBy(orderLine.orderId, itemMaster.itemId)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expectedStatment = "select ol.order_id, ol.quantity, im.item_id, im.description"
                    + " from OrderMaster om join OrderLine ol on om.order_id = ol.order_id"
                    + " left join ItemMaster im on ol.item_id = im.item_id"
                    + " order by order_id, item_id";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatment);
            
            List<Map<String, Object>> rows = mapper.generalSelect(selectStatement);

            assertThat(rows.size()).isEqualTo(5);
            Map<String, Object> row = rows.get(2);
            assertThat(row.get("ORDER_ID")).isEqualTo(2);
            assertThat(row.get("QUANTITY")).isEqualTo(6);
            assertThat(row.get("DESCRIPTION")).isNull();
            assertThat(row.get("ITEM_ID")).isNull();

            row = rows.get(4);
            assertThat(row.get("ORDER_ID")).isEqualTo(2);
            assertThat(row.get("QUANTITY")).isEqualTo(1);
            assertThat(row.get("DESCRIPTION")).isEqualTo("Outfield Glove");
            assertThat(row.get("ITEM_ID")).isEqualTo(44);
        }
    }

    @Test
    public void testLeftJoin3() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            JoinMapper mapper = session.getMapper(JoinMapper.class);
            
            SelectStatementProvider selectStatement = select(orderLine.orderId, orderLine.quantity, itemMaster.itemId, itemMaster.description)
                    .from(orderMaster, "om")
                    .join(orderLine, "ol", on(orderMaster.orderId, equalTo(orderLine.orderId)))
                    .leftJoin(itemMaster, "im", on(orderLine.itemId, equalTo(itemMaster.itemId)))
                    .orderBy(orderLine.orderId, itemMaster.itemId)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expectedStatment = "select ol.order_id, ol.quantity, im.item_id, im.description"
                    + " from OrderMaster om join OrderLine ol on om.order_id = ol.order_id"
                    + " left join ItemMaster im on ol.item_id = im.item_id"
                    + " order by order_id, item_id";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatment);
            
            List<Map<String, Object>> rows = mapper.generalSelect(selectStatement);

            assertThat(rows.size()).isEqualTo(5);
            Map<String, Object> row = rows.get(2);
            assertThat(row.get("ORDER_ID")).isEqualTo(2);
            assertThat(row.get("QUANTITY")).isEqualTo(6);
            assertThat(row.get("DESCRIPTION")).isNull();
            assertThat(row.get("ITEM_ID")).isNull();

            row = rows.get(4);
            assertThat(row.get("ORDER_ID")).isEqualTo(2);
            assertThat(row.get("QUANTITY")).isEqualTo(1);
            assertThat(row.get("DESCRIPTION")).isEqualTo("Outfield Glove");
            assertThat(row.get("ITEM_ID")).isEqualTo(44);
        }
    }

    @Test
    public void testLeftJoinNoAliases() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            JoinMapper mapper = session.getMapper(JoinMapper.class);
            
            SelectStatementProvider selectStatement = select(orderLine.orderId, orderLine.quantity, itemMaster.itemId, itemMaster.description)
                    .from(orderMaster)
                    .join(orderLine).on(orderMaster.orderId, equalTo(orderLine.orderId))
                    .leftJoin(itemMaster).on(orderLine.itemId, equalTo(itemMaster.itemId))
                    .orderBy(orderLine.orderId, itemMaster.itemId)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expectedStatment = "select OrderLine.order_id, OrderLine.quantity, ItemMaster.item_id, ItemMaster.description"
                    + " from OrderMaster join OrderLine on OrderMaster.order_id = OrderLine.order_id"
                    + " left join ItemMaster on OrderLine.item_id = ItemMaster.item_id"
                    + " order by order_id, item_id";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatment);
            
            List<Map<String, Object>> rows = mapper.generalSelect(selectStatement);

            assertThat(rows.size()).isEqualTo(5);
            Map<String, Object> row = rows.get(2);
            assertThat(row.get("ORDER_ID")).isEqualTo(2);
            assertThat(row.get("QUANTITY")).isEqualTo(6);
            assertThat(row.get("DESCRIPTION")).isNull();
            assertThat(row.get("ITEM_ID")).isNull();

            row = rows.get(4);
            assertThat(row.get("ORDER_ID")).isEqualTo(2);
            assertThat(row.get("QUANTITY")).isEqualTo(1);
            assertThat(row.get("DESCRIPTION")).isEqualTo("Outfield Glove");
            assertThat(row.get("ITEM_ID")).isEqualTo(44);
        }
    }

    @Test
    public void testFullJoin() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            JoinMapper mapper = session.getMapper(JoinMapper.class);
            
            SelectStatementProvider selectStatement = select(orderLine.orderId, orderLine.quantity, orderLine.itemId.as("ol_itemid"), itemMaster.itemId.as("im_itemid"), itemMaster.description)
                    .from(itemMaster, "im")
                    .fullJoin(orderLine, "ol").on(itemMaster.itemId, equalTo(orderLine.itemId))
                    .orderBy(sortColumn("im_itemid"))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expectedStatment = "select ol.order_id, ol.quantity, ol.item_id as ol_itemid, im.item_id as im_itemid, im.description"
                    + " from ItemMaster im full join OrderLine ol on im.item_id = ol.item_id"
                    + " order by im_itemid";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatment);
            
            List<Map<String, Object>> rows = mapper.generalSelect(selectStatement);

            assertThat(rows.size()).isEqualTo(6);
            Map<String, Object> row = rows.get(0);
            assertThat(row.get("ORDER_ID")).isEqualTo(2);
            assertThat(row.get("QUANTITY")).isEqualTo(6);
            assertThat(row.get("OL_ITEMID")).isEqualTo(66);
            assertThat(row.get("DESCRIPTION")).isNull();
            assertThat(row.get("IM_ITEMID")).isNull();

            row = rows.get(3);
            assertThat(row.get("ORDER_ID")).isEqualTo(1);
            assertThat(row.get("QUANTITY")).isEqualTo(1);
            assertThat(row.get("DESCRIPTION")).isEqualTo("First Base Glove");
            assertThat(row.get("IM_ITEMID")).isEqualTo(33);

            row = rows.get(5);
            assertThat(row.get("ORDER_ID")).isNull();
            assertThat(row.get("QUANTITY")).isNull();
            assertThat(row.get("DESCRIPTION")).isEqualTo("Catcher Glove");
            assertThat(row.get("IM_ITEMID")).isEqualTo(55);
        }
    }

    @Test
    public void testFullJoin2() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            JoinMapper mapper = session.getMapper(JoinMapper.class);
            
            SelectStatementProvider selectStatement = select(orderLine.orderId, orderLine.quantity, itemMaster.itemId, itemMaster.description)
                    .from(orderMaster, "om")
                    .join(orderLine, "ol").on(orderMaster.orderId, equalTo(orderLine.orderId))
                    .fullJoin(itemMaster, "im").on(orderLine.itemId, equalTo(itemMaster.itemId))
                    .orderBy(orderLine.orderId, itemMaster.itemId)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expectedStatment = "select ol.order_id, ol.quantity, im.item_id, im.description"
                    + " from OrderMaster om join OrderLine ol on om.order_id = ol.order_id"
                    + " full join ItemMaster im on ol.item_id = im.item_id"
                    + " order by order_id, item_id";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatment);
            
            List<Map<String, Object>> rows = mapper.generalSelect(selectStatement);

            assertThat(rows.size()).isEqualTo(6);
            Map<String, Object> row = rows.get(0);
            assertThat(row.get("ORDER_ID")).isNull();
            assertThat(row.get("QUANTITY")).isNull();
            assertThat(row.get("DESCRIPTION")).isEqualTo("Catcher Glove");
            assertThat(row.get("ITEM_ID")).isEqualTo(55);

            row = rows.get(3);
            assertThat(row.get("ORDER_ID")).isEqualTo(2);
            assertThat(row.get("QUANTITY")).isEqualTo(6);
            assertThat(row.get("DESCRIPTION")).isNull();
            assertThat(row.get("ITEM_ID")).isNull();

            row = rows.get(5);
            assertThat(row.get("ORDER_ID")).isEqualTo(2);
            assertThat(row.get("QUANTITY")).isEqualTo(1);
            assertThat(row.get("DESCRIPTION")).isEqualTo("Outfield Glove");
            assertThat(row.get("ITEM_ID")).isEqualTo(44);
        }
    }

    @Test
    public void testFullJoin3() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            JoinMapper mapper = session.getMapper(JoinMapper.class);
            
            SelectStatementProvider selectStatement = select(orderLine.orderId, orderLine.quantity, itemMaster.itemId, itemMaster.description)
                    .from(orderMaster, "om")
                    .join(orderLine, "ol", on(orderMaster.orderId, equalTo(orderLine.orderId)))
                    .fullJoin(itemMaster, "im", on(orderLine.itemId, equalTo(itemMaster.itemId)))
                    .orderBy(orderLine.orderId, itemMaster.itemId)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expectedStatment = "select ol.order_id, ol.quantity, im.item_id, im.description"
                    + " from OrderMaster om join OrderLine ol on om.order_id = ol.order_id"
                    + " full join ItemMaster im on ol.item_id = im.item_id"
                    + " order by order_id, item_id";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatment);
            
            List<Map<String, Object>> rows = mapper.generalSelect(selectStatement);

            assertThat(rows.size()).isEqualTo(6);
            Map<String, Object> row = rows.get(0);
            assertThat(row.get("ORDER_ID")).isNull();
            assertThat(row.get("QUANTITY")).isNull();
            assertThat(row.get("DESCRIPTION")).isEqualTo("Catcher Glove");
            assertThat(row.get("ITEM_ID")).isEqualTo(55);

            row = rows.get(3);
            assertThat(row.get("ORDER_ID")).isEqualTo(2);
            assertThat(row.get("QUANTITY")).isEqualTo(6);
            assertThat(row.get("DESCRIPTION")).isNull();
            assertThat(row.get("ITEM_ID")).isNull();

            row = rows.get(5);
            assertThat(row.get("ORDER_ID")).isEqualTo(2);
            assertThat(row.get("QUANTITY")).isEqualTo(1);
            assertThat(row.get("DESCRIPTION")).isEqualTo("Outfield Glove");
            assertThat(row.get("ITEM_ID")).isEqualTo(44);
        }
    }

    @Test
    public void testFullJoinNoAliases() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            JoinMapper mapper = session.getMapper(JoinMapper.class);
            
            SelectStatementProvider selectStatement = select(orderLine.orderId, orderLine.quantity, itemMaster.itemId, itemMaster.description)
                    .from(orderMaster)
                    .join(orderLine).on(orderMaster.orderId, equalTo(orderLine.orderId))
                    .fullJoin(itemMaster).on(orderLine.itemId, equalTo(itemMaster.itemId))
                    .orderBy(orderLine.orderId, itemMaster.itemId)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expectedStatment = "select OrderLine.order_id, OrderLine.quantity, ItemMaster.item_id, ItemMaster.description"
                    + " from OrderMaster join OrderLine on OrderMaster.order_id = OrderLine.order_id"
                    + " full join ItemMaster on OrderLine.item_id = ItemMaster.item_id"
                    + " order by order_id, item_id";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatment);
            
            List<Map<String, Object>> rows = mapper.generalSelect(selectStatement);

            assertThat(rows.size()).isEqualTo(6);
            Map<String, Object> row = rows.get(0);
            assertThat(row.get("ORDER_ID")).isNull();
            assertThat(row.get("QUANTITY")).isNull();
            assertThat(row.get("DESCRIPTION")).isEqualTo("Catcher Glove");
            assertThat(row.get("ITEM_ID")).isEqualTo(55);

            row = rows.get(3);
            assertThat(row.get("ORDER_ID")).isEqualTo(2);
            assertThat(row.get("QUANTITY")).isEqualTo(6);
            assertThat(row.get("DESCRIPTION")).isNull();
            assertThat(row.get("ITEM_ID")).isNull();

            row = rows.get(5);
            assertThat(row.get("ORDER_ID")).isEqualTo(2);
            assertThat(row.get("QUANTITY")).isEqualTo(1);
            assertThat(row.get("DESCRIPTION")).isEqualTo("Outfield Glove");
            assertThat(row.get("ITEM_ID")).isEqualTo(44);
        }
    }

    @Test
    public void testSelf() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            JoinMapper mapper = session.getMapper(JoinMapper.class);
            
            // get Bamm Bamm's parent - should be Barney
            SelectStatementProvider selectStatement = select(user1.userId, user1.userName, user1.parentId)
                    .from(user1, "u1")
                    .join(user2, "u2").on(user1.userId, equalTo(user2.parentId))
                    .where(user2.userId, isEqualTo(4))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expectedStatment = "select u1.user_id, u1.user_name, u1.parent_id"
                    + " from User u1 join User u2 on u1.user_id = u2.parent_id"
                    + " where u2.user_id = #{parameters.p1,jdbcType=INTEGER}";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatment);
            
            List<User> rows = mapper.selectUsers(selectStatement);

            assertThat(rows.size()).isEqualTo(1);
            User row = rows.get(0);
            assertThat(row.getUserId()).isEqualTo(2);
            assertThat(row.getUserName()).isEqualTo("Barney");
            assertThat(row.getParentId()).isNull();
        }
    }

    @Test
    public void testLimitAndOffsetAfterJoin() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            JoinMapper mapper = session.getMapper(JoinMapper.class);
            
            SelectStatementProvider selectStatement = select(orderLine.orderId, orderLine.quantity, itemMaster.itemId, itemMaster.description)
                    .from(itemMaster, "im")
                    .leftJoin(orderLine, "ol").on(orderLine.itemId, equalTo(itemMaster.itemId))
                    .limit(2)
                    .offset(1)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expectedStatment = "select ol.order_id, ol.quantity, im.item_id, im.description"
                    + " from ItemMaster im left join OrderLine ol on ol.item_id = im.item_id"
                    + " limit #{parameters._limit} offset #{parameters._offset}";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatment);
            
            List<Map<String, Object>> rows = mapper.generalSelect(selectStatement);

            assertThat(rows.size()).isEqualTo(2);
            Map<String, Object> row = rows.get(0);
            assertThat(row.get("ORDER_ID")).isEqualTo(2);
            assertThat(row.get("QUANTITY")).isEqualTo(1);
            assertThat(row.get("DESCRIPTION")).isEqualTo("Helmet");
            assertThat(row.get("ITEM_ID")).isEqualTo(22);

            row = rows.get(1);
            assertThat(row.get("ORDER_ID")).isEqualTo(1);
            assertThat(row.get("QUANTITY")).isEqualTo(1);
            assertThat(row.get("DESCRIPTION")).isEqualTo("First Base Glove");
            assertThat(row.get("ITEM_ID")).isEqualTo(33);
        }
    }

    @Test
    public void testLimitOnlyAfterJoin() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            JoinMapper mapper = session.getMapper(JoinMapper.class);
            
            SelectStatementProvider selectStatement = select(orderLine.orderId, orderLine.quantity, itemMaster.itemId, itemMaster.description)
                    .from(itemMaster, "im")
                    .leftJoin(orderLine, "ol").on(orderLine.itemId, equalTo(itemMaster.itemId))
                    .limit(2)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expectedStatment = "select ol.order_id, ol.quantity, im.item_id, im.description"
                    + " from ItemMaster im left join OrderLine ol on ol.item_id = im.item_id"
                    + " limit #{parameters._limit}";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatment);
            
            List<Map<String, Object>> rows = mapper.generalSelect(selectStatement);

            assertThat(rows.size()).isEqualTo(2);
            Map<String, Object> row = rows.get(0);
            assertThat(row.get("ORDER_ID")).isEqualTo(1);
            assertThat(row.get("QUANTITY")).isEqualTo(1);
            assertThat(row.get("DESCRIPTION")).isEqualTo("Helmet");
            assertThat(row.get("ITEM_ID")).isEqualTo(22);

            row = rows.get(1);
            assertThat(row.get("ORDER_ID")).isEqualTo(2);
            assertThat(row.get("QUANTITY")).isEqualTo(1);
            assertThat(row.get("DESCRIPTION")).isEqualTo("Helmet");
            assertThat(row.get("ITEM_ID")).isEqualTo(22);
        }
    }

    @Test
    public void testOffsetOnlyAfterJoin() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            JoinMapper mapper = session.getMapper(JoinMapper.class);
            
            SelectStatementProvider selectStatement = select(orderLine.orderId, orderLine.quantity, itemMaster.itemId, itemMaster.description)
                    .from(itemMaster, "im")
                    .leftJoin(orderLine, "ol").on(orderLine.itemId, equalTo(itemMaster.itemId))
                    .offset(2)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expectedStatment = "select ol.order_id, ol.quantity, im.item_id, im.description"
                    + " from ItemMaster im left join OrderLine ol on ol.item_id = im.item_id"
                    + " offset #{parameters._offset} rows";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatment);
            
            List<Map<String, Object>> rows = mapper.generalSelect(selectStatement);

            assertThat(rows.size()).isEqualTo(3);
            Map<String, Object> row = rows.get(0);
            assertThat(row.get("ORDER_ID")).isEqualTo(1);
            assertThat(row.get("QUANTITY")).isEqualTo(1);
            assertThat(row.get("DESCRIPTION")).isEqualTo("First Base Glove");
            assertThat(row.get("ITEM_ID")).isEqualTo(33);

            row = rows.get(1);
            assertThat(row.get("ORDER_ID")).isEqualTo(2);
            assertThat(row.get("QUANTITY")).isEqualTo(1);
            assertThat(row.get("DESCRIPTION")).isEqualTo("Outfield Glove");
            assertThat(row.get("ITEM_ID")).isEqualTo(44);
        }
    }

    @Test
    public void testOffsetAndFetchFirstAfterJoin() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            JoinMapper mapper = session.getMapper(JoinMapper.class);
            
            SelectStatementProvider selectStatement = select(orderLine.orderId, orderLine.quantity, itemMaster.itemId, itemMaster.description)
                    .from(itemMaster, "im")
                    .leftJoin(orderLine, "ol").on(orderLine.itemId, equalTo(itemMaster.itemId))
                    .offset(1)
                    .fetchFirst(2).rowsOnly()
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expectedStatment = "select ol.order_id, ol.quantity, im.item_id, im.description"
                    + " from ItemMaster im left join OrderLine ol on ol.item_id = im.item_id"
                    + " offset #{parameters._offset} rows fetch first #{parameters._fetchFirstRows} rows only";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatment);
            
            List<Map<String, Object>> rows = mapper.generalSelect(selectStatement);

            assertThat(rows.size()).isEqualTo(2);
            Map<String, Object> row = rows.get(0);
            assertThat(row.get("ORDER_ID")).isEqualTo(2);
            assertThat(row.get("QUANTITY")).isEqualTo(1);
            assertThat(row.get("DESCRIPTION")).isEqualTo("Helmet");
            assertThat(row.get("ITEM_ID")).isEqualTo(22);

            row = rows.get(1);
            assertThat(row.get("ORDER_ID")).isEqualTo(1);
            assertThat(row.get("QUANTITY")).isEqualTo(1);
            assertThat(row.get("DESCRIPTION")).isEqualTo("First Base Glove");
            assertThat(row.get("ITEM_ID")).isEqualTo(33);
        }
    }

    @Test
    public void testFetchFirstOnlyAfterJoin() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            JoinMapper mapper = session.getMapper(JoinMapper.class);
            
            SelectStatementProvider selectStatement = select(orderLine.orderId, orderLine.quantity, itemMaster.itemId, itemMaster.description)
                    .from(itemMaster, "im")
                    .leftJoin(orderLine, "ol").on(orderLine.itemId, equalTo(itemMaster.itemId))
                    .fetchFirst(2).rowsOnly()
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expectedStatment = "select ol.order_id, ol.quantity, im.item_id, im.description"
                    + " from ItemMaster im left join OrderLine ol on ol.item_id = im.item_id"
                    + " fetch first #{parameters._fetchFirstRows} rows only";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedStatment);
            
            List<Map<String, Object>> rows = mapper.generalSelect(selectStatement);

            assertThat(rows.size()).isEqualTo(2);
            Map<String, Object> row = rows.get(0);
            assertThat(row.get("ORDER_ID")).isEqualTo(1);
            assertThat(row.get("QUANTITY")).isEqualTo(1);
            assertThat(row.get("DESCRIPTION")).isEqualTo("Helmet");
            assertThat(row.get("ITEM_ID")).isEqualTo(22);

            row = rows.get(1);
            assertThat(row.get("ORDER_ID")).isEqualTo(2);
            assertThat(row.get("QUANTITY")).isEqualTo(1);
            assertThat(row.get("DESCRIPTION")).isEqualTo("Helmet");
            assertThat(row.get("ITEM_ID")).isEqualTo(22);
        }
    }
}
