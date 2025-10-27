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
package issues.gh655;

import static examples.mariadb.ItemsDynamicSQLSupport.id;
import static examples.mariadb.ItemsDynamicSQLSupport.items;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mybatis.dynamic.sql.SqlBuilder.add;
import static org.mybatis.dynamic.sql.SqlBuilder.constant;
import static org.mybatis.dynamic.sql.SqlBuilder.isEqualToWhenPresent;
import static org.mybatis.dynamic.sql.SqlBuilder.isGreaterThan;
import static org.mybatis.dynamic.sql.SqlBuilder.select;
import static org.mybatis.dynamic.sql.SqlBuilder.sum;

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
import org.mybatis.dynamic.sql.exception.InvalidSqlException;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.mybatis3.CommonSelectMapper;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mariadb.MariaDBContainer;

@Testcontainers
class Gh655Test {

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
    void sumTest() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(sum(id, isGreaterThan(5)).as("numrows"))
                    .from(items)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select sum(id > #{parameters.p1,jdbcType=INTEGER}) as numrows from items";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
            assertThat(selectStatement.getParameters()).containsEntry("p1", 5);

            Long numrows = mapper.selectOneLong(selectStatement);
            assertThat(numrows).isEqualTo(15L);
        }
    }

    @Test
    void sumWithOptionalTest() {
        SelectModel selectModel = select(sum(id, isEqualToWhenPresent((Integer) null)).as("numrows"))
                .from(items)
                .build();

        assertThatExceptionOfType(InvalidSqlException.class)
                .isThrownBy(() -> selectModel.render(RenderingStrategies.MYBATIS3))
                .withMessage("The \"sum\" function does not support conditions that fail to render");
    }

    @Test
    void sumAndAddTest() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(add(sum(id, isGreaterThan(5)), constant("3")).as("numrows"))
                    .from(items)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select (sum(id > #{parameters.p1,jdbcType=INTEGER}) + 3) as numrows from items";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
            assertThat(selectStatement.getParameters()).containsEntry("p1", 5);

            Long numrows = mapper.selectOneLong(selectStatement);
            assertThat(numrows).isEqualTo(18L);
        }
    }

    @Test
    void columnComparisonTest() {
        // this is a nonsensical query just to test that rendering works as expected
        SelectStatementProvider selectStatement = select(sum(id, isGreaterThan(5)).as("numrows"))
                .from(items)
                .where(id, isGreaterThan(sum(id, isGreaterThan(5))))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select sum(id > #{parameters.p1,jdbcType=INTEGER}) as numrows from items " +
                "where id > sum(id > #{parameters.p2,jdbcType=INTEGER})";
        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(selectStatement.getParameters()).containsEntry("p1", 5);
        assertThat(selectStatement.getParameters()).containsEntry("p2", 5);
    }
}
