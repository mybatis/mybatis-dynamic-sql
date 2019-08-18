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
package examples.column.comparison;

import static examples.column.comparison.ColumnComparisonDynamicSqlSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

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

public class ColumnComparisonTest {
    
    private static final String JDBC_URL = "jdbc:hsqldb:mem:aname";
    private static final String JDBC_DRIVER = "org.hsqldb.jdbcDriver"; 
    
    private SqlSessionFactory sqlSessionFactory;
    
    @BeforeEach
    public void setup() throws Exception {
        Class.forName(JDBC_DRIVER);
        InputStream is = getClass().getResourceAsStream("/examples/column/comparison/CreateDB.sql");
        try (Connection connection = DriverManager.getConnection(JDBC_URL, "sa", "")) {
            ScriptRunner sr = new ScriptRunner(connection);
            sr.setLogWriter(null);
            sr.runScript(new InputStreamReader(is));
        }
        
        UnpooledDataSource ds = new UnpooledDataSource(JDBC_DRIVER, JDBC_URL, "sa", "");
        Environment environment = new Environment("test", new JdbcTransactionFactory(), ds);
        Configuration config = new Configuration(environment);
        config.addMapper(ColumnComparisonMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(config);
    }
    
    @Test
    public void testColumnComparisonLessThan() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            ColumnComparisonMapper mapper = sqlSession.getMapper(ColumnComparisonMapper.class);
            
            SelectStatementProvider selectStatement = select(number1, number2)
                    .from(columnComparison)
                    .where(number1, isLessThan(number2))
                    .orderBy(number1, number2)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expected = "select number1, number2 "
                    + "from ColumnComparison "
                    + "where number1 < number2 "
                    + "order by number1, number2";
            
            List<ColumnComparisonRecord> records = mapper.selectMany(selectStatement);
            
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
                    () -> assertThat(records.size()).isEqualTo(5),
                    () -> assertThat(records.get(0).getNumber1()).isEqualTo(1),
                    () -> assertThat(records.get(4).getNumber1()).isEqualTo(5)
            );
        }
    }
    
    @Test
    public void testColumnComparisonLessThanOrEqual() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            ColumnComparisonMapper mapper = sqlSession.getMapper(ColumnComparisonMapper.class);
            
            SelectStatementProvider selectStatement = select(number1, number2)
                    .from(columnComparison)
                    .where(number1, isLessThanOrEqualTo(number2))
                    .orderBy(number1, number2)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expected = "select number1, number2 "
                    + "from ColumnComparison "
                    + "where number1 <= number2 "
                    + "order by number1, number2";
            
            List<ColumnComparisonRecord> records = mapper.selectMany(selectStatement);
            
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
                    () -> assertThat(records.size()).isEqualTo(6),
                    () -> assertThat(records.get(0).getNumber1()).isEqualTo(1),
                    () -> assertThat(records.get(5).getNumber1()).isEqualTo(6)
            );
        }
    }
    
    @Test
    public void testColumnComparisonGreaterThan() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            ColumnComparisonMapper mapper = sqlSession.getMapper(ColumnComparisonMapper.class);
            
            SelectStatementProvider selectStatement = select(number1, number2)
                    .from(columnComparison)
                    .where(number1, isGreaterThan(number2))
                    .orderBy(number1, number2)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expected = "select number1, number2 "
                    + "from ColumnComparison "
                    + "where number1 > number2 "
                    + "order by number1, number2";
            
            List<ColumnComparisonRecord> records = mapper.selectMany(selectStatement);
            
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
                    () -> assertThat(records.size()).isEqualTo(5),
                    () -> assertThat(records.get(0).getNumber1()).isEqualTo(7),
                    () -> assertThat(records.get(4).getNumber1()).isEqualTo(11)
            );
        }
    }
    
    @Test
    public void testColumnComparisonGreaterThanOrEqual() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            ColumnComparisonMapper mapper = sqlSession.getMapper(ColumnComparisonMapper.class);
            
            SelectStatementProvider selectStatement = select(number1, number2)
                    .from(columnComparison)
                    .where(number1, isGreaterThanOrEqualTo(number2))
                    .orderBy(number1, number2)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expected = "select number1, number2 "
                    + "from ColumnComparison "
                    + "where number1 >= number2 "
                    + "order by number1, number2";
            
            List<ColumnComparisonRecord> records = mapper.selectMany(selectStatement);
            
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
                    () -> assertThat(records.size()).isEqualTo(6),
                    () -> assertThat(records.get(0).getNumber1()).isEqualTo(6),
                    () -> assertThat(records.get(5).getNumber1()).isEqualTo(11)
            );
        }
    }
    
    @Test
    public void testColumnComparisonEqual() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            ColumnComparisonMapper mapper = sqlSession.getMapper(ColumnComparisonMapper.class);
            
            SelectStatementProvider selectStatement = select(number1, number2)
                    .from(columnComparison)
                    .where(number1, isEqualTo(number2))
                    .orderBy(number1, number2)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expected = "select number1, number2 "
                    + "from ColumnComparison "
                    + "where number1 = number2 "
                    + "order by number1, number2";
            
            List<ColumnComparisonRecord> records = mapper.selectMany(selectStatement);
            
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
                    () -> assertThat(records.size()).isEqualTo(1),
                    () -> assertThat(records.get(0).getNumber1()).isEqualTo(6)
            );
        }
    }
    
    @Test
    public void testColumnComparisonNotEqual() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            ColumnComparisonMapper mapper = sqlSession.getMapper(ColumnComparisonMapper.class);
            
            SelectStatementProvider selectStatement = select(number1, number2)
                    .from(columnComparison)
                    .where(number1, isNotEqualTo(number2))
                    .orderBy(number1, number2)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expected = "select number1, number2 "
                    + "from ColumnComparison "
                    + "where number1 <> number2 "
                    + "order by number1, number2";
            
            List<ColumnComparisonRecord> records = mapper.selectMany(selectStatement);
            
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
                    () -> assertThat(records.size()).isEqualTo(10),
                    () -> assertThat(records.get(0).getNumber1()).isEqualTo(1),
                    () -> assertThat(records.get(9).getNumber1()).isEqualTo(11)
            );
        }
    }
}
