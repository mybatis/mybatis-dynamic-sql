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
package examples.generated.always.spring;

import static examples.generated.always.spring.GeneratedAlwaysDynamicSqlSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.insert.render.BatchInsert;
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;
import org.springframework.jdbc.core.RowMapper;
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

public class SpringTest {
    private EmbeddedDatabase db;
    private NamedParameterJdbcTemplate template;
    
    @BeforeEach
    public void setup() {
        db = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.HSQL)
                .generateUniqueName(true)
                .addScript("classpath:/examples/generated/always/CreateGeneratedAlwaysDB.sql")
                .build();
        template = new NamedParameterJdbcTemplate(db);
    }
    
    @Test
    public void testRender() {
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
    public void testSelect() {
        SelectStatementProvider selectStatement = select(id, firstName, lastName, fullName)
                .from(generatedAlways)
                .where(id, isGreaterThan(3))
                .orderBy(id.descending())
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);
        
        SqlParameterSource namedParameters = new MapSqlParameterSource(selectStatement.getParameters());
        
        List<GeneratedAlwaysRecord> records = template.query(selectStatement.getSelectStatement(), namedParameters,
                new RowMapper<GeneratedAlwaysRecord>(){
                    @Override
                    public GeneratedAlwaysRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
                        GeneratedAlwaysRecord record = new GeneratedAlwaysRecord();
                        record.setId(rs.getInt(1));
                        record.setFirstName(rs.getString(2));
                        record.setLastName(rs.getString(3));
                        record.setFullName(rs.getString(4));
                        return record;
                    }
                });
        
        assertThat(records.size()).isEqualTo(3);
        assertThat(records.get(0).getId()).isEqualTo(6);
        assertThat(records.get(0).getFirstName()).isEqualTo("Bamm Bamm");
        assertThat(records.get(0).getLastName()).isEqualTo("Rubble");
        assertThat(records.get(0).getFullName()).isEqualTo("Bamm Bamm Rubble");
        
        assertThat(records.get(1).getId()).isEqualTo(5);
        assertThat(records.get(2).getId()).isEqualTo(4);
        
    }

    @Test
    public void testDelete() {
        DeleteStatementProvider deleteStatement = deleteFrom(generatedAlways)
                .where(id,  isLessThan(3))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);
        
        SqlParameterSource parameterSource = new MapSqlParameterSource(deleteStatement.getParameters());
        
        int rows = template.update(deleteStatement.getDeleteStatement(), parameterSource);
        
        assertThat(rows).isEqualTo(2);
    }
    
    @Test
    public void testInsert() {
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
        
        SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(insertStatement.getRecord());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        int rows = template.update(insertStatement.getInsertStatement(), parameterSource, keyHolder);
        String generatedKey = (String) keyHolder.getKeys().get("FULL_NAME");
        
        assertThat(rows).isEqualTo(1);
        assertThat(generatedKey).isEqualTo("Bob Jones");
        
    }
    
    @Test
    public void testInsertBatch() {
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

        SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(records.toArray());
        
        BatchInsert<GeneratedAlwaysRecord> batchInsert = insert(records)
                .into(generatedAlways)
                .map(id).toProperty("id")
                .map(firstName).toProperty("firstName")
                .map(lastName).toProperty("lastName")
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);
        
        int[] updateCounts = template.batchUpdate(batchInsert.getInsertStatementSQL(), batch);
        
        assertThat(updateCounts.length).isEqualTo(2);
        assertThat(updateCounts[0]).isEqualTo(1);
        assertThat(updateCounts[1]).isEqualTo(1);
    }

    @Test
    public void testUpdate() {
        UpdateStatementProvider updateStatement = update(generatedAlways)
                .set(firstName).equalToStringConstant("Rob")
                .where(id,  isIn(1, 5, 22))
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);
        
        SqlParameterSource parameterSource = new MapSqlParameterSource(updateStatement.getParameters());
        
        int rows = template.update(updateStatement.getUpdateStatement(), parameterSource);
        
        assertThat(rows).isEqualTo(2);
        
    }
    
    @AfterEach
    public void teardown() {
        db.shutdown();
    }
}
