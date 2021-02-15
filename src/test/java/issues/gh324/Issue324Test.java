/*
 *    Copyright 2016-2021 the original author or authors.
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
package issues.gh324;

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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class Issue324Test {
    private static final String JDBC_URL = "jdbc:hsqldb:mem:aname";
    private static final String JDBC_DRIVER = "org.hsqldb.jdbcDriver";

    private SqlSessionFactory sqlSessionFactory;

    @BeforeEach
    void setup() throws Exception {
        Class.forName(JDBC_DRIVER);
        InputStream is = getClass().getResourceAsStream("/issues/gh324/CreateDB.sql");
        try (Connection connection = DriverManager.getConnection(JDBC_URL, "sa", "")) {
            ScriptRunner sr = new ScriptRunner(connection);
            sr.setLogWriter(null);
            sr.runScript(new InputStreamReader(is));
        }

        UnpooledDataSource ds = new UnpooledDataSource(JDBC_DRIVER, JDBC_URL, "sa", "");
        Environment environment = new Environment("test", new JdbcTransactionFactory(), ds);
        Configuration config = new Configuration(environment);
        config.addMapper(NameTableMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(config);
    }

    @Test
    void testCacheWithAutoCommitOnUpdate() {
        insertRecord();
        Optional<NameRecord> returnedRecord = getRecord();
        assertThat(returnedRecord).hasValueSatisfying(rr -> {
            assertThat(rr.getId()).isEqualTo(1);
            assertThat(rr.getName()).isEqualTo("Fred");
        });

        updateRecordWithAutoCommit();
        returnedRecord = getRecord();
        assertThat(returnedRecord).hasValueSatisfying(rr -> {
            assertThat(rr.getId()).isEqualTo(1);
            assertThat(rr.getName()).isEqualTo("Barney");
        });
    }

    @Test
    void testCacheWithNoAutoCommitOnUpdateAndNoExplicitCommit() {
        insertRecord();
        Optional<NameRecord> returnedRecord = getRecord();
        assertThat(returnedRecord).hasValueSatisfying(rr -> {
            assertThat(rr.getId()).isEqualTo(1);
            assertThat(rr.getName()).isEqualTo("Fred");
        });

        // the update should rollback
        updateRecordWithoutAutoCommitAndNoExplicitCommit();
        returnedRecord = getRecord();
        assertThat(returnedRecord).hasValueSatisfying(rr -> {
            assertThat(rr.getId()).isEqualTo(1);
            assertThat(rr.getName()).isEqualTo("Fred");
        });
    }

    @Test
    void testCacheWithNoAutoCommitOnUpdateAndExplicitCommit() {
        insertRecord();
        Optional<NameRecord> returnedRecord = getRecord();
        assertThat(returnedRecord).hasValueSatisfying(rr -> {
            assertThat(rr.getId()).isEqualTo(1);
            assertThat(rr.getName()).isEqualTo("Fred");
        });

        updateRecordWithoutAutoCommitAndExplicitCommit();
        returnedRecord = getRecord();
        assertThat(returnedRecord).hasValueSatisfying(rr -> {
            assertThat(rr.getId()).isEqualTo(1);
            assertThat(rr.getName()).isEqualTo("Barney");
        });
    }

    private void insertRecord() {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            NameTableMapper mapper = session.getMapper(NameTableMapper.class);
            NameRecord record = new NameRecord();
            record.setId(1);
            record.setName("Fred");
            mapper.insert(record);
        }
    }

    private void updateRecordWithAutoCommit() {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            NameTableMapper mapper = session.getMapper(NameTableMapper.class);
            NameRecord record = new NameRecord();
            record.setId(1);
            record.setName("Barney");
            mapper.updateByPrimaryKey(record);
        }
    }

    private void updateRecordWithoutAutoCommitAndNoExplicitCommit() {
        // this should rollback
        try (SqlSession session = sqlSessionFactory.openSession()) {
            NameTableMapper mapper = session.getMapper(NameTableMapper.class);
            NameRecord record = new NameRecord();
            record.setId(1);
            record.setName("Barney");
            mapper.updateByPrimaryKey(record);
        }
    }

    private void updateRecordWithoutAutoCommitAndExplicitCommit() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            NameTableMapper mapper = session.getMapper(NameTableMapper.class);
            NameRecord record = new NameRecord();
            record.setId(1);
            record.setName("Barney");
            mapper.updateByPrimaryKey(record);
            session.commit();
        }
    }

    private Optional<NameRecord> getRecord() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            NameTableMapper mapper = session.getMapper(NameTableMapper.class);
            return mapper.selectByPrimaryKey(1);
        }
    }
}
