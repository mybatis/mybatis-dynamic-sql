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
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Map;

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
import org.mybatis.dynamic.sql.DerivedColumn;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.mybatis3.CommonSelectMapper;

class SubQueryTest {
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
        config.addMapper(CommonSelectMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(config);
    }

    @Test
    void testBasicSubQuery() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
            DerivedColumn<Integer> rowNum = DerivedColumn.of("rownum()");

            SelectStatementProvider selectStatement = select(animalName, rowNum)
                    .from(
                            select(id, animalName)
                                    .from(animalData)
                                    .where(id, isLessThan(22))
                                    .orderBy(animalName.descending())
                    )
                    .where(rowNum, isLessThan(5))
                    .and(animalName, isLike("%a%"))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            assertThat(selectStatement.getSelectStatement()).isEqualTo(
                    "select animal_name, rownum() " +
                            "from (select id, animal_name " +
                            "from AnimalData where id < #{parameters.p1,jdbcType=INTEGER} " +
                            "order by animal_name DESC) " +
                            "where rownum() < #{parameters.p2} and animal_name like #{parameters.p3,jdbcType=VARCHAR}"
            );
            assertThat(selectStatement.getParameters()).containsEntry("p1", 22);
            assertThat(selectStatement.getParameters()).containsEntry("p2", 5);
            assertThat(selectStatement.getParameters()).containsEntry("p3", "%a%");

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);
            assertThat(rows).hasSize(4);

            assertThat(rows.get(2)).containsEntry("ANIMAL_NAME", "Chinchilla");
            assertThat(rows.get(2)).containsEntry("ROWNUM", 3);
        }
    }

    @Test
    void testSimpleAliases() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
            DerivedColumn<Integer> rowNum = DerivedColumn.of("rownum()");

            SelectStatementProvider selectStatement = select(animalName, rowNum)
                    .from(
                            select(id, animalName)
                                    .from(animalData, "a")
                                    .where(id, isLessThan(22))
                                    .orderBy(animalName.descending()),
                            "b"
                    )
                    .where(rowNum, isLessThan(5))
                    .and(animalName, isLike("%a%"))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            assertThat(selectStatement.getSelectStatement()).isEqualTo(
                    "select animal_name, rownum() " +
                            "from (select a.id, a.animal_name " +
                            "from AnimalData a where a.id < #{parameters.p1,jdbcType=INTEGER} " +
                            "order by animal_name DESC) b " +
                            "where rownum() < #{parameters.p2} and animal_name like #{parameters.p3,jdbcType=VARCHAR}"
            );
            assertThat(selectStatement.getParameters()).containsEntry("p1", 22);
            assertThat(selectStatement.getParameters()).containsEntry("p2", 5);
            assertThat(selectStatement.getParameters()).containsEntry("p3", "%a%");

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);
            assertThat(rows).hasSize(4);

            assertThat(rows.get(2)).containsEntry("ANIMAL_NAME", "Chinchilla");
            assertThat(rows.get(2)).containsEntry("ROWNUM", 3);
        }
    }

    @Test
    void testSimpleAliasesWithManualQualifiers() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
            DerivedColumn<Integer> rowNum = DerivedColumn.of("rownum()");

            SelectStatementProvider selectStatement = select(animalName.qualifiedWith("b"), rowNum)
                    .from(
                            select(id, animalName)
                                    .from(animalData, "a")
                                    .where(id, isLessThan(22))
                                    .orderBy(animalName.descending()),
                            "b"
                    )
                    .where(rowNum, isLessThan(5))
                    .and(animalName.qualifiedWith("b"), isLike("%a%"))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            assertThat(selectStatement.getSelectStatement()).isEqualTo(
                    "select b.animal_name, rownum() " +
                            "from (select a.id, a.animal_name " +
                            "from AnimalData a where a.id < #{parameters.p1,jdbcType=INTEGER} " +
                            "order by animal_name DESC) b " +
                            "where rownum() < #{parameters.p2} and b.animal_name like #{parameters.p3,jdbcType=VARCHAR}"
            );
            assertThat(selectStatement.getParameters()).containsEntry("p1", 22);
            assertThat(selectStatement.getParameters()).containsEntry("p2", 5);
            assertThat(selectStatement.getParameters()).containsEntry("p3", "%a%");

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);
            assertThat(rows).hasSize(4);

            assertThat(rows.get(2)).containsEntry("ANIMAL_NAME", "Chinchilla");
            assertThat(rows.get(2)).containsEntry("ROWNUM", 3);
        }
    }

    @Test
    void testBasicSubQueryWithAliases() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
            DerivedColumn<Integer> rowNum = DerivedColumn.of("rownum()");
            SqlColumn<String> outerAnimalName = animalName.qualifiedWith("b");
            DerivedColumn<Integer> animalId = DerivedColumn.of("animalId", "b");

            SelectStatementProvider selectStatement = select(outerAnimalName.asCamelCase(), animalId, rowNum)
                    .from(
                            select(id.as("animalId"), animalName)
                                    .from(animalData, "a")
                                    .where(id, isLessThan(22))
                                    .orderBy(animalName.descending()),
                            "b"
                    )
                    .where(rowNum, isLessThan(5))
                    .and(outerAnimalName, isLike("%a%"))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            assertThat(selectStatement.getSelectStatement()).isEqualTo(
                    "select b.animal_name as \"animalName\", b.animalId, rownum() " +
                            "from (select a.id as animalId, a.animal_name " +
                            "from AnimalData a where a.id < #{parameters.p1,jdbcType=INTEGER} " +
                            "order by animal_name DESC) b " +
                            "where rownum() < #{parameters.p2} and b.animal_name like #{parameters.p3,jdbcType=VARCHAR}"
            );
            assertThat(selectStatement.getParameters()).containsEntry("p1", 22);
            assertThat(selectStatement.getParameters()).containsEntry("p2", 5);
            assertThat(selectStatement.getParameters()).containsEntry("p3", "%a%");

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);
            assertThat(rows).hasSize(4);

            assertThat(rows.get(2)).containsEntry("animalName", "Chinchilla");
            assertThat(rows.get(2)).containsEntry("ANIMALID", 14);
            assertThat(rows.get(2)).containsEntry("ROWNUM", 3);
        }
    }
}
