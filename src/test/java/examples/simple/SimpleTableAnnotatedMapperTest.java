/**
 *    Copyright 2016-2017 the original author or authors.
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
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;

@RunWith(JUnitPlatform.class)
public class SimpleTableAnnotatedMapperTest {

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
        config.addMapper(SimpleTableAnnotatedMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(config);
    }
    
    @Test
    public void testSelectByExample() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            SimpleTableAnnotatedMapper mapper = session.getMapper(SimpleTableAnnotatedMapper.class);
            
            List<SimpleTableRecord> rows = mapper.selectByExample()
                    .where(id, isEqualTo(1))
                    .or(occupation, isNull())
                    .build()
                    .execute();
            
            assertThat(rows.size()).isEqualTo(3);
        } finally {
            session.close();
        }
    }

    @Test
    public void testSelectDistinctByExample() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            SimpleTableAnnotatedMapper mapper = session.getMapper(SimpleTableAnnotatedMapper.class);
            
            List<SimpleTableRecord> rows = mapper.selectDistinctByExample()
                    .where(id, isGreaterThan(1))
                    .or(occupation, isNull())
                    .build()
                    .execute();
            
            assertThat(rows.size()).isEqualTo(5);
        } finally {
            session.close();
        }
    }
    
    @Test
    public void testSelectByExampleWithTypeHandler() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            SimpleTableAnnotatedMapper mapper = session.getMapper(SimpleTableAnnotatedMapper.class);
            
            List<SimpleTableRecord> rows = mapper.selectByExample()
                    .where(employed, isEqualTo(false))
                    .orderBy(id)
                    .build()
                    .execute();
            
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(rows.size()).isEqualTo(2);
                softly.assertThat(rows.get(0).getId()).isEqualTo(3);
                softly.assertThat(rows.get(1).getId()).isEqualTo(6);
            });
        } finally {
            session.close();
        }
    }

    @Test
    public void testFirstNameIn() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            SimpleTableAnnotatedMapper mapper = session.getMapper(SimpleTableAnnotatedMapper.class);
            
            List<SimpleTableRecord> rows = mapper.selectByExample()
                    .where(firstName, isIn("Fred", "Barney"))
                    .build()
                    .execute();
            
            assertThat(rows.size()).isEqualTo(2);
        } finally {
            session.close();
        }
    }

    @Test
    public void testDeleteByExample() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            SimpleTableAnnotatedMapper mapper = session.getMapper(SimpleTableAnnotatedMapper.class);
            int rows = mapper.deleteByExample()
                    .where(occupation, isNull())
                    .build()
                    .execute();
            assertThat(rows).isEqualTo(2);
        } finally {
            session.close();
        }
    }
    
    @Test
    public void testDeleteByPrimaryKey() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            SimpleTableAnnotatedMapper mapper = session.getMapper(SimpleTableAnnotatedMapper.class);
            int rows = mapper.deleteByPrimaryKey(2);
            
            assertThat(rows).isEqualTo(1);
        } finally {
            session.close();
        }
    }
    
    @Test
    public void testInsert() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            SimpleTableAnnotatedMapper mapper = session.getMapper(SimpleTableAnnotatedMapper.class);
            SimpleTableRecord record = new SimpleTableRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName("Jones");
            record.setBirthDate(new Date());
            record.setEmployed(true);
            record.setOccupation("Developer");
            
            int rows = mapper.insert(record);
            assertThat(rows).isEqualTo(1);
        } finally {
            session.close();
        }
    }

    @Test
    public void testInsertSelective() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            SimpleTableAnnotatedMapper mapper = session.getMapper(SimpleTableAnnotatedMapper.class);
            SimpleTableRecord record = new SimpleTableRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName("Jones");
            record.setBirthDate(new Date());
            record.setEmployed(false);
            
            int rows = mapper.insertSelective(record);
            assertThat(rows).isEqualTo(1);
        } finally {
            session.close();
        }
    }

    @Test
    public void testUpdateByPrimaryKey() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            SimpleTableAnnotatedMapper mapper = session.getMapper(SimpleTableAnnotatedMapper.class);
            SimpleTableRecord record = new SimpleTableRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName("Jones");
            record.setBirthDate(new Date());
            record.setEmployed(true);
            record.setOccupation("Developer");
            
            SoftAssertions.assertSoftly(softly -> {
                int rows = mapper.insert(record);
                softly.assertThat(rows).isEqualTo(1);
            
                record.setOccupation("Programmer");
                rows = mapper.updateByPrimaryKey(record);
                softly.assertThat(rows).isEqualTo(1);
            
                SimpleTableRecord newRecord = mapper.selectByPrimaryKey(100);
                softly.assertThat(newRecord.getOccupation()).isEqualTo("Programmer");
            });
        } finally {
            session.close();
        }
    }

    @Test
    public void testUpdateByPrimaryKeySelective() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            SimpleTableAnnotatedMapper mapper = session.getMapper(SimpleTableAnnotatedMapper.class);
            SimpleTableRecord record = new SimpleTableRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName("Jones");
            record.setBirthDate(new Date());
            record.setEmployed(true);
            record.setOccupation("Developer");
            
            SoftAssertions.assertSoftly(softly -> {
                int rows = mapper.insert(record);
                softly.assertThat(rows).isEqualTo(1);

                SimpleTableRecord updateRecord = new SimpleTableRecord();
                updateRecord.setId(100);
                updateRecord.setOccupation("Programmer");
                rows = mapper.updateByPrimaryKeySelective(updateRecord);
                softly.assertThat(rows).isEqualTo(1);

                SimpleTableRecord newRecord = mapper.selectByPrimaryKey(100);
                softly.assertThat(newRecord.getOccupation()).isEqualTo("Programmer");
                softly.assertThat(newRecord.getFirstName()).isEqualTo("Joe");
            });
            
        } finally {
            session.close();
        }
    }

    @Test
    public void testUpdateWithNulls() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            SimpleTableAnnotatedMapper mapper = session.getMapper(SimpleTableAnnotatedMapper.class);
            SimpleTableRecord record = new SimpleTableRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName("Jones");
            record.setBirthDate(new Date());
            record.setEmployed(true);
            record.setOccupation("Developer");
            
            SoftAssertions.assertSoftly(softly -> {
                int rows = mapper.insert(record);
                softly.assertThat(rows).isEqualTo(1);

                UpdateStatementProvider updateStatement = update(simpleTable)
                        .set(occupation).equalToNull()
                        .set(employed).equalTo(false)
                        .where(id, isEqualTo(100))
                        .build()
                        .render(RenderingStrategy.MYBATIS3);

                rows = mapper.update(updateStatement);
                softly.assertThat(rows).isEqualTo(1);

                SimpleTableRecord newRecord = mapper.selectByPrimaryKey(100);
                softly.assertThat(newRecord.getOccupation()).isNull();
                softly.assertThat(newRecord.getEmployed()).isEqualTo(false);
                softly.assertThat(newRecord.getFirstName()).isEqualTo("Joe");
            });
        } finally {
            session.close();
        }
    }

    @Test
    public void testUpdateByExample() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            SimpleTableAnnotatedMapper mapper = session.getMapper(SimpleTableAnnotatedMapper.class);
            SimpleTableRecord record = new SimpleTableRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName("Jones");
            record.setBirthDate(new Date());
            record.setEmployed(true);
            record.setOccupation("Developer");
            
            SoftAssertions.assertSoftly(softly -> {
                int rows = mapper.insert(record);
                softly.assertThat(rows).isEqualTo(1);

                record.setOccupation("Programmer");
                rows = mapper.updateByExample(record)
                        .where(id, isEqualTo(100))
                        .and(firstName, isEqualTo("Joe"))
                        .build()
                        .execute();

                softly.assertThat(rows).isEqualTo(1);

                SimpleTableRecord newRecord = mapper.selectByPrimaryKey(100);
                softly.assertThat(newRecord.getOccupation()).isEqualTo("Programmer");
            });
        } finally {
            session.close();
        }
    }

    @Test
    public void testCountByExample() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            SimpleTableAnnotatedMapper mapper = session.getMapper(SimpleTableAnnotatedMapper.class);
            long rows = mapper.countByExample()
                    .where(occupation, isNull())
                    .build()
                    .execute();
            
            assertThat(rows).isEqualTo(2L);
        } finally {
            session.close();
        }
    }
}
