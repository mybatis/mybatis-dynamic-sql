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
package examples.generated.always;

import static examples.generated.always.GeneratedAlwaysDynamicSqlSupport.*;
import static org.assertj.core.api.Assertions.*;
import static org.mybatis.dynamic.sql.SqlBuilder.*;
import static org.mybatis.dynamic.sql.SqlConditions.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mybatis.dynamic.sql.select.SelectSupport;
import org.mybatis.dynamic.sql.update.render.UpdateSupport;

public class GeneratedAlwaysXmlMapperTest {

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    private static final String JDBC_URL = "jdbc:hsqldb:mem:aname";
    private static final String JDBC_DRIVER = "org.hsqldb.jdbcDriver"; 
    
    private SqlSessionFactory sqlSessionFactory;
    
    @Before
    public void setup() throws Exception {
        Class.forName(JDBC_DRIVER).newInstance();
        InputStream is = getClass().getResourceAsStream("/examples/generated/always/CreateGeneratedAlwaysDB.sql");
        try (Connection connection = DriverManager.getConnection(JDBC_URL, "sa", "")) {
            ScriptRunner sr = new ScriptRunner(connection);
            sr.setLogWriter(null);
            sr.runScript(new InputStreamReader(is));
        }
        
        is = getClass().getResourceAsStream("/examples/generated/always/MapperConfig.xml");
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(is);
    }
    
    @Test
    public void testSelectByExample() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            GeneratedAlwaysXmlMapper mapper = session.getMapper(GeneratedAlwaysXmlMapper.class);
            
            SelectSupport selectSupport = select(id, firstName, lastName, fullName)
                    .from(generatedAlways)
                    .where(id, isEqualTo(1))
                    .build();
            
            List<GeneratedAlwaysRecord> rows = mapper.selectByExample(selectSupport);
            
            assertThat(rows.size()).isEqualTo(1);
        } finally {
            session.close();
        }
    }

    @Test
    public void testFirstNameIn() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            GeneratedAlwaysXmlMapper mapper = session.getMapper(GeneratedAlwaysXmlMapper.class);
            
            SelectSupport selectSupport = select(id, firstName, lastName, fullName)
                    .from(generatedAlways)
                    .where(firstName, isIn("Fred", "Barney"))
                    .build();
            
            List<GeneratedAlwaysRecord> rows = mapper.selectByExample(selectSupport);
            
            assertThat(rows.size()).isEqualTo(2);
        } finally {
            session.close();
        }
    }

    @Test
    public void testInsert() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            GeneratedAlwaysXmlMapper mapper = session.getMapper(GeneratedAlwaysXmlMapper.class);
            GeneratedAlwaysRecord record = new GeneratedAlwaysRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName("Jones");
            
            int rows = mapper.insert(buildInsertSupport(record));
            
            softly.assertThat(rows).isEqualTo(1);
            softly.assertThat(record.getFullName()).isEqualTo("Joe Jones");
        } finally {
            session.close();
        }
    }

    @Test
    public void testInsertSelective() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            GeneratedAlwaysXmlMapper mapper = session.getMapper(GeneratedAlwaysXmlMapper.class);
            GeneratedAlwaysRecord record = new GeneratedAlwaysRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName("Jones");
            
            int rows = mapper.insert(buildInsertSelectiveSupport(record));
            
            softly.assertThat(rows).isEqualTo(1);
            softly.assertThat(record.getFullName()).isEqualTo("Joe Jones");
        } finally {
            session.close();
        }
    }

    @Test
    public void testUpdateByPrimaryKey() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            GeneratedAlwaysXmlMapper mapper = session.getMapper(GeneratedAlwaysXmlMapper.class);
            GeneratedAlwaysRecord record = new GeneratedAlwaysRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName("Jones");
            
            int rows = mapper.insert(buildInsertSupport(record));
            softly.assertThat(rows).isEqualTo(1);
            softly.assertThat(record.getFullName()).isEqualTo("Joe Jones");
            
            record.setLastName("Smith");
            rows = mapper.update(buildUpdateByPrimaryKeySupport(record));
            softly.assertThat(rows).isEqualTo(1);
            
            GeneratedAlwaysRecord newRecord = mapper.selectByPrimaryKey(100);
            softly.assertThat(newRecord.getFullName()).isEqualTo("Joe Smith");
        } finally {
            session.close();
        }
    }

    @Test
    public void testUpdateByExampleSelective() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            GeneratedAlwaysXmlMapper mapper = session.getMapper(GeneratedAlwaysXmlMapper.class);
            GeneratedAlwaysRecord record = new GeneratedAlwaysRecord();
            record.setLastName("Jones");
            
            UpdateSupport updateSupport = updateByExampleSelective(record)
                    .where(lastName, isEqualTo("Flintstone"))
                    .buildAndRender();
            
            int rows = mapper.update(updateSupport);
            assertThat(rows).isEqualTo(3);
            
            SelectSupport selectSupport = select(id, firstName, lastName, fullName)
                    .from(generatedAlways)
                    .where(lastName, isEqualTo("Jones"))
                    .orderBy(firstName)
                    .build();
            
            List<GeneratedAlwaysRecord> records = mapper.selectByExample(selectSupport);
            softly.assertThat(records.size()).isEqualTo(3);
            softly.assertThat(records.get(0).getFullName()).isEqualTo("Fred Jones");
            softly.assertThat(records.get(1).getFullName()).isEqualTo("Pebbles Jones");
            softly.assertThat(records.get(2).getFullName()).isEqualTo("Wilma Jones");
        } finally {
            session.close();
        }
    }
    
    @Test
    public void testUpdateByExample() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            GeneratedAlwaysXmlMapper mapper = session.getMapper(GeneratedAlwaysXmlMapper.class);
            GeneratedAlwaysRecord record = new GeneratedAlwaysRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName("Jones");
            
            int rows = mapper.insert(buildInsertSupport(record));
            assertThat(rows).isEqualTo(1);

            GeneratedAlwaysRecord updateRecord = new GeneratedAlwaysRecord();
            updateRecord.setId(100);
            updateRecord.setLastName("Smith");
            rows = mapper.update(buildUpdateByPrimaryKeySelectiveSupport(updateRecord));
            assertThat(rows).isEqualTo(1);
            
            GeneratedAlwaysRecord newRecord = mapper.selectByPrimaryKey(100);
            softly.assertThat(newRecord.getFirstName()).isEqualTo("Joe");
            softly.assertThat(newRecord.getLastName()).isEqualTo("Smith");
            softly.assertThat(newRecord.getFullName()).isEqualTo("Joe Smith");
            
        } finally {
            session.close();
        }
    }
}
