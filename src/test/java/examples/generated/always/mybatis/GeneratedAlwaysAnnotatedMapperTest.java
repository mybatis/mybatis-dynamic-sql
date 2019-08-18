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
package examples.generated.always.mybatis;

import static examples.generated.always.mybatis.GeneratedAlwaysDynamicSqlSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.insert.render.BatchInsert;
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;

import examples.generated.always.GeneratedAlwaysRecord;

public class GeneratedAlwaysAnnotatedMapperTest {

    private static final String JDBC_URL = "jdbc:hsqldb:mem:aname";
    private static final String JDBC_DRIVER = "org.hsqldb.jdbcDriver"; 
    
    private SqlSessionFactory sqlSessionFactory;
    
    @BeforeEach
    public void setup() throws Exception {
        Class.forName(JDBC_DRIVER);
        InputStream is = getClass().getResourceAsStream("/examples/generated/always/CreateGeneratedAlwaysDB.sql");
        try (Connection connection = DriverManager.getConnection(JDBC_URL, "sa", "")) {
            ScriptRunner sr = new ScriptRunner(connection);
            sr.setLogWriter(null);
            sr.runScript(new InputStreamReader(is));
        }
        
        UnpooledDataSource ds = new UnpooledDataSource(JDBC_DRIVER, JDBC_URL, "sa", "");
        Environment environment = new Environment("test", new JdbcTransactionFactory(), ds);
        Configuration config = new Configuration(environment);
        config.addMapper(GeneratedAlwaysAnnotatedMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(config);
    }
    
    @Test
    public void testSelectByExample() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            GeneratedAlwaysAnnotatedMapper mapper = session.getMapper(GeneratedAlwaysAnnotatedMapper.class);
            
            SelectStatementProvider selectStatement = selectByExample()
                    .where(id, isEqualTo(1))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            List<GeneratedAlwaysRecord> rows = mapper.selectMany(selectStatement);
            
            assertThat(rows.size()).isEqualTo(1);
        }
    }

    @Test
    public void testFirstNameIn() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            GeneratedAlwaysAnnotatedMapper mapper = session.getMapper(GeneratedAlwaysAnnotatedMapper.class);
            
            SelectStatementProvider selectStatement = selectByExample()
                    .where(firstName, isIn("Fred", "Barney"))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            List<GeneratedAlwaysRecord> rows = mapper.selectMany(selectStatement);
            
            assertThat(rows.size()).isEqualTo(2);
        }
    }

    @Test
    public void testInsert() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            GeneratedAlwaysAnnotatedMapper mapper = session.getMapper(GeneratedAlwaysAnnotatedMapper.class);
            GeneratedAlwaysRecord record = new GeneratedAlwaysRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName("Jones");
            
            int rows = mapper.insert(buildInsert(record));
            
            assertAll(
                    () -> assertThat(rows).isEqualTo(1),
                    () -> assertThat(record.getFullName()).isEqualTo("Joe Jones")
            );
        }
    }

    @Test
    public void testBatchInsertWithList() {
        try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            GeneratedAlwaysAnnotatedMapper mapper = session.getMapper(GeneratedAlwaysAnnotatedMapper.class);
            List<GeneratedAlwaysRecord> records = getTestRecords();
            
            BatchInsert<GeneratedAlwaysRecord> batchInsert = insert(records)
                    .into(generatedAlways)
                    .map(id).toProperty("id")
                    .map(firstName).toProperty("firstName")
                    .map(lastName).toProperty("lastName")
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            batchInsert.insertStatements().stream().forEach(mapper::insert);
            
            session.commit();
            
            assertAll(
                    () -> assertThat(records.get(0).getFullName()).isEqualTo("George Jetson"),
                    () -> assertThat(records.get(1).getFullName()).isEqualTo("Jane Jetson"),
                    () -> assertThat(records.get(2).getFullName()).isEqualTo("Judy Jetson"),
                    () -> assertThat(records.get(3).getFullName()).isEqualTo("Elroy Jetson")
            );
        }
    }

    @Test
    public void testBatchInsertWithArray() {
        try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            GeneratedAlwaysAnnotatedMapper mapper = session.getMapper(GeneratedAlwaysAnnotatedMapper.class);

            GeneratedAlwaysRecord record1 = new GeneratedAlwaysRecord();
            record1.setId(1000);
            record1.setFirstName("George");
            record1.setLastName("Jetson");

            GeneratedAlwaysRecord record2 = new GeneratedAlwaysRecord();
            record2.setId(1001);
            record2.setFirstName("Jane");
            record2.setLastName("Jetson");
            
            BatchInsert<GeneratedAlwaysRecord> batchInsert = insert(record1, record2)
                    .into(generatedAlways)
                    .map(id).toProperty("id")
                    .map(firstName).toProperty("firstName")
                    .map(lastName).toProperty("lastName")
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            batchInsert.insertStatements().stream().forEach(mapper::insert);
            
            session.commit();
            
            assertAll(
                    () -> assertThat(record1.getFullName()).isEqualTo("George Jetson"),
                    () -> assertThat(record2.getFullName()).isEqualTo("Jane Jetson")
            );
        }
    }
    
    @Test
    public void testMultiInsertWithRawMyBatisAnnotations() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            GeneratedAlwaysAnnotatedMapper mapper = session.getMapper(GeneratedAlwaysAnnotatedMapper.class);
            List<GeneratedAlwaysRecord> records = getTestRecords();
            
            String statement = "insert into GeneratedAlways (id, first_name, last_name)" +
                    " values" +
                    " (#{records[0].id,jdbcType=INTEGER}, #{records[0].firstName,jdbcType=VARCHAR}, #{records[0].lastName,jdbcType=VARCHAR})," +
                    " (#{records[1].id,jdbcType=INTEGER}, #{records[1].firstName,jdbcType=VARCHAR}, #{records[1].lastName,jdbcType=VARCHAR})," +
                    " (#{records[2].id,jdbcType=INTEGER}, #{records[2].firstName,jdbcType=VARCHAR}, #{records[2].lastName,jdbcType=VARCHAR})," +
                    " (#{records[3].id,jdbcType=INTEGER}, #{records[3].firstName,jdbcType=VARCHAR}, #{records[3].lastName,jdbcType=VARCHAR})";
            
            int rows = mapper.insertMultipleWithGeneratedKeys(statement, records);
            
            assertAll(
                    () -> assertThat(rows).isEqualTo(4),
                    () -> assertThat(records.get(0).getFullName()).isEqualTo("George Jetson"),
                    () -> assertThat(records.get(1).getFullName()).isEqualTo("Jane Jetson"),
                    () -> assertThat(records.get(2).getFullName()).isEqualTo("Judy Jetson"),
                    () -> assertThat(records.get(3).getFullName()).isEqualTo("Elroy Jetson")
            );
        }
    }
    
    @Test
    public void testMultiInsertWithListAndGeneratedKeys() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            GeneratedAlwaysAnnotatedMapper mapper = session.getMapper(GeneratedAlwaysAnnotatedMapper.class);
            List<GeneratedAlwaysRecord> records = getTestRecords();
            
            MultiRowInsertStatementProvider<GeneratedAlwaysRecord> multiRowInsert = insertMultiple(records)
                    .into(generatedAlways)
                    .map(id).toProperty("id")
                    .map(firstName).toProperty("firstName")
                    .map(lastName).toProperty("lastName")
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String statement = "insert into GeneratedAlways (id, first_name, last_name)" +
                    " values" +
                    " (#{records[0].id,jdbcType=INTEGER}, #{records[0].firstName,jdbcType=VARCHAR}, #{records[0].lastName,jdbcType=VARCHAR})," +
                    " (#{records[1].id,jdbcType=INTEGER}, #{records[1].firstName,jdbcType=VARCHAR}, #{records[1].lastName,jdbcType=VARCHAR})," +
                    " (#{records[2].id,jdbcType=INTEGER}, #{records[2].firstName,jdbcType=VARCHAR}, #{records[2].lastName,jdbcType=VARCHAR})," +
                    " (#{records[3].id,jdbcType=INTEGER}, #{records[3].firstName,jdbcType=VARCHAR}, #{records[3].lastName,jdbcType=VARCHAR})";

            assertThat(multiRowInsert.getInsertStatement()).isEqualTo(statement);
            
            int rows = mapper.insertMultipleWithGeneratedKeys(multiRowInsert);
            
            assertAll(
                    () -> assertThat(rows).isEqualTo(4),
                    () -> assertThat(records.get(0).getFullName()).isEqualTo("George Jetson"),
                    () -> assertThat(records.get(1).getFullName()).isEqualTo("Jane Jetson"),
                    () -> assertThat(records.get(2).getFullName()).isEqualTo("Judy Jetson"),
                    () -> assertThat(records.get(3).getFullName()).isEqualTo("Elroy Jetson")
            );
        }
    }

    @Test
    public void testMultiInsertWithArray() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            GeneratedAlwaysAnnotatedMapper mapper = session.getMapper(GeneratedAlwaysAnnotatedMapper.class);

            GeneratedAlwaysRecord record1 = new GeneratedAlwaysRecord();
            record1.setId(1000);
            record1.setFirstName("George");
            record1.setLastName("Jetson");

            GeneratedAlwaysRecord record2 = new GeneratedAlwaysRecord();
            record2.setId(1001);
            record2.setFirstName("Jane");
            record2.setLastName("Jetson");
            
            MultiRowInsertStatementProvider<GeneratedAlwaysRecord> multiRowInsert = insertMultiple(record1, record2)
                    .into(generatedAlways)
                    .map(id).toProperty("id")
                    .map(firstName).toProperty("firstName")
                    .map(lastName).toProperty("lastName")
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String statement = "insert into GeneratedAlways (id, first_name, last_name)" +
                    " values" +
                    " (#{records[0].id,jdbcType=INTEGER}, #{records[0].firstName,jdbcType=VARCHAR}, #{records[0].lastName,jdbcType=VARCHAR})," +
                    " (#{records[1].id,jdbcType=INTEGER}, #{records[1].firstName,jdbcType=VARCHAR}, #{records[1].lastName,jdbcType=VARCHAR})";

            assertThat(multiRowInsert.getInsertStatement()).isEqualTo(statement);
            
            int rows = mapper.insertMultiple(multiRowInsert);
            assertThat(rows).isEqualTo(2);
        }
    }
    
    @Test
    public void testMultiInsertWithArrayAndVariousMappings() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            GeneratedAlwaysAnnotatedMapper mapper = session.getMapper(GeneratedAlwaysAnnotatedMapper.class);

            GeneratedAlwaysRecord record1 = new GeneratedAlwaysRecord();
            record1.setId(1000);
            record1.setFirstName("George");
            record1.setLastName("Jetson");

            MultiRowInsertStatementProvider<GeneratedAlwaysRecord> multiRowInsert = insertMultiple(record1)
                    .into(generatedAlways)
                    .map(id).toConstant("1000")
                    .map(firstName).toStringConstant("George")
                    .map(lastName).toProperty("lastName")
                    .map(age).toNull()
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String statement = "insert into GeneratedAlways (id, first_name, last_name, age)" +
                    " values (1000, 'George', #{records[0].lastName,jdbcType=VARCHAR}, null)";

            assertThat(multiRowInsert.getInsertStatement()).isEqualTo(statement);
            
            int rows = mapper.insertMultipleWithGeneratedKeys(multiRowInsert);
            
            assertAll(
                    () -> assertThat(rows).isEqualTo(1),
                    () -> assertThat(record1.getFullName()).isEqualTo("George Jetson")
            );
        }
    }
    
    private List<GeneratedAlwaysRecord> getTestRecords() {
        List<GeneratedAlwaysRecord> records = new ArrayList<>();
        GeneratedAlwaysRecord record = new GeneratedAlwaysRecord();
        record.setId(1000);
        record.setFirstName("George");
        record.setLastName("Jetson");
        records.add(record);

        record = new GeneratedAlwaysRecord();
        record.setId(1001);
        record.setFirstName("Jane");
        record.setLastName("Jetson");
        records.add(record);

        record = new GeneratedAlwaysRecord();
        record.setId(1002);
        record.setFirstName("Judy");
        record.setLastName("Jetson");
        records.add(record);

        record = new GeneratedAlwaysRecord();
        record.setId(1003);
        record.setFirstName("Elroy");
        record.setLastName("Jetson");
        records.add(record);

        return records;
    }

    @Test
    public void testInsertSelective() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            GeneratedAlwaysAnnotatedMapper mapper = session.getMapper(GeneratedAlwaysAnnotatedMapper.class);
            GeneratedAlwaysRecord record = new GeneratedAlwaysRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName("Jones");
            
            int rows = mapper.insert(buildInsertSelectiveStatement(record));
            
            assertAll(
                    () -> assertThat(rows).isEqualTo(1),
                    () -> assertThat(record.getFullName()).isEqualTo("Joe Jones")
            );
        }
    }

    @Test
    public void testUpdateByPrimaryKey() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            GeneratedAlwaysAnnotatedMapper mapper = session.getMapper(GeneratedAlwaysAnnotatedMapper.class);
            GeneratedAlwaysRecord record = new GeneratedAlwaysRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName("Jones");
            
            int rows = mapper.insert(buildInsert(record));
            assertThat(rows).isEqualTo(1);
            assertThat(record.getFullName()).isEqualTo("Joe Jones");
            
            record.setLastName("Smith");
            rows = mapper.update(buildUpdateByPrimaryKeyStatement(record));
            assertThat(rows).isEqualTo(1);
            
            GeneratedAlwaysRecord newRecord = mapper.selectByPrimaryKey(selectByPrimaryKey(100));
            assertThat(newRecord.getFullName()).isEqualTo("Joe Smith");
        }
    }

    @Test
    public void testUpdateByExampleSelective() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            GeneratedAlwaysAnnotatedMapper mapper = session.getMapper(GeneratedAlwaysAnnotatedMapper.class);
            GeneratedAlwaysRecord record = new GeneratedAlwaysRecord();
            record.setLastName("Jones");
            
            UpdateStatementProvider updateStatement = updateByExampleSelective(record)
                    .where(lastName, isEqualTo("Flintstone"))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            int rows = mapper.update(updateStatement);
            assertThat(rows).isEqualTo(3);
            
            SelectStatementProvider selectStatement = selectByExample()
                    .where(lastName, isEqualTo("Jones"))
                    .orderBy(firstName)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            List<GeneratedAlwaysRecord> records = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(records.size()).isEqualTo(3),
                    () -> assertThat(records.get(0).getFullName()).isEqualTo("Fred Jones"),
                    () -> assertThat(records.get(1).getFullName()).isEqualTo("Pebbles Jones"),
                    () -> assertThat(records.get(2).getFullName()).isEqualTo("Wilma Jones")
            );
        }
    }
    
    @Test
    public void testUpdateByExample() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            GeneratedAlwaysAnnotatedMapper mapper = session.getMapper(GeneratedAlwaysAnnotatedMapper.class);
            GeneratedAlwaysRecord record = new GeneratedAlwaysRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName("Jones");
            
            int rows = mapper.insert(buildInsert(record));
            assertThat(rows).isEqualTo(1);

            GeneratedAlwaysRecord updateRecord = new GeneratedAlwaysRecord();
            updateRecord.setId(100);
            updateRecord.setLastName("Smith");
            rows = mapper.update(buildUpdateByPrimaryKeySelectiveStatement(updateRecord));
            assertThat(rows).isEqualTo(1);
            
            GeneratedAlwaysRecord newRecord = mapper.selectByPrimaryKey(selectByPrimaryKey(100));
            assertAll(
                    () -> assertThat(newRecord.getFirstName()).isEqualTo("Joe"),
                    () -> assertThat(newRecord.getLastName()).isEqualTo("Smith"),
                    () -> assertThat(newRecord.getFullName()).isEqualTo("Joe Smith")
            );
        }
    }
}
