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

class LimitAndOffsetTest {

    private static final String JDBC_URL = "jdbc:hsqldb:mem:aname";
    private static final String JDBC_DRIVER = "org.hsqldb.jdbcDriver";

    private SqlSessionFactory sqlSessionFactory;

    @BeforeEach
    void setup() throws Exception {
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
    void testLimitAndOffsetAfterFrom() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            SelectStatementProvider selectStatement = select(animalData.allColumns())
                    .from(animalData)
                    .limit(3)
                    .offset(22)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            List<AnimalData> records = mapper.selectMany(selectStatement);

            assertAll(
                    () -> assertThat(records).hasSize(3),
                    () -> assertThat(records.get(0).getId()).isEqualTo(23),
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select * from AnimalData limit #{parameters.p1} offset #{parameters.p2}"),
                    () -> assertThat(selectStatement.getParameters()).containsEntry("p1", 3L),
                    () -> assertThat(selectStatement.getParameters()).containsEntry("p2", 22L)
            );
        }
    }

    @Test
    void testLimitOnlyAfterFrom() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            SelectStatementProvider selectStatement = select(animalData.allColumns())
                    .from(animalData)
                    .limit(3)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            List<AnimalData> records = mapper.selectMany(selectStatement);

            assertAll(
                    () -> assertThat(records).hasSize(3),
                    () -> assertThat(records.get(0).getId()).isEqualTo(1),
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select * from AnimalData limit #{parameters.p1}"),
                    () -> assertThat(selectStatement.getParameters()).containsEntry("p1", 3L)
            );
        }
    }

    @Test
    void testOffsetOnlyAfterFrom() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            SelectStatementProvider selectStatement = select(animalData.allColumns())
                    .from(animalData)
                    .offset(22)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            List<AnimalData> records = mapper.selectMany(selectStatement);

            assertAll(
                    () -> assertThat(records).hasSize(43),
                    () -> assertThat(records.get(0).getId()).isEqualTo(23),
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select * from AnimalData offset #{parameters.p1} rows"),
                    () -> assertThat(selectStatement.getParameters()).containsEntry("p1", 22L)
            );
        }
    }

    @Test
    void testLimitAndOffsetAfterWhere() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            SelectStatementProvider selectStatement = select(animalData.allColumns())
                    .from(animalData)
                    .where(id, isLessThan(50))
                    .and(id, isGreaterThan(22))
                    .limit(3)
                    .offset(22)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            List<AnimalData> records = mapper.selectMany(selectStatement);

            assertAll(
                    () -> assertThat(records).hasSize(3),
                    () -> assertThat(records.get(0).getId()).isEqualTo(45),
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select * from AnimalData where id < #{parameters.p1,jdbcType=INTEGER} and id > #{parameters.p2,jdbcType=INTEGER} limit #{parameters.p3} offset #{parameters.p4}"),
                    () -> assertThat(selectStatement.getParameters()).containsEntry("p3", 3L),
                    () -> assertThat(selectStatement.getParameters()).containsEntry("p4", 22L)
            );
        }
    }

    @Test
    void testLimitOnlyAfterWhere() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            SelectStatementProvider selectStatement = select(animalData.allColumns())
                    .from(animalData)
                    .where(id, isLessThan(50))
                    .limit(3)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            List<AnimalData> records = mapper.selectMany(selectStatement);

            assertAll(
                    () -> assertThat(records).hasSize(3),
                    () -> assertThat(records.get(0).getId()).isEqualTo(1),
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select * from AnimalData where id < #{parameters.p1,jdbcType=INTEGER} limit #{parameters.p2}"),
                    () -> assertThat(selectStatement.getParameters()).containsEntry("p2", 3L)
            );
        }
    }

    @Test
    void testOffsetOnlyAfterWhere() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            SelectStatementProvider selectStatement = select(animalData.allColumns())
                    .from(animalData)
                    .where(id, isLessThan(50))
                    .offset(22)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            List<AnimalData> records = mapper.selectMany(selectStatement);

            assertAll(
                    () -> assertThat(records).hasSize(27),
                    () -> assertThat(records.get(0).getId()).isEqualTo(23),
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select * from AnimalData where id < #{parameters.p1,jdbcType=INTEGER} offset #{parameters.p2} rows"),
                    () -> assertThat(selectStatement.getParameters()).containsEntry("p2", 22L)
            );
        }
    }

    @Test
    void testLimitAndOffsetAfterOrderBy() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            SelectStatementProvider selectStatement = select(animalData.allColumns())
                    .from(animalData)
                    .orderBy(id)
                    .limit(3)
                    .offset(22)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            List<AnimalData> records = mapper.selectMany(selectStatement);

            assertAll(
                    () -> assertThat(records).hasSize(3),
                    () -> assertThat(records.get(0).getId()).isEqualTo(23),
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select * from AnimalData order by id limit #{parameters.p1} offset #{parameters.p2}"),
                    () -> assertThat(selectStatement.getParameters()).containsEntry("p1", 3L),
                    () -> assertThat(selectStatement.getParameters()).containsEntry("p2", 22L)
            );
        }
    }

    @Test
    void testLimitOnlyAfterOrderBy() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            SelectStatementProvider selectStatement = select(animalData.allColumns())
                    .from(animalData)
                    .orderBy(id)
                    .limit(3)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            List<AnimalData> records = mapper.selectMany(selectStatement);

            assertAll(
                    () -> assertThat(records).hasSize(3),
                    () -> assertThat(records.get(0).getId()).isEqualTo(1),
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select * from AnimalData order by id limit #{parameters.p1}"),
                    () -> assertThat(selectStatement.getParameters()).containsEntry("p1", 3L)
            );
        }
    }

    @Test
    void testOffsetOnlyAfterOrderBy() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            SelectStatementProvider selectStatement = select(animalData.allColumns())
                    .from(animalData)
                    .orderBy(id)
                    .offset(22)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            AnimalDataMapper mapper = sqlSession.getMapper(AnimalDataMapper.class);
            List<AnimalData> records = mapper.selectMany(selectStatement);

            assertAll(
                    () -> assertThat(records).hasSize(43),
                    () -> assertThat(records.get(0).getId()).isEqualTo(23),
                    () -> assertThat(selectStatement.getSelectStatement()).isEqualTo("select * from AnimalData order by id offset #{parameters.p1} rows"),
                    () -> assertThat(selectStatement.getParameters()).containsEntry("p1", 22L)
            );
        }
    }
}
