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
package examples.simple;

import static examples.simple.CompoundKeyDynamicSqlSupport.compoundKey;
import static examples.simple.CompoundKeyDynamicSqlSupport.id1;
import static examples.simple.CompoundKeyDynamicSqlSupport.id2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.insert;
import static org.mybatis.dynamic.sql.SqlBuilder.insertBatch;
import static org.mybatis.dynamic.sql.SqlBuilder.insertMultiple;
import static org.mybatis.dynamic.sql.SqlBuilder.select;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.insert.render.BatchInsert;
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider;
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;

class MyBatisMapToRowTest {
    private static final String JDBC_URL = "jdbc:hsqldb:mem:aname";
    private static final String JDBC_DRIVER = "org.hsqldb.jdbcDriver";
    private SqlSessionFactory sqlSessionFactory;

    @BeforeEach
    void setup() throws Exception {
        Class.forName(JDBC_DRIVER);
        InputStream is = getClass().getResourceAsStream("/examples/simple/CreateSimpleDB.sql");
        assert is != null;
        try (Connection connection = DriverManager.getConnection(JDBC_URL, "sa", "")) {
            ScriptRunner sr = new ScriptRunner(connection);
            sr.setLogWriter(null);
            sr.runScript(new InputStreamReader(is));
        }

        UnpooledDataSource ds = new UnpooledDataSource(JDBC_DRIVER, JDBC_URL, "sa", "");
        Environment environment = new Environment("test", new JdbcTransactionFactory(), ds);
        Configuration config = new Configuration(environment);
        config.addMapper(CompoundKeyMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(config);
    }

    @Test
    void testInsertOne() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CompoundKeyMapper mapper = session.getMapper(CompoundKeyMapper.class);

            Integer i = 1;

            InsertStatementProvider<Integer> insertStatement = insert(i)
                    .into(compoundKey)
                    .map(id1).toConstant("22")
                    .map(id2).toRow()
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "insert into CompoundKey (id1, id2) values (22, #{row,jdbcType=INTEGER})";
            assertThat(insertStatement.getInsertStatement()).isEqualTo(expected);

            int rows = mapper.insert(insertStatement);
            assertThat(rows).isEqualTo(1);

            SelectStatementProvider selectStatement = select(id1, id2)
                    .from(compoundKey)
                    .orderBy(id1, id2)
                    .build().render(RenderingStrategies.MYBATIS3);

            List<CompoundKeyRow> records = mapper.selectMany(selectStatement, this::mapRow);
            assertThat(records).hasSize(1);
        }
    }

    @Test
    void testInsertMultiple() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CompoundKeyMapper mapper = session.getMapper(CompoundKeyMapper.class);

            List<Integer> integers = List.of(1, 2, 3);

            MultiRowInsertStatementProvider<Integer> insertStatement = insertMultiple(integers)
                    .into(compoundKey)
                    .map(id1).toConstant("22")
                    .map(id2).toRow()
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "insert into CompoundKey (id1, id2) values (22, #{records[0],jdbcType=INTEGER}), (22, #{records[1],jdbcType=INTEGER}), (22, #{records[2],jdbcType=INTEGER})";
            assertThat(insertStatement.getInsertStatement()).isEqualTo(expected);

            int rows = mapper.insertMultiple(insertStatement);
            assertThat(rows).isEqualTo(3);

            SelectStatementProvider selectStatement = select(id1, id2)
                    .from(compoundKey)
                    .orderBy(id1, id2)
                    .build().render(RenderingStrategies.MYBATIS3);

            List<CompoundKeyRow> records = mapper.selectMany(selectStatement, this::mapRow);
            assertThat(records).hasSize(3);
        }
    }

    @Test
    void testInsertBatch() {
        try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            CompoundKeyMapper mapper = session.getMapper(CompoundKeyMapper.class);

            List<Integer> integers = List.of(1, 2, 3);

            BatchInsert<Integer> insertStatement = insertBatch(integers)
                    .into(compoundKey)
                    .map(id1).toConstant("22")
                    .map(id2).toRow()
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "insert into CompoundKey (id1, id2) values (22, #{row,jdbcType=INTEGER})";
            assertThat(insertStatement.getInsertStatementSQL()).isEqualTo(expected);

            insertStatement.insertStatements().forEach(mapper::insert);

            List<BatchResult> batchResults = mapper.flush();

            int rows = batchResults.stream()
                    .map(BatchResult::getUpdateCounts)
                    .flatMapToInt(IntStream::of)
                    .sum();
            assertThat(rows).isEqualTo(3);

            SelectStatementProvider selectStatement = select(id1, id2)
                    .from(compoundKey)
                    .orderBy(id1, id2)
                    .build().render(RenderingStrategies.MYBATIS3);

            List<CompoundKeyRow> records = mapper.selectMany(selectStatement, this::mapRow);
            assertThat(records).hasSize(3);
        }
    }

    private CompoundKeyRow mapRow(Map<String, Object> map) {
        CompoundKeyRow answer = new CompoundKeyRow();
        answer.setId1((Integer) map.get("ID1"));
        answer.setId2((Integer) map.get("ID2"));
        return answer;
    }
}
