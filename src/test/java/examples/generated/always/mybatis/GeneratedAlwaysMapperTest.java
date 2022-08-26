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
import java.util.Optional;

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

import examples.generated.always.GeneratedAlwaysRecord;

class GeneratedAlwaysMapperTest {

    private static final String JDBC_URL = "jdbc:hsqldb:mem:aname";
    private static final String JDBC_DRIVER = "org.hsqldb.jdbcDriver";

    private SqlSessionFactory sqlSessionFactory;

    @BeforeEach
    void setup() throws Exception {
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
        config.addMapper(GeneratedAlwaysMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(config);
    }

    @Test
    void testSelectByPrimaryKey() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            GeneratedAlwaysMapper mapper = session.getMapper(GeneratedAlwaysMapper.class);

            Optional<GeneratedAlwaysRecord> row = mapper.selectByPrimaryKey(1);

            assertThat(row).isPresent();
        }
    }

    @Test
    void testFirstNameIn() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            GeneratedAlwaysMapper mapper = session.getMapper(GeneratedAlwaysMapper.class);

            List<GeneratedAlwaysRecord> rows = mapper.select(c -> c.where(firstName, isIn("Fred", "Barney")));

            assertThat(rows).hasSize(2);
        }
    }

    @Test
    void testInsert() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            GeneratedAlwaysMapper mapper = session.getMapper(GeneratedAlwaysMapper.class);
            GeneratedAlwaysRecord record = new GeneratedAlwaysRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName("Jones");

            int rows = mapper.insert(record);
            assertThat(rows).isEqualTo(1);
            assertThat(record.getFullName()).isEqualTo("Joe Jones");
        }
    }

    @Test
    void testBatchInsertWithList() {
        try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            GeneratedAlwaysMapper mapper = session.getMapper(GeneratedAlwaysMapper.class);
            List<GeneratedAlwaysRecord> records = getTestRecords();

            BatchInsert<GeneratedAlwaysRecord> batchInsert = insertBatch(records)
                    .into(generatedAlways)
                    .map(id).toProperty("id")
                    .map(firstName).toProperty("firstName")
                    .map(lastName).toProperty("lastName")
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            batchInsert.insertStatements().forEach(mapper::insert);

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
    void testBatchInsertWithArray() {
        try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            GeneratedAlwaysMapper mapper = session.getMapper(GeneratedAlwaysMapper.class);

            GeneratedAlwaysRecord record1 = new GeneratedAlwaysRecord();
            record1.setId(1000);
            record1.setFirstName("George");
            record1.setLastName("Jetson");

            GeneratedAlwaysRecord record2 = new GeneratedAlwaysRecord();
            record2.setId(1001);
            record2.setFirstName("Jane");
            record2.setLastName("Jetson");

            BatchInsert<GeneratedAlwaysRecord> batchInsert = insertBatch(record1, record2)
                    .into(generatedAlways)
                    .map(id).toProperty("id")
                    .map(firstName).toProperty("firstName")
                    .map(lastName).toProperty("lastName")
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            batchInsert.insertStatements().forEach(mapper::insert);

            session.commit();

            assertThat(record1.getFullName()).isEqualTo("George Jetson");
            assertThat(record2.getFullName()).isEqualTo("Jane Jetson");
        }
    }

    @Test
    void testMultiInsertWithListAndGeneratedKeys() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            GeneratedAlwaysMapper mapper = session.getMapper(GeneratedAlwaysMapper.class);
            List<GeneratedAlwaysRecord> records = getTestRecords();

            int rows = mapper.insertMultiple(records);

            assertThat(rows).isEqualTo(4);
            assertThat(records.get(0).getFullName()).isEqualTo("George Jetson");
            assertThat(records.get(1).getFullName()).isEqualTo("Jane Jetson");
            assertThat(records.get(2).getFullName()).isEqualTo("Judy Jetson");
            assertThat(records.get(3).getFullName()).isEqualTo("Elroy Jetson");
        }
    }

    @Test
    void testMultiInsertWithArray() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            GeneratedAlwaysMapper mapper = session.getMapper(GeneratedAlwaysMapper.class);

            GeneratedAlwaysRecord record1 = new GeneratedAlwaysRecord();
            record1.setId(1000);
            record1.setFirstName("George");
            record1.setLastName("Jetson");

            GeneratedAlwaysRecord record2 = new GeneratedAlwaysRecord();
            record2.setId(1001);
            record2.setFirstName("Jane");
            record2.setLastName("Jetson");

            int rows = mapper.insertMultiple(record1, record2);
            assertThat(rows).isEqualTo(2);
            assertThat(record1.getFullName()).isEqualTo("George Jetson");
            assertThat(record2.getFullName()).isEqualTo("Jane Jetson");
        }
    }

    @Test
    void testMultiInsertWithArrayAndVariousMappings() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            GeneratedAlwaysMapper mapper = session.getMapper(GeneratedAlwaysMapper.class);

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

            int rows = mapper.insertMultiple(multiRowInsert.getInsertStatement(), multiRowInsert.getRecords());

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
    void testInsertSelective() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            GeneratedAlwaysMapper mapper = session.getMapper(GeneratedAlwaysMapper.class);
            GeneratedAlwaysRecord record = new GeneratedAlwaysRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName("Jones");

            int rows = mapper.insertSelective(record);

            assertAll(
                    () -> assertThat(rows).isEqualTo(1),
                    () -> assertThat(record.getFullName()).isEqualTo("Joe Jones")
            );
        }
    }

    @Test
    void testUpdateByPrimaryKey() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            GeneratedAlwaysMapper mapper = session.getMapper(GeneratedAlwaysMapper.class);
            GeneratedAlwaysRecord record = new GeneratedAlwaysRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName("Jones");

            int rows = mapper.insert(record);
            assertThat(rows).isEqualTo(1);
            assertThat(record.getFullName()).isEqualTo("Joe Jones");

            record.setLastName("Smith");
            rows = mapper.updateByPrimaryKey(record);
            assertThat(rows).isEqualTo(1);

            Optional<GeneratedAlwaysRecord> newRecord = mapper.selectByPrimaryKey(100);
            assertThat(newRecord).hasValueSatisfying(c ->
                    assertThat(c.getFullName()).isEqualTo("Joe Smith")
            );
        }
    }

    @Test
    void testUpdateSelective() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            GeneratedAlwaysMapper mapper = session.getMapper(GeneratedAlwaysMapper.class);
            GeneratedAlwaysRecord record = new GeneratedAlwaysRecord();
            record.setLastName("Jones");

            int rows = mapper.update(c -> GeneratedAlwaysMapper.updateSelectiveColumns(record, c)
                    .where(lastName, isEqualTo("Flintstone")));
            assertThat(rows).isEqualTo(3);

            List<GeneratedAlwaysRecord> records = mapper.select(c ->
                    c.where(lastName, isEqualTo("Jones"))
                    .orderBy(firstName));

            assertAll(
                    () -> assertThat(records).hasSize(3),
                    () -> assertThat(records.get(0).getFullName()).isEqualTo("Fred Jones"),
                    () -> assertThat(records.get(1).getFullName()).isEqualTo("Pebbles Jones"),
                    () -> assertThat(records.get(2).getFullName()).isEqualTo("Wilma Jones")
            );
        }
    }

    @Test
    void testUpdateByPrimaryKeySelective() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            GeneratedAlwaysMapper mapper = session.getMapper(GeneratedAlwaysMapper.class);
            GeneratedAlwaysRecord record = new GeneratedAlwaysRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName("Jones");

            int rows = mapper.insert(record);
            assertThat(rows).isEqualTo(1);

            GeneratedAlwaysRecord updateRecord = new GeneratedAlwaysRecord();
            updateRecord.setId(100);
            updateRecord.setLastName("Smith");
            rows = mapper.updateByPrimaryKeySelective(updateRecord);
            assertThat(rows).isEqualTo(1);

            Optional<GeneratedAlwaysRecord> newRecord = mapper.selectByPrimaryKey(100);
            assertThat(newRecord).hasValueSatisfying(nr -> {
                assertThat(nr.getFirstName()).isEqualTo("Joe");
                assertThat(nr.getLastName()).isEqualTo("Smith");
                assertThat(nr.getFullName()).isEqualTo("Joe Smith");

            });
        }
    }
}
