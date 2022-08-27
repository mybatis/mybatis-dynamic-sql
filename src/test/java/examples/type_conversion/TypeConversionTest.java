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
package examples.type_conversion;

import static examples.type_conversion.MyFilesDynamicSqlSupport.*;
import static examples.type_conversion.ToBase64.toBase64;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Base64;
import java.util.Map;
import java.util.Random;

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

class TypeConversionTest {

    private static final String JDBC_URL = "jdbc:hsqldb:mem:aname";
    private static final String JDBC_DRIVER = "org.hsqldb.jdbcDriver";

    private SqlSessionFactory sqlSessionFactory;

    @BeforeEach
    void setup() throws Exception {
        Class.forName(JDBC_DRIVER);
        InputStream is = getClass().getResourceAsStream("/examples/type_conversion/CreateDB.sql");
        try (Connection connection = DriverManager.getConnection(JDBC_URL, "sa", "")) {
            ScriptRunner sr = new ScriptRunner(connection);
            sr.setLogWriter(null);
            sr.runScript(new InputStreamReader(is));
        }

        UnpooledDataSource ds = new UnpooledDataSource(JDBC_DRIVER, JDBC_URL, "sa", "");
        Environment environment = new Environment("test", new JdbcTransactionFactory(), ds);
        Configuration config = new Configuration(environment);
        config.addMapper(MyFilesMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(config);
    }

    @Test
    void testFunctionInSelect() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            MyFilesMapper mapper = session.getMapper(MyFilesMapper.class);

            Random random = new Random();
            byte[] randomBlob = new byte[1024];
            random.nextBytes(randomBlob);

            GeneralInsertStatementProvider insertStatement = insertInto(myfiles)
                    .set(fileId).toValue(1)
                    .set(fileContents).toValue(randomBlob)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            int rows = mapper.insert(insertStatement);
            assertThat(rows).isEqualTo(1);

            SelectStatementProvider selectStatement = select(fileId, fileContents)
                    .from(myfiles)
                    .where(fileId, isEqualTo(1))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            Map<String, Object> row = mapper.generalSelect(selectStatement);
            assertThat(row).containsExactly(entry("FILE_ID", 1), entry("FILE_CONTENTS", randomBlob));

            selectStatement = select(fileId, toBase64(fileContents).as("checksum"))
                    .from(myfiles)
                    .where(fileId, isEqualTo(1))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select file_id, TO_BASE64(file_contents) as checksum from MyFiles "
                    + "where file_id = #{parameters.p1,jdbcType=INTEGER}";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);

            row = mapper.generalSelect(selectStatement);

            String base64 = Base64.getEncoder().encodeToString(randomBlob);
            assertThat(row).contains(entry("FILE_ID", 1), entry("CHECKSUM", base64));
        }
    }

    @Test
    void testFunctionInWhere() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            MyFilesMapper mapper = session.getMapper(MyFilesMapper.class);

            Random random = new Random();
            byte[] randomBlob = new byte[1024];
            random.nextBytes(randomBlob);

            GeneralInsertStatementProvider insertStatement = insertInto(myfiles)
                    .set(fileId).toValue(1)
                    .set(fileContents).toValue(randomBlob)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            int rows = mapper.insert(insertStatement);
            assertThat(rows).isEqualTo(1);

            SelectStatementProvider selectStatement = select(fileId, fileContents)
                    .from(myfiles)
                    .where(fileId, isEqualTo(1))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            Map<String, Object> row = mapper.generalSelect(selectStatement);
            assertThat(row).contains(entry("FILE_ID", 1), entry("FILE_CONTENTS", randomBlob));

            String base64 = Base64.getEncoder().encodeToString(randomBlob);
            selectStatement = select(fileId, fileContents)
                    .from(myfiles)
                    .where(toBase64(fileContents), isEqualTo(base64))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select file_id, file_contents from MyFiles "
                    + "where TO_BASE64(file_contents) = #{parameters.p1,jdbcType=VARCHAR}";
            assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);

            row = mapper.generalSelect(selectStatement);

            assertThat(row).contains(entry("FILE_ID", 1), entry("FILE_CONTENTS", randomBlob));
        }
    }
}
