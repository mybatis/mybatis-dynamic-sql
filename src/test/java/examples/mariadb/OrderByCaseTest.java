/*
 *    Copyright 2016-2025 the original author or authors.
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
package examples.mariadb;

import static examples.mariadb.NumbersDynamicSQLSupport.description;
import static examples.mariadb.NumbersDynamicSQLSupport.id;
import static examples.mariadb.NumbersDynamicSQLSupport.numbers;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.case_;
import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;
import static org.mybatis.dynamic.sql.SqlBuilder.select;

import java.util.List;
import java.util.Map;

import config.TestContainersConfiguration;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.mybatis3.CommonSelectMapper;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mariadb.MariaDBContainer;

@Testcontainers
class OrderByCaseTest {

    @Container
    private static final MariaDBContainer mariadb =
            new MariaDBContainer(TestContainersConfiguration.MARIADB_LATEST)
                    .withInitScript("examples/mariadb/CreateDB.sql");

    private static SqlSessionFactory sqlSessionFactory;

    @BeforeAll
    static void setup() {
        UnpooledDataSource ds = new UnpooledDataSource(mariadb.getDriverClassName(), mariadb.getJdbcUrl(),
                mariadb.getUsername(), mariadb.getPassword());
        Environment environment = new Environment("test", new JdbcTransactionFactory(), ds);
        Configuration config = new Configuration(environment);
        config.addMapper(CommonSelectMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(config);
    }

    @Test
    void testOrderBySimpleCase() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, description).from(numbers)
                    .orderBy(case_(description)
                            .when("One").then(3)
                            .when("Two").then(5)
                            .when("Three").then(4)
                            .when("Four").then(2)
                            .when("Five").then(1)
                            .else_(99)
                            .end())
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select id, description from numbers order by case description "
                    +  "when #{parameters.p1,jdbcType=VARCHAR} then 3 "
                    +  "when #{parameters.p2,jdbcType=VARCHAR} then 5 "
                    +  "when #{parameters.p3,jdbcType=VARCHAR} then 4 "
                    +  "when #{parameters.p4,jdbcType=VARCHAR} then 2 "
                    +  "when #{parameters.p5,jdbcType=VARCHAR} then 1 else 99 end";

            assertThat(selectStatement.getSelectStatement()).isEqualTo( expected);

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);
            assertThat(rows).hasSize(5);
            assertThat(rows.get(0)).extracting("id", "description").containsExactly(5, "Five");
            assertThat(rows.get(1)).extracting("id", "description").containsExactly(4, "Four");
            assertThat(rows.get(2)).extracting("id", "description").containsExactly(1, "One");
            assertThat(rows.get(3)).extracting("id", "description").containsExactly(3, "Three");
            assertThat(rows.get(4)).extracting("id", "description").containsExactly(2, "Two");
        }
    }

    @Test
    void testOrderBySimpleCaseWithTableAlias() {
        // ignore table aliases in order by phrases
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, description).from(numbers, "n")
                    .orderBy(case_(description)
                            .when("One").then(3)
                            .when("Two").then(5)
                            .when("Three").then(4)
                            .when("Four").then(2)
                            .when("Five").then(1)
                            .else_(99)
                            .end())
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select n.id, n.description from numbers n order by case description "
                    +  "when #{parameters.p1,jdbcType=VARCHAR} then 3 "
                    +  "when #{parameters.p2,jdbcType=VARCHAR} then 5 "
                    +  "when #{parameters.p3,jdbcType=VARCHAR} then 4 "
                    +  "when #{parameters.p4,jdbcType=VARCHAR} then 2 "
                    +  "when #{parameters.p5,jdbcType=VARCHAR} then 1 else 99 end";

            assertThat(selectStatement.getSelectStatement()).isEqualTo( expected);

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);
            assertThat(rows).hasSize(5);
            assertThat(rows.get(0)).extracting("id", "description").containsExactly(5, "Five");
            assertThat(rows.get(1)).extracting("id", "description").containsExactly(4, "Four");
            assertThat(rows.get(2)).extracting("id", "description").containsExactly(1, "One");
            assertThat(rows.get(3)).extracting("id", "description").containsExactly(3, "Three");
            assertThat(rows.get(4)).extracting("id", "description").containsExactly(2, "Two");
        }
    }

    @Test
    void testOrderBySimpleCaseWithColumnAlias() {
        // ignore table aliases in order by phrases
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, description.as("descr")).from(numbers)
                    .orderBy(case_(description.as("descr"))
                            .when("One").then(3)
                            .when("Two").then(5)
                            .when("Three").then(4)
                            .when("Four").then(2)
                            .when("Five").then(1)
                            .else_(99)
                            .end())
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select id, description as descr from numbers order by case descr "
                    +  "when #{parameters.p1,jdbcType=VARCHAR} then 3 "
                    +  "when #{parameters.p2,jdbcType=VARCHAR} then 5 "
                    +  "when #{parameters.p3,jdbcType=VARCHAR} then 4 "
                    +  "when #{parameters.p4,jdbcType=VARCHAR} then 2 "
                    +  "when #{parameters.p5,jdbcType=VARCHAR} then 1 else 99 end";

            assertThat(selectStatement.getSelectStatement()).isEqualTo( expected);

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);
            assertThat(rows).hasSize(5);
            assertThat(rows.get(0)).extracting("id", "descr").containsExactly(5, "Five");
            assertThat(rows.get(1)).extracting("id", "descr").containsExactly(4, "Four");
            assertThat(rows.get(2)).extracting("id", "descr").containsExactly(1, "One");
            assertThat(rows.get(3)).extracting("id", "descr").containsExactly(3, "Three");
            assertThat(rows.get(4)).extracting("id", "descr").containsExactly(2, "Two");
        }
    }

    @Test
    void testOrderBySimpleCaseDescending() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, description).from(numbers)
                    .orderBy(case_(description)
                            .when("One").then(3)
                            .when("Two").then(5)
                            .when("Three").then(4)
                            .when("Four").then(2)
                            .when("Five").then(1)
                            .else_(99)
                            .end().descending())
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select id, description from numbers order by case description "
                    +  "when #{parameters.p1,jdbcType=VARCHAR} then 3 "
                    +  "when #{parameters.p2,jdbcType=VARCHAR} then 5 "
                    +  "when #{parameters.p3,jdbcType=VARCHAR} then 4 "
                    +  "when #{parameters.p4,jdbcType=VARCHAR} then 2 "
                    +  "when #{parameters.p5,jdbcType=VARCHAR} then 1 else 99 end DESC";

            assertThat(selectStatement.getSelectStatement()).isEqualTo( expected);

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);
            assertThat(rows).hasSize(5);
            assertThat(rows.get(4)).extracting("id", "description").containsExactly(5, "Five");
            assertThat(rows.get(3)).extracting("id", "description").containsExactly(4, "Four");
            assertThat(rows.get(2)).extracting("id", "description").containsExactly(1, "One");
            assertThat(rows.get(1)).extracting("id", "description").containsExactly(3, "Three");
            assertThat(rows.get(0)).extracting("id", "description").containsExactly(2, "Two");
        }
    }

    @Test
    void testOrderBySearchedCase() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, description).from(numbers)
                    .orderBy(case_()
                            .when(description, isEqualTo("One")).then(3)
                            .when(description, isEqualTo("Two")).then(5)
                            .when(description, isEqualTo("Three")).then(4)
                            .when(description, isEqualTo("Four")).then(2)
                            .when(description, isEqualTo("Five")).then(1)
                            .else_(99)
                            .end())
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select id, description from numbers order by case "
                    +  "when description = #{parameters.p1,jdbcType=VARCHAR} then 3 "
                    +  "when description = #{parameters.p2,jdbcType=VARCHAR} then 5 "
                    +  "when description = #{parameters.p3,jdbcType=VARCHAR} then 4 "
                    +  "when description = #{parameters.p4,jdbcType=VARCHAR} then 2 "
                    +  "when description = #{parameters.p5,jdbcType=VARCHAR} then 1 else 99 end";

            assertThat(selectStatement.getSelectStatement()).isEqualTo( expected);

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);
            assertThat(rows).hasSize(5);
            assertThat(rows.get(0)).extracting("id", "description").containsExactly(5, "Five");
            assertThat(rows.get(1)).extracting("id", "description").containsExactly(4, "Four");
            assertThat(rows.get(2)).extracting("id", "description").containsExactly(1, "One");
            assertThat(rows.get(3)).extracting("id", "description").containsExactly(3, "Three");
            assertThat(rows.get(4)).extracting("id", "description").containsExactly(2, "Two");
        }
    }

    @Test
    void testOrderBySearchedCaseWithTableAlias() {
        // ignore table aliases in order by phrases
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, description).from(numbers, "n")
                    .orderBy(case_()
                            .when(description, isEqualTo("One")).then(3)
                            .when(description, isEqualTo("Two")).then(5)
                            .when(description, isEqualTo("Three")).then(4)
                            .when(description, isEqualTo("Four")).then(2)
                            .when(description, isEqualTo("Five")).then(1)
                            .else_(99)
                            .end())
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select n.id, n.description from numbers n order by case "
                    +  "when description = #{parameters.p1,jdbcType=VARCHAR} then 3 "
                    +  "when description = #{parameters.p2,jdbcType=VARCHAR} then 5 "
                    +  "when description = #{parameters.p3,jdbcType=VARCHAR} then 4 "
                    +  "when description = #{parameters.p4,jdbcType=VARCHAR} then 2 "
                    +  "when description = #{parameters.p5,jdbcType=VARCHAR} then 1 else 99 end";

            assertThat(selectStatement.getSelectStatement()).isEqualTo( expected);

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);
            assertThat(rows).hasSize(5);
            assertThat(rows.get(0)).extracting("id", "description").containsExactly(5, "Five");
            assertThat(rows.get(1)).extracting("id", "description").containsExactly(4, "Four");
            assertThat(rows.get(2)).extracting("id", "description").containsExactly(1, "One");
            assertThat(rows.get(3)).extracting("id", "description").containsExactly(3, "Three");
            assertThat(rows.get(4)).extracting("id", "description").containsExactly(2, "Two");
        }
    }

    @Test
    void testOrderBySearchedCaseWithColumnAlias() {
        // ignore table aliases in order by phrases
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, description.as("descr")).from(numbers)
                    .orderBy(case_()
                            .when(description.as("descr"), isEqualTo("One")).then(3)
                            .when(description.as("descr"), isEqualTo("Two")).then(5)
                            .when(description.as("descr"), isEqualTo("Three")).then(4)
                            .when(description.as("descr"), isEqualTo("Four")).then(2)
                            .when(description.as("descr"), isEqualTo("Five")).then(1)
                            .else_(99)
                            .end())
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select id, description as descr from numbers order by case "
                    +  "when descr = #{parameters.p1,jdbcType=VARCHAR} then 3 "
                    +  "when descr = #{parameters.p2,jdbcType=VARCHAR} then 5 "
                    +  "when descr = #{parameters.p3,jdbcType=VARCHAR} then 4 "
                    +  "when descr = #{parameters.p4,jdbcType=VARCHAR} then 2 "
                    +  "when descr = #{parameters.p5,jdbcType=VARCHAR} then 1 else 99 end";

            assertThat(selectStatement.getSelectStatement()).isEqualTo( expected);

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);
            assertThat(rows).hasSize(5);
            assertThat(rows.get(0)).extracting("id", "descr").containsExactly(5, "Five");
            assertThat(rows.get(1)).extracting("id", "descr").containsExactly(4, "Four");
            assertThat(rows.get(2)).extracting("id", "descr").containsExactly(1, "One");
            assertThat(rows.get(3)).extracting("id", "descr").containsExactly(3, "Three");
            assertThat(rows.get(4)).extracting("id", "descr").containsExactly(2, "Two");
        }
    }

    @Test
    void testOrderBySearchedCaseDescending() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, description).from(numbers)
                    .orderBy(case_()
                            .when(description, isEqualTo("One")).then(3)
                            .when(description, isEqualTo("Two")).then(5)
                            .when(description, isEqualTo("Three")).then(4)
                            .when(description, isEqualTo("Four")).then(2)
                            .when(description, isEqualTo("Five")).then(1)
                            .else_(99)
                            .end().descending())
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select id, description from numbers order by case "
                    +  "when description = #{parameters.p1,jdbcType=VARCHAR} then 3 "
                    +  "when description = #{parameters.p2,jdbcType=VARCHAR} then 5 "
                    +  "when description = #{parameters.p3,jdbcType=VARCHAR} then 4 "
                    +  "when description = #{parameters.p4,jdbcType=VARCHAR} then 2 "
                    +  "when description = #{parameters.p5,jdbcType=VARCHAR} then 1 else 99 end DESC";

            assertThat(selectStatement.getSelectStatement()).isEqualTo( expected);

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);
            assertThat(rows).hasSize(5);
            assertThat(rows.get(4)).extracting("id", "description").containsExactly(5, "Five");
            assertThat(rows.get(3)).extracting("id", "description").containsExactly(4, "Four");
            assertThat(rows.get(2)).extracting("id", "description").containsExactly(1, "One");
            assertThat(rows.get(1)).extracting("id", "description").containsExactly(3, "Three");
            assertThat(rows.get(0)).extracting("id", "description").containsExactly(2, "Two");
        }
    }
}
