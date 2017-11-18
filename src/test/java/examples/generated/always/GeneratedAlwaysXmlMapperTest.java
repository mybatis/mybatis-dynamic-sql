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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mybatis.dynamic.sql.insert.render.InsertBatchProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.render.SelectProvider;
import org.mybatis.dynamic.sql.update.render.UpdateProvider;

@RunWith(JUnitPlatform.class)
public class GeneratedAlwaysXmlMapperTest {

    private static final String JDBC_URL = "jdbc:hsqldb:mem:aname";
    private static final String JDBC_DRIVER = "org.hsqldb.jdbcDriver"; 
    
    private SqlSessionFactory sqlSessionFactory;
    
    @BeforeEach
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
            
            SelectProvider selectProvider = select(id, firstName, lastName, fullName)
                    .from(generatedAlways)
                    .where(id, isEqualTo(1))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            
            List<GeneratedAlwaysRecord> rows = mapper.selectMany(selectProvider);
            
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
            
            SelectProvider selectProvider = select(id, firstName, lastName, fullName)
                    .from(generatedAlways)
                    .where(firstName, isIn("Fred", "Barney"))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            
            List<GeneratedAlwaysRecord> rows = mapper.selectMany(selectProvider);
            
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
            
            int rows = mapper.insert(buildInsertProvider(record));
            
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(rows).isEqualTo(1);
                softly.assertThat(record.getFullName()).isEqualTo("Joe Jones");
            });
        } finally {
            session.close();
        }
    }

    @Test
    public void testInsertBatchWithList() {
        SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH);
        try {
            GeneratedAlwaysXmlMapper mapper = session.getMapper(GeneratedAlwaysXmlMapper.class);
            List<GeneratedAlwaysRecord> records = getTestRecords();
            
            InsertBatchProvider<GeneratedAlwaysRecord> insertBatchProvider = insert(records)
                    .into(generatedAlways)
                    .map(id).toProperty("id")
                    .map(firstName).toProperty("firstName")
                    .map(lastName).toProperty("lastName")
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            
            insertBatchProvider.insertProviders().stream().forEach(mapper::insert);
            
            session.commit();
            
            assertThat(records.get(0).getFullName()).isEqualTo("George Jetson");
            assertThat(records.get(1).getFullName()).isEqualTo("Jane Jetson");
            assertThat(records.get(2).getFullName()).isEqualTo("Judy Jetson");
            assertThat(records.get(3).getFullName()).isEqualTo("Elroy Jetson");
        } finally {
            session.close();
        }
    }

    @Test
    public void testInsertBatchWithArray() {
        SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH);
        try {
            GeneratedAlwaysXmlMapper mapper = session.getMapper(GeneratedAlwaysXmlMapper.class);

            GeneratedAlwaysRecord record1 = new GeneratedAlwaysRecord();
            record1.setId(1000);
            record1.setFirstName("George");
            record1.setLastName("Jetson");

            GeneratedAlwaysRecord record2 = new GeneratedAlwaysRecord();
            record2.setId(1001);
            record2.setFirstName("Jane");
            record2.setLastName("Jetson");
            
            InsertBatchProvider<GeneratedAlwaysRecord> insertBatchProvider = insert(record1, record2)
                    .into(generatedAlways)
                    .map(id).toProperty("id")
                    .map(firstName).toProperty("firstName")
                    .map(lastName).toProperty("lastName")
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            
            insertBatchProvider.insertProviders().stream().forEach(mapper::insert);
            
            session.commit();
            
            assertThat(record1.getFullName()).isEqualTo("George Jetson");
            assertThat(record2.getFullName()).isEqualTo("Jane Jetson");
        } finally {
            session.close();
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
        SqlSession session = sqlSessionFactory.openSession();
        try {
            GeneratedAlwaysXmlMapper mapper = session.getMapper(GeneratedAlwaysXmlMapper.class);
            GeneratedAlwaysRecord record = new GeneratedAlwaysRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName("Jones");
            
            int rows = mapper.insert(buildInsertSelectiveProvider(record));
            
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(rows).isEqualTo(1);
                softly.assertThat(record.getFullName()).isEqualTo("Joe Jones");
            });
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
            
            SoftAssertions.assertSoftly(softly -> {
                int rows = mapper.insert(buildInsertProvider(record));
                softly.assertThat(rows).isEqualTo(1);
                softly.assertThat(record.getFullName()).isEqualTo("Joe Jones");
            
                record.setLastName("Smith");
                rows = mapper.update(buildUpdateByPrimaryKeyProvider(record));
                softly.assertThat(rows).isEqualTo(1);
            
                GeneratedAlwaysRecord newRecord = mapper.selectByPrimaryKey(100);
                softly.assertThat(newRecord.getFullName()).isEqualTo("Joe Smith");
            });
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
            
            UpdateProvider updateProvider = updateByExampleSelective(record)
                    .where(lastName, isEqualTo("Flintstone"))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            
            int rows = mapper.update(updateProvider);
            assertThat(rows).isEqualTo(3);
            
            SelectProvider selectProvider = select(id, firstName, lastName, fullName)
                    .from(generatedAlways)
                    .where(lastName, isEqualTo("Jones"))
                    .orderBy(firstName)
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            
            List<GeneratedAlwaysRecord> records = mapper.selectMany(selectProvider);
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(records.size()).isEqualTo(3);
                softly.assertThat(records.get(0).getFullName()).isEqualTo("Fred Jones");
                softly.assertThat(records.get(1).getFullName()).isEqualTo("Pebbles Jones");
                softly.assertThat(records.get(2).getFullName()).isEqualTo("Wilma Jones");
            });
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
            
            int rows = mapper.insert(buildInsertProvider(record));
            assertThat(rows).isEqualTo(1);

            GeneratedAlwaysRecord updateRecord = new GeneratedAlwaysRecord();
            updateRecord.setId(100);
            updateRecord.setLastName("Smith");
            rows = mapper.update(buildUpdateByPrimaryKeySelectiveProvider(updateRecord));
            assertThat(rows).isEqualTo(1);
            
            GeneratedAlwaysRecord newRecord = mapper.selectByPrimaryKey(100);
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(newRecord.getFirstName()).isEqualTo("Joe");
                softly.assertThat(newRecord.getLastName()).isEqualTo("Smith");
                softly.assertThat(newRecord.getFullName()).isEqualTo("Joe Smith");
            });
        } finally {
            session.close();
        }
    }
}
