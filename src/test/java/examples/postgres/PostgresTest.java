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
package examples.postgres;

import static examples.postgres.TableCodeDynamicSqlSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

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
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
class PostgresTest {

    @Container
    private static final PostgreSQLContainer postgres =
            new PostgreSQLContainer(TestContainersConfiguration.POSTGRES_LATEST)
                    .withInitScript("examples/postgres/dbInit.sql");

    private static SqlSessionFactory sqlSessionFactory;

    @BeforeAll
    static void setUp() {
        UnpooledDataSource ds = new UnpooledDataSource(postgres.getDriverClassName(), postgres.getJdbcUrl(),
                postgres.getUsername(), postgres.getPassword());
        Environment environment = new Environment("test", new JdbcTransactionFactory(), ds);
        Configuration configuration = new Configuration(environment);
        configuration.addMapper(CommonSelectMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
    }

    @Test
    void testSelectForUpdate() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, description)
                    .from(tableCode)
                    .orderBy(id)
                    .forUpdate()
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            assertThat(selectStatement.getSelectStatement())
                    .isEqualTo("select id, description from TableCode order by id for update");
            List<Map<String, Object>> records = mapper.selectManyMappedRows(selectStatement);
            assertThat(records).hasSize(4);
        }
    }

    @Test
    void testSelectForUpdateNoWait() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, description)
                    .from(tableCode)
                    .orderBy(id)
                    .forUpdate()
                    .nowait()
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            assertThat(selectStatement.getSelectStatement())
                    .isEqualTo("select id, description from TableCode order by id for update nowait");
            List<Map<String, Object>> records = mapper.selectManyMappedRows(selectStatement);
            assertThat(records).hasSize(4);
        }
    }

    @Test
    void testSelectForUpdateSkipLocked() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, description)
                    .from(tableCode)
                    .orderBy(id)
                    .forUpdate()
                    .skipLocked()
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            assertThat(selectStatement.getSelectStatement())
                    .isEqualTo("select id, description from TableCode order by id for update skip locked");
            List<Map<String, Object>> records = mapper.selectManyMappedRows(selectStatement);
            assertThat(records).hasSize(4);
        }
    }

    @Test
    void testSelectForNoKeyUpdate() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, description)
                    .from(tableCode)
                    .orderBy(id)
                    .forNoKeyUpdate()
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            assertThat(selectStatement.getSelectStatement())
                    .isEqualTo("select id, description from TableCode order by id for no key update");
            List<Map<String, Object>> records = mapper.selectManyMappedRows(selectStatement);
            assertThat(records).hasSize(4);
        }
    }

    @Test
    void testSelectForNoKeyUpdateNoWait() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, description)
                    .from(tableCode)
                    .orderBy(id)
                    .forNoKeyUpdate()
                    .nowait()
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            assertThat(selectStatement.getSelectStatement())
                    .isEqualTo("select id, description from TableCode order by id for no key update nowait");
            List<Map<String, Object>> records = mapper.selectManyMappedRows(selectStatement);
            assertThat(records).hasSize(4);
        }
    }

    @Test
    void testSelectForNoKeyUpdateSkipLocked() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, description)
                    .from(tableCode)
                    .orderBy(id)
                    .forNoKeyUpdate()
                    .skipLocked()
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            assertThat(selectStatement.getSelectStatement())
                    .isEqualTo("select id, description from TableCode order by id for no key update skip locked");
            List<Map<String, Object>> records = mapper.selectManyMappedRows(selectStatement);
            assertThat(records).hasSize(4);
        }
    }

    @Test
    void testSelectForShare() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, description)
                    .from(tableCode)
                    .orderBy(id)
                    .forShare()
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            assertThat(selectStatement.getSelectStatement())
                    .isEqualTo("select id, description from TableCode order by id for share");
            List<Map<String, Object>> records = mapper.selectManyMappedRows(selectStatement);
            assertThat(records).hasSize(4);
        }
    }

    @Test
    void testSelectForShareNoWait() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, description)
                    .from(tableCode)
                    .orderBy(id)
                    .forShare()
                    .nowait()
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            assertThat(selectStatement.getSelectStatement())
                    .isEqualTo("select id, description from TableCode order by id for share nowait");
            List<Map<String, Object>> records = mapper.selectManyMappedRows(selectStatement);
            assertThat(records).hasSize(4);
        }
    }

    @Test
    void testSelectForShareSkipLocked() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, description)
                    .from(tableCode)
                    .orderBy(id)
                    .forShare()
                    .skipLocked()
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            assertThat(selectStatement.getSelectStatement())
                    .isEqualTo("select id, description from TableCode order by id for share skip locked");
            List<Map<String, Object>> records = mapper.selectManyMappedRows(selectStatement);
            assertThat(records).hasSize(4);
        }
    }

    @Test
    void testSelectForKeyShare() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, description)
                    .from(tableCode)
                    .orderBy(id)
                    .forKeyShare()
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            assertThat(selectStatement.getSelectStatement())
                    .isEqualTo("select id, description from TableCode order by id for key share");
            List<Map<String, Object>> records = mapper.selectManyMappedRows(selectStatement);
            assertThat(records).hasSize(4);
        }
    }

    @Test
    void testSelectForKeyShareNoWait() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, description)
                    .from(tableCode)
                    .orderBy(id)
                    .forKeyShare()
                    .nowait()
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            assertThat(selectStatement.getSelectStatement())
                    .isEqualTo("select id, description from TableCode order by id for key share nowait");
            List<Map<String, Object>> records = mapper.selectManyMappedRows(selectStatement);
            assertThat(records).hasSize(4);
        }
    }

    @Test
    void testSelectForKeyShareSkipLocked() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, description)
                    .from(tableCode)
                    .orderBy(id)
                    .forKeyShare()
                    .skipLocked()
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            assertThat(selectStatement.getSelectStatement())
                    .isEqualTo("select id, description from TableCode order by id for key share skip locked");
            List<Map<String, Object>> records = mapper.selectManyMappedRows(selectStatement);
            assertThat(records).hasSize(4);
        }
    }
}
