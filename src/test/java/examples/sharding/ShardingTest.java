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
package examples.sharding;

import static examples.sharding.TableCodesDynamicSqlSupport.tableCodes;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.countFrom;
import static org.mybatis.dynamic.sql.SqlBuilder.insertInto;
import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;
import static org.mybatis.dynamic.sql.SqlBuilder.select;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;

import examples.sharding.TableCodesDynamicSqlSupport.TableCodes;
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
import org.mybatis.dynamic.sql.insert.render.GeneralInsertStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;

class ShardingTest {
    private static final String JDBC_URL = "jdbc:hsqldb:mem:aname";
    private static final String JDBC_DRIVER = "org.hsqldb.jdbcDriver";
    private final Map<String, TableCodes> shards = new HashMap<>();

    private SqlSessionFactory sqlSessionFactory;

    @BeforeEach
    void setup() throws Exception {
        Class.forName(JDBC_DRIVER);
        InputStream is = getClass().getResourceAsStream("/examples/sharding/ShardingDB.sql");
        assert is != null;
        try (Connection connection = DriverManager.getConnection(JDBC_URL, "sa", "")) {
            ScriptRunner sr = new ScriptRunner(connection);
            sr.setLogWriter(null);
            sr.runScript(new InputStreamReader(is));
        }

        UnpooledDataSource ds = new UnpooledDataSource(JDBC_DRIVER, JDBC_URL, "sa", "");
        Environment environment = new Environment("test", new JdbcTransactionFactory(), ds);
        Configuration config = new Configuration(environment);
        config.addMapper(ShardedMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(config);
    }

    @Test
    void testShardedSelect() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            ShardedMapper mapper = sqlSession.getMapper(ShardedMapper.class);
            TableCodes table = calculateTable(1);

            SelectStatementProvider selectStatement = select(table.description)
                    .from(table)
                    .where(table.id, isEqualTo(1))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            assertThat(selectStatement.getSelectStatement())
                    .isEqualTo("select description from tableCodes_odd where id = #{parameters.p1,jdbcType=INTEGER}");

            String description = mapper.selectOneString(selectStatement);
            assertThat(description).isNull();
        }
    }

    @Test
    void testShardedInserts() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            ShardedMapper mapper = sqlSession.getMapper(ShardedMapper.class);

            mapper.generalInsert(buildInsert(1, "Description 1"));
            mapper.generalInsert(buildInsert(2, "Description 2"));
            mapper.generalInsert(buildInsert(3, "Description 3"));
            mapper.generalInsert(buildInsert(4, "Description 4"));
            mapper.generalInsert(buildInsert(5, "Description 5"));
            mapper.generalInsert(buildInsert(6, "Description 6"));
            mapper.generalInsert(buildInsert(7, "Description 7"));

            TableCodes oddTable = calculateTable(1);
            SelectStatementProvider oddCountStatement = countFrom(oddTable)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            assertThat(oddCountStatement.getSelectStatement()).isEqualTo("select count(*) from tableCodes_odd");
            long oddRows = mapper.count(oddCountStatement);
            assertThat(oddRows).isEqualTo(4L);

            TableCodes evenTable = calculateTable(2);
            SelectStatementProvider evenCountStatement = countFrom(evenTable)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            assertThat(evenCountStatement.getSelectStatement()).isEqualTo("select count(*) from tableCodes_even");
            long evenRows = mapper.count(evenCountStatement);
            assertThat(evenRows).isEqualTo(3L);
        }
    }

    @Test
    void testShardedSelects() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            ShardedMapper mapper = sqlSession.getMapper(ShardedMapper.class);

            mapper.generalInsert(buildInsert(1, "Description 1"));
            mapper.generalInsert(buildInsert(2, "Description 2"));

            assertThat(mapper.selectOneString(buildSelect(1))).isEqualTo("Description 1");
            assertThat(mapper.selectOneString(buildSelect(2))).isEqualTo("Description 2");
            assertThat(mapper.selectOneString(buildSelect(3))).isNull();
            assertThat(mapper.selectOneString(buildSelect(4))).isNull();
        }
    }

    private GeneralInsertStatementProvider buildInsert(int id, String description) {
        TableCodesDynamicSqlSupport.TableCodes table = calculateTable(id);
        return insertInto(table)
                .set(table.id).toValue(id)
                .set(table.description).toValue(description)
                .build()
                .render(RenderingStrategies.MYBATIS3);
    }

    private SelectStatementProvider buildSelect(int id) {
        TableCodesDynamicSqlSupport.TableCodes table = calculateTable(id);
        return select(table.description)
                .from(table)
                .where(table.id, isEqualTo(id))
                .build()
                .render(RenderingStrategies.MYBATIS3);
    }

    private TableCodes calculateTable(int id) {
        if (id % 2 == 0) {
            return shards.computeIfAbsent("even", k -> tableCodes); // tableCodes_even is the default
        } else {
            return shards.computeIfAbsent("odd", k -> tableCodes.withName("tableCodes_odd"));
        }
    }
}
