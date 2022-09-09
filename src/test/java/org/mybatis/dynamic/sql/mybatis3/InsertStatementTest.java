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
package org.mybatis.dynamic.sql.mybatis3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.insert;

import java.sql.JDBCType;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategies;

class InsertStatementTest {
    private static final SqlTable foo = SqlTable.of("foo");
    private static final SqlColumn<Integer> id = foo.column("id", JDBCType.INTEGER);
    private static final SqlColumn<String> firstName = foo.column("first_name", JDBCType.VARCHAR);
    private static final SqlColumn<String> lastName = foo.column("last_name", JDBCType.VARCHAR);
    private static final SqlColumn<String> occupation = foo.column("occupation", JDBCType.VARCHAR);

    @Test
    void testFullInsertStatementBuilder() {
        TestRecord record = new TestRecord();
        record.setLastName("jones");
        record.setOccupation("dino driver");

        InsertStatementProvider<?> insertStatement = insert(record)
                .into(foo)
                .map(id).toProperty("id")
                .map(firstName).toProperty("firstName")
                .map(lastName).toProperty("lastName")
                .map(occupation).toProperty("occupation")
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "insert into foo (id, first_name, last_name, occupation) "
                + "values (#{row.id,jdbcType=INTEGER}, "
                + "#{row.firstName,jdbcType=VARCHAR}, " + "#{row.lastName,jdbcType=VARCHAR}, "
                + "#{row.occupation,jdbcType=VARCHAR})";
        assertThat(insertStatement.getInsertStatement()).isEqualTo(expected);
    }

    @Test
    void testSelectiveInsertStatementBuilder() {
        TestRecord record = new TestRecord();
        record.setLastName("jones");
        record.setOccupation("dino driver");

        InsertStatementProvider<?> insertStatement = insert(record)
                .into(foo)
                .map(id).toPropertyWhenPresent("id", record::getId)
                .map(firstName).toPropertyWhenPresent("firstName", record::getFirstName)
                .map(lastName).toPropertyWhenPresent("lastName", record::getLastName)
                .map(occupation).toPropertyWhenPresent("occupation", record::getOccupation)
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "insert into foo (last_name, occupation) "
                + "values (#{row.lastName,jdbcType=VARCHAR}, "
                + "#{row.occupation,jdbcType=VARCHAR})";
        assertThat(insertStatement.getInsertStatement()).isEqualTo(expected);
    }

    static class TestRecord {
        private Integer id;
        private String firstName;
        private String lastName;
        private String occupation;

        Integer getId() {
            return id;
        }

        void setId(Integer id) {
            this.id = id;
        }

        String getFirstName() {
            return firstName;
        }

        void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        String getLastName() {
            return lastName;
        }

        void setLastName(String lastName) {
            this.lastName = lastName;
        }

        String getOccupation() {
            return occupation;
        }

        void setOccupation(String occupation) {
            this.occupation = occupation;
        }
    }
}
