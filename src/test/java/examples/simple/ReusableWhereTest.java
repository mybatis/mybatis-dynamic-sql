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

import static examples.simple.PersonDynamicSqlSupport.id;
import static examples.simple.PersonDynamicSqlSupport.occupation;
import static examples.simple.PersonDynamicSqlSupport.person;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;
import static org.mybatis.dynamic.sql.SqlBuilder.isLessThan;
import static org.mybatis.dynamic.sql.SqlBuilder.isNull;
import static org.mybatis.dynamic.sql.SqlBuilder.select;
import static org.mybatis.dynamic.sql.SqlBuilder.where;

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
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.QueryExpressionModel;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.select.SubQuery;
import org.mybatis.dynamic.sql.select.aggregate.CountAll;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.where.WhereApplier;

class ReusableWhereTest {

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

    @Test
    void testTransformToCount() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);

            SelectModel selectModel = select(PersonMapper.selectList)
                    .from(person)
                    .where(id, isLessThan(5))
                    .limit(2)
                    .build();

            SelectStatementProvider selectStatement = selectModel.render(RenderingStrategies.MYBATIS3);

            assertThat(selectStatement.getSelectStatement()).isEqualTo(
                    "select id as A_ID, first_name, last_name, birth_date, employed, occupation, address_id from Person where id < #{parameters.p1,jdbcType=INTEGER} limit #{parameters.p2}");
            assertThat(selectStatement.getParameters()).containsOnly(entry("p1", 5), entry("p2", 2L));

            SelectModel countModel = toCount(selectModel);
            SelectStatementProvider countStatement = countModel.render(RenderingStrategies.MYBATIS3);

            assertThat(countStatement.getSelectStatement()).isEqualTo(
                    "select count(*) from (select id as A_ID, first_name, last_name, birth_date, employed, occupation, address_id from Person where id < #{parameters.p1,jdbcType=INTEGER})");
            assertThat(countStatement.getParameters()).containsOnly(entry("p1", 5));

            long count = mapper.count(countStatement);

            assertThat(count).isEqualTo(4);
        }

    }

    private final WhereApplier commonWhere = where(id, isEqualTo(1)).or(occupation, isNull()).toWhereApplier();

    /**
     * This function transforms a select statement into a count statement by wrapping the select statement into
     * a subquery. This can be used to create a single select statement and use it for both selects and counts
     * in a paging scenario. This is more appropriate than a reusable where clause if the query is complex. For simple
     * queries, a reusable where clause is best.
     *
     * <p>This function will strip any paging configuration, waits, order bys, etc. from the top level query. This
     * will allow usage of a paging query for selects, and the transformed query for a count of all rows.
     *
     * @param selectModel the select model to transform
     * @return a new select model that is "select count(*) from (subquery)" where subquery is the input select statement
     */
    static SelectModel toCount(SelectModel selectModel) {
        // remove any paging configuration, order by, wait clause, etc. from the incoming select model
        SelectModel strippedSelectModel = SelectModel.withQueryExpressions(selectModel.queryExpressions().toList())
                .withStatementConfiguration(selectModel.statementConfiguration())
                .build();

        QueryExpressionModel model = QueryExpressionModel
                .withSelectList(List.of(new CountAll()))
                .withTable(new SubQuery.Builder().withSelectModel(strippedSelectModel).build())
                .build();

        return SelectModel.withQueryExpressions(List.of(model))
                .withStatementConfiguration(selectModel.statementConfiguration())
                .build();
    }
}
