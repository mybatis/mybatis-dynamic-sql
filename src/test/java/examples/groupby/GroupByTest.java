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
package examples.groupby;

import static examples.groupby.AddressDynamicSqlSupport.*;
import static examples.groupby.PersonDynamicSqlSupport.*;
import static examples.groupby.Person2DynamicSqlSupport.person2;
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
                    .render(RenderingStrategies.MYBATIS3);
            
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
                    .render(RenderingStrategies.MYBATIS3);
            
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
    public void testGroupByAfterJoin() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            GroupByMapper mapper = session.getMapper(GroupByMapper.class);
        
            SelectStatementProvider selectStatement = select(lastName, streetAddress, count().as("count"))
                    .from(person, "p").join(address, "a").on(person.addressId, equalTo(address.id))
                    .groupBy(lastName, streetAddress)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expected = "select p.last_name, a.street_address, count(*) as count" +
                    " from Person p join Address a on p.address_id = a.address_id" +
                    " group by p.last_name, a.street_address";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
            
            List<Map<String, Object>> rows = mapper.generalSelect(selectStatement);
            assertThat(rows.size()).isEqualTo(2);
            Map<String, Object> row = rows.get(0);
            assertThat(row.get("LAST_NAME")).isEqualTo("Flintstone");
            assertThat(row.get("STREET_ADDRESS")).isEqualTo("123 Main Street");
            assertThat(row.get("COUNT")).isEqualTo(4L);

            row = rows.get(1);
            assertThat(row.get("LAST_NAME")).isEqualTo("Rubble");
            assertThat(row.get("STREET_ADDRESS")).isEqualTo("456 Main Street");
            assertThat(row.get("COUNT")).isEqualTo(3L);
        }
    }

    @Test
    public void testUnionAfterJoin() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            GroupByMapper mapper = session.getMapper(GroupByMapper.class);
        
            SelectStatementProvider selectStatement = select(lastName, firstName, streetAddress)
                    .from(person, "p").join(address, "a").on(person.addressId, equalTo(address.id))
                    .union()
                    .select(person2.lastName, person2.firstName, streetAddress)
                    .from(person2, "p").join(address, "a").on(person2.addressId, equalTo(address.id))
                    .orderBy(lastName, firstName)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expected = "select p.last_name, p.first_name, a.street_address" +
                    " from Person p join Address a on p.address_id = a.address_id" +
                    " union" +
                    " select p.last_name, p.first_name, a.street_address" +
                    " from Person2 p join Address a on p.address_id = a.address_id" +
                    " order by last_name, first_name";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
            
            List<Map<String, Object>> rows = mapper.generalSelect(selectStatement);
            assertThat(rows.size()).isEqualTo(10);
            Map<String, Object> row = rows.get(0);
            assertThat(row.get("LAST_NAME")).isEqualTo("Flintstone");
            assertThat(row.get("FIRST_NAME")).isEqualTo("Dino");
            assertThat(row.get("STREET_ADDRESS")).isEqualTo("123 Main Street");

            row = rows.get(9);
            assertThat(row.get("LAST_NAME")).isEqualTo("Smith");
            assertThat(row.get("FIRST_NAME")).isEqualTo("Suzy");
            assertThat(row.get("STREET_ADDRESS")).isEqualTo("123 Main Street");
        }
    }

    @Test
    public void testUnionAllAfterJoin() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            GroupByMapper mapper = session.getMapper(GroupByMapper.class);
        
            SelectStatementProvider selectStatement = select(lastName, firstName, streetAddress)
                    .from(person, "p").join(address, "a").on(person.addressId, equalTo(address.id))
                    .unionAll()
                    .select(person2.lastName, person2.firstName, streetAddress)
                    .from(person2, "p").join(address, "a").on(person2.addressId, equalTo(address.id))
                    .orderBy(lastName, firstName)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expected = "select p.last_name, p.first_name, a.street_address" +
                    " from Person p join Address a on p.address_id = a.address_id" +
                    " union all" +
                    " select p.last_name, p.first_name, a.street_address" +
                    " from Person2 p join Address a on p.address_id = a.address_id" +
                    " order by last_name, first_name";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
            
            List<Map<String, Object>> rows = mapper.generalSelect(selectStatement);
            assertThat(rows.size()).isEqualTo(10);
            Map<String, Object> row = rows.get(0);
            assertThat(row.get("LAST_NAME")).isEqualTo("Flintstone");
            assertThat(row.get("FIRST_NAME")).isEqualTo("Dino");
            assertThat(row.get("STREET_ADDRESS")).isEqualTo("123 Main Street");

            row = rows.get(9);
            assertThat(row.get("LAST_NAME")).isEqualTo("Smith");
            assertThat(row.get("FIRST_NAME")).isEqualTo("Suzy");
            assertThat(row.get("STREET_ADDRESS")).isEqualTo("123 Main Street");
        }
    }

    @Test
    public void testUnionAfterGroupBy() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            GroupByMapper mapper = session.getMapper(GroupByMapper.class);
        
            SelectStatementProvider selectStatement = select(stringConstant("Gender"), gender.as("value"), count().as("count"))
                    .from(person)
                    .groupBy(gender)
                    .union()
                    .select(stringConstant("Last Name"), lastName.as("value"), count().as("count"))
                    .from(person)
                    .groupBy(lastName)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expected = "select 'Gender', gender as value, count(*) as count from Person group by gender" +
                    " union" +
                    " select 'Last Name', last_name as value, count(*) as count from Person group by last_name";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
            
            List<Map<String, Object>> rows = mapper.generalSelect(selectStatement);
            assertThat(rows.size()).isEqualTo(4);
            Map<String, Object> row = rows.get(0);
            assertThat(row.get("C1")).isEqualTo("Gender   ");
            assertThat(row.get("VALUE")).isEqualTo("Female");
            assertThat(row.get("COUNT")).isEqualTo(3L);

            row = rows.get(1);
            assertThat(row.get("C1")).isEqualTo("Gender   ");
            assertThat(row.get("VALUE")).isEqualTo("Male");
            assertThat(row.get("COUNT")).isEqualTo(4L);

            row = rows.get(2);
            assertThat(row.get("C1")).isEqualTo("Last Name");
            assertThat(row.get("VALUE")).isEqualTo("Flintstone");
            assertThat(row.get("COUNT")).isEqualTo(4L);

            row = rows.get(3);
            assertThat(row.get("C1")).isEqualTo("Last Name");
            assertThat(row.get("VALUE")).isEqualTo("Rubble");
            assertThat(row.get("COUNT")).isEqualTo(3L);
        }
    }

    @Test
    public void testUnionAllAfterGroupBy() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            GroupByMapper mapper = session.getMapper(GroupByMapper.class);
        
            SelectStatementProvider selectStatement = select(stringConstant("Gender"), gender.as("value"), count().as("count"))
                    .from(person)
                    .groupBy(gender)
                    .unionAll()
                    .select(stringConstant("Last Name"), lastName.as("value"), count().as("count"))
                    .from(person)
                    .groupBy(lastName)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expected = "select 'Gender', gender as value, count(*) as count from Person group by gender" +
                    " union all" +
                    " select 'Last Name', last_name as value, count(*) as count from Person group by last_name";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
            
            List<Map<String, Object>> rows = mapper.generalSelect(selectStatement);
            assertThat(rows.size()).isEqualTo(4);
            Map<String, Object> row = rows.get(0);
            assertThat(row.get("C1")).isEqualTo("Gender   ");
            assertThat(row.get("VALUE")).isEqualTo("Male");
            assertThat(row.get("COUNT")).isEqualTo(4L);

            row = rows.get(1);
            assertThat(row.get("C1")).isEqualTo("Gender   ");
            assertThat(row.get("VALUE")).isEqualTo("Female");
            assertThat(row.get("COUNT")).isEqualTo(3L);

            row = rows.get(2);
            assertThat(row.get("C1")).isEqualTo("Last Name");
            assertThat(row.get("VALUE")).isEqualTo("Flintstone");
            assertThat(row.get("COUNT")).isEqualTo(4L);

            row = rows.get(3);
            assertThat(row.get("C1")).isEqualTo("Last Name");
            assertThat(row.get("VALUE")).isEqualTo("Rubble");
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
                    .render(RenderingStrategies.MYBATIS3);
            
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
                    .render(RenderingStrategies.MYBATIS3);
            
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
                    .render(RenderingStrategies.MYBATIS3);
            
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

    @Test
    public void testLimitAndOffsetAfterGroupBy() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            GroupByMapper mapper = session.getMapper(GroupByMapper.class);
        
            SelectStatementProvider selectStatement = select(lastName, count().as("count"))
                    .from(person)
                    .groupBy(lastName)
                    .limit(1)
                    .offset(1)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expected = "select last_name, count(*) as count from Person group by last_name limit #{parameters._limit} offset #{parameters._offset}";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
            
            List<Map<String, Object>> rows = mapper.generalSelect(selectStatement);
            assertThat(rows.size()).isEqualTo(1);
            Map<String, Object> row = rows.get(0);
            assertThat(row.get("LAST_NAME")).isEqualTo("Rubble");
            assertThat(row.get("COUNT")).isEqualTo(3L);
        }
    }

    @Test
    public void testLimitOnlyAfterGroupBy() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            GroupByMapper mapper = session.getMapper(GroupByMapper.class);
        
            SelectStatementProvider selectStatement = select(lastName, count().as("count"))
                    .from(person)
                    .groupBy(lastName)
                    .limit(1)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expected = "select last_name, count(*) as count from Person group by last_name limit #{parameters._limit}";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
            
            List<Map<String, Object>> rows = mapper.generalSelect(selectStatement);
            assertThat(rows.size()).isEqualTo(1);
            Map<String, Object> row = rows.get(0);
            assertThat(row.get("LAST_NAME")).isEqualTo("Flintstone");
            assertThat(row.get("COUNT")).isEqualTo(4L);
        }
    }

    @Test
    public void testOffsetOnlyAfterGroupBy() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            GroupByMapper mapper = session.getMapper(GroupByMapper.class);
        
            SelectStatementProvider selectStatement = select(lastName, count().as("count"))
                    .from(person)
                    .groupBy(lastName)
                    .offset(1)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expected = "select last_name, count(*) as count from Person group by last_name offset #{parameters._offset} rows";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
            
            List<Map<String, Object>> rows = mapper.generalSelect(selectStatement);
            assertThat(rows.size()).isEqualTo(1);
            Map<String, Object> row = rows.get(0);
            assertThat(row.get("LAST_NAME")).isEqualTo("Rubble");
            assertThat(row.get("COUNT")).isEqualTo(3L);
        }
    }

    @Test
    public void testOffsetAndFetchFirstAfterGroupBy() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            GroupByMapper mapper = session.getMapper(GroupByMapper.class);
        
            SelectStatementProvider selectStatement = select(lastName, count().as("count"))
                    .from(person)
                    .groupBy(lastName)
                    .offset(1)
                    .fetchFirst(1).rowsOnly()
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expected = "select last_name, count(*) as count from Person group by last_name offset #{parameters._offset} rows fetch first #{parameters._fetchFirstRows} rows only";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
            
            List<Map<String, Object>> rows = mapper.generalSelect(selectStatement);
            assertThat(rows.size()).isEqualTo(1);
            Map<String, Object> row = rows.get(0);
            assertThat(row.get("LAST_NAME")).isEqualTo("Rubble");
            assertThat(row.get("COUNT")).isEqualTo(3L);
        }
    }

    @Test
    public void testFetchFirstOnlyAfterGroupBy() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            GroupByMapper mapper = session.getMapper(GroupByMapper.class);
        
            SelectStatementProvider selectStatement = select(lastName, count().as("count"))
                    .from(person)
                    .groupBy(lastName)
                    .fetchFirst(1).rowsOnly()
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expected = "select last_name, count(*) as count from Person group by last_name fetch first #{parameters._fetchFirstRows} rows only";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
            
            List<Map<String, Object>> rows = mapper.generalSelect(selectStatement);
            assertThat(rows.size()).isEqualTo(1);
            Map<String, Object> row = rows.get(0);
            assertThat(row.get("LAST_NAME")).isEqualTo("Flintstone");
            assertThat(row.get("COUNT")).isEqualTo(4L);
        }
    }

    @Test
    public void testCountDistinct() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            GroupByMapper mapper = session.getMapper(GroupByMapper.class);
        
            SelectStatementProvider selectStatement = select(countDistinct(lastName).as("count"))
                    .from(person)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expected = "select count(distinct last_name) as count from Person";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
            
            List<Map<String, Object>> rows = mapper.generalSelect(selectStatement);
            assertThat(rows.size()).isEqualTo(1);
            Map<String, Object> row = rows.get(0);
            assertThat(row.get("COUNT")).isEqualTo(2L);
        }
    }
}
