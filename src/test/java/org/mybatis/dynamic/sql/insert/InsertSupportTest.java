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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mybatis.dynamic.sql.insert.InsertSupportBuilder.*;

import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;

import org.junit.Test;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.insert.InsertSupport;
import org.mybatis.dynamic.sql.insert.InsertSupportBuilder.InsertSupportBuildStep2.CollectorSupport;
import org.mybatis.dynamic.sql.insert.InsertSupportBuilder.InsertSupportBuildStep2.ColumnMapping;

public class InsertSupportTest {
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
                .withColumnMapping(id, "id", record::getId)
                .withColumnMapping(firstName, "firstName", record::getFirstName)
                .withColumnMapping(lastName, "lastName", record::getLastName)
                .withColumnMapping(occupation, "occupation", record::getOccupation)
                .buildFullInsert();

        String expectedColumnsPhrase = "(id, first_name, last_name, occupation)";
        assertThat(insertSupport.getColumnsPhrase(), is(expectedColumnsPhrase));

        String expectedValuesPhrase = "values ({record.id}, {record.firstName}, {record.lastName}, {record.occupation})";
        assertThat(insertSupport.getValuesPhrase(), is(expectedValuesPhrase));
    }

    @Test
    public void testSelectiveInsertSupportBuilder() {
        TestRecord record = new TestRecord();
        record.setLastName("jones");
        record.setOccupation("dino driver");
        
        InsertSupport<TestRecord> insertSupport = insert(record)
                .into(foo)
                .withColumnMapping(id, "id", record::getId)
                .withColumnMapping(firstName, "firstName", record::getFirstName)
                .withColumnMapping(lastName, "lastName", record::getLastName)
                .withColumnMapping(occupation, "occupation", record::getOccupation)
                .buildSelectiveInsert();

        String expectedColumnsPhrase = "(last_name, occupation)";
        assertThat(insertSupport.getColumnsPhrase(), is(expectedColumnsPhrase));

        String expectedValuesPhrase = "values ({record.lastName}, {record.occupation})";
        assertThat(insertSupport.getValuesPhrase(), is(expectedValuesPhrase));
    }

    @Test
    public void testParallelStream() {

        TestRecord record = new TestRecord();
        record.setLastName("jones");
        record.setOccupation("dino driver");
        
        List<ColumnMapping<?>> mappings = new ArrayList<>();
        
        mappings.add(ColumnMapping.of(id, "id", record::getId));
        mappings.add(ColumnMapping.of(firstName, "firstName", record::getFirstName));
        mappings.add(ColumnMapping.of(lastName, "lastName", record::getLastName));
        mappings.add(ColumnMapping.of(occupation, "occupation", record::getOccupation));
        
        InsertSupport<TestRecord> insertSupport = 
                mappings.parallelStream().collect(Collector.of(
                        CollectorSupport::new,
                        CollectorSupport::add,
                        CollectorSupport::merge,
                        c -> c.toInsertSupport(record, foo)));
                
        String expectedColumnsPhrase = "(id, first_name, last_name, occupation)";
        assertThat(insertSupport.getColumnsPhrase(), is(expectedColumnsPhrase));

        String expectedValuesPhrase = "values ({record.id}, {record.firstName}, {record.lastName}, {record.occupation})";
        assertThat(insertSupport.getValuesPhrase(), is(expectedValuesPhrase));
    }
    
    @Test
    public void testFullInsertStatementBuilder() {

        TestRecord record = new TestRecord();
        record.setLastName("jones");
        record.setOccupation("dino driver");
        
        InsertSupport<TestRecord> insertSupport = insert(record)
                .into(foo)
                .withColumnMapping(id, "id", record::getId)
                .withColumnMapping(firstName, "firstName", record::getFirstName)
                .withColumnMapping(lastName, "lastName", record::getLastName)
                .withColumnMapping(occupation, "occupation", record::getOccupation)
                .buildFullInsert();

        String expectedStatement = "insert into foo "
                + "(id, first_name, last_name, occupation) "
                + "values ({record.id}, {record.firstName}, {record.lastName}, {record.occupation})";
        
        assertThat(insertSupport.getFullInsertStatement(), is(expectedStatement));
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
