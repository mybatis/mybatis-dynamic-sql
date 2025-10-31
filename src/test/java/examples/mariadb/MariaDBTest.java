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

import static org.assertj.core.api.Assertions.assertThat;

import static examples.mariadb.ItemsDynamicSQLSupport.amount;
import static examples.mariadb.ItemsDynamicSQLSupport.id;
import static examples.mariadb.ItemsDynamicSQLSupport.items;
import static examples.mariadb.ItemsDynamicSQLSupport.description;
import static org.assertj.core.api.Assertions.entry;
import static org.mybatis.dynamic.sql.SqlBuilder.add;
import static org.mybatis.dynamic.sql.SqlBuilder.constant;
import static org.mybatis.dynamic.sql.SqlBuilder.deleteFrom;
import static org.mybatis.dynamic.sql.SqlBuilder.isLessThan;
import static org.mybatis.dynamic.sql.SqlBuilder.select;
import static org.mybatis.dynamic.sql.SqlBuilder.update;

import config.TestContainersConfiguration;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;
import org.mybatis.dynamic.sql.util.mybatis3.CommonDeleteMapper;
import org.mybatis.dynamic.sql.util.mybatis3.CommonSelectMapper;
import org.mybatis.dynamic.sql.util.mybatis3.CommonUpdateMapper;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mariadb.MariaDBContainer;

@Testcontainers
class MariaDBTest {

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
        config.addMapper(CommonDeleteMapper.class);
        config.addMapper(CommonSelectMapper.class);
        config.addMapper(CommonUpdateMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(config);
    }

