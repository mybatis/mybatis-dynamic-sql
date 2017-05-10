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
package org.mybatis.dynamic.sql.insert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.insert;

import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.insert.render.FieldAndValue;
import org.mybatis.dynamic.sql.insert.render.FieldAndValueCollector;
import org.mybatis.dynamic.sql.insert.render.InsertSupport;
import org.mybatis.dynamic.sql.render.RenderingStrategy;

public class InsertSupportTest {
    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    private static final SqlTable foo = SqlTable.of("foo");
    private static final SqlColumn<Integer> id = SqlColumn.of("id", JDBCType.INTEGER);
    private static final SqlColumn<String> firstName = SqlColumn.of("first_name", JDBCType.VARCHAR);
    private static final SqlColumn<String> lastName = SqlColumn.of("last_name", JDBCType.VARCHAR);
    private static final SqlColumn<String> occupation = SqlColumn.of("occupation", JDBCType.VARCHAR);
    
    @Test
    public void testFullInsertSupportBuilder() {

        TestRecord record = new TestRecord();
        record.setLastName("jones");
        record.setOccupation("dino driver");
        
        InsertSupport<TestRecord> insertSupport = insert(record)
                .into(foo)
                .map(id).toProperty("id")
                .map(firstName).toProperty("firstName")
                .map(lastName).toProperty("lastName")
                .map(occupation).toProperty("occupation")
                .buildAndRender(RenderingStrategy.MYBATIS3);

        String expectedColumnsPhrase = "(id, first_name, last_name, occupation)";
        softly.assertThat(insertSupport.getColumnsPhrase()).isEqualTo(expectedColumnsPhrase);

        String expectedValuesPhrase = "values (#{record.id,jdbcType=INTEGER}, #{record.firstName,jdbcType=VARCHAR}, #{record.lastName,jdbcType=VARCHAR}, #{record.occupation,jdbcType=VARCHAR})";
        softly.assertThat(insertSupport.getValuesPhrase()).isEqualTo(expectedValuesPhrase);
    }

    @Test
    public void testInsertSupportBuilderWithNulls() {

        TestRecord record = new TestRecord();
        
        InsertSupport<TestRecord> insertSupport = insert(record)
                .into(foo)
                .map(id).toProperty("id")
                .map(firstName).toProperty("firstName")
                .map(lastName).toProperty("lastName")
                .map(occupation).toNull()
                .buildAndRender(RenderingStrategy.MYBATIS3);

        String expectedColumnsPhrase = "(id, first_name, last_name, occupation)";
        softly.assertThat(insertSupport.getColumnsPhrase()).isEqualTo(expectedColumnsPhrase);

        String expectedValuesPhrase = "values (#{record.id,jdbcType=INTEGER}, #{record.firstName,jdbcType=VARCHAR}, #{record.lastName,jdbcType=VARCHAR}, null)";
        softly.assertThat(insertSupport.getValuesPhrase()).isEqualTo(expectedValuesPhrase);
    }

    @Test
    public void testInsertSupportBuilderWithConstants() {

        TestRecord record = new TestRecord();
        
        InsertSupport<TestRecord> insertSupport = insert(record)
                .into(foo)
                .map(id).toProperty("id")
                .map(firstName).toProperty("firstName")
                .map(lastName).toProperty("lastName")
                .map(occupation).toConstant("'Y'")
                .buildAndRender(RenderingStrategy.MYBATIS3);

        String expectedColumnsPhrase = "(id, first_name, last_name, occupation)";
        softly.assertThat(insertSupport.getColumnsPhrase()).isEqualTo(expectedColumnsPhrase);

        String expectedValuesPhrase = "values (#{record.id,jdbcType=INTEGER}, #{record.firstName,jdbcType=VARCHAR}, #{record.lastName,jdbcType=VARCHAR}, 'Y')";
        softly.assertThat(insertSupport.getValuesPhrase()).isEqualTo(expectedValuesPhrase);
    }
    
    @Test
    public void testSelectiveInsertSupportBuilder() {
        TestRecord record = new TestRecord();
        record.setLastName("jones");
        record.setOccupation("dino driver");
        
        InsertSupport<TestRecord> insertSupport = insert(record)
                .into(foo)
                .map(id).toPropertyWhenPresent("id")
                .map(firstName).toPropertyWhenPresent("firstName")
                .map(lastName).toPropertyWhenPresent("lastName")
                .map(occupation).toPropertyWhenPresent("occupation")
                .buildAndRender(RenderingStrategy.MYBATIS3);

        String expectedColumnsPhrase = "(last_name, occupation)";
        softly.assertThat(insertSupport.getColumnsPhrase()).isEqualTo(expectedColumnsPhrase);

        String expectedValuesPhrase = "values (#{record.lastName,jdbcType=VARCHAR}, #{record.occupation,jdbcType=VARCHAR})";
        softly.assertThat(insertSupport.getValuesPhrase()).isEqualTo(expectedValuesPhrase);
    }

    @Test
    public void testParallelStream() {

        TestRecord record = new TestRecord();
        record.setLastName("jones");
        record.setOccupation("dino driver");
        
        List<FieldAndValue> mappings = new ArrayList<>();
        
        mappings.add(FieldAndValue.of(id.name(), "{record.id}"));
        mappings.add(FieldAndValue.of(firstName.name(), "{record.firstName}"));
        mappings.add(FieldAndValue.of(lastName.name(), "{record.lastName}"));
        mappings.add(FieldAndValue.of(occupation.name(), "{record.occupation}"));
        
        FieldAndValueCollector<TestRecord> collector = 
                mappings.parallelStream().collect(Collector.of(
                        () -> new FieldAndValueCollector<>(record, foo),
                        FieldAndValueCollector::add,
                        FieldAndValueCollector::merge));
                
        String expectedColumnsPhrase = "(id, first_name, last_name, occupation)";
        softly.assertThat(collector.columnsPhrase()).isEqualTo(expectedColumnsPhrase);

        String expectedValuesPhrase = "values ({record.id}, {record.firstName}, {record.lastName}, {record.occupation})";
        softly.assertThat(collector.valuesPhrase()).isEqualTo(expectedValuesPhrase);
    }
    
    @Test
    public void testFullInsertStatementBuilder() {

        TestRecord record = new TestRecord();
        record.setLastName("jones");
        record.setOccupation("dino driver");
        
        InsertSupport<TestRecord> insertSupport = insert(record)
                .into(foo)
                .map(id).toProperty("id")
                .map(firstName).toProperty("firstName")
                .map(lastName).toProperty("lastName")
                .map(occupation).toProperty("occupation")
                .buildAndRender(RenderingStrategy.MYBATIS3);
        
        String expectedStatement = "insert into foo "
                + "(id, first_name, last_name, occupation) "
                + "values (#{record.id,jdbcType=INTEGER}, #{record.firstName,jdbcType=VARCHAR}, #{record.lastName,jdbcType=VARCHAR}, #{record.occupation,jdbcType=VARCHAR})";
        
        assertThat(insertSupport.getFullInsertStatement()).isEqualTo(expectedStatement);
    }

    public static class TestRecord {
        private Integer id;
        private String firstName;
        private String lastName;
        private String occupation;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getOccupation() {
            return occupation;
        }

        public void setOccupation(String occupation) {
            this.occupation = occupation;
        }
    }
}
