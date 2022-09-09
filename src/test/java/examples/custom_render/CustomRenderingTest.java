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
package examples.custom_render;

import static examples.custom_render.JsonTestDynamicSqlSupport.description;
import static examples.custom_render.JsonTestDynamicSqlSupport.id;
import static examples.custom_render.JsonTestDynamicSqlSupport.info;
import static examples.custom_render.JsonTestDynamicSqlSupport.jsonTest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.deleteFrom;
import static org.mybatis.dynamic.sql.SqlBuilder.insert;
import static org.mybatis.dynamic.sql.SqlBuilder.insertInto;
import static org.mybatis.dynamic.sql.SqlBuilder.insertMultiple;
import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;
import static org.mybatis.dynamic.sql.SqlBuilder.select;
import static org.mybatis.dynamic.sql.SqlBuilder.update;

import java.sql.JDBCType;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.insert.render.GeneralInsertStatementProvider;
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider;
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;
import org.mybatis.dynamic.sql.util.mybatis3.CommonSelectMapper;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class CustomRenderingTest {

    @Container
    private static final PgContainer postgres = new PgContainer("examples/custom_render/dbInit.sql");

    private static SqlSessionFactory sqlSessionFactory;

    @BeforeAll
    static void setUp() {
        Configuration configuration = new Configuration();
        Environment environment = new Environment("development", new JdbcTransactionFactory(),
                postgres.getUnpooledDataSource());
        configuration.setEnvironment(environment);
        configuration.addMapper(JsonTestMapper.class);
        configuration.addMapper(CommonSelectMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
    }

    @BeforeEach
    void resetDatabase() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            JsonTestMapper mapper = sqlSession.getMapper(JsonTestMapper.class);

            DeleteStatementProvider deleteStatement = deleteFrom(jsonTest).build().render(RenderingStrategies.MYBATIS3);

            mapper.delete(deleteStatement);
        }
    }

    @Test
    void testInsertRecord() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            JsonTestMapper mapper = sqlSession.getMapper(JsonTestMapper.class);

            JsonTestRecord record = new JsonTestRecord();
            record.setId(1);
            record.setDescription("Fred");
            record.setInfo("{\"firstName\": \"Fred\", \"lastName\": \"Flintstone\", \"age\": 30}");

            InsertStatementProvider<JsonTestRecord> insertStatement = insert(record).into(jsonTest)
                    .map(id).toProperty("id")
                    .map(description).toProperty("description")
                    .map(info).toProperty("info")
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "insert into JsonTest (id, description, info) "
                    + "values (#{row.id,jdbcType=INTEGER}, #{row.description,jdbcType=VARCHAR}, "
                    + "#{row.info,jdbcType=VARCHAR}::json)";

            assertThat(insertStatement.getInsertStatement()).isEqualTo(expected);

            int rows = mapper.insert(insertStatement);
            assertThat(rows).isEqualTo(1);

            record = new JsonTestRecord();
            record.setId(2);
            record.setDescription("Wilma");
            record.setInfo("{\"firstName\": \"Wilma\", \"lastName\": \"Flintstone\", \"age\": 25}");

            insertStatement = insert(record).into(jsonTest)
                    .map(id).toProperty("id")
                    .map(description).toProperty("description")
                    .map(info).toProperty("info")
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            rows = mapper.insert(insertStatement);
            assertThat(rows).isEqualTo(1);

            SelectStatementProvider selectStatement = select(id, description, info)
                    .from(jsonTest)
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<JsonTestRecord> records = mapper.selectMany(selectStatement);
            assertThat(records).hasSize(2);
            assertThat(records.get(0).getInfo()).isEqualTo("{\"firstName\": \"Fred\", \"lastName\": \"Flintstone\", \"age\": 30}");
            assertThat(records.get(1).getInfo()).isEqualTo("{\"firstName\": \"Wilma\", \"lastName\": \"Flintstone\", \"age\": 25}");
        }
    }

    @Test
    void testGeneralInsert() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            JsonTestMapper mapper = sqlSession.getMapper(JsonTestMapper.class);

            GeneralInsertStatementProvider insertStatement = insertInto(jsonTest)
                    .set(id).toValue(1)
                    .set(description).toValue("Fred")
                    .set(info).toValue("{\"firstName\": \"Fred\", \"lastName\": \"Flintstone\", \"age\": 30}")
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "insert into JsonTest (id, description, info) "
                    + "values (#{parameters.p1,jdbcType=INTEGER}, #{parameters.p2,jdbcType=VARCHAR}, "
                    + "#{parameters.p3,jdbcType=VARCHAR}::json)";

            assertThat(insertStatement.getInsertStatement()).isEqualTo(expected);

            int rows = mapper.generalInsert(insertStatement);
            assertThat(rows).isEqualTo(1);

            insertStatement = insertInto(jsonTest)
                    .set(id).toValue(2)
                    .set(description).toValue("Wilma")
                    .set(info).toValue("{\"firstName\": \"Wilma\", \"lastName\": \"Flintstone\", \"age\": 25}")
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            rows = mapper.generalInsert(insertStatement);
            assertThat(rows).isEqualTo(1);

            SelectStatementProvider selectStatement = select(id, description, info)
                    .from(jsonTest)
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<JsonTestRecord> records = mapper.selectMany(selectStatement);
            assertThat(records).hasSize(2);
            assertThat(records.get(0).getInfo()).isEqualTo("{\"firstName\": \"Fred\", \"lastName\": \"Flintstone\", \"age\": 30}");
            assertThat(records.get(1).getInfo()).isEqualTo("{\"firstName\": \"Wilma\", \"lastName\": \"Flintstone\", \"age\": 25}");
        }
    }

    @Test
    void testInsertMultiple() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            JsonTestMapper mapper = sqlSession.getMapper(JsonTestMapper.class);

            JsonTestRecord record1 = new JsonTestRecord();
            record1.setId(1);
            record1.setDescription("Fred");
            record1.setInfo("{\"firstName\": \"Fred\", \"lastName\": \"Flintstone\", \"age\": 30}");

            JsonTestRecord record2 = new JsonTestRecord();
            record2.setId(2);
            record2.setDescription("Wilma");
            record2.setInfo("{\"firstName\": \"Wilma\", \"lastName\": \"Flintstone\", \"age\": 25}");

            MultiRowInsertStatementProvider<JsonTestRecord> insertStatement = insertMultiple(record1, record2)
                    .into(jsonTest)
                    .map(id).toProperty("id")
                    .map(description).toProperty("description")
                    .map(info).toProperty("info")
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "insert into JsonTest (id, description, info) "
                    + "values (#{records[0].id,jdbcType=INTEGER}, #{records[0].description,jdbcType=VARCHAR}, "
                    + "#{records[0].info,jdbcType=VARCHAR}::json), "
                    + "(#{records[1].id,jdbcType=INTEGER}, #{records[1].description,jdbcType=VARCHAR}, "
                    + "#{records[1].info,jdbcType=VARCHAR}::json)";

            assertThat(insertStatement.getInsertStatement()).isEqualTo(expected);

            int rows = mapper.insertMultiple(insertStatement);
            assertThat(rows).isEqualTo(2);

            SelectStatementProvider selectStatement = select(id, description, info)
                    .from(jsonTest)
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<JsonTestRecord> records = mapper.selectMany(selectStatement);
            assertThat(records).hasSize(2);
            assertThat(records.get(0).getInfo()).isEqualTo("{\"firstName\": \"Fred\", \"lastName\": \"Flintstone\", \"age\": 30}");
            assertThat(records.get(1).getInfo()).isEqualTo("{\"firstName\": \"Wilma\", \"lastName\": \"Flintstone\", \"age\": 25}");
        }
    }

    @Test
    void testUpdate() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            JsonTestMapper mapper = sqlSession.getMapper(JsonTestMapper.class);

            JsonTestRecord record1 = new JsonTestRecord();
            record1.setId(1);
            record1.setDescription("Fred");
            record1.setInfo("{\"firstName\": \"Fred\", \"lastName\": \"Flintstone\", \"age\": 30}");

            JsonTestRecord record2 = new JsonTestRecord();
            record2.setId(2);
            record2.setDescription("Wilma");
            record2.setInfo("{\"firstName\": \"Wilma\", \"lastName\": \"Flintstone\", \"age\": 25}");

            MultiRowInsertStatementProvider<JsonTestRecord> insertStatement = insertMultiple(record1, record2)
                    .into(jsonTest)
                    .map(id).toProperty("id")
                    .map(description).toProperty("description")
                    .map(info).toProperty("info")
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            int rows = mapper.insertMultiple(insertStatement);
            assertThat(rows).isEqualTo(2);

            UpdateStatementProvider updateStatement = update(jsonTest)
                    .set(info).equalTo("{\"firstName\": \"Wilma\", \"lastName\": \"Flintstone\", \"age\": 26}")
                    .where(id, isEqualTo(2))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "update JsonTest "
                    + "set info = #{parameters.p1,jdbcType=VARCHAR}::json "
                    + "where id = #{parameters.p2,jdbcType=INTEGER}";

            assertThat(updateStatement.getUpdateStatement()).isEqualTo(expected);

            rows = mapper.update(updateStatement);
            assertThat(rows).isEqualTo(1);

            SelectStatementProvider selectStatement = select(id, description, info)
                    .from(jsonTest)
                    .orderBy(id)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<JsonTestRecord> records = mapper.selectMany(selectStatement);
            assertThat(records).hasSize(2);
            assertThat(records.get(0).getInfo()).isEqualTo("{\"firstName\": \"Fred\", \"lastName\": \"Flintstone\", \"age\": 30}");
            assertThat(records.get(1).getInfo()).isEqualTo("{\"firstName\": \"Wilma\", \"lastName\": \"Flintstone\", \"age\": 26}");
        }
    }

    @Test
    void testDeReference() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            JsonTestMapper mapper = sqlSession.getMapper(JsonTestMapper.class);

            JsonTestRecord record1 = new JsonTestRecord();
            record1.setId(1);
            record1.setDescription("Fred");
            record1.setInfo("{\"firstName\": \"Fred\", \"lastName\": \"Flintstone\", \"age\": 30}");

            JsonTestRecord record2 = new JsonTestRecord();
            record2.setId(2);
            record2.setDescription("Wilma");
            record2.setInfo("{\"firstName\": \"Wilma\", \"lastName\": \"Flintstone\", \"age\": 25}");

            MultiRowInsertStatementProvider<JsonTestRecord> insertStatement = insertMultiple(record1, record2)
                    .into(jsonTest)
                    .map(id).toProperty("id")
                    .map(description).toProperty("description")
                    .map(info).toProperty("info")
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            int rows = mapper.insertMultiple(insertStatement);
            assertThat(rows).isEqualTo(2);

            SelectStatementProvider selectStatement = select(id, description, info)
                    .from(jsonTest)
                    .where(dereference(info, "age"), isEqualTo("25"))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select id, description, info "
                    + "from JsonTest "
                    + "where info->>'age' = #{parameters.p1,jdbcType=VARCHAR}";

            assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);

            Optional<JsonTestRecord> record = mapper.selectOne(selectStatement);

            assertThat(record).hasValueSatisfying( r ->
                assertThat(r.getInfo())
                        .isEqualTo("{\"firstName\": \"Wilma\", \"lastName\": \"Flintstone\", \"age\": 25}")
            );
        }
    }

    @Test
    void testDereference2() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            JsonTestMapper mapper = sqlSession.getMapper(JsonTestMapper.class);

            JsonTestRecord record1 = new JsonTestRecord();
            record1.setId(1);
            record1.setDescription("Fred");
            record1.setInfo("{\"firstName\": \"Fred\", \"lastName\": \"Flintstone\", \"age\": 30}");

            JsonTestRecord record2 = new JsonTestRecord();
            record2.setId(2);
            record2.setDescription("Wilma");
            record2.setInfo("{\"firstName\": \"Wilma\", \"lastName\": \"Flintstone\", \"age\": 25}");

            MultiRowInsertStatementProvider<JsonTestRecord> insertStatement = insertMultiple(record1, record2)
                    .into(jsonTest)
                    .map(id).toProperty("id")
                    .map(description).toProperty("description")
                    .map(info).toProperty("info")
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            int rows = mapper.insertMultiple(insertStatement);
            assertThat(rows).isEqualTo(2);

            SelectStatementProvider selectStatement = select(dereference(info, "firstName").as("firstname"))
                    .from(jsonTest)
                    .where(dereference(info, "age"), isEqualTo("25"))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = "select info->>'firstName' as firstname "
                    + "from JsonTest "
                    + "where info->>'age' = #{parameters.p1,jdbcType=VARCHAR}";

            assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);

            List<Map<String, Object>> records = mapper.selectManyMappedRows(selectStatement);
            assertThat(records).hasSize(1);
            assertThat(records.get(0)).containsEntry("firstname", "Wilma");
        }
    }

    private <T> SqlColumn<String> dereference(SqlColumn<T> column, String attribute) {
        return SqlColumn.of(column.name() + "->>'" + attribute + "'", column.table(), JDBCType.VARCHAR);
    }
}
