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
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;

public class OptionalConditionsAnimalDataTest {
    
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
                    .where(id, isGreaterThanWhenPresent(NULL_INTEGER))  // the where clause should not render
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
                    .and(id, isNotEqualToWhenPresent(NULL_INTEGER))
                    .or(id, isEqualToWhenPresent(4))
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
                    .where(id, isLessThanWhenPresent(NULL_INTEGER))
                    .and(id, isEqualToWhenPresent(3))
                    .or(id, isEqualToWhenPresent(4))
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
                    .where(id, isLessThanWhenPresent(NULL_INTEGER), and(id, isGreaterThanOrEqualToWhenPresent(NULL_INTEGER)))
                    .and(id, isEqualToWhenPresent(NULL_INTEGER), or(id, isEqualTo(3), and(id, isLessThanWhenPresent(NULL_INTEGER))))
                    .or(id, isEqualToWhenPresent(4), and(id, isGreaterThanOrEqualToWhenPresent(NULL_INTEGER)))
                    .and(id, isNotEqualToWhenPresent(NULL_INTEGER))
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
                    .where(id, isLessThanWhenPresent(NULL_INTEGER), and(id, isEqualToWhenPresent(3)))
                    .or(id, isEqualToWhenPresent(4))
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
    public void testEqualWhenPresentWithValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isEqualToWhenPresent(4))
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
    public void testEqualWhenPresentWithoutValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isEqualToWhenPresent(NULL_INTEGER))
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
    public void testNotEqualWhenPresentWithValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isNotEqualToWhenPresent(4))
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
    public void testNotEqualWhenPresentWithoutValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isNotEqualToWhenPresent(NULL_INTEGER))
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
    public void testGreaterThanWhenPresentWithValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isGreaterThanWhenPresent(4))
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
    public void testGreaterThanWhenPresentWithoutValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isGreaterThanWhenPresent(NULL_INTEGER))
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
    public void testGreaterThanOrEqualToWhenPresentWithValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isGreaterThanOrEqualToWhenPresent(4))
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
    public void testGreaterThanOrEqualToWhenPresentWithoutValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isGreaterThanOrEqualToWhenPresent(NULL_INTEGER))
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
    public void testLessThanWhenPresentWithValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isLessThanWhenPresent(4))
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
    public void testLessThanWhenPresentWithoutValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isLessThanWhenPresent(NULL_INTEGER))
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
    public void testLessThanOrEqualToWhenPresentWithValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isLessThanOrEqualToWhenPresent(4))
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
    public void testLessThanOrEqualToWhenPresentWithoutValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isLessThanOrEqualToWhenPresent(NULL_INTEGER))
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
    public void testIsInWhenPresentWithValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isInWhenPresent(4, 5, 6))
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
    public void testIsInWhenPresentWithSomeValues() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isInWhenPresent(3, NULL_INTEGER, 5))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData where id in (#{parameters.p1,jdbcType=INTEGER},#{parameters.p2,jdbcType=INTEGER}) order by id"),
                    () -> assertThat(animals.size()).isEqualTo(2),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(3)
            );
        }
    }

    @Test
    public void testIsInWhenPresentWithNoValues() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isInWhenPresent())
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
    public void testIsInCaseInsensitiveWhenPresentWithValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isInCaseInsensitiveWhenPresent("mouse", "musk shrew"))
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
    public void testIsInCaseInsensitiveWhenPresentWithSomeValues() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isInCaseInsensitiveWhenPresent("mouse", null, "musk shrew"))
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
    public void testIsInCaseInsensitiveWhenPresentWithNoValues() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isInCaseInsensitiveWhenPresent())
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
    public void testIsNotInWhenPresentWithValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isNotInWhenPresent(4, 5, 6))
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
    public void testIsNotInWhenPresentWithSomeValues() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isNotInWhenPresent(3, NULL_INTEGER, 5))
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
    public void testIsNotInWhenPresentWithNoValues() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isNotInWhenPresent())
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
    public void testIsNotInCaseInsensitiveWhenPresentWithValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isNotInCaseInsensitiveWhenPresent("mouse", "musk shrew"))
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
    public void testIsNotInCaseInsensitiveWhenPresentWithSomeValues() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isNotInCaseInsensitiveWhenPresent("mouse", null, "musk shrew"))
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
    public void testIsNotInCaseInsensitiveWhenPresentWithNoValues() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isNotInCaseInsensitiveWhenPresent())
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
    public void testIsBetweenWhenPresentWithValues() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isBetweenWhenPresent(3).and(6))
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
    public void testIsBetweenWhenPresentWithFirstMissing() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isBetweenWhenPresent(NULL_INTEGER).and(6))
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
    public void testIsBetweenWhenPresentWithSecondMissing() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isBetweenWhenPresent(3).and(NULL_INTEGER))
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
    public void testIsBetweenWhenPresentWithBothMissing() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isBetweenWhenPresent(NULL_INTEGER).and(NULL_INTEGER))
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
    public void testIsNotBetweenWhenPresentWithValues() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isNotBetweenWhenPresent(3).and(6))
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
    public void testIsNotBetweenWhenPresentWithFirstMissing() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isNotBetweenWhenPresent(NULL_INTEGER).and(6))
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
    public void testIsNotBetweenWhenPresentWithSecondMissing() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isNotBetweenWhenPresent(3).and(NULL_INTEGER))
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
    public void testIsNotBetweenWhenPresentWithBothMissing() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isNotBetweenWhenPresent(NULL_INTEGER).and(NULL_INTEGER))
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
    public void testIsLikeWhenPresentWithValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isLikeWhenPresent("%mole"))
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
    public void testIsLikeWhenPresentWithoutValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isLikeWhenPresent((String) null))
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
    public void testIsLikeCaseInsensitiveWhenPresentWithValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isLikeCaseInsensitiveWhenPresent("%MoLe"))
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
    public void testIsLikeCaseInsensitiveWhenPresentWithoutValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isLikeCaseInsensitiveWhenPresent((String) null))
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
    public void testIsNotLikeWhenPresentWithValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isNotLikeWhenPresent("%mole"))
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
    public void testIsNotLikeWhenPresentWithoutValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isNotLikeWhenPresent((String) null))
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
    public void testIsNotLikeCaseInsensitiveWhenPresentWithValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isNotLikeCaseInsensitiveWhenPresent("%MoLe"))
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
    public void testIsNotLikeCaseInsensitiveWhenPresentWithoutValue() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(animalName, isNotLikeCaseInsensitiveWhenPresent((String) null))
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
