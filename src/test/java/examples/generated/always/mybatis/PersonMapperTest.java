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
package examples.generated.always.mybatis;

import static examples.generated.always.mybatis.PersonDynamicSqlSupport.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
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
import org.mybatis.dynamic.sql.SqlBuilder;
import org.mybatis.dynamic.sql.insert.render.InsertSelectStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategies;

import examples.generated.always.PersonRecord;

class PersonMapperTest {

    private static final String JDBC_URL = "jdbc:hsqldb:mem:aname";
    private static final String JDBC_DRIVER = "org.hsqldb.jdbcDriver";

    private SqlSessionFactory sqlSessionFactory;

    @BeforeEach
    void setup() throws Exception {
        Class.forName(JDBC_DRIVER);
        InputStream is = getClass().getResourceAsStream("/examples/generated/always/CreateGeneratedAlwaysDB.sql");
        try (Connection connection = DriverManager.getConnection(JDBC_URL, "sa", "")) {
            ScriptRunner sr = new ScriptRunner(connection);
            sr.setLogWriter(null);
            sr.runScript(new InputStreamReader(is));
        }

        UnpooledDataSource ds = new UnpooledDataSource(JDBC_DRIVER, JDBC_URL, "sa", "");
        Environment environment = new Environment("test", new JdbcTransactionFactory(), ds);
        Configuration config = new Configuration(environment);
        config.addMapper(PersonMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(config);
    }

    @Test
    void testInsertSelectWithOneRecord() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);

            PersonRecord record = new PersonRecord();
            record.setFirstName("Fred");
            record.setLastName("Flintstone");

            int rows = mapper.insert(record);

            assertThat(rows).isEqualTo(1);
            assertThat(record.getId()).isEqualTo(22);

            InsertSelectStatementProvider insertSelectStatement = SqlBuilder.insertInto(person)
                    .withColumnList(firstName, lastName)
                    .withSelectStatement(SqlBuilder.select(firstName, lastName).from(person))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            rows = mapper.insertSelect(insertSelectStatement);
            assertThat(rows).isEqualTo(1);
            assertThat(insertSelectStatement.getParameters()).containsEntry("id", 23);

            List<PersonRecord> records = mapper.select(c -> c.orderBy(id));
            assertThat(records).hasSize(2);
            assertThat(records.get(0).getId()).isEqualTo(22);
            assertThat(records.get(1).getId()).isEqualTo(23);
        }
    }

    @Test
    void testInsertSelectWithMultipleRecords() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);

            PersonRecord record1 = new PersonRecord();
            record1.setFirstName("Fred");
            record1.setLastName("Flintstone");

            PersonRecord record2 = new PersonRecord();
            record2.setFirstName("Barney");
            record2.setLastName("Rubble");

            int rows = mapper.insertMultiple(record1, record2);

            assertThat(rows).isEqualTo(2);
            assertThat(record1.getId()).isEqualTo(22);
            assertThat(record2.getId()).isEqualTo(23);

            InsertSelectStatementProvider insertSelectStatement = SqlBuilder.insertInto(person)
                    .withColumnList(firstName, lastName)
                    .withSelectStatement(SqlBuilder.select(firstName, lastName).from(person))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            insertSelectStatement.getParameters().put("keys", new ArrayList<GeneratedKey>());

            GeneratedKeyList keys = new GeneratedKeyList(5);

            rows = mapper.insertSelect(insertSelectStatement, keys);
            assertThat(rows).isEqualTo(2);
            assertThat(keys.get(0).getKey()).isEqualTo(24);
            assertThat(keys.get(1).getKey()).isEqualTo(25);

            List<PersonRecord> records = mapper.select(c -> c.orderBy(id));
            assertThat(records).hasSize(4);
            assertThat(records.get(0).getId()).isEqualTo(22);
            assertThat(records.get(1).getId()).isEqualTo(23);
            assertThat(records.get(2).getId()).isEqualTo(24);
            assertThat(records.get(3).getId()).isEqualTo(25);
        }
    }
}
