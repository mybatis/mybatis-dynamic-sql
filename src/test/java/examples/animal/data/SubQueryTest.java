/*
 *    Copyright 2016-2020 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
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
            SelectStatementProvider selectStatement = select(count())
                    .from(
                            select(animalData.allColumns())
                            .from(animalData)
                            .where(id, isLessThan(22))
                    )
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            assertThat(selectStatement.getSelectStatement()).isEqualTo(
                    "select count(*) " +
                            "from (select * from AnimalData where id < #{parameters.p1,jdbcType=INTEGER})"
            );
            assertThat(selectStatement.getParameters()).containsEntry("p1", 22);

            int rows = mapper.selectOneInteger(selectStatement);
            assertThat(rows).isEqualTo(21);
        }
    }

    @Test
    void testBasicSubQueryWithAlias() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = sqlSession.getMapper(CommonSelectMapper.class);
            SelectStatementProvider selectStatement = select(count())
                    .from(
                            select(animalData.allColumns())
                                    .from(animalData)
                                    .where(id, isLessThan(22)),
                            "a"
                    )
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            assertThat(selectStatement.getSelectStatement()).isEqualTo(
                    "select count(*) " +
                            "from (select * from AnimalData where id < #{parameters.p1,jdbcType=INTEGER}) a"
            );
            assertThat(selectStatement.getParameters()).containsEntry("p1", 22);

            int rows = mapper.selectOneInteger(selectStatement);
            assertThat(rows).isEqualTo(21);
        }
    }
}
