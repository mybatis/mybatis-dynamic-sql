/**
 *    Copyright 2016 the original author or authors.
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
package org.mybatis.qbe.sql.insert;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mybatis.qbe.sql.insert.InsertSupportBuilder.insertSupport;

import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;

import org.junit.Test;
import org.mybatis.qbe.sql.SqlField;
import org.mybatis.qbe.sql.insert.InsertSupport;
import org.mybatis.qbe.sql.insert.InsertSupportBuilder.Builder.CollectorSupport;
import org.mybatis.qbe.sql.insert.InsertSupportBuilder.Builder.FieldMapping;

public class InsertSupportTest {
    private static final SqlField<Integer> id = SqlField.of("id", JDBCType.INTEGER);
    private static final SqlField<String> firstName = SqlField.of("first_name", JDBCType.VARCHAR);
    private static final SqlField<String> lastName = SqlField.of("last_name", JDBCType.VARCHAR);
    private static final SqlField<String> occupation = SqlField.of("occupation", JDBCType.VARCHAR);
    
    @Test
    public void testFullInsertSupportBuilder() {

        TestRecord record = new TestRecord();
        record.setLastName("jones");
        record.setOccupation("dino driver");
        
        InsertSupport<TestRecord> insertSupport = insertSupport(record)
                .withFieldMapping(id, "id", record::getId)
                .withFieldMapping(firstName, "firstName", record::getFirstName)
                .withFieldMapping(lastName, "lastName", record::getLastName)
                .withFieldMapping(occupation, "occupation", record::getOccupation)
                .buildFullInsert();

        String expectedFieldsPhrase = "(id, first_name, last_name, occupation)";
        assertThat(insertSupport.getFieldsPhrase(), is(expectedFieldsPhrase));

        String expectedValuesPhrase = "values (?, ?, ?, ?)";
        assertThat(insertSupport.getValuesPhrase(), is(expectedValuesPhrase));
    }

    @Test
    public void testSelectiveInsertSupportBuilder() {
        TestRecord record = new TestRecord();
        record.setLastName("jones");
        record.setOccupation("dino driver");
        
        InsertSupport<TestRecord> insertSupport = insertSupport(record)
                .withFieldMapping(id, "id", record::getId)
                .withFieldMapping(firstName, "firstName", record::getFirstName)
                .withFieldMapping(lastName, "lastName", record::getLastName)
                .withFieldMapping(occupation, "occupation", record::getOccupation)
                .buildSelectiveInsert();

        String expectedFieldsPhrase = "(last_name, occupation)";
        assertThat(insertSupport.getFieldsPhrase(), is(expectedFieldsPhrase));

        String expectedValuesPhrase = "values (?, ?)";
        assertThat(insertSupport.getValuesPhrase(), is(expectedValuesPhrase));
    }

    @Test
    public void testParallelStream() {

        TestRecord record = new TestRecord();
        record.setLastName("jones");
        record.setOccupation("dino driver");
        
        List<FieldMapping<?>> mappings = new ArrayList<>();
        
        mappings.add(FieldMapping.of(id, "id", record::getId));
        mappings.add(FieldMapping.of(firstName, "first_name", record::getFirstName));
        mappings.add(FieldMapping.of(lastName, "last_name", record::getLastName));
        mappings.add(FieldMapping.of(occupation, "occupation", record::getOccupation));
        
        InsertSupport<TestRecord> insertSupport = 
                mappings.parallelStream().collect(Collector.of(
                        CollectorSupport::new,
                        CollectorSupport::add,
                        CollectorSupport::merge,
                        c -> c.toInsertSupport(record)));
                
        String expectedFieldsPhrase = "(id, first_name, last_name, occupation)";
        assertThat(insertSupport.getFieldsPhrase(), is(expectedFieldsPhrase));

        String expectedValuesPhrase = "values (?, ?, ?, ?)";
        assertThat(insertSupport.getValuesPhrase(), is(expectedValuesPhrase));
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
