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

import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mybatis.dynamic.sql.delete.render.DeleteStatement;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.render.SelectStatement;

@RunWith(JUnitPlatform.class)
public class SimpleTableXmlMapperTest {

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
        
        is = getClass().getResourceAsStream("/examples/simple/MapperConfig.xml");
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(is);
    }
    
    @Test
    public void testSelectByExample() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            SimpleTableXmlMapper mapper = session.getMapper(SimpleTableXmlMapper.class);
            
            SelectStatement selectStatement = selectByExample()
                    .where(id, isEqualTo(1))
                    .or(occupation, isNull())
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            
            List<SimpleTableRecord> rows = mapper.selectMany(selectStatement);
            
            assertThat(rows.size()).isEqualTo(3);
        } finally {
            session.close();
        }
    }

    @Test
    public void testSelectByExampleWithTypeHandler() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            SimpleTableXmlMapper mapper = session.getMapper(SimpleTableXmlMapper.class);
            
            SelectStatement selectStatement = selectByExample()
                    .where(employed, isEqualTo(false))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            
            List<SimpleTableRecord> rows = mapper.selectMany(selectStatement);
            
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
            SimpleTableXmlMapper mapper = session.getMapper(SimpleTableXmlMapper.class);
            
            SelectStatement selectStatement = selectByExample()
                    .where(firstName, isIn("Fred", "Barney"))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            
            List<SimpleTableRecord> rows = mapper.selectMany(selectStatement);
            
            assertThat(rows.size()).isEqualTo(2);
        } finally {
            session.close();
        }
    }

    @Test
    public void testDeleteByExample() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            SimpleTableXmlMapper mapper = session.getMapper(SimpleTableXmlMapper.class);
            DeleteStatement deleteStatement = deleteFrom(simpleTable)
                    .where(occupation, isNull())
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            int rows = mapper.delete(deleteStatement);
            
            assertThat(rows).isEqualTo(2);
        } finally {
            session.close();
        }
    }

    @Test
    public void testDeleteByPrimaryKey() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            SimpleTableXmlMapper mapper = session.getMapper(SimpleTableXmlMapper.class);
            DeleteStatement deleteStatement = buildDeleteByPrimaryKeyStatement(2);
            int rows = mapper.delete(deleteStatement);
            
            assertThat(rows).isEqualTo(1);
        } finally {
            session.close();
        }
    }

    @Test
    public void testInsert() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            SimpleTableXmlMapper mapper = session.getMapper(SimpleTableXmlMapper.class);
            SimpleTableRecord record = new SimpleTableRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName("Jones");
            record.setBirthDate(new Date());
            record.setEmployed(true);
            record.setOccupation("Developer");
            
            int rows = mapper.insert(buildFullInsert(record));
            
            assertThat(rows).isEqualTo(1);
        } finally {
            session.close();
        }
    }

    @Test
    public void testInsertSelective() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            SimpleTableXmlMapper mapper = session.getMapper(SimpleTableXmlMapper.class);
            SimpleTableRecord record = new SimpleTableRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName("Jones");
            record.setBirthDate(new Date());
            record.setEmployed(false);
            
            SoftAssertions.assertSoftly(softly -> {
                int rows = mapper.insert(buildSelectiveInsert(record));
                softly.assertThat(rows).isEqualTo(1);

                SimpleTableRecord returnedRecord = mapper.selectOne(buildSelectByPrimaryKeyStatement(100));
                softly.assertThat(returnedRecord.getOccupation()).isNull();
            });
        } finally {
            session.close();
        }
    }

    @Test
    public void testUpdateByPrimaryKey() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            SimpleTableXmlMapper mapper = session.getMapper(SimpleTableXmlMapper.class);
            SimpleTableRecord record = new SimpleTableRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName("Jones");
            record.setBirthDate(new Date());
            record.setEmployed(true);
            record.setOccupation("Developer");
            
            SoftAssertions.assertSoftly(softly -> {
                int rows = mapper.insert(buildFullInsert(record));
                softly.assertThat(rows).isEqualTo(1);

                record.setOccupation("Programmer");
                rows = mapper.update(buildFullUpdateByPrimaryKeyStatement(record));
                softly.assertThat(rows).isEqualTo(1);

                SimpleTableRecord newRecord = mapper.selectOne(buildSelectByPrimaryKeyStatement(100));
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
            SimpleTableXmlMapper mapper = session.getMapper(SimpleTableXmlMapper.class);
            SimpleTableRecord record = new SimpleTableRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName("Jones");
            record.setBirthDate(new Date());
            record.setEmployed(true);
            record.setOccupation("Developer");
            
            SoftAssertions.assertSoftly(softly -> {
                int rows = mapper.insert(buildFullInsert(record));
                softly.assertThat(rows).isEqualTo(1);

                SimpleTableRecord updateRecord = new SimpleTableRecord();
                updateRecord.setId(100);
                updateRecord.setOccupation("Programmer");
                rows = mapper.update(buildSelectiveUpdateByPrimaryKeyStatement(updateRecord));
                softly.assertThat(rows).isEqualTo(1);

                SimpleTableRecord newRecord = mapper.selectOne(buildSelectByPrimaryKeyStatement(100));
                softly.assertThat(newRecord.getOccupation()).isEqualTo("Programmer");
                softly.assertThat(newRecord.getFirstName()).isEqualTo("Joe");
            });
        } finally {
            session.close();
        }
    }


    @Test
    public void testCountByExample() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            SimpleTableXmlMapper mapper = session.getMapper(SimpleTableXmlMapper.class);
            SelectStatement selectStatement = select(count())
                    .from(simpleTable)
                    .where(occupation, isNull())
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            long rows = mapper.count(selectStatement);
            
            assertThat(rows).isEqualTo(2L);
        } finally {
            session.close();
        }
    }
}
