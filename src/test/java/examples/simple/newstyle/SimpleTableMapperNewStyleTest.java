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
package examples.simple.newstyle;

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
import java.util.Optional;

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
import org.mybatis.dynamic.sql.util.mybatis3.MyBatis3CountHelper;
import org.mybatis.dynamic.sql.util.mybatis3.MyBatis3DeleteHelper;
import org.mybatis.dynamic.sql.util.mybatis3.MyBatis3SelectListHelper;

import examples.simple.LastName;
import examples.simple.SimpleTableRecord;

public class SimpleTableMapperNewStyleTest {

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
        config.addMapper(SimpleTableMapperNewStyle.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(config);
    }
    
    @Test
    public void testSelect() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            SimpleTableMapperNewStyle mapper = session.getMapper(SimpleTableMapperNewStyle.class);
            
            List<SimpleTableRecord> rows = mapper.select(h ->
                    h.where(id, isEqualTo(1))
                    .or(occupation, isNull()));
            
            assertThat(rows.size()).isEqualTo(3);
        }
    }

    @Test
    public void testSelectAll() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            SimpleTableMapperNewStyle mapper = session.getMapper(SimpleTableMapperNewStyle.class);
            
            List<SimpleTableRecord> rows = mapper.select(MyBatis3SelectListHelper.allRows());
            
            assertThat(rows.size()).isEqualTo(6);
            assertThat(rows.get(0).getId()).isEqualTo(1);
            assertThat(rows.get(5).getId()).isEqualTo(6);
        }
    }

    @Test
    public void testSelectAllOrdered() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            SimpleTableMapperNewStyle mapper = session.getMapper(SimpleTableMapperNewStyle.class);
            
            List<SimpleTableRecord> rows = mapper
                    .select(MyBatis3SelectListHelper.allRowsOrderdBy(lastName.descending(), firstName.descending()));
            
            assertThat(rows.size()).isEqualTo(6);
            assertThat(rows.get(0).getId()).isEqualTo(5);
            assertThat(rows.get(5).getId()).isEqualTo(1);
        }
    }

    @Test
    public void testSelectDistinct() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            SimpleTableMapperNewStyle mapper = session.getMapper(SimpleTableMapperNewStyle.class);
            
            List<SimpleTableRecord> rows = mapper.selectDistinct(h ->
                    h.where(id, isGreaterThan(1))
                    .or(occupation, isNull()));
            
            assertThat(rows.size()).isEqualTo(5);
        }
    }
    
    @Test
    public void testSelectWithTypeHandler() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            SimpleTableMapperNewStyle mapper = session.getMapper(SimpleTableMapperNewStyle.class);
            
            List<SimpleTableRecord> rows = mapper.select(h ->
                    h.where(employed, isEqualTo(false))
                    .orderBy(id));
            
            assertAll(
                    () -> assertThat(rows.size()).isEqualTo(2),
                    () -> assertThat(rows.get(0).getId()).isEqualTo(3),
                    () -> assertThat(rows.get(1).getId()).isEqualTo(6)
            );
        }
    }

    @Test
    public void testSelectByPrimaryKeyWithMissingRecord() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            SimpleTableMapperNewStyle mapper = session.getMapper(SimpleTableMapperNewStyle.class);
            
            Optional<SimpleTableRecord> record = mapper.selectByPrimaryKey(300);
            assertThat(record.isPresent()).isFalse();
        }
    }

    @Test
    public void testFirstNameIn() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            SimpleTableMapperNewStyle mapper = session.getMapper(SimpleTableMapperNewStyle.class);
            
            List<SimpleTableRecord> rows = mapper.select(h ->
                    h.where(firstName, isIn("Fred", "Barney")));
            
            assertAll(
                    () -> assertThat(rows.size()).isEqualTo(2),
                    () -> assertThat(rows.get(0).getLastName().getName()).isEqualTo("Flintstone"),
                    () -> assertThat(rows.get(1).getLastName().getName()).isEqualTo("Rubble")
            );
        }
    }

    @Test
    public void testDelete() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            SimpleTableMapperNewStyle mapper = session.getMapper(SimpleTableMapperNewStyle.class);
            int rows = mapper.delete(h ->
                    h.where(occupation, isNull()));
            assertThat(rows).isEqualTo(2);
        }
    }
    
    @Test
    public void testDeleteAll() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            SimpleTableMapperNewStyle mapper = session.getMapper(SimpleTableMapperNewStyle.class);
            int rows = mapper.delete(MyBatis3DeleteHelper.allRows());
                    
            assertThat(rows).isEqualTo(6);
        }
    }
    
    @Test
    public void testDeleteByPrimaryKey() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            SimpleTableMapperNewStyle mapper = session.getMapper(SimpleTableMapperNewStyle.class);
            int rows = mapper.deleteByPrimaryKey(2);
            
            assertThat(rows).isEqualTo(1);
        }
    }
    
    @Test
    public void testInsert() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            SimpleTableMapperNewStyle mapper = session.getMapper(SimpleTableMapperNewStyle.class);
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
            SimpleTableMapperNewStyle mapper = session.getMapper(SimpleTableMapperNewStyle.class);
            
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
            SimpleTableMapperNewStyle mapper = session.getMapper(SimpleTableMapperNewStyle.class);
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
            SimpleTableMapperNewStyle mapper = session.getMapper(SimpleTableMapperNewStyle.class);
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
            
            Optional<SimpleTableRecord> newRecord = mapper.selectByPrimaryKey(100);
            assertThat(newRecord.isPresent()).isTrue();
            assertThat(newRecord.get().getOccupation()).isEqualTo("Programmer");
        }
    }

    @Test
    public void testUpdateByPrimaryKeySelective() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            SimpleTableMapperNewStyle mapper = session.getMapper(SimpleTableMapperNewStyle.class);
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

            Optional<SimpleTableRecord> newRecord = mapper.selectByPrimaryKey(100);
            assertThat(newRecord.isPresent()).isTrue();
            assertThat(newRecord.get().getOccupation()).isEqualTo("Programmer");
            assertThat(newRecord.get().getFirstName()).isEqualTo("Joe");
        }
    }

    @Test
    public void testUpdate() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            SimpleTableMapperNewStyle mapper = session.getMapper(SimpleTableMapperNewStyle.class);
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
            
            rows = mapper.update(h ->
                SimpleTableMapperNewStyle.setAll(record, h)
                .where(id, isEqualTo(100))
                .and(firstName, isEqualTo("Joe")));

            assertThat(rows).isEqualTo(1);

            Optional<SimpleTableRecord> newRecord = mapper.selectByPrimaryKey(100);
            assertThat(newRecord.isPresent()).isTrue();
            assertThat(newRecord.get().getOccupation()).isEqualTo("Programmer");
        }
    }

    @Test
    public void testUpdateOneField() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            SimpleTableMapperNewStyle mapper = session.getMapper(SimpleTableMapperNewStyle.class);
            SimpleTableRecord record = new SimpleTableRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName(LastName.of("Jones"));
            record.setBirthDate(new Date());
            record.setEmployed(true);
            record.setOccupation("Developer");
            
            int rows = mapper.insert(record);
            assertThat(rows).isEqualTo(1);
            
            rows = mapper.update(h ->
                h.set(occupation).equalTo("Programmer")
                .where(id, isEqualTo(100)));

            assertThat(rows).isEqualTo(1);

            Optional<SimpleTableRecord> newRecord = mapper.selectByPrimaryKey(100);
            assertThat(newRecord.isPresent()).isTrue();
            assertThat(newRecord.get().getOccupation()).isEqualTo("Programmer");
        }
    }

    @Test
    public void testUpdateAll() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            SimpleTableMapperNewStyle mapper = session.getMapper(SimpleTableMapperNewStyle.class);
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
            updateRecord.setOccupation("Programmer");
            rows = mapper.update(h ->
                SimpleTableMapperNewStyle.setSelective(updateRecord, h));

            assertThat(rows).isEqualTo(7);

            Optional<SimpleTableRecord> newRecord = mapper.selectByPrimaryKey(100);
            assertThat(newRecord.isPresent()).isTrue();
            assertThat(newRecord.get().getOccupation()).isEqualTo("Programmer");
        }
    }

    @Test
    public void testUpdateSelective() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            SimpleTableMapperNewStyle mapper = session.getMapper(SimpleTableMapperNewStyle.class);
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
            updateRecord.setOccupation("Programmer");
            rows = mapper.update(h ->
                SimpleTableMapperNewStyle.setSelective(updateRecord, h)
                .where(id, isEqualTo(100)));

            assertThat(rows).isEqualTo(1);

            Optional<SimpleTableRecord> newRecord = mapper.selectByPrimaryKey(100);
            assertThat(newRecord.isPresent()).isTrue();
            assertThat(newRecord.get().getOccupation()).isEqualTo("Programmer");
        }
    }

    @Test
    public void testCount() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            SimpleTableMapperNewStyle mapper = session.getMapper(SimpleTableMapperNewStyle.class);
            long rows = mapper.count(h ->
                    h.where(occupation, isNull()));
            
            assertThat(rows).isEqualTo(2L);
        }
    }
    
    @Test
    public void testCountAll() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            SimpleTableMapperNewStyle mapper = session.getMapper(SimpleTableMapperNewStyle.class);
            long rows = mapper.count(MyBatis3CountHelper.allRows());
            
            assertThat(rows).isEqualTo(6L);
        }
    }
    
    @Test
    public void testTypeHandledLike() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            SimpleTableMapperNewStyle mapper = session.getMapper(SimpleTableMapperNewStyle.class);
            
            List<SimpleTableRecord> rows = mapper.select(h ->
                    h.where(lastName, isLike(LastName.of("Fl%")))
                    .orderBy(id));
            
            assertThat(rows.size()).isEqualTo(3);
            assertThat(rows.get(0).getFirstName()).isEqualTo("Fred");
        }
    }
    
    @Test
    public void testTypeHandledNotLike() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            SimpleTableMapperNewStyle mapper = session.getMapper(SimpleTableMapperNewStyle.class);
            
            List<SimpleTableRecord> rows = mapper.select(h -> 
                    h.where(lastName, isNotLike(LastName.of("Fl%")))
                    .orderBy(id));
            
            assertThat(rows.size()).isEqualTo(3);
            assertThat(rows.get(0).getFirstName()).isEqualTo("Barney");
        }
    }
}
