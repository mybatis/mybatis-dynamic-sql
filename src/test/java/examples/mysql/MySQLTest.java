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
package examples.mysql;

import static examples.mysql.MemberOfCondition.memberOf;
import static examples.mysql.MemberOfFunction.memberOf;
import static examples.mariadb.ItemsDynamicSQLSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.util.List;
import java.util.Map;

import config.TestContainersConfiguration;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.mybatis3.CommonSelectMapper;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class MySQLTest {

    @SuppressWarnings("resource")
    @Container
    private static final MySQLContainer<?> mysql =
            new MySQLContainer<>(TestContainersConfiguration.MYSQL_LATEST)
                    .withInitScript("examples/mariadb/CreateDB.sql");

    private static SqlSessionFactory sqlSessionFactory;

    @BeforeAll
    static void setup() {
        UnpooledDataSource ds = new UnpooledDataSource(mysql.getDriverClassName(), mysql.getJdbcUrl(),
                mysql.getUsername(), mysql.getPassword());
        Environment environment = new Environment("test", new JdbcTransactionFactory(), ds);
        Configuration config = new Configuration(environment);
        config.addMapper(CommonSelectMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(config);
    }

    @Test
    void smokeTest() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, description)
                    .from(items)
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
             List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);
             assertThat(rows).hasSize(20);
        }
    }

    @Test
    void testMemberOfAsCondition() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, memberOf(id, "'[1, 2, 3]'").as("inList"))
                    .from(items)
                    .where(id, memberOf("'[1, 2, 3]'"))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            assertThat(selectStatement.getSelectStatement())
                    .isEqualTo("select id, id member of('[1, 2, 3]') as inList from items where id member of('[1, 2, 3]') order by id");

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);
            assertThat(rows).hasSize(3);
            assertThat(rows.get(2)).containsOnly(entry("id", 3), entry("inList", 1L));
        }
    }

    @Test
    void testMemberOfAsFunction() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, memberOf(id, "'[1, 2, 3]'").as("inList"))
                    .from(items)
                    .where(memberOf(id,"'[1, 2, 3]'"), isEqualTo(1L))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            assertThat(selectStatement.getSelectStatement())
                    .isEqualTo("select id, id member of('[1, 2, 3]') as inList from items where id member of('[1, 2, 3]') = #{parameters.p1} order by id");

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);
            assertThat(rows).hasSize(3);
            assertThat(rows.get(2)).containsOnly(entry("id", 3), entry("inList", 1L));
        }
    }

    @Test
    void testIsLikeEscape() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);

            SelectStatementProvider selectStatement = select(id, description)
                    .from(items)
                    .where(description, IsLikeEscape.isLike("Item 1%", "#").map(s -> s))
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            assertThat(selectStatement.getSelectStatement())
                    .isEqualTo("select id, description from items where description like #{parameters.p1,jdbcType=VARCHAR} ESCAPE '#' order by id");

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);
            assertThat(rows).hasSize(11);
            assertThat(rows.get(2)).containsOnly(entry("id", 11), entry("description", "Item 11"));
        }
    }
}
