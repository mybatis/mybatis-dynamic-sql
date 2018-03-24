/**
 *    Copyright 2016-2018 the original author or authors.
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
package examples.groupby;

import static examples.groupby.PersonDynamicSqlSupport.*;
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
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;

@RunWith(JUnitPlatform.class)
public class GroupByTest {

    private static final String JDBC_URL = "jdbc:hsqldb:mem:aname";
    private static final String JDBC_DRIVER = "org.hsqldb.jdbcDriver";
    
    private SqlSessionFactory sqlSessionFactory;
    
    @BeforeEach
    public void setup() throws Exception {
        Class.forName(JDBC_DRIVER);
        InputStream is = getClass().getResourceAsStream("/examples/groupby/CreateGroupByDB.sql");
        try (Connection connection = DriverManager.getConnection(JDBC_URL, "sa", "")) {
            ScriptRunner sr = new ScriptRunner(connection);
            sr.setLogWriter(null);
            sr.runScript(new InputStreamReader(is));
        }
        
        UnpooledDataSource ds = new UnpooledDataSource(JDBC_DRIVER, JDBC_URL, "sa", "");
        Environment environment = new Environment("test", new JdbcTransactionFactory(), ds);
        Configuration config = new Configuration(environment);
        config.addMapper(GroupByMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(config);
    }
    
    @Test
    public void testBasicGroupBy() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            GroupByMapper mapper = session.getMapper(GroupByMapper.class);
        
            SelectStatementProvider selectStatement = select(gender, count())
                    .from(person)
                    .groupBy(gender)
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            
            String expected = "select gender, count(*) from Person group by gender";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
            
            List<Map<String, Object>> rows = mapper.generalSelect(selectStatement);
            assertThat(rows.size()).isEqualTo(2);
            Map<String, Object> row = rows.get(0);
            assertThat(row.get("GENDER")).isEqualTo("Male");
            assertThat(row.get("C2")).isEqualTo(4L);

            row = rows.get(1);
            assertThat(row.get("GENDER")).isEqualTo("Female");
            assertThat(row.get("C2")).isEqualTo(3L);
        }
    }

    @Test
    public void testBasicGroupByWithAggregateAlias() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            GroupByMapper mapper = session.getMapper(GroupByMapper.class);
        
            SelectStatementProvider selectStatement = select(gender, count().as("count"))
                    .from(person)
                    .groupBy(gender)
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            
            String expected = "select gender, count(*) as count from Person group by gender";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
            
            List<Map<String, Object>> rows = mapper.generalSelect(selectStatement);
            assertThat(rows.size()).isEqualTo(2);
            Map<String, Object> row = rows.get(0);
            assertThat(row.get("GENDER")).isEqualTo("Male");
            assertThat(row.get("COUNT")).isEqualTo(4L);

            row = rows.get(1);
            assertThat(row.get("GENDER")).isEqualTo("Female");
            assertThat(row.get("COUNT")).isEqualTo(3L);
        }
    }

    @Test
    public void testBasicGroupByOrderByWithAggregateAlias() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            GroupByMapper mapper = session.getMapper(GroupByMapper.class);
        
            SelectStatementProvider selectStatement = select(gender, count().as("count"))
                    .from(person)
                    .groupBy(gender)
                    .orderBy(gender)
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            
            String expected = "select gender, count(*) as count from Person group by gender order by gender";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
            
            List<Map<String, Object>> rows = mapper.generalSelect(selectStatement);
            assertThat(rows.size()).isEqualTo(2);
            Map<String, Object> row = rows.get(0);
            assertThat(row.get("GENDER")).isEqualTo("Female");
            assertThat(row.get("COUNT")).isEqualTo(3L);

            row = rows.get(1);
            assertThat(row.get("GENDER")).isEqualTo("Male");
            assertThat(row.get("COUNT")).isEqualTo(4L);
        }
    }

    @Test
    public void testBasicGroupByOrderByWithCalculatedColumnAndTableAlias() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            GroupByMapper mapper = session.getMapper(GroupByMapper.class);
        
            SelectStatementProvider selectStatement = select(substring(gender, 1, 1).as("ShortGender"), avg(age).as("AverageAge"))
                    .from(person, "a")
                    .groupBy(substring(gender, 1, 1))
                    .orderBy(sortColumn("ShortGender").descending())
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            
            String expected = "select substring(a.gender, 1, 1) as ShortGender, avg(a.age) as AverageAge from Person a group by substring(a.gender, 1, 1) order by ShortGender DESC";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
            
            List<Map<String, Object>> rows = mapper.generalSelect(selectStatement);
            assertThat(rows.size()).isEqualTo(2);
            Map<String, Object> row = rows.get(0);
            assertThat(row.get("SHORTGENDER")).isEqualTo("M");
            assertThat(row.get("AVERAGEAGE")).isEqualTo(25);

            row = rows.get(1);
            assertThat(row.get("SHORTGENDER")).isEqualTo("F");
            assertThat(row.get("AVERAGEAGE")).isEqualTo(27);
        }
    }

    @Test
    public void testGroupByAfterWhere() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            GroupByMapper mapper = session.getMapper(GroupByMapper.class);
        
            SelectStatementProvider selectStatement = select(lastName, count().as("count"))
                    .from(person, "a")
                    .where(gender, isEqualTo("Male"))
                    .groupBy(lastName)
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            
            String expected = "select a.last_name, count(*) as count from Person a where a.gender = #{parameters.p1,jdbcType=VARCHAR} group by a.last_name";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
            
            List<Map<String, Object>> rows = mapper.generalSelect(selectStatement);
            assertThat(rows.size()).isEqualTo(2);
            Map<String, Object> row = rows.get(0);
            assertThat(row.get("LAST_NAME")).isEqualTo("Flintstone");
            assertThat(row.get("COUNT")).isEqualTo(2L);

            row = rows.get(1);
            assertThat(row.get("LAST_NAME")).isEqualTo("Rubble");
            assertThat(row.get("COUNT")).isEqualTo(2L);
        }
    }
}
