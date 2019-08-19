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
package examples.animal.data;

import static examples.animal.data.AnimalDataDynamicSqlSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.insert.render.BatchInsert;
import org.mybatis.dynamic.sql.insert.render.InsertSelectStatementProvider;
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.render.TableAliasCalculator;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;
import org.mybatis.dynamic.sql.util.mybatis3.MyBatis3Utils;
import org.mybatis.dynamic.sql.where.render.WhereClauseProvider;

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
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(animals.size()).isEqualTo(65),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    public void testSelectAllRowsWithOrder() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .orderBy(id.descending())
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(animals.size()).isEqualTo(65),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(65)
            );
        }
    }
    
    @Test
    public void testSelectAllRowsAllColumns() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(animalData.allColumns())
                    .from(animalData)
                    .orderBy(id.descending())
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<Map<String, Object>> animals = mapper.generalSelect(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select * from AnimalData order by id DESC"),
                    () -> assertThat(animals.size()).isEqualTo(65),
                    () -> assertThat(animals.get(0).get("ID")).isEqualTo(65),
                    () -> assertThat(animals.get(0).get("ANIMAL_NAME")).isEqualTo("Brachiosaurus")
            );
        }
    }
    
    @Test
    public void testSelectAllRowsAllColumnsWithOrder() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(animalData.allColumns())
                    .from(animalData)
                    .orderBy(id.descending())
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select * from AnimalData order by id DESC"),
                    () -> assertThat(animals.size()).isEqualTo(65),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(65)
            );
        }
    }
    
    @Test
    public void testSelectAllRowsAllColumnsWithOrderAndAlias() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(animalData.allColumns())
                    .from(animalData, "ad")
                    .orderBy(id.descending())
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select ad.* from AnimalData ad order by id DESC"),
                    () -> assertThat(animals.size()).isEqualTo(65),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(65)
            );
        }
    }
    
    @Test
    public void testSelectRowsLessThan20() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isLessThan(20))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertThat(animals.size()).isEqualTo(19);
        }
    }

    @Test
    public void testSelectRowsBetween30And40() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isBetween(30).and(40))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertThat(animals.size()).isEqualTo(11);
        }
    }

    @Test
    public void testSelectRowsNotBetween() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isNotBetween(10).and(60))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertThat(animals.size()).isEqualTo(14);
        }
    }

    @Test
    public void testSelectRowsNotBetweenWithStandaloneWhereClause() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            WhereClauseProvider whereClause = where(id, isNotBetween(10).and(60))
                    .or(id, isIn(25, 27))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            List<AnimalData> animals = mapper.selectByExample(whereClause);
            assertThat(animals.size()).isEqualTo(16);
        }
    }
    
    @Test
    public void testComplexConditionWithStandaloneWhereAndTableAlias() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            WhereClauseProvider whereClause = where(id, isEqualTo(1), or(bodyWeight, isGreaterThan(1.0)))
                    .build()
                    .render(RenderingStrategies.MYBATIS3, TableAliasCalculator.of(animalData, "a"));
            
            assertThat(whereClause.getWhereClause()).isEqualTo("where (a.id = #{parameters.p1,jdbcType=INTEGER} or a.body_weight > #{parameters.p2,jdbcType=DOUBLE})");

            List<AnimalData> animals = mapper.selectByExampleWithAlias(whereClause);
            assertThat(animals.size()).isEqualTo(59);
        }
    }
    
    @Test
    public void testSelectRowsNotBetweenWithStandaloneWhereClauseLimitAndOffset() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            WhereClauseProvider whereClause = where(id, isLessThan(60))
                    .build()
                    .render(RenderingStrategies.MYBATIS3, "whereClauseProvider");
            
            List<AnimalData> animals = mapper.selectByExampleWithLimitAndOffset(whereClause, 5, 15);
            assertAll(
                    () -> assertThat(animals.size()).isEqualTo(5),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(16)
            );
        }
    }
    
    @Test
    public void testSelectRowsNotBetweenWithStandaloneWhereClauseAliasLimitAndOffset() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            WhereClauseProvider whereClause = where(id, isLessThan(60))
                    .build()
                    .render(RenderingStrategies.MYBATIS3, TableAliasCalculator.of(animalData, "b"),  "whereClauseProvider");
            
            List<AnimalData> animals = mapper.selectByExampleWithAliasLimitAndOffset(whereClause, 3, 24);
            assertAll(
                    () -> assertThat(animals.size()).isEqualTo(3),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(25)
            );
        }
    }
    
    @Test
    public void testUnionSelectWithWhere() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isLessThan(20))
                    .union()
                    .select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isGreaterThan(40))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expected = "select id, animal_name, body_weight, brain_weight "
                    + "from AnimalData "
                    + "where id < #{parameters.p1,jdbcType=INTEGER} "
                    + "union "
                    + "select id, animal_name, body_weight, brain_weight "
                    + "from AnimalData "
                    + "where id > #{parameters.p2,jdbcType=INTEGER}";

            
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
                    () -> assertThat(animals.size()).isEqualTo(44),
                    () -> assertThat(selectStatement.getParameters().size()).isEqualTo(2),
                    () -> assertThat(selectStatement.getParameters().get("p1")).isEqualTo(20),
                    () -> assertThat(selectStatement.getParameters().get("p2")).isEqualTo(40)
            );
        }
    }

    @Test
    public void testUnionSelectWithoutWhere() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .union()
                    .selectDistinct(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expected = "select id, animal_name, body_weight, brain_weight "
                    + "from AnimalData "
                    + "union "
                    + "select distinct id, animal_name, body_weight, brain_weight "
                    + "from AnimalData "
                    + "order by id";

            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
                    () -> assertThat(animals.size()).isEqualTo(65),
                    () -> assertThat(selectStatement.getParameters().size()).isEqualTo(0)
            );
        }
    }
    
    @Test
    public void testUnionAllSelectWithoutWhere() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .unionAll()
                    .selectDistinct(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expected = "select id, animal_name, body_weight, brain_weight "
                    + "from AnimalData "
                    + "union all "
                    + "select distinct id, animal_name, body_weight, brain_weight "
                    + "from AnimalData "
                    + "order by id";

            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
                    () -> assertThat(animals.size()).isEqualTo(130),
                    () -> assertThat(selectStatement.getParameters().size()).isEqualTo(0)
            );
        }
    }
    
    @Test
    public void testUnionSelectWithTableAliases() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData, "a")
                    .where(id, isLessThan(20))
                    .union()
                    .select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData, "b")
                    .where(id, isGreaterThan(40))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expected = "select a.id, a.animal_name, a.body_weight, a.brain_weight "
                    + "from AnimalData a "
                    + "where a.id < #{parameters.p1,jdbcType=INTEGER} "
                    + "union "
                    + "select b.id, b.animal_name, b.body_weight, b.brain_weight "
                    + "from AnimalData b "
                    + "where b.id > #{parameters.p2,jdbcType=INTEGER} "
                    + "order by id";

            List<AnimalData> animals = mapper.selectMany(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
                    () -> assertThat(animals.size()).isEqualTo(44),
                    () -> assertThat(selectStatement.getParameters().size()).isEqualTo(2),
                    () -> assertThat(selectStatement.getParameters().get("p1")).isEqualTo(20),
                    () -> assertThat(selectStatement.getParameters().get("p2")).isEqualTo(40)
            );
        }
    }

    @Test
    public void testUnionAllSelectWithTableAliases() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData, "a")
                    .where(id, isLessThan(20))
                    .unionAll()
                    .select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData, "b")
                    .where(id, isGreaterThan(40))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expected = "select a.id, a.animal_name, a.body_weight, a.brain_weight "
                    + "from AnimalData a "
                    + "where a.id < #{parameters.p1,jdbcType=INTEGER} "
                    + "union all "
                    + "select b.id, b.animal_name, b.body_weight, b.brain_weight "
                    + "from AnimalData b "
                    + "where b.id > #{parameters.p2,jdbcType=INTEGER} "
                    + "order by id";

            List<AnimalData> animals = mapper.selectMany(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
                    () -> assertThat(animals.size()).isEqualTo(44),
                    () -> assertThat(selectStatement.getParameters().size()).isEqualTo(2),
                    () -> assertThat(selectStatement.getParameters().get("p1")).isEqualTo(20),
                    () -> assertThat(selectStatement.getParameters().get("p2")).isEqualTo(40)
            );
        }
    }

    @Test
    public void testUnionSelectWithTableAndColumnAliases() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(id.as("animalId"), animalName, bodyWeight, brainWeight)
                    .from(animalData, "a")
                    .where(id, isLessThan(20))
                    .union()
                    .select(id.as("animalId"), animalName, bodyWeight, brainWeight)
                    .from(animalData, "b")
                    .where(id, isGreaterThan(40))
                    .orderBy(sortColumn("animalId"))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expected = "select a.id as animalId, a.animal_name, a.body_weight, a.brain_weight "
                    + "from AnimalData a "
                    + "where a.id < #{parameters.p1,jdbcType=INTEGER} "
                    + "union "
                    + "select b.id as animalId, b.animal_name, b.body_weight, b.brain_weight "
                    + "from AnimalData b "
                    + "where b.id > #{parameters.p2,jdbcType=INTEGER} "
                    + "order by animalId";

            List<AnimalData> animals = mapper.selectMany(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
                    () -> assertThat(animals.size()).isEqualTo(44),
                    () -> assertThat(selectStatement.getParameters().size()).isEqualTo(2),
                    () -> assertThat(selectStatement.getParameters().get("p1")).isEqualTo(20),
                    () -> assertThat(selectStatement.getParameters().get("p2")).isEqualTo(40)
            );
        }
    }

    @Test
    public void testIsEqualCondition() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isEqualTo(5))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertThat(animals.size()).isEqualTo(1);
        }
    }

    @Test
    public void testIsNotEqualCondition() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isNotEqualTo(5))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertThat(animals.size()).isEqualTo(64);
        }
    }
    
    @Test
    public void testIsGreaterThanOrEqualToCondition() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isGreaterThanOrEqualTo(60))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertThat(animals.size()).isEqualTo(6);
        }
    }
    
    @Test
    public void testIsLessThanOrEqualToCondition() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isLessThanOrEqualTo(10))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertThat(animals.size()).isEqualTo(10);
        }
    }
    
    @Test
    public void testInCondition() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isIn(5, 8, 10))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertThat(animals.size()).isEqualTo(3);
        }
    }

    @Test
    public void testInCaseSensitiveCondition() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isInCaseInsensitive("yellow-bellied marmot", "verbet", null))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertThat(animals.size()).isEqualTo(2);
        }
    }
    
    @Test
    public void testNotInCondition() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isNotIn(5, 8, 10))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertThat(animals.size()).isEqualTo(62);
        }
    }

    @Test
    public void testNotInCaseSensitiveCondition() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isNotInCaseInsensitive("yellow-bellied marmot", "verbet"))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertThat(animals.size()).isEqualTo(63);
        }
    }

    @Test
    public void testNotInCaseSensitiveConditionWithNull() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isNotInCaseInsensitive((String)null))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertThat(animals.size()).isEqualTo(0);
        }
    }

    @Test
    public void testLikeCondition() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isLike("%squirrel"))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertThat(animals.size()).isEqualTo(2);
        }
    }

    @Test
    public void testLikeCaseInsensitive() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isLikeCaseInsensitive("%squirrel"))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectStatement);

            assertAll(
                    () -> assertThat(animals.size()).isEqualTo(2),
                    () -> assertThat(animals.get(0).getAnimalName()).isEqualTo("Ground squirrel"),
                    () -> assertThat(animals.get(1).getAnimalName()).isEqualTo("Artic ground squirrel")
            );
        }
    }
    
    @Test
    public void testLikeLowerCase() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(id, lower(animalName).as("AnimalName"), bodyWeight, brainWeight)
                    .from(animalData)
                    .where(lower(animalName), isLike("%squirrel"))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<Map<String, Object>> animals = mapper.generalSelect(selectStatement);

            assertAll(
                    () -> assertThat(animals.size()).isEqualTo(2),
                    () -> assertThat(animals.get(0).get("ANIMALNAME")).isEqualTo("ground squirrel"),
                    () -> assertThat(animals.get(1).get("ANIMALNAME")).isEqualTo("artic ground squirrel")
            );
        }
    }

    @Test
    public void testLikeUpperCase() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(id, upper(animalName).as("animalname"), bodyWeight, brainWeight)
                    .from(animalData)
                    .where(upper(animalName), isLike("%SQUIRREL"))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<Map<String, Object>> animals = mapper.generalSelect(selectStatement);

            assertAll(
                    () -> assertThat(animals.size()).isEqualTo(2),
                    () -> assertThat(animals.get(0).get("ANIMALNAME")).isEqualTo("GROUND SQUIRREL"),
                    () -> assertThat(animals.get(1).get("ANIMALNAME")).isEqualTo("ARTIC GROUND SQUIRREL")
            );
        }
    }

    @Test
    public void testNumericConstant() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(id, animalName, constant("3").as("some_number"))
                    .from(animalData, "a")
                    .where(add(bodyWeight, brainWeight), isGreaterThan(10000.0))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select a.id, a.animal_name, 3 as some_number "
                    + "from AnimalData a "
                    + "where (a.body_weight + a.brain_weight) > #{parameters.p1,jdbcType=DOUBLE}";
            
            List<Map<String, Object>> animals = mapper.generalSelect(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
                    () -> assertThat(animals.size()).isEqualTo(3),
                    () -> assertThat(animals.get(0).get("ANIMAL_NAME")).isEqualTo("African elephant"),
                    () -> assertThat(animals.get(0).get("SOME_NUMBER")).isEqualTo(3),
                    () -> assertThat(animals.get(1).get("ANIMAL_NAME")).isEqualTo("Dipliodocus"),
                    () -> assertThat(animals.get(1).get("SOME_NUMBER")).isEqualTo(3),
                    () -> assertThat(animals.get(2).get("ANIMAL_NAME")).isEqualTo("Brachiosaurus"),
                    () -> assertThat(animals.get(2).get("SOME_NUMBER")).isEqualTo(3)
            );
        }
    }

    @Test
    public void testStringConstant() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(id, animalName, stringConstant("fred").as("some_string"))
                    .from(animalData, "a")
                    .where(add(bodyWeight, brainWeight), isGreaterThan(10000.0))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select a.id, a.animal_name, 'fred' as some_string "
                    + "from AnimalData a "
                    + "where (a.body_weight + a.brain_weight) > #{parameters.p1,jdbcType=DOUBLE}";
            
            List<Map<String, Object>> animals = mapper.generalSelect(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
                    () -> assertThat(animals.size()).isEqualTo(3),
                    () -> assertThat(animals.get(0).get("ANIMAL_NAME")).isEqualTo("African elephant"),
                    () -> assertThat(animals.get(0).get("SOME_STRING")).isEqualTo("fred"),
                    () -> assertThat(animals.get(1).get("ANIMAL_NAME")).isEqualTo("Dipliodocus"),
                    () -> assertThat(animals.get(1).get("SOME_STRING")).isEqualTo("fred"),
                    () -> assertThat(animals.get(2).get("ANIMAL_NAME")).isEqualTo("Brachiosaurus"),
                    () -> assertThat(animals.get(2).get("SOME_STRING")).isEqualTo("fred")
            );
        }
    }

    @Test
    public void testAdd() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(id, animalName, add(bodyWeight, brainWeight).as("calculated_weight"))
                    .from(animalData, "a")
                    .where(add(bodyWeight, brainWeight), isGreaterThan(10000.0))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select a.id, a.animal_name, (a.body_weight + a.brain_weight) as calculated_weight "
                    + "from AnimalData a "
                    + "where (a.body_weight + a.brain_weight) > #{parameters.p1,jdbcType=DOUBLE}";
            
            List<Map<String, Object>> animals = mapper.generalSelect(selectStatement);
            
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
                    () -> assertThat(animals.size()).isEqualTo(3),
                    () -> assertThat(animals.get(0).get("ANIMAL_NAME")).isEqualTo("African elephant"),
                    () -> assertThat(animals.get(0).get("CALCULATED_WEIGHT")).isEqualTo(12366.0),
                    () -> assertThat(animals.get(1).get("ANIMAL_NAME")).isEqualTo("Dipliodocus"),
                    () -> assertThat(animals.get(1).get("CALCULATED_WEIGHT")).isEqualTo(11750.0),
                    () -> assertThat(animals.get(2).get("ANIMAL_NAME")).isEqualTo("Brachiosaurus"),
                    () -> assertThat(animals.get(2).get("CALCULATED_WEIGHT")).isEqualTo(87154.5)
            );
        }
    }
    
    @Test
    public void testAddConstant() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(id, animalName, add(bodyWeight, constant("22"), constant("33")).as("calculated_weight"))
                    .from(animalData, "a")
                    .where(add(bodyWeight, brainWeight), isGreaterThan(10000.0))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select a.id, a.animal_name, (a.body_weight + 22 + 33) as calculated_weight "
                    + "from AnimalData a "
                    + "where (a.body_weight + a.brain_weight) > #{parameters.p1,jdbcType=DOUBLE}";
            
            List<Map<String, Object>> animals = mapper.generalSelect(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
                    () -> assertThat(animals.size()).isEqualTo(3),
                    () -> assertThat(animals.get(0).get("ANIMAL_NAME")).isEqualTo("African elephant"),
                    () -> assertThat(animals.get(0).get("CALCULATED_WEIGHT")).isEqualTo(5767.0),
                    () -> assertThat(animals.get(1).get("ANIMAL_NAME")).isEqualTo("Dipliodocus"),
                    () -> assertThat(animals.get(1).get("CALCULATED_WEIGHT")).isEqualTo(105.0),
                    () -> assertThat(animals.get(2).get("ANIMAL_NAME")).isEqualTo("Brachiosaurus"),
                    () -> assertThat(animals.get(2).get("CALCULATED_WEIGHT")).isEqualTo(209.5)
            );
        }
    }
    
    @Test
    public void testDivide() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(id, animalName, divide(bodyWeight, brainWeight).as("calculated_weight"))
                    .from(animalData, "a")
                    .where(add(bodyWeight, brainWeight), isGreaterThan(10000.0))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select a.id, a.animal_name, (a.body_weight / a.brain_weight) as calculated_weight "
                    + "from AnimalData a "
                    + "where (a.body_weight + a.brain_weight) > #{parameters.p1,jdbcType=DOUBLE}";
            
            List<Map<String, Object>> animals = mapper.generalSelect(selectStatement);
            
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
                    () -> assertThat(animals.size()).isEqualTo(3),
                    () -> assertThat(animals.get(0).get("ANIMAL_NAME")).isEqualTo("African elephant"),
                    () -> assertThat((Double) animals.get(0).get("CALCULATED_WEIGHT")).isEqualTo(0.858, within(0.001)),
                    () -> assertThat(animals.get(1).get("ANIMAL_NAME")).isEqualTo("Dipliodocus"),
                    () -> assertThat((Double) animals.get(1).get("CALCULATED_WEIGHT")).isEqualTo(0.004, within(0.001)),
                    () -> assertThat(animals.get(2).get("ANIMAL_NAME")).isEqualTo("Brachiosaurus"),
                    () -> assertThat((Double) animals.get(2).get("CALCULATED_WEIGHT")).isEqualTo(0.001, within(0.001))
            );
        }
    }
    
    @Test
    public void testDivideConstant() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(id, animalName, divide(bodyWeight, constant("10.0")).as("calculated_weight"))
                    .from(animalData, "a")
                    .where(add(bodyWeight, brainWeight), isGreaterThan(10000.0))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select a.id, a.animal_name, (a.body_weight / 10.0) as calculated_weight "
                    + "from AnimalData a "
                    + "where (a.body_weight + a.brain_weight) > #{parameters.p1,jdbcType=DOUBLE}";
            
            List<Map<String, Object>> animals = mapper.generalSelect(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
                    () -> assertThat(animals.size()).isEqualTo(3),
                    () -> assertThat(animals.get(0).get("ANIMAL_NAME")).isEqualTo("African elephant"),
                    () -> assertThat(animals.get(0).get("CALCULATED_WEIGHT")).isEqualTo(571.2),
                    () -> assertThat(animals.get(1).get("ANIMAL_NAME")).isEqualTo("Dipliodocus"),
                    () -> assertThat(animals.get(1).get("CALCULATED_WEIGHT")).isEqualTo(5.0),
                    () -> assertThat(animals.get(2).get("ANIMAL_NAME")).isEqualTo("Brachiosaurus"),
                    () -> assertThat(animals.get(2).get("CALCULATED_WEIGHT")).isEqualTo(15.45)
            );
        }
    }
    
    @Test
    public void testMultiply() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(id, animalName, multiply(bodyWeight, brainWeight).as("calculated_weight"))
                    .from(animalData, "a")
                    .where(add(bodyWeight, brainWeight), isGreaterThan(10000.0))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select a.id, a.animal_name, (a.body_weight * a.brain_weight) as calculated_weight "
                    + "from AnimalData a "
                    + "where (a.body_weight + a.brain_weight) > #{parameters.p1,jdbcType=DOUBLE}";
            
            List<Map<String, Object>> animals = mapper.generalSelect(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
                    () -> assertThat(animals.size()).isEqualTo(3),
                    () -> assertThat(animals.get(0).get("ANIMAL_NAME")).isEqualTo("African elephant"),
                    () -> assertThat(animals.get(0).get("CALCULATED_WEIGHT")).isEqualTo(38007648.0),
                    () -> assertThat(animals.get(1).get("ANIMAL_NAME")).isEqualTo("Dipliodocus"),
                    () -> assertThat(animals.get(1).get("CALCULATED_WEIGHT")).isEqualTo(585000.0),
                    () -> assertThat(animals.get(2).get("ANIMAL_NAME")).isEqualTo("Brachiosaurus"),
                    () -> assertThat(animals.get(2).get("CALCULATED_WEIGHT")).isEqualTo(13441500.0)
            );
        }
    }
    
    @Test
    public void testMultiplyConstant() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(id, animalName, multiply(bodyWeight, constant("2.0")).as("calculated_weight"))
                    .from(animalData, "a")
                    .where(add(bodyWeight, brainWeight), isGreaterThan(10000.0))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select a.id, a.animal_name, (a.body_weight * 2.0) as calculated_weight "
                    + "from AnimalData a "
                    + "where (a.body_weight + a.brain_weight) > #{parameters.p1,jdbcType=DOUBLE}";
            
            List<Map<String, Object>> animals = mapper.generalSelect(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
                    () -> assertThat(animals.size()).isEqualTo(3),
                    () -> assertThat(animals.get(0).get("ANIMAL_NAME")).isEqualTo("African elephant"),
                    () -> assertThat(animals.get(0).get("CALCULATED_WEIGHT")).isEqualTo(11424.0),
                    () -> assertThat(animals.get(1).get("ANIMAL_NAME")).isEqualTo("Dipliodocus"),
                    () -> assertThat(animals.get(1).get("CALCULATED_WEIGHT")).isEqualTo(100.0),
                    () -> assertThat(animals.get(2).get("ANIMAL_NAME")).isEqualTo("Brachiosaurus"),
                    () -> assertThat(animals.get(2).get("CALCULATED_WEIGHT")).isEqualTo(309.0)
            );
        }
    }
    
    @Test
    public void testSubtract() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(id, animalName, subtract(bodyWeight, brainWeight).as("calculated_weight"))
                    .from(animalData, "a")
                    .where(add(bodyWeight, brainWeight), isGreaterThan(10000.0))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select a.id, a.animal_name, (a.body_weight - a.brain_weight) as calculated_weight "
                    + "from AnimalData a "
                    + "where (a.body_weight + a.brain_weight) > #{parameters.p1,jdbcType=DOUBLE}";
            
            List<Map<String, Object>> animals = mapper.generalSelect(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
                    () -> assertThat(animals.size()).isEqualTo(3),
                    () -> assertThat(animals.get(0).get("ANIMAL_NAME")).isEqualTo("African elephant"),
                    () -> assertThat(animals.get(0).get("CALCULATED_WEIGHT")).isEqualTo(-942.0),
                    () -> assertThat(animals.get(1).get("ANIMAL_NAME")).isEqualTo("Dipliodocus"),
                    () -> assertThat(animals.get(1).get("CALCULATED_WEIGHT")).isEqualTo(-11650.0),
                    () -> assertThat(animals.get(2).get("ANIMAL_NAME")).isEqualTo("Brachiosaurus"),
                    () -> assertThat(animals.get(2).get("CALCULATED_WEIGHT")).isEqualTo(-86845.5)
            );
        }
    }
    
    @Test
    public void testSubtractConstant() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(id, animalName, subtract(bodyWeight, constant("5.5")).as("calculated_weight"))
                    .from(animalData, "a")
                    .where(add(bodyWeight, brainWeight), isGreaterThan(10000.0))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select a.id, a.animal_name, (a.body_weight - 5.5) as calculated_weight "
                    + "from AnimalData a "
                    + "where (a.body_weight + a.brain_weight) > #{parameters.p1,jdbcType=DOUBLE}";
            
            List<Map<String, Object>> animals = mapper.generalSelect(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
                    () -> assertThat(animals.size()).isEqualTo(3),
                    () -> assertThat(animals.get(0).get("ANIMAL_NAME")).isEqualTo("African elephant"),
                    () -> assertThat(animals.get(0).get("CALCULATED_WEIGHT")).isEqualTo(5706.5),
                    () -> assertThat(animals.get(1).get("ANIMAL_NAME")).isEqualTo("Dipliodocus"),
                    () -> assertThat(animals.get(1).get("CALCULATED_WEIGHT")).isEqualTo(44.5),
                    () -> assertThat(animals.get(2).get("ANIMAL_NAME")).isEqualTo("Brachiosaurus"),
                    () -> assertThat(animals.get(2).get("CALCULATED_WEIGHT")).isEqualTo(149.0)
            );
        }
    }
    
    @Test
    public void testComplexExpression() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(id, animalName, add(multiply(bodyWeight, constant("5.5")), subtract(brainWeight, constant("2"))).as("calculated_weight"))
                    .from(animalData, "a")
                    .where(add(bodyWeight, brainWeight), isGreaterThan(10000.0))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select a.id, a.animal_name, ((a.body_weight * 5.5) + (a.brain_weight - 2)) as calculated_weight "
                    + "from AnimalData a "
                    + "where (a.body_weight + a.brain_weight) > #{parameters.p1,jdbcType=DOUBLE}";
            
            List<Map<String, Object>> animals = mapper.generalSelect(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
                    () -> assertThat(animals.size()).isEqualTo(3),
                    () -> assertThat(animals.get(0).get("ANIMAL_NAME")).isEqualTo("African elephant"),
                    () -> assertThat(animals.get(0).get("CALCULATED_WEIGHT")).isEqualTo(38068.0),
                    () -> assertThat(animals.get(1).get("ANIMAL_NAME")).isEqualTo("Dipliodocus"),
                    () -> assertThat(animals.get(1).get("CALCULATED_WEIGHT")).isEqualTo(11973.0),
                    () -> assertThat(animals.get(2).get("ANIMAL_NAME")).isEqualTo("Brachiosaurus"),
                    () -> assertThat(animals.get(2).get("CALCULATED_WEIGHT")).isEqualTo(87847.75)
            );
        }
    }

    @Test
    public void testNotLikeCondition() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isNotLike("%squirrel"))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertThat(animals.size()).isEqualTo(63);
        }
    }

    @Test
    public void testNotLikeCaseInsensistveCondition() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isNotLikeCaseInsensitive("%squirrel"))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertThat(animals.size()).isEqualTo(63);
        }
    }

    @Test
    public void testDeleteThreeRows() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            DeleteStatementProvider deleteStatement = deleteFrom(animalData)
                    .where(id, isIn(5, 8, 10))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            int rowCount = mapper.delete(deleteStatement);
            assertThat(rowCount).isEqualTo(3);
        }
    }
    
    @Test
    public void testComplexDelete() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            DeleteStatementProvider deleteStatement = deleteFrom(animalData)
                    .where(id, isLessThan(10))
                    .or(id, isGreaterThan(60))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            int rowCount = mapper.delete(deleteStatement);
            assertThat(rowCount).isEqualTo(14);
        }
    }
    
    @Test
    public void testIsNullCondition() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isNull())
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertThat(animals.size()).isEqualTo(0);
        }
    }

    @Test
    public void testIsNotNullCondition() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isNotNull())
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertThat(animals.size()).isEqualTo(65);
        }
    }

    @Test
    public void testComplexCondition() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isIn(1, 5, 7))
                    .or(id, isIn(2, 6, 8), and(animalName, isLike("%bat")))
                    .or(id, isGreaterThan(60))
                    .and(bodyWeight, isBetween(1.0).and(3.0))
                    .orderBy(id.descending(), bodyWeight)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertThat(animals.size()).isEqualTo(4);
        }
    }

    @Test
    public void testUpdateByExample() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            AnimalData record = new AnimalData();
            record.setBodyWeight(2.6);
            
            UpdateStatementProvider updateStatement = update(animalData)
                    .set(bodyWeight).equalTo(record.getBodyWeight())
                    .set(animalName).equalToNull()
                    .where(id, isIn(1, 5, 7))
                    .or(id, isIn(2, 6, 8), and(animalName, isLike("%bat")))
                    .or(id, isGreaterThan(60))
                    .and(bodyWeight, isBetween(1.0).and(3.0))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            int rows = mapper.update(updateStatement);
            assertThat(rows).isEqualTo(4);
        }
    }

    @Test
    public void testInsert() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            AnimalData record = new AnimalData();
            record.setId(100);
            record.setAnimalName("Old Shep");
            record.setBodyWeight(22.5);
            record.setBrainWeight(1.2);
            
            InsertStatementProvider<AnimalData> insertStatement = insert(record)
                    .into(animalData)
                    .map(id).toProperty("id")
                    .map(animalName).toProperty("animalName")
                    .map(bodyWeight).toProperty("bodyWeight")
                    .map(brainWeight).toProperty("brainWeight")
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            int rows = mapper.insert(insertStatement);
            assertThat(rows).isEqualTo(1);
        }
    }

    @Test
    public void testInsertNull() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            AnimalData record = new AnimalData();
            record.setId(100);
            record.setAnimalName("Old Shep");
            record.setBodyWeight(22.5);
            record.setBrainWeight(1.2);
            
            InsertStatementProvider<AnimalData> insertStatement = insert(record)
                    .into(animalData)
                    .map(id).toProperty("id")
                    .map(animalName).toNull()
                    .map(bodyWeight).toProperty("bodyWeight")
                    .map(brainWeight).toProperty("brainWeight")
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            int rows = mapper.insert(insertStatement);
            assertThat(rows).isEqualTo(1);
        }
    }
    
    @Test
    public void testBulkInsert() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            List<AnimalData> records = new ArrayList<>();
            AnimalData record = new AnimalData();
            record.setId(100);
            record.setAnimalName("Old Shep");
            record.setBodyWeight(22.5);
            records.add(record);
            
            record = new AnimalData();
            record.setId(101);
            record.setAnimalName("Old Dan");
            record.setBodyWeight(22.5);
            records.add(record);
            
            BatchInsert<AnimalData> batchInsert = insert(records)
                    .into(animalData)
                    .map(id).toProperty("id")
                    .map(animalName).toNull()
                    .map(bodyWeight).toProperty("bodyWeight")
                    .map(brainWeight).toConstant("1.2")
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            batchInsert.insertStatements().stream().forEach(mapper::insert);
            sqlSession.commit();
            
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isIn(100, 101))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectStatement);

            assertAll(
                    () -> assertThat(animals.size()).isEqualTo(2),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(100),
                    () -> assertThat(animals.get(0).getBrainWeight()).isEqualTo(1.2),
                    () -> assertThat(animals.get(0).getAnimalName()).isNull(),
                    () -> assertThat(animals.get(1).getId()).isEqualTo(101),
                    () -> assertThat(animals.get(1).getBrainWeight()).isEqualTo(1.2),
                    () -> assertThat(animals.get(1).getAnimalName()).isNull()
            );
        }
    }
    
    @Test
    public void testBulkInsert2() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            List<AnimalData> records = new ArrayList<>();
            AnimalData record = new AnimalData();
            record.setId(100);
            record.setAnimalName("Old Shep");
            record.setBodyWeight(22.5);
            records.add(record);
            
            record = new AnimalData();
            record.setId(101);
            record.setAnimalName("Old Dan");
            record.setBodyWeight(22.5);
            records.add(record);
            
            BatchInsert<AnimalData> batchInsert = insert(records)
                    .into(animalData)
                    .map(id).toProperty("id")
                    .map(animalName).toStringConstant("Old Fred")
                    .map(bodyWeight).toProperty("bodyWeight")
                    .map(brainWeight).toConstant("1.2")
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            batchInsert.insertStatements().stream().forEach(mapper::insert);
            sqlSession.commit();
            
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isIn(100, 101))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<AnimalData> animals = mapper.selectMany(selectStatement);
            
            assertAll(
                    () -> assertThat(animals.size()).isEqualTo(2),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(100),
                    () -> assertThat(animals.get(0).getBrainWeight()).isEqualTo(1.2),
                    () -> assertThat(animals.get(0).getAnimalName()).isEqualTo("Old Fred"),
                    () -> assertThat(animals.get(1).getId()).isEqualTo(101),
                    () -> assertThat(animals.get(1).getBrainWeight()).isEqualTo(1.2),
                    () -> assertThat(animals.get(1).getAnimalName()).isEqualTo("Old Fred")
            );
        }
    }

    @Test
    public void testOrderByAndDistinct() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = selectDistinct(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isLessThan(10))
                    .or(id,  isGreaterThan(60))
                    .orderBy(id.descending(), animalName)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            List<AnimalData> rows = mapper.selectMany(selectStatement);
            
            assertAll(
                    () -> assertThat(rows.size()).isEqualTo(14),
                    () -> assertThat(rows.get(0).getId()).isEqualTo(65)
            );
        }
    }

    @Test
    public void testOrderByWithFullClause() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isLessThan(10))
                    .or(id,  isGreaterThan(60))
                    .orderBy(id.descending())
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            List<AnimalData> rows = mapper.selectMany(selectStatement);
            
            assertAll(
                    () -> assertThat(rows.size()).isEqualTo(14),
                    () -> assertThat(rows.get(0).getId()).isEqualTo(65)
            );
        }
    }
    
    @Test
    public void testCount() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(count().as("total"))
                    .from(animalData, "a")
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            Long count = mapper.selectALong(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select count(*) as total from AnimalData a"),
                    () -> assertThat(count).isEqualTo(65)
            );
        }
    }

    @Test
    public void testCountField() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(count(brainWeight).as("total"))
                    .from(animalData, "a")
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            Long count = mapper.selectALong(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select count(a.brain_weight) as total from AnimalData a"),
                    () -> assertThat(count).isEqualTo(65)
            );
        }
    }
    
    @Test
    public void testCountNoAlias() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(count())
                    .from(animalData)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            Long count = mapper.selectALong(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select count(*) from AnimalData"),
                    () -> assertThat(count).isEqualTo(65)
            );
        }
    }

    @Test
    public void testMax() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(max(brainWeight).as("total"))
                    .from(animalData, "a")
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            Double max = mapper.selectADouble(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select max(a.brain_weight) as total from AnimalData a"),
                    () -> assertThat(max).isEqualTo(87000.0)
            );
        }
    }

    @Test
    public void testMaxNoAlias() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(max(brainWeight))
                    .from(animalData)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            Double max = mapper.selectADouble(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select max(brain_weight) from AnimalData"),
                    () -> assertThat(max).isEqualTo(87000.0)
            );
        }
    }

    @Test
    public void testMaxSubselect() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData, "a")
                    .where(brainWeight, isEqualTo(select(max(brainWeight)).from(animalData, "b")))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            List<AnimalData> records = mapper.selectMany(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select a.id, a.animal_name, a.body_weight, a.brain_weight from AnimalData a where a.brain_weight = (select max(b.brain_weight) from AnimalData b)"),
                    () -> assertThat(records.size()).isEqualTo(1),
                    () -> assertThat(records.get(0).getAnimalName()).isEqualTo("Brachiosaurus")
            );
        }
    }

    @Test
    public void testMin() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(min(brainWeight).as("total"))
                    .from(animalData, "a")
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            Double min = mapper.selectADouble(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select min(a.brain_weight) as total from AnimalData a"),
                    () -> assertThat(min).isEqualTo(0.005)
            );
        }
    }

    @Test
    public void testMinNoAlias() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(min(brainWeight))
                    .from(animalData)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            Double min = mapper.selectADouble(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select min(brain_weight) from AnimalData"),
                    () -> assertThat(min).isEqualTo(0.005)
            );
        }
    }

    @Test
    public void testMinSubselect() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData, "a")
                    .where(brainWeight, isNotEqualTo(select(min(brainWeight)).from(animalData, "b")))
                    .orderBy(animalName)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            List<AnimalData> records = mapper.selectMany(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select a.id, a.animal_name, a.body_weight, a.brain_weight from AnimalData a where a.brain_weight <> (select min(b.brain_weight) from AnimalData b) order by animal_name"),
                    () -> assertThat(records.size()).isEqualTo(64),
                    () -> assertThat(records.get(0).getAnimalName()).isEqualTo("African elephant")
            );
        }
    }

    @Test
    public void testMinSubselectNoAlias() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(brainWeight, isNotEqualTo(select(min(brainWeight)).from(animalData)))
                    .orderBy(animalName)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            List<AnimalData> records = mapper.selectMany(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where brain_weight <> (select min(brain_weight) from AnimalData) order by animal_name"),
                    () -> assertThat(records.size()).isEqualTo(64),
                    () -> assertThat(records.get(0).getAnimalName()).isEqualTo("African elephant")
            );
        }
    }

    @Test
    public void testAvg() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(avg(brainWeight).as("average"))
                    .from(animalData, "a")
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            Double average = mapper.selectADouble(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select avg(a.brain_weight) as average from AnimalData a"),
                    () -> assertThat(average).isEqualTo(1852.69, within(.01))
            );
        }
    }

    @Test
    public void testSum() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(sum(brainWeight).as("total"))
                    .from(animalData)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            Double total = mapper.selectADouble(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select sum(brain_weight) as total from AnimalData"),
                    () -> assertThat(total).isEqualTo(120424.97, within(.01))
            );
        }
    }

    @Test
    public void testLessThanSubselect() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData, "a")
                    .where(brainWeight, isLessThan(select(max(brainWeight)).from(animalData, "b")))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            List<AnimalData> records = mapper.selectMany(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select a.id, a.animal_name, a.body_weight, a.brain_weight from AnimalData a where a.brain_weight < (select max(b.brain_weight) from AnimalData b)"),
                    () -> assertThat(records.size()).isEqualTo(64)
            );
        }
    }

    @Test
    public void testLessThanOrEqualToSubselect() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData, "a")
                    .where(brainWeight, isLessThanOrEqualTo(select(max(brainWeight)).from(animalData, "b")))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            List<AnimalData> records = mapper.selectMany(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select a.id, a.animal_name, a.body_weight, a.brain_weight from AnimalData a where a.brain_weight <= (select max(b.brain_weight) from AnimalData b)"),
                    () -> assertThat(records.size()).isEqualTo(65)
            );
        }
    }

    @Test
    public void testGreaterThanSubselect() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData, "a")
                    .where(brainWeight, isGreaterThan(select(min(brainWeight)).from(animalData, "b")))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            List<AnimalData> records = mapper.selectMany(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select a.id, a.animal_name, a.body_weight, a.brain_weight from AnimalData a where a.brain_weight > (select min(b.brain_weight) from AnimalData b)"),
                    () -> assertThat(records.size()).isEqualTo(64)
            );
        }
    }

    @Test
    public void testGreaterThanOrEqualToSubselect() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData, "a")
                    .where(brainWeight, isGreaterThanOrEqualTo(select(min(brainWeight)).from(animalData, "b")))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            List<AnimalData> records = mapper.selectMany(selectStatement);

            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select a.id, a.animal_name, a.body_weight, a.brain_weight from AnimalData a where a.brain_weight >= (select min(b.brain_weight) from AnimalData b)"),
                    () -> assertThat(records.size()).isEqualTo(65)
            );
        }
    }
    
    @Test
    public void testInsertSelectWithColumnList() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            SqlTable animalDataCopy = SqlTable.of("AnimalDataCopy");
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            InsertSelectStatementProvider insertSelectStatement = insertInto(animalDataCopy)
                    .withColumnList(id, animalName, bodyWeight, brainWeight)
                    .withSelectStatement(select(id, animalName, bodyWeight, brainWeight).from(animalData).where(id, isLessThan(22)))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expected = "insert into AnimalDataCopy (id, animal_name, body_weight, brain_weight) "
                    + "select id, animal_name, body_weight, brain_weight "
                    + "from AnimalData "
                    + "where id < #{parameters.p1,jdbcType=INTEGER}";
            
            int rows = mapper.insertSelect(insertSelectStatement);

            assertAll(
                    () -> assertThat(insertSelectStatement.getInsertStatement()).isEqualTo(expected),
                    () -> assertThat(insertSelectStatement.getParameters().size()).isEqualTo(1),
                    () -> assertThat(insertSelectStatement.getParameters().get("p1")).isEqualTo(22),
                    () -> assertThat(rows).isEqualTo(21)
            );
        }
    }

    @Test
    public void testInsertSelectWithoutColumnList() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            SqlTable animalDataCopy = SqlTable.of("AnimalDataCopy");
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            InsertSelectStatementProvider insertSelectStatement = insertInto(animalDataCopy)
                    .withSelectStatement(select(id, animalName, bodyWeight, brainWeight).from(animalData).where(id, isLessThan(33)))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expected = "insert into AnimalDataCopy "
                    + "select id, animal_name, body_weight, brain_weight "
                    + "from AnimalData "
                    + "where id < #{parameters.p1,jdbcType=INTEGER}";
            int rows = mapper.insertSelect(insertSelectStatement);

            assertAll(
                    () -> assertThat(insertSelectStatement.getInsertStatement()).isEqualTo(expected),
                    () -> assertThat(insertSelectStatement.getParameters().size()).isEqualTo(1),
                    () -> assertThat(insertSelectStatement.getParameters().get("p1")).isEqualTo(33),
                    () -> assertThat(rows).isEqualTo(32)
            );
        }
    }

    @Test
    public void testUpdateWithSelect() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            UpdateStatementProvider updateStatement = update(animalData)
                    .set(brainWeight).equalTo(select(avg(brainWeight)).from(animalData).where(brainWeight, isGreaterThan(22.0)))
                    .where(brainWeight, isLessThan(1.0))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expected = "update AnimalData "
                    + "set brain_weight = (select avg(brain_weight) from AnimalData where brain_weight > #{parameters.p1,jdbcType=DOUBLE}) "
                    + "where brain_weight < #{parameters.p2,jdbcType=DOUBLE}";
            int rows = mapper.update(updateStatement);

            assertAll(
                    () -> assertThat(updateStatement.getUpdateStatement()).isEqualTo(expected),
                    () -> assertThat(updateStatement.getParameters().size()).isEqualTo(2),
                    () -> assertThat(updateStatement.getParameters().get("p1")).isEqualTo(22.0),
                    () -> assertThat(updateStatement.getParameters().get("p2")).isEqualTo(1.0),
                    () -> assertThat(rows).isEqualTo(20)
            );
        }
    }

    @Test
    public void testUpdateWithAddAndSubtract() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            UpdateStatementProvider updateStatement = update(animalData)
                    .set(brainWeight).equalTo(add(brainWeight, constant("2")))
                    .set(bodyWeight).equalTo(subtract(bodyWeight, constant("3")))
                    .where(id, isEqualTo(1))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expected = "update AnimalData "
                    + "set brain_weight = (brain_weight + 2), body_weight = (body_weight - 3) "
                    + "where id = #{parameters.p1,jdbcType=INTEGER}";

            assertThat(updateStatement.getUpdateStatement()).isEqualTo(expected);
            assertThat(updateStatement.getParameters().size()).isEqualTo(1);
            assertThat(updateStatement.getParameters().get("p1")).isEqualTo(1);

            int rows = mapper.update(updateStatement);
            assertThat(rows).isEqualTo(1);
            
            AnimalData record = MyBatis3Utils.selectOne(mapper::selectOne,
                    BasicColumn.columnList(id, bodyWeight, brainWeight),
                    animalData,
                    c -> c.where(id, isEqualTo(1))
            );
            
            assertThat(record.getBodyWeight()).isEqualTo(-2.86);
            assertThat(record.getBrainWeight()).isEqualTo(2.005);
        }
    }

    @Test
    public void testUpdateWithMultiplyAndDivide() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            
            UpdateStatementProvider updateStatement = update(animalData)
                    .set(brainWeight).equalTo(divide(brainWeight, constant("2")))
                    .set(bodyWeight).equalTo(multiply(bodyWeight, constant("3")))
                    .where(id, isEqualTo(1))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            
            String expected = "update AnimalData "
                    + "set brain_weight = (brain_weight / 2), body_weight = (body_weight * 3) "
                    + "where id = #{parameters.p1,jdbcType=INTEGER}";
            assertThat(updateStatement.getUpdateStatement()).isEqualTo(expected);
            assertThat(updateStatement.getParameters().size()).isEqualTo(1);
            assertThat(updateStatement.getParameters().get("p1")).isEqualTo(1);

            int rows = mapper.update(updateStatement);
            assertThat(rows).isEqualTo(1);
            
            AnimalData record = MyBatis3Utils.selectOne(mapper::selectOne,
                    BasicColumn.columnList(id, bodyWeight, brainWeight),
                    animalData,
                    c -> c.where(id, isEqualTo(1))
            );
            
            assertThat(record.getBodyWeight()).isEqualTo(0.42, within(.001));
            assertThat(record.getBrainWeight()).isEqualTo(.0025);
        }
    }
}