    @Test
    void smokeTest() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, description)
                    .from(items)
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
             List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);
             assertThat(rows).hasSize(20);
        }
    }

    @Test
    void testDeleteWithLimit() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonDeleteMapper mapper = session.getMapper(CommonDeleteMapper.class);

            DeleteStatementProvider deleteStatement = deleteFrom(items)
                    .limit(5)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            assertThat(deleteStatement.getDeleteStatement())
                    .isEqualTo("delete from items limit #{parameters.p1}");
            assertThat(deleteStatement.getParameters()).containsOnly(entry("p1", 5L));

            int rows = mapper.delete(deleteStatement);
            assertThat(rows).isEqualTo(5);
        }
    }

    @Test
    void testDeleteWithOrderBy() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonDeleteMapper mapper = session.getMapper(CommonDeleteMapper.class);

            DeleteStatementProvider deleteStatement = deleteFrom(items)
                    .orderBy(amount.descending())
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            assertThat(deleteStatement.getDeleteStatement())
                    .isEqualTo("delete from items order by amount DESC");
            assertThat(deleteStatement.getParameters()).isEmpty();

            int rows = mapper.delete(deleteStatement);
            assertThat(rows).isEqualTo(20);
        }
    }

    @Test
    void testDeleteWithOrderByAndLimit() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonDeleteMapper mapper = session.getMapper(CommonDeleteMapper.class);

            DeleteStatementProvider deleteStatement = deleteFrom(items)
                    .orderBy(amount.descending())
                    .limit(5)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            assertThat(deleteStatement.getDeleteStatement())
                    .isEqualTo("delete from items order by amount DESC limit #{parameters.p1}");
            assertThat(deleteStatement.getParameters()).containsOnly(entry("p1", 5L));

            int rows = mapper.delete(deleteStatement);
            assertThat(rows).isEqualTo(5);
        }
    }

    @Test
    void testDeleteWithWhereAndLimit() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonDeleteMapper mapper = session.getMapper(CommonDeleteMapper.class);

            DeleteStatementProvider deleteStatement = deleteFrom(items)
                    .where(id, isLessThan(10))
                    .limit(5)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "delete from items where id < #{parameters.p1,jdbcType=INTEGER} limit #{parameters.p2}";
            assertThat(deleteStatement.getDeleteStatement()).isEqualTo(expected);
            assertThat(deleteStatement.getParameters()).containsOnly(entry("p1", 10), entry("p2", 5L));

            int rows = mapper.delete(deleteStatement);
            assertThat(rows).isEqualTo(5);
        }
    }

    @Test
    void testDeleteWithWhereAndOrderBy() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonDeleteMapper mapper = session.getMapper(CommonDeleteMapper.class);

            DeleteStatementProvider deleteStatement = deleteFrom(items)
                    .where(id, isLessThan(10))
                    .orderBy(amount.descending())
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "delete from items where id < #{parameters.p1,jdbcType=INTEGER} order by amount DESC";
            assertThat(deleteStatement.getDeleteStatement()).isEqualTo(expected);
            assertThat(deleteStatement.getParameters()).containsOnly(entry("p1", 10));

            int rows = mapper.delete(deleteStatement);
            assertThat(rows).isEqualTo(9);
        }
    }

    @Test
    void testDeleteWithWhereAndOrderByAndLimit() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonDeleteMapper mapper = session.getMapper(CommonDeleteMapper.class);

            DeleteStatementProvider deleteStatement = deleteFrom(items)
                    .where(id, isLessThan(10))
                    .orderBy(amount)
                    .limit(5)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "delete from items where id < #{parameters.p1,jdbcType=INTEGER} order by amount "
                    + "limit #{parameters.p2}";
            assertThat(deleteStatement.getDeleteStatement()).isEqualTo(expected);
            assertThat(deleteStatement.getParameters()).containsOnly(entry("p1", 10), entry("p2", 5L));

            int rows = mapper.delete(deleteStatement);
            assertThat(rows).isEqualTo(5);
        }
    }

    @Test
    void testUpdateWithLimit() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonUpdateMapper mapper = session.getMapper(CommonUpdateMapper.class);

            UpdateStatementProvider updateStatement = update(items)
                    .set(amount).equalTo(add(amount, constant("100")))
                    .limit(5)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "update items set amount = (amount + 100) limit #{parameters.p1}";
            assertThat(updateStatement.getUpdateStatement()).isEqualTo(expected);
            assertThat(updateStatement.getParameters()).containsOnly(entry("p1", 5L));

            int rows = mapper.update(updateStatement);
            assertThat(rows).isEqualTo(5);
        }
    }

    @Test
    void testUpdateWithOrderBy() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonUpdateMapper mapper = session.getMapper(CommonUpdateMapper.class);

            UpdateStatementProvider updateStatement = update(items)
                    .set(amount).equalTo(add(amount, constant("100")))
                    .orderBy(amount.descending())
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            assertThat(updateStatement.getUpdateStatement())
                    .isEqualTo("update items set amount = (amount + 100) order by amount DESC");
            assertThat(updateStatement.getParameters()).isEmpty();

            int rows = mapper.update(updateStatement);
            assertThat(rows).isEqualTo(20);
        }
    }

    @Test
    void testUpdateWithOrderByAndLimit() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonUpdateMapper mapper = session.getMapper(CommonUpdateMapper.class);

            UpdateStatementProvider updateStatement = update(items)
                    .set(amount).equalTo(add(amount, constant("100")))
                    .orderBy(amount.descending())
                    .limit(5)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "update items set amount = (amount + 100) order by amount DESC limit #{parameters.p1}";
            assertThat(updateStatement.getUpdateStatement()).isEqualTo(expected);
            assertThat(updateStatement.getParameters()).containsOnly(entry("p1", 5L));

            int rows = mapper.update(updateStatement);
            assertThat(rows).isEqualTo(5);
        }
    }

    @Test
    void testUpdateWithWhereAndLimit() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonUpdateMapper mapper = session.getMapper(CommonUpdateMapper.class);

            UpdateStatementProvider updateStatement = update(items)
                    .set(amount).equalTo(add(amount, constant("100")))
                    .where(id, isLessThan(10))
                    .limit(5)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "update items set amount = (amount + 100) where id < #{parameters.p1,jdbcType=INTEGER} "
                    + "limit #{parameters.p2}";
            assertThat(updateStatement.getUpdateStatement()).isEqualTo(expected);
            assertThat(updateStatement.getParameters()).containsOnly(entry("p1", 10), entry("p2", 5L));

            int rows = mapper.update(updateStatement);
            assertThat(rows).isEqualTo(5);
        }
    }

    @Test
    void testUpdateWithWhereAndOrderBy() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonUpdateMapper mapper = session.getMapper(CommonUpdateMapper.class);

            UpdateStatementProvider updateStatement = update(items)
                    .set(amount).equalTo(add(amount, constant("100")))
                    .where(id, isLessThan(10))
                    .orderBy(amount.descending())
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "update items set amount = (amount + 100) where id < #{parameters.p1,jdbcType=INTEGER} "
                    + "order by amount DESC";
            assertThat(updateStatement.getUpdateStatement()).isEqualTo(expected);
            assertThat(updateStatement.getParameters()).containsOnly(entry("p1", 10));

            int rows = mapper.update(updateStatement);
            assertThat(rows).isEqualTo(9);
        }
    }

    @Test
    void testUpdateWithWhereAndOrderByAndLimit() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonUpdateMapper mapper = session.getMapper(CommonUpdateMapper.class);

            UpdateStatementProvider updateStatement = update(items)
                    .set(amount).equalTo(add(amount, constant("100")))
                    .where(id, isLessThan(10))
                    .orderBy(amount)
                    .limit(5)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "update items set amount = (amount + 100) where id < #{parameters.p1,jdbcType=INTEGER} "
                    + "order by amount limit #{parameters.p2}";
            assertThat(updateStatement.getUpdateStatement()).isEqualTo(expected);
            assertThat(updateStatement.getParameters()).containsOnly(entry("p1", 10), entry("p2", 5L));

            int rows = mapper.update(updateStatement);
            assertThat(rows).isEqualTo(5);
        }
    }
}
