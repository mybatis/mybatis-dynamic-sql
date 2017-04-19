/**
 *    Copyright 2016-2017 the original author or authors.
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

import static examples.generated.always.spring.GeneratedAlwaysDynamicSqlSupport.buildInsertSupport;
import static examples.generated.always.spring.GeneratedAlwaysDynamicSqlSupport.id;
import static examples.generated.always.spring.GeneratedAlwaysDynamicSqlSupport.selectByExample;
import static org.mybatis.dynamic.sql.SqlConditions.isGreaterThan;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mybatis.dynamic.sql.insert.InsertSupport;
import org.mybatis.dynamic.sql.select.SelectSupport;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

public class SpringTest {
    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    private EmbeddedDatabase db;
    
    @Before
    public void setup() {
        db = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.HSQL)
                .generateUniqueName(true)
                .addScript("classpath:/examples/generated/always/CreateGeneratedAlwaysDB.sql")
                .build();
    }
    
    @Test
    public void testSelect() {
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(db);
        
        SelectSupport selectSupport = selectByExample()
                .where(id, isGreaterThan(3))
                .orderBy(id.descending())
                .build();
        
        softly.assertThat(selectSupport.getColumnList()).isEqualTo("a.id as A_ID, a.first_name, a.last_name, a.full_name");
        
        List<GeneratedAlwaysRecord> records = template.query(selectSupport.getFullSelectStatement(), selectSupport.getParameters(),
                new RowMapper<GeneratedAlwaysRecord>(){
                    public GeneratedAlwaysRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
                        GeneratedAlwaysRecord record = new GeneratedAlwaysRecord();
                        record.setId(rs.getInt(1));
                        record.setFirstName(rs.getString(2));
                        record.setLastName(rs.getString(3));
                        return record;
                    }
                });
        
        softly.assertThat(records.size()).isEqualTo(3);
        softly.assertThat(records.get(0).getId()).isEqualTo(6);
        softly.assertThat(records.get(1).getId()).isEqualTo(5);
        softly.assertThat(records.get(2).getId()).isEqualTo(4);
    }
    
    @Test
    public void testInsert() {
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(db);
        
        GeneratedAlwaysRecord record = new GeneratedAlwaysRecord();
        record.setId(100);
        record.setFirstName("Bob");
        record.setLastName("Jones");
        
        InsertSupport<GeneratedAlwaysRecord> insertSupport = buildInsertSupport(record);
        
        SqlParameterSource ps = new BeanPropertySqlParameterSource(record);
        KeyHolder kh = new GeneratedKeyHolder();
        
        int rows = template.update(insertSupport.getFullInsertStatement(), ps, kh);
        
        softly.assertThat(rows).isEqualTo(1);
        softly.assertThat(kh.getKeys().get("FULL_NAME")).isEqualTo("Bob Jones");
    }
    
    @After
    public void teardown() {
        db.shutdown();
    }
}
