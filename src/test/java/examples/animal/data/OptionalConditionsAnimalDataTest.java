/*
 *    Copyright 2016-2022 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
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

class OptionalConditionsAnimalDataTest {

    private static final String JDBC_URL = "jdbc:hsqldb:mem:aname";
    private static final String JDBC_DRIVER = "org.hsqldb.jdbcDriver";
    private static final Integer NULL_INTEGER = null;

    private SqlSessionFactory sqlSessionFactory;

    @BeforeEach
    void setup() throws Exception {
        Class.forName(JDBC_DRIVER);
        InputStream is = getClass().getResourceAsStream("/examples/animal/data/CreateAnimalData.sql");
        assert is != null;
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
    void testAllIgnored() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            SelectStatementProvider selectStatement = select(id, animalName, bodyWeight, brainWeight)
                    .from(animalData)
                    .where(id, isGreaterThanWhenPresent(NULL_INTEGER))  // the where clause should not render
                    .orderBy(id)
                    .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<AnimalData> animals = mapper.selectMany(selectStatement);
            assertAll(
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select id, animal_name, body_weight, brain_weight from AnimalData order by id"),
                    () -> assertThat(animals).hasSize(65),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1),
                    () -> assertThat(animals.get(1).getId()).isEqualTo(2)
            );
        }
    }

    @Test
    void testIgnoredBetweenRendered() {
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
                    () -> assertThat(animals).hasSize(2),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(3),
                    () -> assertThat(animals.get(1).getId()).isEqualTo(4)
            );
        }
    }

    @Test
    void testIgnoredInWhere() {
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
                    () -> assertThat(animals).hasSize(2),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(3),
                    () -> assertThat(animals.get(1).getId()).isEqualTo(4)
            );
        }
    }

    @Test
    void testManyIgnored() {
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
                    () -> assertThat(animals).hasSize(2),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(3),
                    () -> assertThat(animals.get(1).getId()).isEqualTo(4)
            );
        }
    }

    @Test
    void testIgnoredInitialWhere() {
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
                    () -> assertThat(animals).hasSize(2),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(3),
                    () -> assertThat(animals.get(1).getId()).isEqualTo(4)
            );
        }
    }

    @Test
    void testEqualWhenPresentWithValue() {
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
                    () -> assertThat(animals).hasSize(1),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(4)
            );
        }
    }

    @Test
    void testEqualWhenPresentWithoutValue() {
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
                    () -> assertThat(animals).hasSize(10),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    void testNotEqualWhenPresentWithValue() {
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
                    () -> assertThat(animals).hasSize(9),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    void testNotEqualWhenPresentWithoutValue() {
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
                    () -> assertThat(animals).hasSize(10),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    void testGreaterThanWhenPresentWithValue() {
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
                    () -> assertThat(animals).hasSize(6),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(5)
            );
        }
    }

    @Test
    void testGreaterThanWhenPresentWithoutValue() {
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
                    () -> assertThat(animals).hasSize(10),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    void testGreaterThanOrEqualToWhenPresentWithValue() {
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
                    () -> assertThat(animals).hasSize(7),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(4)
            );
        }
    }

    @Test
    void testGreaterThanOrEqualToWhenPresentWithoutValue() {
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
                    () -> assertThat(animals).hasSize(10),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    void testLessThanWhenPresentWithValue() {
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
                    () -> assertThat(animals).hasSize(3),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    void testLessThanWhenPresentWithoutValue() {
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
                    () -> assertThat(animals).hasSize(10),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    void testLessThanOrEqualToWhenPresentWithValue() {
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
                    () -> assertThat(animals).hasSize(4),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    void testLessThanOrEqualToWhenPresentWithoutValue() {
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
                    () -> assertThat(animals).hasSize(10),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    void testIsInWhenPresentWithValue() {
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
                    () -> assertThat(animals).hasSize(3),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(4)
            );
        }
    }

    @Test
    void testIsInWhenPresentWithSomeValues() {
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
                    () -> assertThat(animals).hasSize(2),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(3)
            );
        }
    }

    @Test
    void testIsInWhenPresentWithNoValues() {
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
                    () -> assertThat(animals).hasSize(10),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    void testIsInCaseInsensitiveWhenPresentWithValue() {
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
                    () -> assertThat(animals).hasSize(2),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(4)
            );
        }
    }

    @Test
    void testIsInCaseInsensitiveWhenPresentWithSomeValues() {
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
                    () -> assertThat(animals).hasSize(2),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(4)
            );
        }
    }

    @Test
    void testIsInCaseInsensitiveWhenPresentWithNoValues() {
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
                    () -> assertThat(animals).hasSize(10),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    void testIsNotInWhenPresentWithValue() {
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
                    () -> assertThat(animals).hasSize(7),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    void testIsNotInWhenPresentWithSomeValues() {
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
                    () -> assertThat(animals).hasSize(8),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    void testIsNotInWhenPresentWithNoValues() {
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
                    () -> assertThat(animals).hasSize(10),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    void testIsNotInCaseInsensitiveWhenPresentWithValue() {
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
                    () -> assertThat(animals).hasSize(8),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    void testIsNotInCaseInsensitiveWhenPresentWithSomeValues() {
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
                    () -> assertThat(animals).hasSize(8),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    void testIsNotInCaseInsensitiveWhenPresentWithNoValues() {
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
                    () -> assertThat(animals).hasSize(10),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    void testIsBetweenWhenPresentWithValues() {
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
                    () -> assertThat(animals).hasSize(4),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(3)
            );
        }
    }

    @Test
    void testIsBetweenWhenPresentWithFirstMissing() {
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
                    () -> assertThat(animals).hasSize(10),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    void testIsBetweenWhenPresentWithSecondMissing() {
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
                    () -> assertThat(animals).hasSize(10),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    void testIsBetweenWhenPresentWithBothMissing() {
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
                    () -> assertThat(animals).hasSize(10),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    void testIsNotBetweenWhenPresentWithValues() {
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
                    () -> assertThat(animals).hasSize(6),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    void testIsNotBetweenWhenPresentWithFirstMissing() {
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
                    () -> assertThat(animals).hasSize(10),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    void testIsNotBetweenWhenPresentWithSecondMissing() {
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
                    () -> assertThat(animals).hasSize(10),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    void testIsNotBetweenWhenPresentWithBothMissing() {
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
                    () -> assertThat(animals).hasSize(10),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    void testIsLikeWhenPresentWithValue() {
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
                    () -> assertThat(animals).hasSize(2),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(6)
            );
        }
    }

    @Test
    void testIsLikeWhenPresentWithoutValue() {
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
                    () -> assertThat(animals).hasSize(10),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    void testIsLikeCaseInsensitiveWhenPresentWithValue() {
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
                    () -> assertThat(animals).hasSize(2),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(6)
            );
        }
    }

    @Test
    void testIsLikeCaseInsensitiveWhenPresentWithoutValue() {
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
                    () -> assertThat(animals).hasSize(10),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    void testIsNotLikeWhenPresentWithValue() {
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
                    () -> assertThat(animals).hasSize(8),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    void testIsNotLikeWhenPresentWithoutValue() {
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
                    () -> assertThat(animals).hasSize(10),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    void testIsNotLikeCaseInsensitiveWhenPresentWithValue() {
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
                    () -> assertThat(animals).hasSize(8),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }

    @Test
    void testIsNotLikeCaseInsensitiveWhenPresentWithoutValue() {
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
                    () -> assertThat(animals).hasSize(10),
                    () -> assertThat(animals.get(0).getId()).isEqualTo(1)
            );
        }
    }
}
