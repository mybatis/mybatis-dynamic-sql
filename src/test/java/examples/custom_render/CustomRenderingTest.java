/**
 *    Copyright 2016-2020 the original author or authors.
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
package examples.custom_render;

import static examples.custom_render.JsonTestDynamicSqlSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.delete.DeleteDSLCompleter;
import org.mybatis.dynamic.sql.select.SelectDSLCompleter;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class CustomRenderingTest {

    @Container
    private static PgContainer postgres = new PgContainer("examples/custom_render/dbInit.sql");

    private static SqlSessionFactory sqlSessionFactory;

    @BeforeAll
    static void setUp() {
        Configuration configuration = new Configuration();
        Environment environment = new Environment("development", new JdbcTransactionFactory(),
                postgres.getUnpooledDataSource());
        configuration.setEnvironment(environment);
        configuration.addMapper(JsonTestMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
    }

    @BeforeEach
    void resetDatabase() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            JsonTestMapper mapper = sqlSession.getMapper(JsonTestMapper.class);
            
            mapper.delete(DeleteDSLCompleter.allRows());
        }
    }
    
    @Test
    public void testInsert() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            JsonTestMapper mapper = sqlSession.getMapper(JsonTestMapper.class);

            JsonTest record = new JsonTest();
            record.setId(1);
            record.setDescription("Fred");
            record.setInfo("{\"firstName\": \"Fred\", \"lastName\": \"Flintstone\", \"age\": 30}");

            int rows = mapper.insert(record);
            assertThat(rows).isEqualTo(1);
            
            record = new JsonTest();
            record.setId(2);
            record.setDescription("Wilma");
            record.setInfo("{\"firstName\": \"Wilma\", \"lastName\": \"Flintstone\", \"age\": 25}");

            rows = mapper.insert(record);
            assertThat(rows).isEqualTo(1);
            
            List<JsonTest> records = mapper.selectMany(SelectDSLCompleter.allRowsOrderedBy(id));
            assertThat(records.size()).isEqualTo(2);
            assertThat(records.get(0).getInfo()).isEqualTo("{\"firstName\": \"Fred\", \"lastName\": \"Flintstone\", \"age\": 30}");
            assertThat(records.get(1).getInfo()).isEqualTo("{\"firstName\": \"Wilma\", \"lastName\": \"Flintstone\", \"age\": 25}");
        }
    }

    @Test
    public void testInsertMultiple() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            JsonTestMapper mapper = sqlSession.getMapper(JsonTestMapper.class);

            JsonTest record1 = new JsonTest();
            record1.setId(1);
            record1.setDescription("Fred");
            record1.setInfo("{\"firstName\": \"Fred\", \"lastName\": \"Flintstone\", \"age\": 30}");

            JsonTest record2 = new JsonTest();
            record2.setId(2);
            record2.setDescription("Wilma");
            record2.setInfo("{\"firstName\": \"Wilma\", \"lastName\": \"Flintstone\", \"age\": 25}");

            int rows = mapper.insertMultiple(record1, record2);
            assertThat(rows).isEqualTo(2);
            
            List<JsonTest> records = mapper.selectMany(SelectDSLCompleter.allRowsOrderedBy(id));
            assertThat(records.size()).isEqualTo(2);
            assertThat(records.get(0).getInfo()).isEqualTo("{\"firstName\": \"Fred\", \"lastName\": \"Flintstone\", \"age\": 30}");
            assertThat(records.get(1).getInfo()).isEqualTo("{\"firstName\": \"Wilma\", \"lastName\": \"Flintstone\", \"age\": 25}");
        }
    }

    @Test
    public void testUpdate() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            JsonTestMapper mapper = sqlSession.getMapper(JsonTestMapper.class);

            JsonTest record1 = new JsonTest();
            record1.setId(1);
            record1.setDescription("Fred");
            record1.setInfo("{\"firstName\": \"Fred\", \"lastName\": \"Flintstone\", \"age\": 30}");

            JsonTest record2 = new JsonTest();
            record2.setId(2);
            record2.setDescription("Wilma");
            record2.setInfo("{\"firstName\": \"Wilma\", \"lastName\": \"Flintstone\", \"age\": 25}");

            int rows = mapper.insertMultiple(record1, record2);
            assertThat(rows).isEqualTo(2);
            
            rows = mapper.update(c ->
                c.set(info).equalTo("{\"firstName\": \"Wilma\", \"lastName\": \"Flintstone\", \"age\": 26}")
                .where(id, isEqualTo(2))
            );
            assertThat(rows).isEqualTo(1);
            
            List<JsonTest> records = mapper.selectMany(SelectDSLCompleter.allRowsOrderedBy(id));
            assertThat(records.size()).isEqualTo(2);
            assertThat(records.get(0).getInfo()).isEqualTo("{\"firstName\": \"Fred\", \"lastName\": \"Flintstone\", \"age\": 30}");
            assertThat(records.get(1).getInfo()).isEqualTo("{\"firstName\": \"Wilma\", \"lastName\": \"Flintstone\", \"age\": 26}");
        }
    }

    @Test
    public void testJsonQuery() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            JsonTestMapper mapper = sqlSession.getMapper(JsonTestMapper.class);

            JsonTest record1 = new JsonTest();
            record1.setId(1);
            record1.setDescription("Fred");
            record1.setInfo("{\"firstName\": \"Fred\", \"lastName\": \"Flintstone\", \"age\": 30}");

            JsonTest record2 = new JsonTest();
            record2.setId(2);
            record2.setDescription("Wilma");
            record2.setInfo("{\"firstName\": \"Wilma\", \"lastName\": \"Flintstone\", \"age\": 25}");

            int rows = mapper.insertMultiple(record1, record2);
            assertThat(rows).isEqualTo(2);
            
            Optional<JsonTest> record = mapper.selectOne(c ->
                c.where(info, isEqualTo("{\"firstName\": \"Wilma\", \"lastName\": \"Flintstone\", \"age\": 25}")));
            
            assertThat(record).hasValueSatisfying( c ->
                c.getInfo().equals("{\"firstName\": \"Wilma\", \"lastName\": \"Flintstone\", \"age\": 25}"));
        }
    }
    
    @Test
    public void testDefererence() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            JsonTestMapper mapper = sqlSession.getMapper(JsonTestMapper.class);

            JsonTest record1 = new JsonTest();
            record1.setId(1);
            record1.setDescription("Fred");
            record1.setInfo("{\"firstName\": \"Fred\", \"lastName\": \"Flintstone\", \"age\": 30}");

            JsonTest record2 = new JsonTest();
            record2.setId(2);
            record2.setDescription("Wilma");
            record2.setInfo("{\"firstName\": \"Wilma\", \"lastName\": \"Flintstone\", \"age\": 25}");

            int rows = mapper.insertMultiple(record1, record2);
            assertThat(rows).isEqualTo(2);
            
            Optional<JsonTest> record = mapper.selectOne(c ->
                c.where(dereference(info, "->>'age'"), isEqualTo("25")));
            
            assertThat(record).hasValueSatisfying( c ->
                c.getInfo().equals("{\"firstName\": \"Wilma\", \"lastName\": \"Flintstone\", \"age\": 25}"));
        }
    }
    
    private <T> SqlColumn<String> dereference(SqlColumn<T> column, String drString) {
        return SqlColumn.of(column.name() + drString, column.table());
    }
}
