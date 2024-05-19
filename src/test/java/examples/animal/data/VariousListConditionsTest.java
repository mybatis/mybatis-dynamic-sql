/*
 *    Copyright 2016-2024 the original author or authors.
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

import static examples.animal.data.AnimalDataDynamicSqlSupport.animalData;
import static examples.animal.data.AnimalDataDynamicSqlSupport.animalName;
import static examples.animal.data.AnimalDataDynamicSqlSupport.id;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mybatis.dynamic.sql.SqlBuilder.isIn;
import static org.mybatis.dynamic.sql.SqlBuilder.isInCaseInsensitive;
import static org.mybatis.dynamic.sql.SqlBuilder.isInCaseInsensitiveWhenPresent;
import static org.mybatis.dynamic.sql.SqlBuilder.isInWhenPresent;
import static org.mybatis.dynamic.sql.SqlBuilder.isNotIn;
import static org.mybatis.dynamic.sql.SqlBuilder.isNotInCaseInsensitive;
import static org.mybatis.dynamic.sql.SqlBuilder.isNotInCaseInsensitiveWhenPresent;
import static org.mybatis.dynamic.sql.SqlBuilder.isNotInWhenPresent;
import static org.mybatis.dynamic.sql.SqlBuilder.select;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLSyntaxErrorException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.exceptions.PersistenceException;
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
import org.mybatis.dynamic.sql.util.mybatis3.CommonSelectMapper;

class VariousListConditionsTest {
    private static final String JDBC_URL = "jdbc:hsqldb:mem:aname";
    private static final String JDBC_DRIVER = "org.hsqldb.jdbcDriver";

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
        config.addMapper(CommonSelectMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(config);
    }

    @Test
    void testInWithNull() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName)
                    .from(animalData)
                    .where(id, isIn(2, 3, null))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            assertThat(selectStatement.getSelectStatement()).isEqualTo(
                    "select id, animal_name from AnimalData where id " +
                            "in (#{parameters.p1,jdbcType=INTEGER},#{parameters.p2,jdbcType=INTEGER},#{parameters.p3,jdbcType=INTEGER}) " +
                            "order by id"
            );
            assertThat(selectStatement.getParameters()).containsEntry("p1", 2);
            assertThat(selectStatement.getParameters()).containsEntry("p2", 3);
            assertThat(selectStatement.getParameters()).containsEntry("p3", null);

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);
            assertThat(rows).hasSize(2);

            assertThat(rows.get(0)).containsEntry("ID", 2);
        }
    }

    @Test
    void testInWhenPresentWithNull() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName)
                    .from(animalData)
                    .where(id, isInWhenPresent(2, 3, null))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            assertThat(selectStatement.getSelectStatement()).isEqualTo(
                    "select id, animal_name from AnimalData " +
                            "where id in (#{parameters.p1,jdbcType=INTEGER},#{parameters.p2,jdbcType=INTEGER}) " +
                            "order by id"
            );
            assertThat(selectStatement.getParameters()).containsEntry("p1", 2);
            assertThat(selectStatement.getParameters()).containsEntry("p2", 3);

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);
            assertThat(rows).hasSize(2);

            assertThat(rows.get(0)).containsEntry("ID", 2);
        }
    }

    @Test
    void testInWithEmptyList() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName)
                    .from(animalData)
                    .where(id, isIn(Collections.emptyList()))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            assertThat(selectStatement.getSelectStatement()).isEqualTo(
                    "select id, animal_name from AnimalData " +
                            "where id in () " +
                            "order by id"
            );

            assertThatExceptionOfType(PersistenceException.class).isThrownBy(() ->
                mapper.selectManyMappedRows(selectStatement)
            ).withCauseInstanceOf(SQLSyntaxErrorException.class);
        }
    }

    @Test
    void testInWhenPresentWithEmptyList() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName)
                    .from(animalData)
                    .where(id, isInWhenPresent(Collections.emptyList()))
                    .orderBy(id)
                    .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            assertThat(selectStatement.getSelectStatement()).isEqualTo(
                    "select id, animal_name from AnimalData " +
                            "order by id");

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);
            assertThat(rows).hasSize(65);

            assertThat(rows.get(0)).containsEntry("ID", 1);
        }
    }

    @Test
    void testInWithNullList() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() ->
                isIn((Collection<Integer>) null)
        );
    }

    @Test
    void testInWhenPresentWithNullList() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName)
                    .from(animalData)
                    .where(id, isInWhenPresent((Collection<Integer>) null))
                    .orderBy(id)
                    .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            assertThat(selectStatement.getSelectStatement()).isEqualTo(
                    "select id, animal_name from AnimalData " +
                            "order by id");

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);
            assertThat(rows).hasSize(65);

            assertThat(rows.get(0)).containsEntry("ID", 1);
        }
    }

    @Test
    void testInWhenPresentMap() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName)
                    .from(animalData)
                    .where(id, isInWhenPresent(2, 3).map(i -> i + 3))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            assertThat(selectStatement.getSelectStatement()).isEqualTo(
                    "select id, animal_name from AnimalData " +
                            "where id in (#{parameters.p1,jdbcType=INTEGER},#{parameters.p2,jdbcType=INTEGER}) " +
                            "order by id"
            );
            assertThat(selectStatement.getParameters()).containsEntry("p1", 5);
            assertThat(selectStatement.getParameters()).containsEntry("p2", 6);

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);
            assertThat(rows).hasSize(2);

            assertThat(rows.get(0)).containsEntry("ID", 5);
        }
    }

    @Test
    void testNotInWhenPresentMap() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName)
                    .from(animalData)
                    .where(id, isNotInWhenPresent(2, 3).map(i -> i + 3))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            assertThat(selectStatement.getSelectStatement()).isEqualTo(
                    "select id, animal_name from AnimalData " +
                            "where id not in (#{parameters.p1,jdbcType=INTEGER},#{parameters.p2,jdbcType=INTEGER}) " +
                            "order by id"
            );
            assertThat(selectStatement.getParameters()).containsEntry("p1", 5);
            assertThat(selectStatement.getParameters()).containsEntry("p2", 6);

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);
            assertThat(rows).hasSize(63);

            assertThat(rows.get(0)).containsEntry("ID", 1);
        }
    }

    @Test
    void testInCaseInsensitiveWhenPresentMap() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName)
                    .from(animalData)
                    .where(animalName, isInCaseInsensitiveWhenPresent("Fred", "Betty").filter(s -> false))
                    .orderBy(id)
                    .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            assertThat(selectStatement.getSelectStatement()).isEqualTo(
                    "select id, animal_name from AnimalData " +
                            "order by id"
            );

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);
            assertThat(rows).hasSize(65);

            assertThat(rows.get(0)).containsEntry("ID", 1);
        }
    }

    @Test
    void testNotInCaseInsensitiveWhenPresentMap() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName)
                    .from(animalData)
                    .where(animalName, isNotInCaseInsensitiveWhenPresent("Fred", "Betty").filter(s -> false))
                    .orderBy(id)
                    .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            assertThat(selectStatement.getSelectStatement()).isEqualTo(
                    "select id, animal_name from AnimalData " +
                            "order by id"
            );

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);
            assertThat(rows).hasSize(65);

            assertThat(rows.get(0)).containsEntry("ID", 1);
        }
    }

    @Test
    void testInEventuallyEmpty() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName)
                    .from(animalData)
                    .where(id, isIn(1, 2).filter(s -> false))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            assertThat(selectStatement.getSelectStatement()).isEqualTo(
                    "select id, animal_name from AnimalData " +
                            "where id in () " +
                            "order by id"
            );

            assertThatExceptionOfType(PersistenceException.class).isThrownBy(
                    () -> mapper.selectManyMappedRows(selectStatement))
                    .withCauseInstanceOf(SQLSyntaxErrorException.class);
        }
    }

    @Test
    void testInCaseInsensitiveEventuallyEmpty() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName)
                    .from(animalData)
                    .where(animalName, isInCaseInsensitive("Fred", "Betty").filter(s -> false))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            assertThat(selectStatement.getSelectStatement()).isEqualTo(
                    "select id, animal_name from AnimalData " +
                            "where upper(animal_name) in () " +
                            "order by id"
            );

            assertThatExceptionOfType(PersistenceException.class).isThrownBy(
                            () -> mapper.selectManyMappedRows(selectStatement))
                    .withCauseInstanceOf(SQLSyntaxErrorException.class);
        }
    }

    @Test
    void testNotInEventuallyEmpty() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName)
                    .from(animalData)
                    .where(id, isNotIn(1, 2).filter(s -> false))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            assertThat(selectStatement.getSelectStatement()).isEqualTo(
                    "select id, animal_name from AnimalData " +
                            "where id not in () " +
                            "order by id"
            );

            assertThatExceptionOfType(PersistenceException.class).isThrownBy(
                            () -> mapper.selectManyMappedRows(selectStatement))
                    .withCauseInstanceOf(SQLSyntaxErrorException.class);
        }
    }

    @Test
    void testNotInCaseInsensitiveEventuallyEmpty() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName)
                    .from(animalData)
                    .where(animalName, isNotInCaseInsensitive("Fred", "Betty").filter(s -> false))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            assertThat(selectStatement.getSelectStatement()).isEqualTo(
                    "select id, animal_name from AnimalData " +
                            "where upper(animal_name) not in () " +
                            "order by id"
            );

            assertThatExceptionOfType(PersistenceException.class).isThrownBy(
                            () -> mapper.selectManyMappedRows(selectStatement))
                    .withCauseInstanceOf(SQLSyntaxErrorException.class);
        }
    }

    @Test
    void testInEventuallyEmptyDoubleFilter() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, animalName)
                    .from(animalData)
                    .where(id, isIn(1, 2).filter(s -> false).filter(s -> false))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            assertThat(selectStatement.getSelectStatement()).isEqualTo(
                    "select id, animal_name from AnimalData " +
                            "where id in () " +
                            "order by id"
            );

            assertThatExceptionOfType(PersistenceException.class).isThrownBy(
                            () -> mapper.selectManyMappedRows(selectStatement))
                    .withCauseInstanceOf(SQLSyntaxErrorException.class);
        }
    }
}
