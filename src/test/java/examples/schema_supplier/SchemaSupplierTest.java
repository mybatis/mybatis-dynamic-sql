/**
 *    Copyright 2016-2019 the original author or authors.
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
package examples.schema_supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.select.SelectDSLCompleter;

public class SchemaSupplierTest {

    private static final String JDBC_URL = "jdbc:hsqldb:mem:aname";
    private static final String JDBC_DRIVER = "org.hsqldb.jdbcDriver";

    private SqlSessionFactory sqlSessionFactory;

    @BeforeEach
    public void setup() throws Exception {
        Class.forName(JDBC_DRIVER);
        InputStream is = getClass().getResourceAsStream("/examples/schema_supplier/CreateDB.sql");
        try (Connection connection = DriverManager.getConnection(JDBC_URL, "sa", "")) {
            ScriptRunner sr = new ScriptRunner(connection);
            sr.setLogWriter(null);
            sr.runScript(new InputStreamReader(is));
        }

        UnpooledDataSource ds = new UnpooledDataSource(JDBC_DRIVER, JDBC_URL, "sa", "");
        Environment environment = new Environment("test", new JdbcTransactionFactory(), ds);
        Configuration config = new Configuration(environment);
        config.addMapper(UserMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(config);
    }

    @Test
    public void testUnsetSchemaProperty() {
        System.clearProperty(SchemaSupplier.schema_property);

        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);

            assertThrows(PersistenceException.class, () -> {
                User user = new User();
                user.setId(1);
                user.setName("Fred");

                int rows = mapper.insert(user);

                assertThat(rows).isEqualTo(1);
            });
        }
    }

    @Test
    public void testSchemaProperty() {
        System.setProperty(SchemaSupplier.schema_property, "schema1");

        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);

            insertFlintstones(mapper);
            
            List<User> records = mapper.select(SelectDSLCompleter.allRows());
            assertThat(records.size()).isEqualTo(2);
        }
    }

    @Test
    public void testSchemaSwitchingProperty() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);

            System.setProperty(SchemaSupplier.schema_property, "schema1");
            insertFlintstones(mapper);
            
            List<User> records = mapper.select(SelectDSLCompleter.allRows());
            assertThat(records.size()).isEqualTo(2);

            
            System.setProperty(SchemaSupplier.schema_property, "schema2");
            insertRubbles(mapper);
            
            records = mapper.select(SelectDSLCompleter.allRows());
            assertThat(records.size()).isEqualTo(3);
        }
    }

    private void insertFlintstones(UserMapper mapper) {
        User user = new User();
        user.setId(1);
        user.setName("Fred");
        int rows = mapper.insert(user);
        assertThat(rows).isEqualTo(1);

        user = new User();
        user.setId(2);
        user.setName("Wilma");
        rows = mapper.insert(user);
        assertThat(rows).isEqualTo(1);
    }

    private void insertRubbles(UserMapper mapper) {
        User user = new User();
        user.setId(1);
        user.setName("Barney");
        int rows = mapper.insert(user);
        assertThat(rows).isEqualTo(1);

        user = new User();
        user.setId(2);
        user.setName("Betty");
        rows = mapper.insert(user);
        assertThat(rows).isEqualTo(1);

        user = new User();
        user.setId(3);
        user.setName("Bamm Bamm");
        rows = mapper.insert(user);
        assertThat(rows).isEqualTo(1);
    }
}
