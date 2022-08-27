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
package examples.array;

import static examples.array.NamesTableDynamicSqlSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Optional;

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

class ArrayTest {
    private static final String JDBC_URL = "jdbc:hsqldb:mem:aname";
    private static final String JDBC_DRIVER = "org.hsqldb.jdbcDriver";

    private SqlSessionFactory sqlSessionFactory;

    @BeforeEach
    void setup() throws Exception {
        Class.forName(JDBC_DRIVER);
        InputStream is = getClass().getResourceAsStream("/examples/array/CreateDB.sql");
        try (Connection connection = DriverManager.getConnection(JDBC_URL, "sa", "")) {
            ScriptRunner sr = new ScriptRunner(connection);
            sr.setLogWriter(null);
            sr.runScript(new InputStreamReader(is));
        }

        UnpooledDataSource ds = new UnpooledDataSource(JDBC_DRIVER, JDBC_URL, "sa", "");
        Environment environment = new Environment("test", new JdbcTransactionFactory(), ds);
        Configuration config = new Configuration(environment);
        config.addMapper(NamesTableMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(config);
    }

    @Test
    void testInsertSelectById() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            NamesTableMapper mapper = sqlSession.getMapper(NamesTableMapper.class);

            String[] someNames = {"Fred", "Wilma", "Pebbles"};

            GeneralInsertStatementProvider insertStatement = insertInto(namesTable)
                    .set(id).toValue(1)
                    .set(names).toValue(someNames)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            int rows = mapper.generalInsert(insertStatement);
            assertThat(rows).isEqualTo(1);

            SelectStatementProvider selectStatement = select(id, NamesTableDynamicSqlSupport.names)
                    .from(namesTable)
                    .where(id, isEqualTo(1))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            Optional<NamesRecord> record = mapper.selectOne(selectStatement);
            assertThat(record).hasValueSatisfying( r -> {
                assertThat(r.getId()).isEqualTo(1);
                assertThat(r.getNames()).isEqualTo(someNames);
            });
        }
    }

    @Test
    void testInsertSelectByArray() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            NamesTableMapper mapper = sqlSession.getMapper(NamesTableMapper.class);

            String[] someNames = {"Fred", "Wilma", "Pebbles"};

            GeneralInsertStatementProvider insertStatement = insertInto(namesTable)
                    .set(id).toValue(1)
                    .set(names).toValue(someNames)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            int rows = mapper.generalInsert(insertStatement);
            assertThat(rows).isEqualTo(1);

            SelectStatementProvider selectStatement = select(id, NamesTableDynamicSqlSupport.names)
                    .from(namesTable)
                    .where(names, isEqualTo(someNames))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            Optional<NamesRecord> record = mapper.selectOne(selectStatement);
            assertThat(record).hasValueSatisfying( r -> {
                assertThat(r.getId()).isEqualTo(1);
                assertThat(r.getNames()).isEqualTo(someNames);
            });
        }
    }
}
