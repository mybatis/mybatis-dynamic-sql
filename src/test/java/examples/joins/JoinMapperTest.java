/**
 *    Copyright 2016-2017 the original author or authors.
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

import static examples.joins.ItemMasterDynamicSQLSupport.*;
import static examples.joins.OrderDetailDynamicSQLSupport.*;
import static examples.joins.OrderLineDynamicSQLSupport.*;
import static examples.joins.OrderMasterDynamicSQLSupport.*;
import static org.mybatis.dynamic.sql.SqlConditions.*;
import static org.mybatis.dynamic.sql.select.join.JoinConditions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.select;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.render.SelectSupport;

@RunWith(JUnitPlatform.class)
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
        
        is = getClass().getResourceAsStream("/examples/joins/MapperConfig.xml");
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(is);
    }
    
    @Test
    public void testSingleTableJoin() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            JoinMapper mapper = session.getMapper(JoinMapper.class);
            
            SelectSupport selectSupport = select(orderMaster.orderId, orderDate, orderDetail.lineNumber, orderDetail.description, orderDetail.quantity)
                    .from(orderMaster, "om")
                    .join(orderDetail, "od").on(orderMaster.orderId, equalTo(orderDetail.orderId))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            
            String expectedStatment = "select om.order_id, om.order_date, od.line_number, od.description, od.quantity"
                    + " from OrderMaster om join OrderDetail od on om.order_id = od.order_id";
            assertThat(selectSupport.getFullSelectStatement()).isEqualTo(expectedStatment);
            
            List<OrderMaster> rows = mapper.selectMany(selectSupport);

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
        } finally {
            session.close();
        }
    }
    
    @Test
    public void testCompoundJoin1() {
        // this is a nonsensical join, but it does test the "and" capability
        SelectSupport selectSupport = select(orderMaster.orderId, orderDate, orderDetail.lineNumber, orderDetail.description, orderDetail.quantity)
                .from(orderMaster, "om")
                .join(orderDetail, "od").on(orderMaster.orderId, equalTo(orderDetail.orderId), and(orderMaster.orderId, equalTo(orderDetail.orderId)))
                .build()
                .render(RenderingStrategy.MYBATIS3);
        
        String expectedStatment = "select om.order_id, om.order_date, od.line_number, od.description, od.quantity"
                + " from OrderMaster om join OrderDetail od on om.order_id = od.order_id and om.order_id = od.order_id";
        assertThat(selectSupport.getFullSelectStatement()).isEqualTo(expectedStatment);
    }

    @Test
    public void testCompoundJoin2() {
        // this is a nonsensical join, but it does test the "and" capability
        SelectSupport selectSupport = select(orderMaster.orderId, orderDate, orderDetail.lineNumber, orderDetail.description, orderDetail.quantity)
                .from(orderMaster, "om")
                .join(orderDetail, "od").on(orderMaster.orderId, equalTo(orderDetail.orderId))
                .and(orderMaster.orderId, equalTo(orderDetail.orderId))
                .build()
                .render(RenderingStrategy.MYBATIS3);
        
        String expectedStatment = "select om.order_id, om.order_date, od.line_number, od.description, od.quantity"
                + " from OrderMaster om join OrderDetail od on om.order_id = od.order_id and om.order_id = od.order_id";
        assertThat(selectSupport.getFullSelectStatement()).isEqualTo(expectedStatment);
    }
    
    @Test
    public void testMultipleTableJoinWithWhereClause() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            JoinMapper mapper = session.getMapper(JoinMapper.class);
            
            SelectSupport selectSupport = select(orderMaster.orderId, orderDate, orderLine.lineNumber, itemMaster.description, orderLine.quantity)
                    .from(orderMaster, "om")
                    .join(orderLine, "ol").on(orderMaster.orderId, equalTo(orderLine.orderId))
                    .join(itemMaster, "im").on(orderLine.itemId, equalTo(itemMaster.itemId))
                    .where(orderMaster.orderId, isEqualTo(2))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            
            String expectedStatment = "select om.order_id, om.order_date, ol.line_number, im.description, ol.quantity"
                    + " from OrderMaster om join OrderLine ol on om.order_id = ol.order_id join ItemMaster im on ol.item_id = im.item_id"
                    + " where om.order_id = #{parameters.p1,jdbcType=INTEGER}";
            assertThat(selectSupport.getFullSelectStatement()).isEqualTo(expectedStatment);
            
            List<OrderMaster> rows = mapper.selectMany(selectSupport);

            assertThat(rows.size()).isEqualTo(1);
            OrderMaster orderMaster = rows.get(0);
            assertThat(orderMaster.getId()).isEqualTo(2);
            assertThat(orderMaster.getDetails().size()).isEqualTo(2);
            OrderDetail orderDetail = orderMaster.getDetails().get(0);
            assertThat(orderDetail.getLineNumber()).isEqualTo(1);
            orderDetail = orderMaster.getDetails().get(1);
            assertThat(orderDetail.getLineNumber()).isEqualTo(2);
        } finally {
            session.close();
        }
    }

    @Test
    public void testMultipleTableJoinWithComplexWhereClause() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            JoinMapper mapper = session.getMapper(JoinMapper.class);
            
            SelectSupport selectSupport = select(orderMaster.orderId, orderDate, orderLine.lineNumber, itemMaster.description, orderLine.quantity)
                    .from(orderMaster, "om")
                    .join(orderLine, "ol").on(orderMaster.orderId, equalTo(orderLine.orderId))
                    .join(itemMaster, "im").on(orderLine.itemId, equalTo(itemMaster.itemId))
                    .where(orderMaster.orderId, isEqualTo(2), and(orderLine.lineNumber, isEqualTo(2)))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            
            String expectedStatment = "select om.order_id, om.order_date, ol.line_number, im.description, ol.quantity"
                    + " from OrderMaster om join OrderLine ol on om.order_id = ol.order_id join ItemMaster im on ol.item_id = im.item_id"
                    + " where (om.order_id = #{parameters.p1,jdbcType=INTEGER} and ol.line_number = #{parameters.p2,jdbcType=INTEGER})";
            assertThat(selectSupport.getFullSelectStatement()).isEqualTo(expectedStatment);
            
            List<OrderMaster> rows = mapper.selectMany(selectSupport);

            assertThat(rows.size()).isEqualTo(1);
            OrderMaster orderMaster = rows.get(0);
            assertThat(orderMaster.getId()).isEqualTo(2);
            assertThat(orderMaster.getDetails().size()).isEqualTo(1);
            OrderDetail orderDetail = orderMaster.getDetails().get(0);
            assertThat(orderDetail.getLineNumber()).isEqualTo(2);
        } finally {
            session.close();
        }
    }

    @Test
    public void testMultipleTableJoinWithOrderBy() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            JoinMapper mapper = session.getMapper(JoinMapper.class);
            
            SelectSupport selectSupport = select(orderMaster.orderId, orderDate, orderLine.lineNumber, itemMaster.description, orderLine.quantity)
                    .from(orderMaster, "om")
                    .join(orderLine, "ol").on(orderMaster.orderId, equalTo(orderLine.orderId))
                    .join(itemMaster, "im").on(orderLine.itemId, equalTo(itemMaster.itemId))
                    .orderBy(orderMaster.orderId)
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            
            String expectedStatment = "select om.order_id, om.order_date, ol.line_number, im.description, ol.quantity"
                    + " from OrderMaster om join OrderLine ol on om.order_id = ol.order_id join ItemMaster im on ol.item_id = im.item_id"
                    + " order by order_id ASC";
            assertThat(selectSupport.getFullSelectStatement()).isEqualTo(expectedStatment);
            
            List<OrderMaster> rows = mapper.selectMany(selectSupport);

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
        } finally {
            session.close();
        }
    }

    @Test
    @Disabled("Disabled until we have a solution for no aliases")
    public void testMultibleTableJoinNoAliasWithOrderBy() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            JoinMapper mapper = session.getMapper(JoinMapper.class);
            
            SelectSupport selectSupport = select(orderMaster.orderId, orderDate, orderLine.lineNumber, itemMaster.description, orderLine.quantity)
                    .from(orderMaster)
                    .join(orderLine).on(orderMaster.orderId, equalTo(orderLine.orderId))
                    .join(itemMaster).on(orderLine.itemId, equalTo(itemMaster.itemId))
                    .orderBy(orderMaster.orderId)
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            
            String expectedStatment = "select order_id, order_date, line_number, description, quantity"
                    + " from OrderMaster join OrderLine on order_id = order_id join ItemMaster on item_id = item_id"
                    + " order by order_id ASC";
            assertThat(selectSupport.getFullSelectStatement()).isEqualTo(expectedStatment);
            
            List<OrderMaster> rows = mapper.selectMany(selectSupport);

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
        } finally {
            session.close();
        }
    }
}
