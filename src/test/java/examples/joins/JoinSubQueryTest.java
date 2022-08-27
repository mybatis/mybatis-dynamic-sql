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
import static examples.joins.OrderDetailDynamicSQLSupport.orderDetail;
import static examples.joins.OrderLineDynamicSQLSupport.orderLine;
import static examples.joins.OrderMasterDynamicSQLSupport.orderMaster;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.equalTo;
import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;
import static org.mybatis.dynamic.sql.SqlBuilder.select;
import static org.mybatis.dynamic.sql.SqlBuilder.sortColumn;

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
import org.mybatis.dynamic.sql.util.mybatis3.CommonSelectMapper;

class JoinSubQueryTest {

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

            SelectStatementProvider selectStatement = select(orderMaster.orderId, orderMaster.orderDate,
                    orderDetail.lineNumber, orderDetail.description, orderDetail.quantity)
                    .from(orderMaster, "om")
                    .join(select(orderDetail.orderId, orderDetail.lineNumber, orderDetail.description, orderDetail.quantity)
                          .from(orderDetail),
                          "od").on(orderMaster.orderId, equalTo(orderDetail.orderId.qualifiedWith("od")))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "select om.order_id, om.order_date, line_number, description, quantity"
                    + " from OrderMaster om join "
                    + "(select order_id, line_number, description, quantity from OrderDetail) od on om.order_id = od.order_id";
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
    void testMultipleTableJoinWithWhereClause() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            JoinMapper mapper = session.getMapper(JoinMapper.class);

            SelectStatementProvider selectStatement = select(orderMaster.orderId, orderMaster.orderDate,
                    orderLine.lineNumber, itemMaster.description, orderLine.quantity)
                    .from(orderMaster, "om")
                    .join(select(orderLine.orderId, orderLine.itemId, orderLine.quantity, orderLine.lineNumber)
                            .from(orderLine),
                            "ol")
                    .on(orderMaster.orderId, equalTo(orderLine.orderId.qualifiedWith("ol")))
                    .join(select(itemMaster.itemId, itemMaster.description)
                            .from(itemMaster),
                            "im")
                    .on(orderLine.itemId.qualifiedWith("ol"), equalTo(itemMaster.itemId.qualifiedWith("im")))
                    .where(orderMaster.orderId, isEqualTo(2))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "select om.order_id, om.order_date, line_number, description, quantity"
                    + " from OrderMaster om join "
                    + "(select order_id, item_id, quantity, line_number from OrderLine) ol on om.order_id = ol.order_id "
                    + "join (select item_id, description from ItemMaster) im on ol.item_id = im.item_id"
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
    void testMultipleTableJoinWithSelectStar() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            JoinMapper mapper = session.getMapper(JoinMapper.class);

            SelectStatementProvider selectStatement = select(orderMaster.orderId, orderMaster.orderDate, orderLine.lineNumber, itemMaster.description, orderLine.quantity)
                    .from(orderMaster, "om")
                    .join(select(orderLine.allColumns()).from(orderLine), "ol")
                    .on(orderMaster.orderId, equalTo(orderLine.orderId.qualifiedWith("ol")))
                    .join(select(itemMaster.allColumns()).from(itemMaster), "im")
                    .on(orderLine.itemId.qualifiedWith("ol"), equalTo(itemMaster.itemId.qualifiedWith("im")))
                    .where(orderMaster.orderId, isEqualTo(2))
                    .orderBy(orderMaster.orderId)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "select om.order_id, om.order_date, line_number, description, quantity"
                    + " from OrderMaster om join (select * from OrderLine) ol on om.order_id = ol.order_id"
                    + " join (select * from ItemMaster) im on ol.item_id = im.item_id"
                    + " where om.order_id = #{parameters.p1,jdbcType=INTEGER}"
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

            SelectStatementProvider selectStatement = select(orderLine.orderId, orderLine.quantity,
                    itemMaster.itemId.qualifiedWith("im"), itemMaster.description)
                    .from(orderLine, "ol")
                    .rightJoin(select(itemMaster.allColumns()).from(itemMaster), "im")
                    .on(orderLine.itemId, equalTo(itemMaster.itemId.qualifiedWith("im")))
                    .orderBy(itemMaster.itemId)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "select ol.order_id, ol.quantity, im.item_id, description"
                    + " from OrderLine ol right join (select * from ItemMaster) im on ol.item_id = im.item_id"
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

