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
package examples.generated.always.spring;

import static examples.generated.always.spring.GeneratedAlwaysDynamicSqlSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.insert.BatchInsertModel;
import org.mybatis.dynamic.sql.insert.GeneralInsertModel;
import org.mybatis.dynamic.sql.insert.InsertModel;
import org.mybatis.dynamic.sql.insert.MultiRowInsertModel;
import org.mybatis.dynamic.sql.insert.render.BatchInsert;
import org.mybatis.dynamic.sql.insert.render.GeneralInsertStatementProvider;
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;
import org.mybatis.dynamic.sql.util.Buildable;
import org.mybatis.dynamic.sql.util.spring.NamedParameterJdbcTemplateExtensions;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import examples.generated.always.GeneratedAlwaysRecord;

class SpringTest {
    private NamedParameterJdbcTemplate template;

    @BeforeEach
    void setup() {
        EmbeddedDatabase db = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.HSQL)
                .generateUniqueName(true)
                .addScript("classpath:/examples/generated/always/CreateGeneratedAlwaysDB.sql")
                .build();
        template = new NamedParameterJdbcTemplate(db);
    }

    @Test
    void testRender() {
        SelectStatementProvider selectStatement = select(id.as("A_ID"), firstName, lastName, fullName)
                .from(generatedAlways, "a")
                .where(id, isGreaterThan(3))
                .orderBy(id.descending())
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        String expected = "select a.id as A_ID, a.first_name, a.last_name, a.full_name "
                + "from GeneratedAlways a "
                + "where a.id > :p1 "
                + "order by id DESC";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
    }

    @Test
    void testSelect() {
        SelectStatementProvider selectStatement = select(id, firstName, lastName, fullName)
                .from(generatedAlways)
                .where(id, isGreaterThan(3))
                .orderBy(id.descending())
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        SqlParameterSource namedParameters = new MapSqlParameterSource(selectStatement.getParameters());

        List<GeneratedAlwaysRecord> records = template.query(selectStatement.getSelectStatement(), namedParameters,
                (rs, rowNum) -> {
                    GeneratedAlwaysRecord record = new GeneratedAlwaysRecord();
                    record.setId(rs.getInt(1));
                    record.setFirstName(rs.getString(2));
                    record.setLastName(rs.getString(3));
                    record.setFullName(rs.getString(4));
                    return record;
                });

        assertThat(records).hasSize(3);
        assertThat(records.get(0).getId()).isEqualTo(6);
        assertThat(records.get(0).getFirstName()).isEqualTo("Bamm Bamm");
        assertThat(records.get(0).getLastName()).isEqualTo("Rubble");
        assertThat(records.get(0).getFullName()).isEqualTo("Bamm Bamm Rubble");

        assertThat(records.get(1).getId()).isEqualTo(5);
        assertThat(records.get(2).getId()).isEqualTo(4);

    }

    @Test
    void testDelete() {
        DeleteStatementProvider deleteStatement = deleteFrom(generatedAlways)
                .where(id,  isLessThan(3))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        SqlParameterSource parameterSource = new MapSqlParameterSource(deleteStatement.getParameters());

        int rows = template.update(deleteStatement.getDeleteStatement(), parameterSource);

        assertThat(rows).isEqualTo(2);
    }

    @Test
    void testInsert() {
        GeneratedAlwaysRecord record = new GeneratedAlwaysRecord();
        record.setId(100);
        record.setFirstName("Bob");
        record.setLastName("Jones");

        InsertStatementProvider<GeneratedAlwaysRecord> insertStatement = insert(record)
                .into(generatedAlways)
                .map(id).toProperty("id")
                .map(firstName).toProperty("firstName")
                .map(lastName).toProperty("lastName")
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(insertStatement.getRow());
        KeyHolder keyHolder = new GeneratedKeyHolder();

        int rows = template.update(insertStatement.getInsertStatement(), parameterSource, keyHolder);
        String generatedKey = (String) keyHolder.getKeys().get("FULL_NAME");

        assertThat(rows).isEqualTo(1);
        assertThat(generatedKey).isEqualTo("Bob Jones");
    }

    @Test
    void testInsertWithExtensions() {
        GeneratedAlwaysRecord record = new GeneratedAlwaysRecord();
        record.setId(100);
        record.setFirstName("Bob");
        record.setLastName("Jones");

        Buildable<InsertModel<GeneratedAlwaysRecord>> insertStatement = insert(record)
                .into(generatedAlways)
                .map(id).toProperty("id")
                .map(firstName).toProperty("firstName")
                .map(lastName).toProperty("lastName");

        NamedParameterJdbcTemplateExtensions extensions = new NamedParameterJdbcTemplateExtensions(template);

        KeyHolder keyHolder = new GeneratedKeyHolder();

        int rows = extensions.insert(insertStatement, keyHolder);
        String generatedKey = (String) keyHolder.getKeys().get("FULL_NAME");

        assertThat(rows).isEqualTo(1);
        assertThat(generatedKey).isEqualTo("Bob Jones");
    }

    @Test
    void testGeneralInsert() {
        GeneralInsertStatementProvider insertStatement = insertInto(generatedAlways)
                .set(id).toValue(100)
                .set(firstName).toValue("Bob")
                .set(lastName).toValue("Jones")
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        int rows = template.update(insertStatement.getInsertStatement(), insertStatement.getParameters());

        assertThat(rows).isEqualTo(1);
    }

    @Test
    void testGeneralInsertWithGeneratedKey() {
        GeneralInsertStatementProvider insertStatement = insertInto(generatedAlways)
                .set(id).toValue(100)
                .set(firstName).toValue("Bob")
                .set(lastName).toValue("Jones")
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        MapSqlParameterSource parameterSource = new MapSqlParameterSource(insertStatement.getParameters());
        KeyHolder keyHolder = new GeneratedKeyHolder();

        int rows = template.update(insertStatement.getInsertStatement(), parameterSource, keyHolder);
        String generatedKey = (String) keyHolder.getKeys().get("FULL_NAME");

        assertThat(rows).isEqualTo(1);
        assertThat(generatedKey).isEqualTo("Bob Jones");
    }

    @Test
    void testGeneralInsertWithGeneratedKeyAndExtensions() {
        Buildable<GeneralInsertModel> insertStatement = insertInto(generatedAlways)
                .set(id).toValue(100)
                .set(firstName).toValue("Bob")
                .set(lastName).toValue("Jones");

        NamedParameterJdbcTemplateExtensions extensions = new NamedParameterJdbcTemplateExtensions(template);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        int rows = extensions.generalInsert(insertStatement, keyHolder);
        String generatedKey = (String) keyHolder.getKeys().get("FULL_NAME");

        assertThat(rows).isEqualTo(1);
        assertThat(generatedKey).isEqualTo("Bob Jones");
    }

    @Test
    void testInsertBatch() {
        List<GeneratedAlwaysRecord> records = new ArrayList<>();
        GeneratedAlwaysRecord record = new GeneratedAlwaysRecord();
        record.setId(100);
        record.setFirstName("Bob");
        record.setLastName("Jones");
        records.add(record);

        record = new GeneratedAlwaysRecord();
        record.setId(101);
        record.setFirstName("Jim");
        record.setLastName("Smith");
        records.add(record);

        SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(records);

        BatchInsert<GeneratedAlwaysRecord> batchInsert = insertBatch(records)
                .into(generatedAlways)
                .map(id).toProperty("id")
                .map(firstName).toProperty("firstName")
                .map(lastName).toProperty("lastName")
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        int[] updateCounts = template.batchUpdate(batchInsert.getInsertStatementSQL(), batch);

        assertThat(updateCounts).hasSize(2);
        assertThat(updateCounts[0]).isEqualTo(1);
        assertThat(updateCounts[1]).isEqualTo(1);
    }

    @Test
    void testInsertBatchWithExtensions() {
        NamedParameterJdbcTemplateExtensions extensions = new NamedParameterJdbcTemplateExtensions(template);

        List<GeneratedAlwaysRecord> records = new ArrayList<>();
        GeneratedAlwaysRecord record = new GeneratedAlwaysRecord();
        record.setId(100);
        record.setFirstName("Bob");
        record.setLastName("Jones");
        records.add(record);

        record = new GeneratedAlwaysRecord();
        record.setId(101);
        record.setFirstName("Jim");
        record.setLastName("Smith");
        records.add(record);

        Buildable<BatchInsertModel<GeneratedAlwaysRecord>> insertStatement = insertBatch(records)
                .into(generatedAlways)
                .map(id).toProperty("id")
                .map(firstName).toProperty("firstName")
                .map(lastName).toProperty("lastName");

        int[] updateCounts = extensions.insertBatch(insertStatement);

        assertThat(updateCounts).hasSize(2);
        assertThat(updateCounts[0]).isEqualTo(1);
        assertThat(updateCounts[1]).isEqualTo(1);
    }

    @Test
    void testMultiRowInsert() {
        NamedParameterJdbcTemplateExtensions extensions = new NamedParameterJdbcTemplateExtensions(template);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        List<GeneratedAlwaysRecord> records = new ArrayList<>();
        GeneratedAlwaysRecord record = new GeneratedAlwaysRecord();
        record.setId(100);
        record.setFirstName("Bob");
        record.setLastName("Jones");
        records.add(record);

        record = new GeneratedAlwaysRecord();
        record.setId(101);
        record.setFirstName("Jim");
        record.setLastName("Smith");
        records.add(record);

        Buildable<MultiRowInsertModel<GeneratedAlwaysRecord>> insertStatement = insertMultiple(records).into(generatedAlways)
                .map(id).toProperty("id")
                .map(firstName).toProperty("firstName")
                .map(lastName).toProperty("lastName");

        int rows = extensions.insertMultiple(insertStatement, keyHolder);

        assertThat(rows).isEqualTo(2);
        assertThat(keyHolder.getKeyList().get(0)).contains(entry("FULL_NAME", "Bob Jones"));
        assertThat(keyHolder.getKeyList().get(1)).contains(entry("FULL_NAME", "Jim Smith"));
    }

    @Test
    void testUpdate() {
        UpdateStatementProvider updateStatement = update(generatedAlways)
                .set(firstName).equalToStringConstant("Rob")
                .where(id,  isIn(1, 5, 22))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        SqlParameterSource parameterSource = new MapSqlParameterSource(updateStatement.getParameters());

        int rows = template.update(updateStatement.getUpdateStatement(), parameterSource);

        assertThat(rows).isEqualTo(2);
    }
}
