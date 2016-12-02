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
package org.mybatis.qbe.sql;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mybatis.qbe.sql.insert.InsertSupportBuilder.insertSupport;

import java.sql.JDBCType;

import org.junit.Test;
import org.mybatis.qbe.sql.insert.InsertSupport;

public class InsertSupportTest {
    
    @Test
    public void testFullInsertSupportBuilder() {
        SqlField<Integer> id = SqlField.of("id", JDBCType.INTEGER);
        SqlField<String> firstName = SqlField.of("first_name", JDBCType.VARCHAR);
        SqlField<String> lastName = SqlField.of("last_name", JDBCType.VARCHAR);
        SqlField<String> occupation = SqlField.of("occupation", JDBCType.VARCHAR);

        TestRecord testRecord = new TestRecord();
        testRecord.setLastName("jones");
        testRecord.setOccupation("dino driver");
        
        InsertSupport<?> insertSupport = insertSupport(testRecord)
                .withFieldMapping(id, "id", TestRecord::getId)
                .withFieldMapping(firstName, "firstName", TestRecord::getFirstName)
                .withFieldMapping(lastName, "lastName", TestRecord::getLastName)
                .withFieldMapping(occupation, "occupation", TestRecord::getOccupation)
                .buildFullInsert();

        String expectedFieldsPhrase = "(id, first_name, last_name, occupation)";
        assertThat(insertSupport.getFieldsPhrase(), is(expectedFieldsPhrase));

        String expectedValuesPhrase = "values (?, ?, ?, ?)";
        assertThat(insertSupport.getValuesPhrase(), is(expectedValuesPhrase));
    }

    @Test
    public void testSelectiveInsertSupportBuilder() {
        SqlField<Integer> id = SqlField.of("id", JDBCType.INTEGER);
        SqlField<String> firstName = SqlField.of("first_name", JDBCType.VARCHAR);
        SqlField<String> lastName = SqlField.of("last_name", JDBCType.VARCHAR);
        SqlField<String> occupation = SqlField.of("occupation", JDBCType.VARCHAR);

        TestRecord testRecord = new TestRecord();
        testRecord.setLastName("jones");
        testRecord.setOccupation("dino driver");
        
        InsertSupport<?> insertSupport = insertSupport(testRecord)
                .withFieldMapping(id, "id", TestRecord::getId)
                .withFieldMapping(firstName, "firstName", TestRecord::getFirstName)
                .withFieldMapping(lastName, "lastName", TestRecord::getLastName)
                .withFieldMapping(occupation, "occupation", TestRecord::getOccupation)
                .buildSelectiveInsert();

        String expectedFieldsPhrase = "(last_name, occupation)";
        assertThat(insertSupport.getFieldsPhrase(), is(expectedFieldsPhrase));

        String expectedValuesPhrase = "values (?, ?)";
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
