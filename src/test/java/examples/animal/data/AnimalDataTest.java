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
package examples.animal.data;

import static examples.animal.data.AnimalDataDynamicSqlSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mybatis.dynamic.sql.SqlBuilder.*;
import static org.mybatis.dynamic.sql.SqlConditions.*;

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
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mybatis.dynamic.sql.delete.render.DeleteSupport;
import org.mybatis.dynamic.sql.insert.render.InsertSupport;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.render.SelectSupport;
import org.mybatis.dynamic.sql.update.render.UpdateSupport;

@RunWith(JUnitPlatform.class)
public class AnimalDataTest {
    
    private static final String JDBC_URL = "jdbc:hsqldb:mem:aname";
    private static final String JDBC_DRIVER = "org.hsqldb.jdbcDriver"; 
    
    private SqlSessionFactory sqlSessionFactory;
    
    @BeforeEach
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
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectSupport);
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(animals.size()).isEqualTo(65);
                softly.assertThat(animals.get(0).getId()).isEqualTo(1);
            });
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testSelectAllRowsWithOrder() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .orderBy(id.descending())
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectSupport);
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(animals.size()).isEqualTo(65);
                softly.assertThat(animals.get(0).getId()).isEqualTo(65);
            });
        } finally {
            sqlSession.close();
        }
    }
    
    @Test
    public void testSelectRowsLessThan20() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isLessThan(20))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            
            List<AnimalData> animals = mapper.selectMany(selectSupport);
            assertThat(animals.size()).isEqualTo(19);
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testSelectRowsBetween30And40() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isBetween(30).and(40))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectSupport);
            assertThat(animals.size()).isEqualTo(11);
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testSelectRowsNotBetweenWithProvider() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isNotBetween(10).and(60))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectSupport);
            assertThat(animals.size()).isEqualTo(14);
        } finally {
            sqlSession.close();
        }
    }
    
    @Test
    public void testSelectRowsNotBetween() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isNotBetween(10).and(60))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectSupport);
            assertThat(animals.size()).isEqualTo(14);
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testIsEqualCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isEqualTo(5))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectSupport);
            assertThat(animals.size()).isEqualTo(1);
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testIsNotEqualCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isNotEqualTo(5))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectSupport);
            assertThat(animals.size()).isEqualTo(64);
        } finally {
            sqlSession.close();
        }
    }
    
    @Test
    public void testIsGreaterThanOrEqualToCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isGreaterThanOrEqualTo(60))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectSupport);
            assertThat(animals.size()).isEqualTo(6);
        } finally {
            sqlSession.close();
        }
    }
    
    @Test
    public void testIsLessThanOrEqualToCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isLessThanOrEqualTo(10))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectSupport);
            assertThat(animals.size()).isEqualTo(10);
        } finally {
            sqlSession.close();
        }
    }
    
    @Test
    public void testInCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isIn(5, 8, 10))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectSupport);
            assertThat(animals.size()).isEqualTo(3);
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testInCaseSensitiveCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isInCaseInsensitive("yellow-bellied marmot", "verbet"))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectSupport);
            assertThat(animals.size()).isEqualTo(2);
        } finally {
            sqlSession.close();
        }
    }
    
    @Test
    public void testNotInCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isNotIn(5, 8, 10))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectSupport);
            assertThat(animals.size()).isEqualTo(62);
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testNotInCaseSensitiveCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isNotInCaseInsensitive("yellow-bellied marmot", "verbet"))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectSupport);
            assertThat(animals.size()).isEqualTo(63);
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testLikeCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isLike("%squirrel"))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectSupport);
            assertThat(animals.size()).isEqualTo(2);
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testLikeCaseInsensitive() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isLikeCaseInsensitive("%squirrel"))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectSupport);
            assertThat(animals.size()).isEqualTo(2);
        } finally {
            sqlSession.close();
        }
    }
    
    @Test
    public void testNotLikeCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isNotLike("%squirrel"))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectSupport);
            assertThat(animals.size()).isEqualTo(63);
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testNotLikeCaseInsensistveCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isNotLikeCaseInsensitive("%squirrel"))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectSupport);
            assertThat(animals.size()).isEqualTo(63);
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testDeleteThreeRows() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            DeleteSupport deleteSupport = deleteFrom(animalData)
                    .where(id, isIn(5, 8, 10))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            int rowCount = mapper.delete(deleteSupport);
            assertThat(rowCount).isEqualTo(3);
        } finally {
            sqlSession.close();
        }
    }
    
    @Test
    public void testComplexDelete() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            DeleteSupport deleteSupport = deleteFrom(animalData)
                    .where(id, isLessThan(10))
                    .or(id, isGreaterThan(60))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            int rowCount = mapper.delete(deleteSupport);
            assertThat(rowCount).isEqualTo(14);
        } finally {
            sqlSession.close();
        }
    }
    
    @Test
    public void testIsNullCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isNull())
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectSupport);
            assertThat(animals.size()).isEqualTo(0);
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testIsNotNullCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isNotNull())
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectSupport);
            assertThat(animals.size()).isEqualTo(65);
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testComplexCondition() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isIn(1, 5, 7))
                    .or(id, isIn(2, 6, 8), and(animalName, isLike("%bat")))
                    .or(id, isGreaterThan(60))
                    .and(bodyWeight, isBetween(1.0).and(3.0))
                    .orderBy(id.descending(), bodyWeight)
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectSupport);
            assertThat(animals.size()).isEqualTo(4);
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
            
            UpdateSupport updateSupport = update(animalData)
                    .set(bodyWeight).equalTo(record.getBodyWeight())
                    .set(animalName).equalToNull()
                    .where(id, isIn(1, 5, 7))
                    .or(id, isIn(2, 6, 8), and(animalName, isLike("%bat")))
                    .or(id, isGreaterThan(60))
                    .and(bodyWeight, isBetween(1.0).and(3.0))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            int rows = mapper.update(updateSupport);
            assertThat(rows).isEqualTo(4);
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
            
            InsertSupport<AnimalData> insertSupport = insert(record)
                    .into(animalData)
                    .map(id).toProperty("id")
                    .map(animalName).toProperty("animalName")
                    .map(bodyWeight).toProperty("bodyWeight")
                    .map(brainWeight).toProperty("brainWeight")
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            
            int rows = mapper.insert(insertSupport);
            assertThat(rows).isEqualTo(1);
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testInsertNull() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            AnimalData record = new AnimalData();
            record.setId(100);
            record.setAnimalName("Old Shep");
            record.setBodyWeight(22.5);
            record.setBrainWeight(1.2);
            
            InsertSupport<AnimalData> insertSupport = insert(record)
                    .into(animalData)
                    .map(id).toProperty("id")
                    .map(animalName).toNull()
                    .map(bodyWeight).toProperty("bodyWeight")
                    .map(brainWeight).toProperty("brainWeight")
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            
            int rows = mapper.insert(insertSupport);
            assertThat(rows).isEqualTo(1);
        } finally {
            sqlSession.close();
        }
    }
    
    @Test
    public void testOrderByAndDistinct() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = selectDistinct(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isLessThan(10))
                    .or(id,  isGreaterThan(60))
                    .orderBy(id.descending(), animalName)
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            
            List<AnimalData> rows = mapper.selectMany(selectSupport);
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(rows.size()).isEqualTo(14);
                softly.assertThat(rows.get(0).getId()).isEqualTo(65);
            });
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testOrderByWithFullClause() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isLessThan(10))
                    .or(id,  isGreaterThan(60))
                    .orderBy(id.descending())
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            
            List<AnimalData> rows = mapper.selectMany(selectSupport);
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(rows.size()).isEqualTo(14);
                softly.assertThat(rows.get(0).getId()).isEqualTo(65);
            });
        } finally {
            sqlSession.close();
        }
    }
    
    @Test
    public void testCount() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(count().as("total"))
                    .from(animalData, "a")
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(selectSupport.getColumnList()).isEqualTo("count(*) as total");
                softly.assertThat(selectSupport.getFullSelectStatement()).isEqualTo("select count(*) as total from AnimalData a");
            
                Long count = mapper.selectALong(selectSupport);
                softly.assertThat(count).isEqualTo(65);
            });
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testCountField() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(count(brainWeight).as("total"))
                    .from(animalData, "a")
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(selectSupport.getColumnList()).isEqualTo("count(a.brain_weight) as total");
                softly.assertThat(selectSupport.getFullSelectStatement()).isEqualTo("select count(a.brain_weight) as total from AnimalData a");
            
                Long count = mapper.selectALong(selectSupport);
                softly.assertThat(count).isEqualTo(65);
            });
        } finally {
            sqlSession.close();
        }
    }
    
    @Test
    public void testCountNoAlias() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(count())
                    .from(animalData)
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(selectSupport.getColumnList()).isEqualTo("count(*)");
                softly.assertThat(selectSupport.getFullSelectStatement()).isEqualTo("select count(*) from AnimalData");
            
                Long count = mapper.selectALong(selectSupport);
                softly.assertThat(count).isEqualTo(65);
            });
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testMax() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(max(brainWeight).as("total"))
                    .from(animalData, "a")
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(selectSupport.getColumnList()).isEqualTo("max(a.brain_weight) as total");
                softly.assertThat(selectSupport.getFullSelectStatement()).isEqualTo("select max(a.brain_weight) as total from AnimalData a");
            
                Double max = mapper.selectADouble(selectSupport);
                softly.assertThat(max).isEqualTo(87000.0);
            });
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testMaxNoAlias() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(max(brainWeight))
                    .from(animalData)
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(selectSupport.getColumnList()).isEqualTo("max(brain_weight)");
                softly.assertThat(selectSupport.getFullSelectStatement()).isEqualTo("select max(brain_weight) from AnimalData");
            
                Double max = mapper.selectADouble(selectSupport);
                softly.assertThat(max).isEqualTo(87000.0);
            });
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testMaxSubselect() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData, "a")
                    .where(brainWeight, isEqualTo(select(max(brainWeight)).from(animalData, "b")))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(selectSupport.getFullSelectStatement()).isEqualTo("select a.id, a.animal_name, a.body_weight, a.brain_weight from AnimalData a where a.brain_weight = (select max(b.brain_weight) from AnimalData b)");
            
                List<AnimalData> records = mapper.selectMany(selectSupport);
                softly.assertThat(records.size()).isEqualTo(1);
                AnimalData record = records.get(0);
                softly.assertThat(record.getAnimalName()).isEqualTo("Brachiosaurus");
            });
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testMin() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(min(brainWeight).as("total"))
                    .from(animalData, "a")
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(selectSupport.getColumnList()).isEqualTo("min(a.brain_weight) as total");
                softly.assertThat(selectSupport.getFullSelectStatement()).isEqualTo("select min(a.brain_weight) as total from AnimalData a");
            
                Double min = mapper.selectADouble(selectSupport);
                softly.assertThat(min).isEqualTo(0.005);
            });
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testMinNoAlias() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(min(brainWeight))
                    .from(animalData)
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(selectSupport.getColumnList()).isEqualTo("min(brain_weight)");
                softly.assertThat(selectSupport.getFullSelectStatement()).isEqualTo("select min(brain_weight) from AnimalData");
            
                Double min = mapper.selectADouble(selectSupport);
                softly.assertThat(min).isEqualTo(0.005);
            });
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testMinSubselect() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData, "a")
                    .where(brainWeight, isNotEqualTo(select(min(brainWeight)).from(animalData, "b")))
                    .orderBy(animalName)
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(selectSupport.getFullSelectStatement()).isEqualTo("select a.id, a.animal_name, a.body_weight, a.brain_weight from AnimalData a where a.brain_weight <> (select min(b.brain_weight) from AnimalData b) order by a.animal_name");
            
                List<AnimalData> records = mapper.selectMany(selectSupport);
                softly.assertThat(records.size()).isEqualTo(64);
                AnimalData record = records.get(0);
                softly.assertThat(record.getAnimalName()).isEqualTo("African elephant");
            });
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testMinSubselectNoAlias() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(brainWeight, isNotEqualTo(select(min(brainWeight)).from(animalData)))
                    .orderBy(animalName)
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(selectSupport.getFullSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where brain_weight <> (select min(brain_weight) from AnimalData) order by animal_name");
            
                List<AnimalData> records = mapper.selectMany(selectSupport);
                softly.assertThat(records.size()).isEqualTo(64);
                AnimalData record = records.get(0);
                softly.assertThat(record.getAnimalName()).isEqualTo("African elephant");
            });
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testAvg() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(avg(brainWeight).as("average"))
                    .from(animalData, "a")
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(selectSupport.getColumnList()).isEqualTo("avg(a.brain_weight) as average");
                softly.assertThat(selectSupport.getFullSelectStatement()).isEqualTo("select avg(a.brain_weight) as average from AnimalData a");
            
                Double average = mapper.selectADouble(selectSupport);
                softly.assertThat(average).isEqualTo(1852.69, within(.01));
            });
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testSum() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(sum(brainWeight).as("total"))
                    .from(animalData)
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(selectSupport.getColumnList()).isEqualTo("sum(brain_weight) as total");
                softly.assertThat(selectSupport.getFullSelectStatement()).isEqualTo("select sum(brain_weight) as total from AnimalData");
            
                Double total = mapper.selectADouble(selectSupport);
                softly.assertThat(total).isEqualTo(120424.97, within(.01));
            });
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testLessThanSubselect() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData, "a")
                    .where(brainWeight, isLessThan(select(max(brainWeight)).from(animalData, "b")))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(selectSupport.getFullSelectStatement()).isEqualTo("select a.id, a.animal_name, a.body_weight, a.brain_weight from AnimalData a where a.brain_weight < (select max(b.brain_weight) from AnimalData b)");
            
                List<AnimalData> records = mapper.selectMany(selectSupport);
                softly.assertThat(records.size()).isEqualTo(64);
            });
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testLessThanOrEqualToSubselect() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData, "a")
                    .where(brainWeight, isLessThanOrEqualTo(select(max(brainWeight)).from(animalData, "b")))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(selectSupport.getFullSelectStatement()).isEqualTo("select a.id, a.animal_name, a.body_weight, a.brain_weight from AnimalData a where a.brain_weight <= (select max(b.brain_weight) from AnimalData b)");
            
                List<AnimalData> records = mapper.selectMany(selectSupport);
                softly.assertThat(records.size()).isEqualTo(65);
            });
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testGreaterThanSubselect() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData, "a")
                    .where(brainWeight, isGreaterThan(select(min(brainWeight)).from(animalData, "b")))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(selectSupport.getFullSelectStatement()).isEqualTo("select a.id, a.animal_name, a.body_weight, a.brain_weight from AnimalData a where a.brain_weight > (select min(b.brain_weight) from AnimalData b)");
            
                List<AnimalData> records = mapper.selectMany(selectSupport);
                softly.assertThat(records.size()).isEqualTo(64);
            });
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testGreaterThanOrEqualToSubselect() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectSupport selectSupport = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData, "a")
                    .where(brainWeight, isGreaterThanOrEqualTo(select(min(brainWeight)).from(animalData, "b")))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(selectSupport.getFullSelectStatement()).isEqualTo("select a.id, a.animal_name, a.body_weight, a.brain_weight from AnimalData a where a.brain_weight >= (select min(b.brain_weight) from AnimalData b)");
            
                List<AnimalData> records = mapper.selectMany(selectSupport);
                softly.assertThat(records.size()).isEqualTo(65);
            });
        } finally {
            sqlSession.close();
        }
    }
}
