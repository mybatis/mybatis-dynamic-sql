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
package issues.gh324;

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

public class NameService {
    private static final String JDBC_URL = "jdbc:hsqldb:mem:aname";
    private static final String JDBC_DRIVER = "org.hsqldb.jdbcDriver";

    private final SqlSessionFactory sqlSessionFactory;

    public NameService() {
        try {
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void insertRecord() {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            NameTableMapper mapper = session.getMapper(NameTableMapper.class);
            NameRecord record = new NameRecord();
            record.setId(1);
            record.setName("Fred");
            mapper.insert(record);
        }
    }

    public void updateRecordWithAutoCommit() {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            NameTableMapper mapper = session.getMapper(NameTableMapper.class);
            NameRecord record = new NameRecord();
            record.setId(1);
            record.setName("Barney");
            mapper.updateByPrimaryKey(record);
        }
    }

    public void updateRecordWithoutAutoCommitAndNoExplicitCommit() {
        // this should rollback
        try (SqlSession session = sqlSessionFactory.openSession()) {
            NameTableMapper mapper = session.getMapper(NameTableMapper.class);
            NameRecord record = new NameRecord();
            record.setId(1);
            record.setName("Barney");
            mapper.updateByPrimaryKey(record);
        }
    }

    public void updateRecordWithoutAutoCommitAndExplicitCommit() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            NameTableMapper mapper = session.getMapper(NameTableMapper.class);
            NameRecord record = new NameRecord();
            record.setId(1);
            record.setName("Barney");
            mapper.updateByPrimaryKey(record);
            session.commit();
        }
    }

    public Optional<NameRecord> getRecord() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            NameTableMapper mapper = session.getMapper(NameTableMapper.class);
            return mapper.selectByPrimaryKey(1);
        }
    }

    public void resetDatabase() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            NameTableMapper mapper = session.getMapper(NameTableMapper.class);
            mapper.deleteAll();
        }
    }
}
