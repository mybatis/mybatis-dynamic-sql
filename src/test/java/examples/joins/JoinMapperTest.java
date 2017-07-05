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

import static org.mybatis.dynamic.sql.select.join.JoinConditions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.select;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.JDBCType;
import java.util.Date;
import java.util.List;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.render.SelectSupport;

@RunWith(JUnitPlatform.class)
public class JoinMapperTest {

    private static final SqlTable orderMaster = SqlTable.of("OrderMaster").withAlias("om");
    private static final SqlColumn<Integer> orderId = SqlColumn.of("order_id", JDBCType.INTEGER).inTable(orderMaster);
    private static final SqlColumn<Date> orderDate = SqlColumn.of("order_date", JDBCType.DATE).inTable(orderMaster);
    
    private static final SqlTable orderDetail = SqlTable.of("OrderDetail").withAlias("od");
    private static final SqlColumn<Integer> orderId_od = SqlColumn.of("order_id", JDBCType.INTEGER).inTable(orderDetail);
    private static final SqlColumn<Integer> lineNumber = SqlColumn.of("line_number", JDBCType.INTEGER).inTable(orderDetail);
    private static final SqlColumn<String> description = SqlColumn.of("description", JDBCType.VARCHAR).inTable(orderDetail);
    private static final SqlColumn<Integer> quantity = SqlColumn.of("quantity", JDBCType.INTEGER).inTable(orderDetail);
    
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
    public void testSelectByExample() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            JoinMapper mapper = session.getMapper(JoinMapper.class);
            
            SelectSupport selectSupport = select(orderId, orderDate, lineNumber, description, quantity)
                    .from(orderMaster)
                    .join(orderDetail).on(orderId, equalTo(orderId_od))
                    .buildAndRender(RenderingStrategy.MYBATIS3);
            
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
}
