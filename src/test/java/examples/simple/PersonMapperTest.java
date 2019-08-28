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

import static examples.simple.PersonDynamicSqlSupport.*;
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
import org.mybatis.dynamic.sql.delete.DeleteDSLCompleter;
import org.mybatis.dynamic.sql.select.CountDSLCompleter;
import org.mybatis.dynamic.sql.select.SelectDSLCompleter;

public class PersonMapperTest {

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
        config.addMapper(PersonMapper.class);
        config.addMapper(PersonWithAddressMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(config);
    }
    
    @Test
    public void testSelect() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);
            
            List<PersonRecord> rows = mapper.select(c ->
                    c.where(id, isEqualTo(1))
                    .or(occupation, isNull()));
            
            assertThat(rows.size()).isEqualTo(3);
        }
    }

    @Test
    public void testSelectAll() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);
            
            List<PersonRecord> rows = mapper.select(SelectDSLCompleter.allRows());
            
            assertThat(rows.size()).isEqualTo(6);
            assertThat(rows.get(0).getId()).isEqualTo(1);
            assertThat(rows.get(5).getId()).isEqualTo(6);
        }
    }

    @Test
    public void testSelectAllOrdered() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);
            
            List<PersonRecord> rows = mapper
                    .select(SelectDSLCompleter.allRowsOrderedBy(lastName.descending(), firstName.descending()));
            
            assertThat(rows.size()).isEqualTo(6);
            assertThat(rows.get(0).getId()).isEqualTo(5);
            assertThat(rows.get(5).getId()).isEqualTo(1);
        }
    }

    @Test
    public void testSelectDistinct() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);
            
            List<PersonRecord> rows = mapper.selectDistinct(c ->
                    c.where(id, isGreaterThan(1))
                    .or(occupation, isNull()));
            
            assertThat(rows.size()).isEqualTo(5);
        }
    }
    
    @Test
    public void testSelectWithTypeHandler() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);
            
            List<PersonRecord> rows = mapper.select(c ->
                    c.where(employed, isEqualTo(false))
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
            PersonMapper mapper = session.getMapper(PersonMapper.class);
            
            Optional<PersonRecord> record = mapper.selectByPrimaryKey(300);
            assertThat(record.isPresent()).isFalse();
        }
    }

    @Test
    public void testFirstNameIn() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);
            
            List<PersonRecord> rows = mapper.select(c ->
                    c.where(firstName, isIn("Fred", "Barney")));
            
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
            PersonMapper mapper = session.getMapper(PersonMapper.class);
            int rows = mapper.delete(c ->
                    c.where(occupation, isNull()));
            assertThat(rows).isEqualTo(2);
        }
    }
    
    @Test
    public void testDeleteAll() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);
            int rows = mapper.delete(DeleteDSLCompleter.allRows());
                    
            assertThat(rows).isEqualTo(6);
        }
    }
    
    @Test
    public void testDeleteByPrimaryKey() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);
            int rows = mapper.deleteByPrimaryKey(2);
            
            assertThat(rows).isEqualTo(1);
        }
    }
    
    @Test
    public void testInsert() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);
            PersonRecord record = new PersonRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName(LastName.of("Jones"));
            record.setBirthDate(new Date());
            record.setEmployed(true);
            record.setOccupation("Developer");
            record.setAddressId(1);
            
            int rows = mapper.insert(record);
            assertThat(rows).isEqualTo(1);
        }
    }

    @Test
    public void testInsertMultiple() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);
            
            List<PersonRecord> records = new ArrayList<>();
            
            PersonRecord record = new PersonRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName(LastName.of("Jones"));
            record.setBirthDate(new Date());
            record.setEmployed(true);
            record.setOccupation("Developer");
            record.setAddressId(1);
            records.add(record);
            
            record = new PersonRecord();
            record.setId(101);
            record.setFirstName("Sarah");
            record.setLastName(LastName.of("Smith"));
            record.setBirthDate(new Date());
            record.setEmployed(true);
            record.setOccupation("Architect");
            record.setAddressId(2);
            records.add(record);
            
            int rows = mapper.insertMultiple(records);
            assertThat(rows).isEqualTo(2);
        }
    }

    @Test
    public void testInsertSelective() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);
            PersonRecord record = new PersonRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName(LastName.of("Jones"));
            record.setBirthDate(new Date());
            record.setEmployed(false);
            record.setAddressId(1);
            
            int rows = mapper.insertSelective(record);
            assertThat(rows).isEqualTo(1);
        }
    }

    @Test
    public void testUpdateByPrimaryKey() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);
            PersonRecord record = new PersonRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName(LastName.of("Jones"));
            record.setBirthDate(new Date());
            record.setEmployed(true);
            record.setOccupation("Developer");
            record.setAddressId(1);
            
            int rows = mapper.insert(record);
            assertThat(rows).isEqualTo(1);
            
            record.setOccupation("Programmer");
            rows = mapper.updateByPrimaryKey(record);
            assertThat(rows).isEqualTo(1);
            
            Optional<PersonRecord> newRecord = mapper.selectByPrimaryKey(100);
            assertThat(newRecord.isPresent()).isTrue();
            assertThat(newRecord.get().getOccupation()).isEqualTo("Programmer");
        }
    }

    @Test
    public void testUpdateByPrimaryKeySelective() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);
            PersonRecord record = new PersonRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName(LastName.of("Jones"));
            record.setBirthDate(new Date());
            record.setEmployed(true);
            record.setOccupation("Developer");
            record.setAddressId(1);
            
            int rows = mapper.insert(record);
            assertThat(rows).isEqualTo(1);

            PersonRecord updateRecord = new PersonRecord();
            updateRecord.setId(100);
            updateRecord.setOccupation("Programmer");
            rows = mapper.updateByPrimaryKeySelective(updateRecord);
            assertThat(rows).isEqualTo(1);

            Optional<PersonRecord> newRecord = mapper.selectByPrimaryKey(100);
            assertThat(newRecord.isPresent()).isTrue();
            assertThat(newRecord.get().getOccupation()).isEqualTo("Programmer");
            assertThat(newRecord.get().getFirstName()).isEqualTo("Joe");
        }
    }

    @Test
    public void testUpdate() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);
            PersonRecord record = new PersonRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName(LastName.of("Jones"));
            record.setBirthDate(new Date());
            record.setEmployed(true);
            record.setOccupation("Developer");
            record.setAddressId(1);
            
            int rows = mapper.insert(record);
            assertThat(rows).isEqualTo(1);

            record.setOccupation("Programmer");
            
            rows = mapper.update(c ->
                PersonMapper.updateAllColumns(record, c)
                .where(id, isEqualTo(100))
                .and(firstName, isEqualTo("Joe")));

            assertThat(rows).isEqualTo(1);

            Optional<PersonRecord> newRecord = mapper.selectByPrimaryKey(100);
            assertThat(newRecord.isPresent()).isTrue();
            assertThat(newRecord.get().getOccupation()).isEqualTo("Programmer");
        }
    }

    @Test
    public void testUpdateOneField() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);
            PersonRecord record = new PersonRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName(LastName.of("Jones"));
            record.setBirthDate(new Date());
            record.setEmployed(true);
            record.setOccupation("Developer");
            record.setAddressId(1);
            
            int rows = mapper.insert(record);
            assertThat(rows).isEqualTo(1);
            
            rows = mapper.update(c ->
                c.set(occupation).equalTo("Programmer")
                .where(id, isEqualTo(100)));

            assertThat(rows).isEqualTo(1);

            Optional<PersonRecord> newRecord = mapper.selectByPrimaryKey(100);
            assertThat(newRecord.isPresent()).isTrue();
            assertThat(newRecord.get().getOccupation()).isEqualTo("Programmer");
        }
    }

    @Test
    public void testUpdateAll() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);
            PersonRecord record = new PersonRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName(LastName.of("Jones"));
            record.setBirthDate(new Date());
            record.setEmployed(true);
            record.setOccupation("Developer");
            record.setAddressId(1);
            
            int rows = mapper.insert(record);
            assertThat(rows).isEqualTo(1);
            
            PersonRecord updateRecord = new PersonRecord();
            updateRecord.setOccupation("Programmer");
            rows = mapper.update(c ->
                PersonMapper.updateSelectiveColumns(updateRecord, c));

            assertThat(rows).isEqualTo(7);

            Optional<PersonRecord> newRecord = mapper.selectByPrimaryKey(100);
            assertThat(newRecord.isPresent()).isTrue();
            assertThat(newRecord.get().getOccupation()).isEqualTo("Programmer");
        }
    }

    @Test
    public void testUpdateSelective() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);
            PersonRecord record = new PersonRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName(LastName.of("Jones"));
            record.setBirthDate(new Date());
            record.setEmployed(true);
            record.setOccupation("Developer");
            record.setAddressId(1);
            
            int rows = mapper.insert(record);
            assertThat(rows).isEqualTo(1);
            
            PersonRecord updateRecord = new PersonRecord();
            updateRecord.setOccupation("Programmer");
            rows = mapper.update(c ->
                PersonMapper.updateSelectiveColumns(updateRecord, c)
                .where(id, isEqualTo(100)));

            assertThat(rows).isEqualTo(1);

            Optional<PersonRecord> newRecord = mapper.selectByPrimaryKey(100);
            assertThat(newRecord.isPresent()).isTrue();
            assertThat(newRecord.get().getOccupation()).isEqualTo("Programmer");
        }
    }

    @Test
    public void testCount() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);
            long rows = mapper.count(c ->
                    c.where(occupation, isNull()));
            
            assertThat(rows).isEqualTo(2L);
        }
    }
    
    @Test
    public void testCountAll() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);
            long rows = mapper.count(CountDSLCompleter.allRows());
            
            assertThat(rows).isEqualTo(6L);
        }
    }
    
    @Test
    public void testTypeHandledLike() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);
            
            List<PersonRecord> rows = mapper.select(c ->
                    c.where(lastName, isLike(LastName.of("Fl%")))
                    .orderBy(id));
            
            assertThat(rows.size()).isEqualTo(3);
            assertThat(rows.get(0).getFirstName()).isEqualTo("Fred");
        }
    }
    
    @Test
    public void testTypeHandledNotLike() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);
            
            List<PersonRecord> rows = mapper.select(c -> 
                    c.where(lastName, isNotLike(LastName.of("Fl%")))
                    .orderBy(id));
            
            assertThat(rows.size()).isEqualTo(3);
            assertThat(rows.get(0).getFirstName()).isEqualTo("Barney");
        }
    }

    @Test
    public void testJoinAllRows() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonWithAddressMapper mapper = session.getMapper(PersonWithAddressMapper.class);
            List<PersonWithAddress> records = mapper.select(
                    SelectDSLCompleter.allRowsOrderedBy(id)
            );
            
            assertThat(records.size()).isEqualTo(6L);
            assertThat(records.get(0).getId()).isEqualTo(1);
            assertThat(records.get(0).getEmployed()).isTrue();
            assertThat(records.get(0).getFirstName()).isEqualTo("Fred");
            assertThat(records.get(0).getLastName()).isEqualTo(LastName.of("Flintstone"));
            assertThat(records.get(0).getOccupation()).isEqualTo("Brontosaurus Operator");
            assertThat(records.get(0).getBirthDate()).isNotNull();
            assertThat(records.get(0).getAddress().getId()).isEqualTo(1);
            assertThat(records.get(0).getAddress().getStreetAddress()).isEqualTo("123 Main Street");
            assertThat(records.get(0).getAddress().getCity()).isEqualTo("Bedrock");
            assertThat(records.get(0).getAddress().getState()).isEqualTo("IN");
        }
    }

    @Test
    public void testJoinOneRow() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonWithAddressMapper mapper = session.getMapper(PersonWithAddressMapper.class);
            List<PersonWithAddress> records = mapper.select(c -> c.where(id, isEqualTo(1)));
            
            assertThat(records.size()).isEqualTo(1L);
            assertThat(records.get(0).getId()).isEqualTo(1);
            assertThat(records.get(0).getEmployed()).isTrue();
            assertThat(records.get(0).getFirstName()).isEqualTo("Fred");
            assertThat(records.get(0).getLastName()).isEqualTo(LastName.of("Flintstone"));
            assertThat(records.get(0).getOccupation()).isEqualTo("Brontosaurus Operator");
            assertThat(records.get(0).getBirthDate()).isNotNull();
            assertThat(records.get(0).getAddress().getId()).isEqualTo(1);
            assertThat(records.get(0).getAddress().getStreetAddress()).isEqualTo("123 Main Street");
            assertThat(records.get(0).getAddress().getCity()).isEqualTo("Bedrock");
            assertThat(records.get(0).getAddress().getState()).isEqualTo("IN");
        }
    }

    @Test
    public void testJoinPrimaryKey() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonWithAddressMapper mapper = session.getMapper(PersonWithAddressMapper.class);
            Optional<PersonWithAddress> record = mapper.selectByPrimaryKey(1);
            
            assertThat(record).hasValueSatisfying(r -> {
                assertThat(r.getId()).isEqualTo(1);
                assertThat(r.getEmployed()).isTrue();
                assertThat(r.getFirstName()).isEqualTo("Fred");
                assertThat(r.getLastName()).isEqualTo(LastName.of("Flintstone"));
                assertThat(r.getOccupation()).isEqualTo("Brontosaurus Operator");
                assertThat(r.getBirthDate()).isNotNull();
                assertThat(r.getAddress().getId()).isEqualTo(1);
                assertThat(r.getAddress().getStreetAddress()).isEqualTo("123 Main Street");
                assertThat(r.getAddress().getCity()).isEqualTo("Bedrock");
                assertThat(r.getAddress().getState()).isEqualTo("IN");
            });
        }
    }

    @Test
    public void testJoinPrimaryKeyInvalidRecord() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonWithAddressMapper mapper = session.getMapper(PersonWithAddressMapper.class);
            Optional<PersonWithAddress> record = mapper.selectByPrimaryKey(55);
            
            assertThat(record).isEmpty();
        }
    }

    @Test
    public void testJoinCount() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonWithAddressMapper mapper = session.getMapper(PersonWithAddressMapper.class);
            long count = mapper.count(c -> c.where(person.id, isEqualTo(55)));
            
            assertThat(count).isEqualTo(0);
        }
    }

    @Test
    public void testJoinCountWithSubcriteria() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonWithAddressMapper mapper = session.getMapper(PersonWithAddressMapper.class);
            long count = mapper.count(c -> c.where(person.id, isEqualTo(55), or(person.id, isEqualTo(1))));
            
            assertThat(count).isEqualTo(1);
        }
    }
}
