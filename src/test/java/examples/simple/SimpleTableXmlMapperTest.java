/**
 *    Copyright 2016 the original author or authors.
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

import static examples.simple.SimpleTableFields.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mybatis.qbe.sql.SqlConditions.*;
import static org.mybatis.qbe.sql.select.SelectSupportBuilder.selectSupport;
import static org.mybatis.qbe.sql.delete.DeleteSupportBuilder.deleteSupport;

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
import org.junit.Before;
import org.junit.Test;
import org.mybatis.qbe.sql.delete.DeleteSupport;
import org.mybatis.qbe.sql.select.SelectSupport;

public class SimpleTableXmlMapperTest {

    private static final String JDBC_URL = "jdbc:hsqldb:mem:aname";
    private static final String JDBC_DRIVER = "org.hsqldb.jdbcDriver"; 
    
    private SqlSessionFactory sqlSessionFactory;
    
    @Before
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
            
            SelectSupport selectSupport = selectSupport()
                    .where(id, isEqualTo(1))
                    .or(occupation, isNull())
                    .build();
            
            List<SimpleTableRecord> rows = mapper.selectByExample(selectSupport);
            
            assertThat(rows.size(), is(3));
        } finally {
            session.close();
        }
    }

    @Test
    public void testFirstNameIn() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            SimpleTableXmlMapper mapper = session.getMapper(SimpleTableXmlMapper.class);
            
            SelectSupport selectSupport = selectSupport()
                    .where(firstName, isIn("Fred", "Barney"))
                    .build();
            
            List<SimpleTableRecord> rows = mapper.selectByExample(selectSupport);
            
            assertThat(rows.size(), is(2));
        } finally {
            session.close();
        }
    }

    @Test
    public void testDeleteByExample() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            SimpleTableXmlMapper mapper = session.getMapper(SimpleTableXmlMapper.class);
            DeleteSupport deleteSupport = deleteSupport()
                    .where(occupation, isNull())
                    .build();
            int rows = mapper.deleteByExample(deleteSupport);
            
            assertThat(rows, is(2));
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
            record.setOccupation("Developer");
            
            int rows = mapper.insert(buildInsertSupport(record));
            
            assertThat(rows, is(1));
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
            
            int rows = mapper.insert(buildInsertSelectiveSupport(record));
            
            assertThat(rows, is(1));
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
            record.setOccupation("Developer");
            
            int rows = mapper.insert(buildInsertSupport(record));
            assertThat(rows, is(1));
            
            record.setOccupation("Programmer");
            rows = mapper.update(buildUpdateByPrimaryKeySupport(record));
            assertThat(rows, is(1));
            
            SimpleTableRecord newRecord = mapper.selectByPrimaryKey(100);
            assertThat(newRecord.getOccupation(), is("Programmer"));
            
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
            record.setOccupation("Developer");
            
            int rows = mapper.insert(buildInsertSupport(record));
            assertThat(rows, is(1));

            SimpleTableRecord updateRecord = new SimpleTableRecord();
            updateRecord.setId(100);
            updateRecord.setOccupation("Programmer");
            rows = mapper.update(buildUpdateByPrimaryKeySelectiveSupport(updateRecord));
            assertThat(rows, is(1));
            
            SimpleTableRecord newRecord = mapper.selectByPrimaryKey(100);
            assertThat(newRecord.getOccupation(), is("Programmer"));
            assertThat(newRecord.getFirstName(), is("Joe"));
            
        } finally {
            session.close();
        }
    }
}
