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
package examples.simple.legacy;

import static examples.simple.PersonDynamicSqlSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Date;
import java.util.List;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;

import examples.simple.LastName;
import examples.simple.PersonRecord;

public class LegacyPersonMapperTest {

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
        config.addMapper(LegacyPersonMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(config);
    }
    
    @Test
    public void testSelectByExample() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            LegacyPersonMapper mapper = session.getMapper(LegacyPersonMapper.class);
            
            List<PersonRecord> rows = mapper.selectByExample()
                    .where(id, isEqualTo(1))
                    .or(occupation, isNull())
                    .build()
                    .execute();
            
            assertThat(rows.size()).isEqualTo(3);
        }
    }

    @Test
    public void testSelectByExampleWithRowbounds() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            LegacyPersonMapper mapper = session.getMapper(LegacyPersonMapper.class);
            RowBounds rowBounds = new RowBounds(2, 2);
            
            List<PersonRecord> rows = mapper.selectByExample(rowBounds)
                    .where(id, isEqualTo(1))
                    .or(occupation, isNull())
                    .build()
                    .execute();
            
            assertThat(rows.size()).isEqualTo(1);
        }
    }

    @Test
    public void testSelectDistinctByExample() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            LegacyPersonMapper mapper = session.getMapper(LegacyPersonMapper.class);
            
            List<PersonRecord> rows = mapper.selectDistinctByExample()
                    .where(id, isGreaterThan(1))
                    .or(occupation, isNull())
                    .build()
                    .execute();
            
            assertThat(rows.size()).isEqualTo(5);
        }
    }
    
    @Test
    public void testSelectDistinctByExampleWithRowbounds() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            LegacyPersonMapper mapper = session.getMapper(LegacyPersonMapper.class);
            RowBounds rowBounds = new RowBounds(2, 2);
            
            List<PersonRecord> rows = mapper.selectDistinctByExample(rowBounds)
                    .where(id, isGreaterThan(1))
                    .or(occupation, isNull())
                    .build()
                    .execute();
            
            assertThat(rows.size()).isEqualTo(2);
        }
    }
    
    @Test
    public void testSelectByExampleWithTypeHandler() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            LegacyPersonMapper mapper = session.getMapper(LegacyPersonMapper.class);
            
            List<PersonRecord> rows = mapper.selectByExample()
                    .where(employed, isEqualTo(false))
                    .orderBy(id)
                    .build()
                    .execute();
            
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
            LegacyPersonMapper mapper = session.getMapper(LegacyPersonMapper.class);
            
            List<PersonRecord> rows = mapper.selectByExample()
                    .where(firstName, isIn("Fred", "Barney"))
                    .build()
                    .execute();
            
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
            LegacyPersonMapper mapper = session.getMapper(LegacyPersonMapper.class);
            int rows = mapper.deleteByExample()
                    .where(occupation, isNull())
                    .build()
                    .execute();
            assertThat(rows).isEqualTo(2);
        }
    }
    
    @Test
    public void testDeleteByPrimaryKey() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            LegacyPersonMapper mapper = session.getMapper(LegacyPersonMapper.class);
            int rows = mapper.deleteByPrimaryKey(2);
            
            assertThat(rows).isEqualTo(1);
        }
    }
    
    @Test
    public void testInsert() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            LegacyPersonMapper mapper = session.getMapper(LegacyPersonMapper.class);
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
    public void testInsertSelective() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            LegacyPersonMapper mapper = session.getMapper(LegacyPersonMapper.class);
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
            LegacyPersonMapper mapper = session.getMapper(LegacyPersonMapper.class);
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
            
            PersonRecord newRecord = mapper.selectByPrimaryKey(100);
            assertThat(newRecord.getOccupation()).isEqualTo("Programmer");
        }
    }

    @Test
    public void testUpdateByPrimaryKeySelective() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            LegacyPersonMapper mapper = session.getMapper(LegacyPersonMapper.class);
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

            PersonRecord newRecord = mapper.selectByPrimaryKey(100);
            assertThat(newRecord.getOccupation()).isEqualTo("Programmer");
            assertThat(newRecord.getFirstName()).isEqualTo("Joe");
        }
    }

    @Test
    public void testUpdateWithNulls() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            LegacyPersonMapper mapper = session.getMapper(LegacyPersonMapper.class);
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

            UpdateStatementProvider updateStatement = update(person)
                    .set(occupation).equalToNull()
                    .set(employed).equalTo(false)
                    .where(id, isEqualTo(100))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            rows = mapper.update(updateStatement);
            assertThat(rows).isEqualTo(1);

            PersonRecord newRecord = mapper.selectByPrimaryKey(100);
            assertAll(
                    () -> assertThat(newRecord.getOccupation()).isNull(),
                    () -> assertThat(newRecord.getEmployed()).isEqualTo(false),
                    () -> assertThat(newRecord.getFirstName()).isEqualTo("Joe")
            );
        }
    }

    @Test
    public void testUpdateByExample() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            LegacyPersonMapper mapper = session.getMapper(LegacyPersonMapper.class);
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
            rows = mapper.updateByExample(record)
                    .where(id, isEqualTo(100))
                    .and(firstName, isEqualTo("Joe"))
                    .build()
                    .execute();

            assertThat(rows).isEqualTo(1);

            PersonRecord newRecord = mapper.selectByPrimaryKey(100);
            assertThat(newRecord.getOccupation()).isEqualTo("Programmer");
        }
    }

    @Test
    public void testCountByExample() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            LegacyPersonMapper mapper = session.getMapper(LegacyPersonMapper.class);
            long rows = mapper.countByExample()
                    .where(occupation, isNull())
                    .build()
                    .execute();
            
            assertThat(rows).isEqualTo(2L);
        }
    }
    
    @Test
    public void testTypeHandledLike() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            LegacyPersonMapper mapper = session.getMapper(LegacyPersonMapper.class);
            
            List<PersonRecord> rows = mapper.selectByExample()
                    .where(lastName, isLike(LastName.of("Fl%")))
                    .orderBy(id)
                    .build()
                    .execute();
            
            assertThat(rows.size()).isEqualTo(3);
            assertThat(rows.get(0).getFirstName()).isEqualTo("Fred");
        }
    }
    
    @Test
    public void testTypeHandledNotLike() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            LegacyPersonMapper mapper = session.getMapper(LegacyPersonMapper.class);
            
            List<PersonRecord> rows = mapper.selectByExample()
                    .where(lastName, isNotLike(LastName.of("Fl%")))
                    .orderBy(id)
                    .build()
                    .execute();
            
            assertThat(rows.size()).isEqualTo(3);
            assertThat(rows.get(0).getFirstName()).isEqualTo("Barney");
        }
    }
}
