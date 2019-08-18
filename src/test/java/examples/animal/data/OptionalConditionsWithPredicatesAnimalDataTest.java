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

import static examples.animal.data.AnimalDataDynamicSqlSupport.animalData;
import static examples.animal.data.AnimalDataDynamicSqlSupport.animalName;
import static examples.animal.data.AnimalDataDynamicSqlSupport.bodyWeight;
import static examples.animal.data.AnimalDataDynamicSqlSupport.brainWeight;
import static examples.animal.data.AnimalDataDynamicSqlSupport.id;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Objects;

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
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.Predicates;

public class OptionalConditionsWithPredicatesAnimalDataTest {
    
    private static final String JDBC_URL = "jdbc:hsqldb:mem:aname";
    private static final String JDBC_DRIVER = "org.hsqldb.jdbcDriver";
    private static final Integer NULL_INTEGER = null;
    
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
    public void testAllIgnored() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isGreaterThan(NULL_INTEGER).when(Objects::nonNull))  // the where clause should not render
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData order by id"),
                    () -> assertThat(animals.size()).isEqualTo(65),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1),
                    () -> assertThat(animals.get(1).getId()).isEqualTo(2)
            );
        }
    }
    
    @Test
    public void testIgnoredBetweenRendered() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isEqualTo(3))
                    .and(id, isNotEqualTo(NULL_INTEGER).when(Objects::nonNull))
                    .or(id, isEqualTo(4).when(Objects::nonNull))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id = #{parameters.p1,jdbcType=INTEGER} or id = #{parameters.p2,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals.size()).isEqualTo(2),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(3),
                    () -> assertThat(animals.get(1).getId()).isEqualTo(4)
            );
        }
    }

    @Test
    public void testIgnoredInWhere() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isLessThan(NULL_INTEGER).when(Objects::nonNull))
                    .and(id, isEqualTo(3).when(Objects::nonNull))
                    .or(id, isEqualTo(4).when(Objects::nonNull))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id = #{parameters.p1,jdbcType=INTEGER} or id = #{parameters.p2,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals.size()).isEqualTo(2),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(3),
                    () -> assertThat(animals.get(1).getId()).isEqualTo(4)
            );
        }
    }

    @Test
    public void testManyIgnored() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isLessThan(NULL_INTEGER).when(Objects::nonNull), and(id, isGreaterThanOrEqualTo(NULL_INTEGER).when(Objects::nonNull)))
                    .and(id, isEqualTo(NULL_INTEGER).when(Objects::nonNull), or(id, isEqualTo(3), and(id, isLessThan(NULL_INTEGER).when(Objects::nonNull))))
                    .or(id, isEqualTo(4).when(Objects::nonNull), and(id, isGreaterThanOrEqualTo(NULL_INTEGER).when(Objects::nonNull)))
                    .and(id, isNotEqualTo(NULL_INTEGER).when(Objects::nonNull))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id = #{parameters.p1,jdbcType=INTEGER} or id = #{parameters.p2,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals.size()).isEqualTo(2),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(3),
                    () -> assertThat(animals.get(1).getId()).isEqualTo(4)
            );
        }
    }

    @Test
    public void testIgnoredInitialWhere() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isLessThan(NULL_INTEGER).when(Objects::nonNull), and(id, isEqualTo(3).when(Objects::nonNull)))
                    .or(id, isEqualTo(4).when(Objects::nonNull))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id = #{parameters.p1,jdbcType=INTEGER} or id = #{parameters.p2,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals.size()).isEqualTo(2),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(3),
                    () -> assertThat(animals.get(1).getId()).isEqualTo(4)
            );
        }
    }

    @Test
    public void testEqualWhenWithValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isEqualTo(4).when(Objects::nonNull))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id = #{parameters.p1,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals.size()).isEqualTo(1),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(4)
            );
        }
    }

    @Test
    public void testEqualWhenWithoutValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isEqualTo(NULL_INTEGER).when(Objects::nonNull))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id <= #{parameters.p1,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals.size()).isEqualTo(10),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    public void testNotEqualWhenWithValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isNotEqualTo(4).when(Objects::nonNull))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id <> #{parameters.p1,jdbcType=INTEGER} and id <= #{parameters.p2,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals.size()).isEqualTo(9),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    public void testNotEqualWhenWithoutValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isNotEqualTo(NULL_INTEGER).when(Objects::nonNull))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id <= #{parameters.p1,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals.size()).isEqualTo(10),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    public void testGreaterThanWhenWithValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isGreaterThan(4).when(Objects::nonNull))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id > #{parameters.p1,jdbcType=INTEGER} and id <= #{parameters.p2,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals.size()).isEqualTo(6),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(5)
            );
        }
    }

    @Test
    public void testGreaterThanWhenWithoutValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isGreaterThan(NULL_INTEGER).when(Objects::nonNull))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id <= #{parameters.p1,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals.size()).isEqualTo(10),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    public void testGreaterThanOrEqualToWhenWithValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isGreaterThanOrEqualTo(4).when(Objects::nonNull))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id >= #{parameters.p1,jdbcType=INTEGER} and id <= #{parameters.p2,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals.size()).isEqualTo(7),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(4)
            );
        }
    }

    @Test
    public void testGreaterThanOrEqualToWhenWithoutValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isGreaterThanOrEqualTo(NULL_INTEGER).when(Objects::nonNull))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id <= #{parameters.p1,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals.size()).isEqualTo(10),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    public void testLessThanWhenWithValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isLessThan(4).when(Objects::nonNull))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id < #{parameters.p1,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals.size()).isEqualTo(3),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    public void testLessThanWhenWithoutValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isLessThan(NULL_INTEGER).when(Objects::nonNull))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id <= #{parameters.p1,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals.size()).isEqualTo(10),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    public void testLessThanOrEqualToWhenWithValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isLessThanOrEqualTo(4).when(Objects::nonNull))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id <= #{parameters.p1,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals.size()).isEqualTo(4),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    public void testLessThanOrEqualToWhenWithoutValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isLessThanOrEqualTo(NULL_INTEGER).when(Objects::nonNull))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id <= #{parameters.p1,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals.size()).isEqualTo(10),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    public void testIsInWhenWithValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isIn(4, 5, 6))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id in (#{parameters.p1,jdbcType=INTEGER},#{parameters.p2,jdbcType=INTEGER},#{parameters.p3,jdbcType=INTEGER}) order by id"),
                    () -> assertThat(animals.size()).isEqualTo(3),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(4)
            );
        }
    }

    @Test
    public void testIsInWhenWithSomeValues() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isIn(3, NULL_INTEGER, 5).then(s -> s.filter(Objects::nonNull).map(i -> i + 3)))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id in (#{parameters.p1,jdbcType=INTEGER},#{parameters.p2,jdbcType=INTEGER}) order by id"),
                    () -> assertThat(animals.size()).isEqualTo(2),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(6),
                    () -> assertThat(animals.get(1).getId()).isEqualTo(8)
            );
        }
    }

    @Test
    public void testIsInCaseInsensitiveWhenWithValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isInCaseInsensitive("mouse", "musk shrew"))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where upper(animal_name) in (#{parameters.p1,jdbcType=VARCHAR},#{parameters.p2,jdbcType=VARCHAR}) order by id"),
                    () -> assertThat(animals.size()).isEqualTo(2),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(4)
            );
        }
    }

    @Test
    public void testValueStreamTransformer() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isIn("  Mouse", "  ", null, "", "Musk shrew  ")
                            .then(s -> s.filter(Objects::nonNull)
                                    .map(String::trim)
                                    .filter(st -> !st.isEmpty())))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where animal_name in (#{parameters.p1,jdbcType=VARCHAR},#{parameters.p2,jdbcType=VARCHAR}) order by id"),
                    () -> assertThat(animals.size()).isEqualTo(2),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(4)
            );
        }
    }
    
    @Test
    public void testValueStreamTransformerWithCustomCondition() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, MyInCondition.isIn("  Mouse", "  ", null, "", "Musk shrew  "))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where animal_name in (#{parameters.p1,jdbcType=VARCHAR},#{parameters.p2,jdbcType=VARCHAR}) order by id"),
                    () -> assertThat(animals.size()).isEqualTo(2),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(4)
            );
        }
    }
    
    @Test
    public void testIsInCaseInsensitiveWhenWithSomeValues() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isInCaseInsensitive("mouse", null, "musk shrew").then(s -> s.filter(Objects::nonNull)))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where upper(animal_name) in (#{parameters.p1,jdbcType=VARCHAR},#{parameters.p2,jdbcType=VARCHAR}) order by id"),
                    () -> assertThat(animals.size()).isEqualTo(2),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(4)
            );
        }
    }

    @Test
    public void testIsInCaseInsensitiveWhenWithNoValues() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isInCaseInsensitive())
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id <= #{parameters.p1,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals.size()).isEqualTo(10),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    public void testIsNotInWhenWithValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isNotIn(4, 5, 6))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id not in (#{parameters.p1,jdbcType=INTEGER},#{parameters.p2,jdbcType=INTEGER},#{parameters.p3,jdbcType=INTEGER}) and id <= #{parameters.p4,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals.size()).isEqualTo(7),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    public void testIsNotInWhenWithSomeValues() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isNotIn(3, NULL_INTEGER, 5).then(s -> s.filter(Objects::nonNull)))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id not in (#{parameters.p1,jdbcType=INTEGER},#{parameters.p2,jdbcType=INTEGER}) and id <= #{parameters.p3,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals.size()).isEqualTo(8),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    public void testIsNotInCaseInsensitiveWhenWithValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isNotInCaseInsensitive("mouse", "musk shrew"))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where upper(animal_name) not in (#{parameters.p1,jdbcType=VARCHAR},#{parameters.p2,jdbcType=VARCHAR}) and id <= #{parameters.p3,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals.size()).isEqualTo(8),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    public void testIsNotInCaseInsensitiveWhenWithSomeValues() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isNotInCaseInsensitive("mouse", null, "musk shrew").then(s -> s.filter(Objects::nonNull)))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where upper(animal_name) not in (#{parameters.p1,jdbcType=VARCHAR},#{parameters.p2,jdbcType=VARCHAR}) and id <= #{parameters.p3,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals.size()).isEqualTo(8),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    public void testIsNotInCaseInsensitiveWhenWithNoValues() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isNotInCaseInsensitive())
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id <= #{parameters.p1,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals.size()).isEqualTo(10),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    public void testIsBetweenWhenWithValues() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isBetween(3).and(6).when(Predicates.bothPresent()))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id between #{parameters.p1,jdbcType=INTEGER} and #{parameters.p2,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals.size()).isEqualTo(4),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(3)
            );
        }
    }

    @Test
    public void testIsBetweenWhenWithFirstMissing() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isBetween(NULL_INTEGER).and(6).when(Predicates.bothPresent()))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id <= #{parameters.p1,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals.size()).isEqualTo(10),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    public void testIsBetweenWhenWithSecondMissing() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isBetween(3).and(NULL_INTEGER).when(Predicates.bothPresent()))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id <= #{parameters.p1,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals.size()).isEqualTo(10),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    public void testIsBetweenWhenWithBothMissing() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isBetween(NULL_INTEGER).and(NULL_INTEGER).when(Predicates.bothPresent()))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id <= #{parameters.p1,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals.size()).isEqualTo(10),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    public void testIsNotBetweenWhenWithValues() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isNotBetween(3).and(6).when(Predicates.bothPresent()))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id not between #{parameters.p1,jdbcType=INTEGER} and #{parameters.p2,jdbcType=INTEGER} and id <= #{parameters.p3,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals.size()).isEqualTo(6),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    public void testIsNotBetweenWhenWithFirstMissing() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isNotBetween(NULL_INTEGER).and(6).when(Predicates.bothPresent()))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id <= #{parameters.p1,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals.size()).isEqualTo(10),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    public void testIsNotBetweenWhenWithSecondMissing() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isNotBetween(3).and(NULL_INTEGER).when(Predicates.bothPresent()))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id <= #{parameters.p1,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals.size()).isEqualTo(10),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    public void testIsNotBetweenWhenWithBothMissing() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isNotBetween(NULL_INTEGER).and(NULL_INTEGER).when(Predicates.bothPresent()))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id <= #{parameters.p1,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals.size()).isEqualTo(10),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    public void testIsLikeWhenWithValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isLike("%mole").when(Objects::nonNull))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where animal_name like #{parameters.p1,jdbcType=VARCHAR} and id <= #{parameters.p2,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals.size()).isEqualTo(2),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(6)
            );
        }
    }

    @Test
    public void testIsLikeWhenWithoutValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isLike((String) null).when(Objects::nonNull))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id <= #{parameters.p1,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals.size()).isEqualTo(10),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    public void testIsLikeCaseInsensitiveWhenWithValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isLikeCaseInsensitive("%MoLe").when(Objects::nonNull))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where upper(animal_name) like #{parameters.p1,jdbcType=VARCHAR} and id <= #{parameters.p2,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals.size()).isEqualTo(2),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(6)
            );
        }
    }

    @Test
    public void testIsLikeCaseInsensitiveWhenWithoutValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isLikeCaseInsensitive((String) null).when(Objects::nonNull))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id <= #{parameters.p1,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals.size()).isEqualTo(10),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    public void testIsNotLikeWhenWithValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isNotLike("%mole").when(Objects::nonNull))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where animal_name not like #{parameters.p1,jdbcType=VARCHAR} and id <= #{parameters.p2,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals.size()).isEqualTo(8),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    public void testIsNotLikeWhenWithoutValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isNotLike((String) null).when(Objects::nonNull))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id <= #{parameters.p1,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals.size()).isEqualTo(10),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    public void testIsNotLikeCaseInsensitiveWhenWithValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isNotLikeCaseInsensitive("%MoLe").when(Objects::nonNull))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where upper(animal_name) not like #{parameters.p1,jdbcType=VARCHAR} and id <= #{parameters.p2,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals.size()).isEqualTo(8),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    public void testIsNotLikeCaseInsensitiveWhenWithoutValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isNotLikeCaseInsensitive((String) null).when(Objects::nonNull))
                    .and(id, isLessThanOrEqualTo(10))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id <= #{parameters.p1,jdbcType=INTEGER} order by id"),
                    () -> assertThat(animals.size()).isEqualTo(10),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }
}
