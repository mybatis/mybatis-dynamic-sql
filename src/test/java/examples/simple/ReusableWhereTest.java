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
package examples.simple;

import static examples.simple.PersonDynamicSqlSupport.id;
import static examples.simple.PersonDynamicSqlSupport.occupation;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;
import static org.mybatis.dynamic.sql.SqlBuilder.isNull;

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
import org.mybatis.dynamic.sql.where.WhereApplier;

class ReusableWhereTest {

    private static final String JDBC_URL = "jdbc:hsqldb:mem:aname";
    private static final String JDBC_DRIVER = "org.hsqldb.jdbcDriver";

    private SqlSessionFactory sqlSessionFactory;

    @BeforeEach
    void setup() throws Exception {
        Class.forName(JDBC_DRIVER);
        InputStream is = getClass().getResourceAsStream("/examples/simple/CreateSimpleDB.sql");
        try (Connection connection = DriverManager.getConnection(JDBC_URL, "sa", "")) {
            ScriptRunner sr = new ScriptRunner(connection);
            sr.setLogWriter(null);
            sr.runScript(new InputStreamReader(is));
        }

        UnpooledDataSource ds = new UnpooledDataSource(JDBC_DRIVER, JDBC_URL, "sa", "");
        Environment environment = new Environment("test", new JdbcTransactionFactory(), ds);
        Configuration config = new Configuration(environment);
        config.addMapper(PersonMapper.class);
        config.addMapper(PersonWithAddressMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(config);
    }

    @Test
    void testCount() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);

            long rows = mapper.count(c -> c.applyWhere(commonWhere));

            assertThat(rows).isEqualTo(3);
        }
    }

    @Test
    void testDelete() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);

            int rows = mapper.delete(c -> c.applyWhere(commonWhere));

            assertThat(rows).isEqualTo(3);
        }
    }

    @Test
    void testSelect() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);

            List<PersonRecord> rows = mapper.select(c ->
                c.applyWhere(commonWhere)
                .orderBy(id));

            assertThat(rows).hasSize(3);
        }
    }

    @Test
    void testUpdate() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);

            int rows = mapper.update(c ->
                c.set(occupation).equalToStringConstant("worker")
                .applyWhere(commonWhere));

            assertThat(rows).isEqualTo(3);
        }
    }

    private WhereApplier commonWhere = d -> d.where(id, isEqualTo(1)).or(occupation, isNull());
}