            SelectStatementProvider selectStatement = select(orderLine.orderId, orderLine.quantity,
                    itemMaster.itemId.qualifiedWith(("im")), itemMaster.description)
                    .from(orderMaster, "om")
                    .join(orderLine, "ol").on(orderMaster.orderId, equalTo(orderLine.orderId))
                    .rightJoin(select(itemMaster.allColumns()).from(itemMaster), "im")
                    .on(orderLine.itemId, equalTo(itemMaster.itemId.qualifiedWith("im")))
                    .orderBy(orderLine.orderId, itemMaster.itemId)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "select ol.order_id, ol.quantity, im.item_id, description"
                    + " from OrderMaster om join OrderLine ol on om.order_id = ol.order_id"
                    + " right join (select * from ItemMaster) im on ol.item_id = im.item_id"
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

            SelectStatementProvider selectStatement = select(orderLine.orderId, orderLine.quantity,
                    itemMaster.itemId.qualifiedWith("im"), itemMaster.description)
                    .from(itemMaster, "im")
                    .leftJoin(select(orderLine.allColumns()).from(orderLine), "ol")
                    .on(orderLine.itemId.qualifiedWith("ol"), equalTo(itemMaster.itemId))
                    .orderBy(itemMaster.itemId)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "select order_id, quantity, im.item_id, im.description"
                    + " from ItemMaster im"
                    + " left join (select * from OrderLine) ol on ol.item_id = im.item_id"
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

            SelectStatementProvider selectStatement = select(orderLine.orderId, orderLine.quantity,
                    itemMaster.itemId.qualifiedWith("im"), itemMaster.description)
                    .from(orderMaster, "om")
                    .join(orderLine, "ol").on(orderMaster.orderId, equalTo(orderLine.orderId))
                    .leftJoin(select(itemMaster.allColumns()).from(itemMaster), "im")
                    .on(orderLine.itemId, equalTo(itemMaster.itemId.qualifiedWith("im")))
                    .orderBy(orderLine.orderId, itemMaster.itemId)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "select ol.order_id, ol.quantity, im.item_id, description"
                    + " from OrderMaster om join OrderLine ol on om.order_id = ol.order_id"
                    + " left join (select * from ItemMaster) im on ol.item_id = im.item_id"
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

            SelectStatementProvider selectStatement = select(orderLine.orderId, orderLine.quantity,
                    orderLine.itemId.as("ol_itemid").qualifiedWith("ol"), itemMaster.itemId.as("im_itemid"), itemMaster.description)
                    .from(itemMaster, "im")
                    .fullJoin(select(orderLine.allColumns()).from(orderLine), "ol")
                    .on(itemMaster.itemId, equalTo(orderLine.itemId.qualifiedWith("ol")))
                    .orderBy(orderLine.orderId, sortColumn("im_itemid"))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "select order_id, quantity, ol.item_id as ol_itemid, im.item_id as im_itemid, im.description"
                    + " from ItemMaster im"
                    + " full join (select * from OrderLine) ol on im.item_id = ol.item_id"
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

            SelectStatementProvider selectStatement = select(orderLine.orderId, orderLine.quantity,
                    itemMaster.itemId.qualifiedWith("im"), itemMaster.description)
                    .from(orderMaster, "om")
                    .join(orderLine, "ol").on(orderMaster.orderId, equalTo(orderLine.orderId))
                    .fullJoin(select(itemMaster.allColumns()).from(itemMaster), "im")
                    .on(orderLine.itemId, equalTo(itemMaster.itemId.qualifiedWith("im")))
                    .orderBy(orderLine.orderId, itemMaster.itemId)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expectedStatement = "select ol.order_id, ol.quantity, im.item_id, description"
                    + " from OrderMaster om join OrderLine ol on om.order_id = ol.order_id"
                    + " full join (select * from ItemMaster) im on ol.item_id = im.item_id"
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
}
