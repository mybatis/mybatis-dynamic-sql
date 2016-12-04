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
package examples.animal.data;

import static examples.animal.data.AnimalDataFields.animalName;
import static examples.animal.data.AnimalDataFields.bodyWeight;
import static examples.animal.data.AnimalDataFields.brainWeight;
import static examples.animal.data.AnimalDataFields.id;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mybatis.qbe.sql.SqlConditions.*;
import static org.mybatis.qbe.sql.delete.DeleteSupportBuilder.deleteSupport;
import static org.mybatis.qbe.sql.insert.InsertSupportBuilder.insertSupport;
import static org.mybatis.qbe.sql.select.SelectSupportBuilder.selectSupport;
import static org.mybatis.qbe.sql.update.UpdateSupportBuilder.updateSupport;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.Before;
import org.junit.Test;
import org.mybatis.qbe.sql.delete.DeleteSupport;
import org.mybatis.qbe.sql.insert.InsertSupport;
import org.mybatis.qbe.sql.select.SelectSupport;
import org.mybatis.qbe.sql.update.UpdateSupport;

public class AnimalDataTest {

    private static final String JDBC_URL = "jdbc:hsqldb:mem:aname";
    private static final String JDBC_DRIVER = "org.hsqldb.jdbcDriver"; 
    
    private SqlSessionFactory sqlSessionFactory;
    
