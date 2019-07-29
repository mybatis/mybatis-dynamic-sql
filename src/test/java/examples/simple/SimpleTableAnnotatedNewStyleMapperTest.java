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
package examples.simple;

import static examples.simple.SimpleTableDynamicSqlSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Date;
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
import org.mybatis.dynamic.sql.util.mybatis3.MyBatis3CountByExampleHelper;
import org.mybatis.dynamic.sql.util.mybatis3.MyBatis3DeleteByExampleHelper;
import org.mybatis.dynamic.sql.util.mybatis3.MyBatis3SelectByExampleHelper;
import org.mybatis.dynamic.sql.util.mybatis3.MyBatis3UpdateByExampleHelper;

public class SimpleTableAnnotatedNewStyleMapperTest {

    private static final String JDBC_URL = "jdbc:hsqldb:mem:aname";
    private static final String JDBC_DRIVER = "org.hsqldb.jdbcDriver"; 
    
    private SqlSessionFactory sqlSessionFactory;
    
    @BeforeEach
    public void setup() throws Exception {
        Class.forName(JDBC_DRIVER);
        InputStream is = getClass().getResourceAsStream("/examples/simple/CreateSimpleDB.sql");
        try (Connection connection = DriverManager.getConnection(JDBC_URL, "sa", "")) {
            ScriptRunner sr = new ScriptRunner(connection);
            sr.setLogWriter(null);
            sr.runScript(new InputStreamReader(is));
        }
        
        UnpooledDataSource ds = new UnpooledDataSource(JDBC_DRIVER, JDBC_URL, "sa", "");
        Environment environment = new Environment("test", new JdbcTransactionFactory(), ds);
        Configuration config = new Configuration(environment);
        config.addMapper(SimpleTableAnnotatedMapperNewStyle.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(config);
    }
    
    @Test
    public void testSelectByExample() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            SimpleTableAnnotatedMapperNewStyle mapper = session.getMapper(SimpleTableAnnotatedMapperNewStyle.class);
            
            List<SimpleTableRecord> rows = mapper.selectByExample(q ->
                    q.where(id, isEqualTo(1))
                    .or(occupation, isNull()));
            
            assertThat(rows.size()).isEqualTo(3);
        }
    }

    @Test
    public void testSelectAll() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            SimpleTableAnnotatedMapperNewStyle mapper = session.getMapper(SimpleTableAnnotatedMapperNewStyle.class);
            
            List<SimpleTableRecord> rows = mapper.selectByExample(MyBatis3SelectByExampleHelper.allRows());
            
            assertThat(rows.size()).isEqualTo(6);
        }
    }

    @Test
    public void testSelectDistinctByExample() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            SimpleTableAnnotatedMapperNewStyle mapper = session.getMapper(SimpleTableAnnotatedMapperNewStyle.class);
            
            List<SimpleTableRecord> rows = mapper.selectDistinctByExample(q ->
                    q.where(id, isGreaterThan(1))
                    .or(occupation, isNull()));
            
            assertThat(rows.size()).isEqualTo(5);
        }
    }
    
    @Test
    public void testSelectByExampleWithTypeHandler() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            SimpleTableAnnotatedMapperNewStyle mapper = session.getMapper(SimpleTableAnnotatedMapperNewStyle.class);
            
            List<SimpleTableRecord> rows = mapper.selectByExample(q ->
                    q.where(employed, isEqualTo(false))
                    .orderBy(id));
            
            assertAll(
                    () -> assertThat(rows.size()).isEqualTo(2),
                    () -> assertThat(rows.get(0).getId()).isEqualTo(3),
                    () -> assertThat(rows.get(1).getId()).isEqualTo(6)
            );
        }
    }

    @Test
    public void testFirstNameIn() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            SimpleTableAnnotatedMapperNewStyle mapper = session.getMapper(SimpleTableAnnotatedMapperNewStyle.class);
            
            List<SimpleTableRecord> rows = mapper.selectByExample(q ->
                    q.where(firstName, isIn("Fred", "Barney")));
            
            assertAll(
                    () -> assertThat(rows.size()).isEqualTo(2),
                    () -> assertThat(rows.get(0).getLastName().getName()).isEqualTo("Flintstone"),
                    () -> assertThat(rows.get(1).getLastName().getName()).isEqualTo("Rubble")
            );
        }
    }

    @Test
    public void testDeleteByExample() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            SimpleTableAnnotatedMapperNewStyle mapper = session.getMapper(SimpleTableAnnotatedMapperNewStyle.class);
            int rows = mapper.deleteByExample(q ->
                    q.where(occupation, isNull()));
            assertThat(rows).isEqualTo(2);
        }
    }
    
    @Test
    public void testDeleteAll() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            SimpleTableAnnotatedMapperNewStyle mapper = session.getMapper(SimpleTableAnnotatedMapperNewStyle.class);
            int rows = mapper.deleteByExample(MyBatis3DeleteByExampleHelper.allRows());
                    
            assertThat(rows).isEqualTo(6);
        }
    }
    
    @Test
    public void testDeleteByPrimaryKey() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            SimpleTableAnnotatedMapperNewStyle mapper = session.getMapper(SimpleTableAnnotatedMapperNewStyle.class);
            int rows = mapper.deleteByPrimaryKey(2);
            
            assertThat(rows).isEqualTo(1);
        }
    }
    
    @Test
    public void testInsert() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            SimpleTableAnnotatedMapperNewStyle mapper = session.getMapper(SimpleTableAnnotatedMapperNewStyle.class);
            SimpleTableRecord record = new SimpleTableRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName(LastName.of("Jones"));
            record.setBirthDate(new Date());
            record.setEmployed(true);
            record.setOccupation("Developer");
            
            int rows = mapper.insert(record);
            assertThat(rows).isEqualTo(1);
        }
    }

    @Test
    public void testInsertMultiple() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            SimpleTableAnnotatedMapperNewStyle mapper = session.getMapper(SimpleTableAnnotatedMapperNewStyle.class);
            
            List<SimpleTableRecord> records = new ArrayList<>();
            
            SimpleTableRecord record = new SimpleTableRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName(LastName.of("Jones"));
            record.setBirthDate(new Date());
            record.setEmployed(true);
            record.setOccupation("Developer");
            records.add(record);
            
            record = new SimpleTableRecord();
            record.setId(101);
            record.setFirstName("Sarah");
            record.setLastName(LastName.of("Smith"));
            record.setBirthDate(new Date());
            record.setEmployed(true);
            record.setOccupation("Architect");
            records.add(record);
            
            int rows = mapper.insertMultiple(records);
            assertThat(rows).isEqualTo(2);
        }
    }

    @Test
    public void testInsertSelective() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            SimpleTableAnnotatedMapperNewStyle mapper = session.getMapper(SimpleTableAnnotatedMapperNewStyle.class);
            SimpleTableRecord record = new SimpleTableRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName(LastName.of("Jones"));
            record.setBirthDate(new Date());
            record.setEmployed(false);
            
            int rows = mapper.insertSelective(record);
            assertThat(rows).isEqualTo(1);
        }
    }

    @Test
    public void testUpdateByPrimaryKey() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            SimpleTableAnnotatedMapperNewStyle mapper = session.getMapper(SimpleTableAnnotatedMapperNewStyle.class);
            SimpleTableRecord record = new SimpleTableRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName(LastName.of("Jones"));
            record.setBirthDate(new Date());
            record.setEmployed(true);
            record.setOccupation("Developer");
            
            int rows = mapper.insert(record);
            assertThat(rows).isEqualTo(1);
            
            record.setOccupation("Programmer");
            rows = mapper.updateByPrimaryKey(record);
            assertThat(rows).isEqualTo(1);
            
            SimpleTableRecord newRecord = mapper.selectByPrimaryKey(100);
            assertThat(newRecord.getOccupation()).isEqualTo("Programmer");
        }
    }

    @Test
    public void testUpdateByPrimaryKeySelective() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            SimpleTableAnnotatedMapperNewStyle mapper = session.getMapper(SimpleTableAnnotatedMapperNewStyle.class);
            SimpleTableRecord record = new SimpleTableRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName(LastName.of("Jones"));
            record.setBirthDate(new Date());
            record.setEmployed(true);
            record.setOccupation("Developer");
            
            int rows = mapper.insert(record);
            assertThat(rows).isEqualTo(1);

            SimpleTableRecord updateRecord = new SimpleTableRecord();
            updateRecord.setId(100);
            updateRecord.setOccupation("Programmer");
            rows = mapper.updateByPrimaryKeySelective(updateRecord);
            assertThat(rows).isEqualTo(1);

            SimpleTableRecord newRecord = mapper.selectByPrimaryKey(100);
            assertThat(newRecord.getOccupation()).isEqualTo("Programmer");
            assertThat(newRecord.getFirstName()).isEqualTo("Joe");
        }
    }

    @Test
    public void testUpdateByExample() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            SimpleTableAnnotatedMapperNewStyle mapper = session.getMapper(SimpleTableAnnotatedMapperNewStyle.class);
            SimpleTableRecord record = new SimpleTableRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName(LastName.of("Jones"));
            record.setBirthDate(new Date());
            record.setEmployed(true);
            record.setOccupation("Developer");
            
            int rows = mapper.insert(record);
            assertThat(rows).isEqualTo(1);

            record.setOccupation("Programmer");
            
            rows = mapper.updateByExample(q ->
                    q.where(id, isEqualTo(100))
                    .and(firstName, isEqualTo("Joe")))
                    .usingRecord(record);

            assertThat(rows).isEqualTo(1);

            SimpleTableRecord newRecord = mapper.selectByPrimaryKey(100);
            assertThat(newRecord.getOccupation()).isEqualTo("Programmer");
        }
    }

    @Test
    public void testUpdateAll() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            SimpleTableAnnotatedMapperNewStyle mapper = session.getMapper(SimpleTableAnnotatedMapperNewStyle.class);
            SimpleTableRecord record = new SimpleTableRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName(LastName.of("Jones"));
            record.setBirthDate(new Date());
            record.setEmployed(true);
            record.setOccupation("Developer");
            
            int rows = mapper.insert(record);
            assertThat(rows).isEqualTo(1);
            
            record = new SimpleTableRecord();
            record.setOccupation("Programmer");
            rows = mapper.updateByExampleSelective(MyBatis3UpdateByExampleHelper.allRows()).usingRecord(record);

            assertThat(rows).isEqualTo(7);

            SimpleTableRecord newRecord = mapper.selectByPrimaryKey(100);
            assertThat(newRecord.getOccupation()).isEqualTo("Programmer");
        }
    }

    @Test
    public void testCountByExample() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            SimpleTableAnnotatedMapperNewStyle mapper = session.getMapper(SimpleTableAnnotatedMapperNewStyle.class);
            long rows = mapper.countByExample(q ->
                    q.where(occupation, isNull()));
            
            assertThat(rows).isEqualTo(2L);
        }
    }
    
    @Test
    public void testCountAll() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            SimpleTableAnnotatedMapperNewStyle mapper = session.getMapper(SimpleTableAnnotatedMapperNewStyle.class);
            long rows = mapper.countByExample(MyBatis3CountByExampleHelper.allRows());
            
            assertThat(rows).isEqualTo(6L);
        }
    }
    
    @Test
    public void testTypeHandledLike() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            SimpleTableAnnotatedMapperNewStyle mapper = session.getMapper(SimpleTableAnnotatedMapperNewStyle.class);
            
            List<SimpleTableRecord> rows = mapper.selectByExample(q ->
                    q.where(lastName, isLike(LastName.of("Fl%")))
                    .orderBy(id));
            
            assertThat(rows.size()).isEqualTo(3);
            assertThat(rows.get(0).getFirstName()).isEqualTo("Fred");
        }
    }
    
    @Test
    public void testTypeHandledNotLike() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            SimpleTableAnnotatedMapperNewStyle mapper = session.getMapper(SimpleTableAnnotatedMapperNewStyle.class);
            
            List<SimpleTableRecord> rows = mapper.selectByExample(q -> 
                    q.where(lastName, isNotLike(LastName.of("Fl%")))
                    .orderBy(id));
            
            assertThat(rows.size()).isEqualTo(3);
            assertThat(rows.get(0).getFirstName()).isEqualTo("Barney");
        }
    }
}