    @Before
    public void setup() throws Exception {
        Class.forName(JDBC_DRIVER);
        InputStream is = getClass().getResourceAsStream("/examples/animal/data/CreateAnimalData.sql");
        try (Connection connection = DriverManager.getConnection(JDBC_URL, "sa", "")) {
            ScriptRunner sr = new ScriptRunner(connection);
            sr.setLogWriter(null);
            sr.runScript(new InputStreamReader(is));
        }
        
        UnpooledDataSource ds = new UnpooledDataSource(JDBC_DRIVER, JDBC_URL, "sa", "");
        Environment environment = new Environment("test", new JdbcTransactionFactory(), ds);
        Configuration config = new Configuration(environment);
        config.addMapper(AnimalDataMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(config);
    }
    
    @Test
    public void testSelectAllRows() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            List<AnimalData> animals = mapper.selectByExample(null);
            assertThat(animals.size(), is(65));
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testSelectRowsLessThan20() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = selectSupport()
                    .where(id, isLessThan(20))
                    .build();
            
            List<AnimalData> animals = mapper.selectByExample(selectSupport);
            assertThat(animals.size(), is(19));
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testSelectRowsBetween30And40() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = selectSupport()
                    .where(id, isBetween(30).and(40))
                    .build();

            List<AnimalData> animals = mapper.selectByExample(selectSupport);
            assertThat(animals.size(), is(11));
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testSelectRowsNotBetweenWithProvider() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = selectSupport()
                    .where(id, isNotBetween(10).and(60))
                    .build();

            List<AnimalData> animals = mapper.selectByExample(selectSupport);
            assertThat(animals.size(), is(14));
        } finally {
            sqlSession.close();
        }
    }
    
    @Test
    public void testSelectRowsNotBetween() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = selectSupport()
                    .where(id, isNotBetween(10).and(60))
                    .build();

            List<AnimalData> animals = mapper.selectByExample(selectSupport);
            assertThat(animals.size(), is(14));
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testIsEqualCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = selectSupport()
                    .where(id, isEqualTo(5))
                    .build();

            List<AnimalData> animals = mapper.selectByExample(selectSupport);
            assertThat(animals.size(), is(1));
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testIsNotEqualCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = selectSupport()
                    .where(id, isNotEqualTo(5))
                    .build();

            List<AnimalData> animals = mapper.selectByExample(selectSupport);
            assertThat(animals.size(), is(64));
        } finally {
            sqlSession.close();
        }
    }
    
    @Test
    public void testIsGreaterThanOrEqualToCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = selectSupport()
                    .where(id, isGreaterThanOrEqualTo(60))
                    .build();

            List<AnimalData> animals = mapper.selectByExample(selectSupport);
            assertThat(animals.size(), is(6));
        } finally {
            sqlSession.close();
        }
    }
    
    @Test
    public void testIsLessThanOrEqualToCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = selectSupport()
                    .where(id, isLessThanOrEqualTo(10))
                    .build();

            List<AnimalData> animals = mapper.selectByExample(selectSupport);
            assertThat(animals.size(), is(10));
        } finally {
            sqlSession.close();
        }
    }
    
    @Test
    public void testInCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = selectSupport()
                    .where(id, isIn(5, 8, 10))
                    .build();

            List<AnimalData> animals = mapper.selectByExample(selectSupport);
            assertThat(animals.size(), is(3));
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testNotInCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = selectSupport()
                    .where(id, isNotIn(5, 8, 10))
                    .build();

            List<AnimalData> animals = mapper.selectByExample(selectSupport);
            assertThat(animals.size(), is(62));
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testLikeCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = selectSupport()
                    .where(animalName, isLike("%squirrel"))
                    .build();

            List<AnimalData> animals = mapper.selectByExample(selectSupport);
            assertThat(animals.size(), is(2));
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testLikeCaseInsensitive() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = selectSupport()
                    .where(animalName, isLikeCaseInsensitive("%squirrel"))
                    .build();

            List<AnimalData> animals = mapper.selectByExample(selectSupport);
            assertThat(animals.size(), is(2));
        } finally {
            sqlSession.close();
        }
    }
    
    @Test
    public void testNotLikeCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = selectSupport()
                    .where(animalName, isNotLike("%squirrel"))
                    .build();

            List<AnimalData> animals = mapper.selectByExample(selectSupport);
            assertThat(animals.size(), is(63));
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testNotLikeCaseInsensistveCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = selectSupport()
                    .where(animalName, isNotLikeCaseInsensitive("%squirrel"))
                    .build();

            List<AnimalData> animals = mapper.selectByExample(selectSupport);
            assertThat(animals.size(), is(63));
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testDeleteThreeRows() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            DeleteSupport deleteSupport = deleteSupport()
                    .where(id, isIn(5, 8, 10))
                    .build();

            int rowCount = mapper.deleteByExample(deleteSupport);
            assertThat(rowCount, is(3));
        } finally {
            sqlSession.close();
        }
    }
    
    @Test
    public void testComplexDelete() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            DeleteSupport deleteSupport = deleteSupport()
                    .where(id, isLessThan(10))
                    .or(id, isGreaterThan(60))
                    .build();

            int rowCount = mapper.deleteByExample(deleteSupport);
            assertThat(rowCount, is(14));
        } finally {
            sqlSession.close();
        }
    }
    
    @Test
    public void testIsNullCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = selectSupport()
                    .where(id, isNull())
                    .build();

            List<AnimalData> animals = mapper.selectByExample(selectSupport);
            assertThat(animals.size(), is(0));
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testIsNotNullCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = selectSupport()
                    .where(id, isNotNull())
                    .build();

            List<AnimalData> animals = mapper.selectByExample(selectSupport);
            assertThat(animals.size(), is(65));
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testComplexCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = selectSupport()
                    .where(id, isIn(1, 5, 7))
                    .or(id, isIn(2, 6, 8), and(animalName, isLike("%bat")))
                    .or(id, isGreaterThan(60))
                    .and(bodyWeight, isBetween(1.0).and(3.0))
                    .build();

            List<AnimalData> animals = mapper.selectByExample(selectSupport);
            assertThat(animals.size(), is(4));
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testUpdateByExample() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            AnimalData record = new AnimalData();
            record.setBodyWeight(2.6);
            
            UpdateSupport updateSupport = updateSupport()
                    .set(bodyWeight, record.getBodyWeight())
                    .setNull(animalName)
                    .where(id, isIn(1, 5, 7))
                    .or(id, isIn(2, 6, 8), and(animalName, isLike("%bat")))
                    .or(id, isGreaterThan(60))
                    .and(bodyWeight, isBetween(1.0).and(3.0))
                    .build();

            int rows = mapper.updateByExample(updateSupport);
            assertThat(rows, is(4));
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testInsert() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            AnimalData record = new AnimalData();
            record.setId(100);
            record.setAnimalName("Old Shep");
            record.setBodyWeight(22.5);
            record.setBrainWeight(1.2);
            
            InsertSupport<AnimalData> insertSupport = insertSupport(record) 
                    .withFieldMapping(id, "id", record::getId)
                    .withFieldMapping(animalName, "animalName", record::getAnimalName)
                    .withFieldMapping(bodyWeight, "bodyWeight", record::getBodyWeight)
                    .withFieldMapping(brainWeight, "brainWeight", record::getBrainWeight)
                    .buildFullInsert();
            
            int rows = mapper.insert(insertSupport);
            assertThat(rows, is(1));
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testOrderByAndDistinct() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = selectSupport()
                    .distinct()
                    .where(id, isLessThan(10))
                    .or(id,  isGreaterThan(60))
                    .orderBy("id desc")
                    .build();
            
            List<AnimalData> rows = mapper.selectByExample(selectSupport);
            assertThat(rows.size(), is(14));
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testOrderByWithFullClause() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = selectSupport()
                    .where(id, isLessThan(10))
                    .or(id,  isGreaterThan(60))
                    .orderBy("order by id desc")
                    .build();
            
            List<AnimalData> rows = mapper.selectByExample(selectSupport);
            assertThat(rows.size(), is(14));
        } finally {
            sqlSession.close();
        }
    }
}
