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
package org.mybatis.dynamic.sql.insert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.insert;

import java.sql.JDBCType;

import org.jspecify.annotations.Nullable;
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

        TestRecord row = new TestRecord(null, null, "jones", "dino driver");

        InsertStatementProvider<TestRecord> insertStatement = insert(row)
                .into(foo)
                .map(id).toProperty("id")
                .map(firstName).toProperty("firstName")
                .map(lastName).toProperty("lastName")
                .map(occupation).toProperty("occupation")
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expectedStatement = "insert into foo "
                + "(id, first_name, last_name, occupation) "
                + "values (#{row.id,jdbcType=INTEGER}, #{row.firstName,jdbcType=VARCHAR}, #{row.lastName,jdbcType=VARCHAR}, #{row.occupation,jdbcType=VARCHAR})";

        assertThat(insertStatement.getInsertStatement()).isEqualTo(expectedStatement);
    }

    @Test
    void testInsertStatementBuilderWithNulls() {

        TestRecord row = new TestRecord();

        InsertStatementProvider<TestRecord> insertStatement = insert(row)
                .into(foo)
                .map(id).toProperty("id")
                .map(firstName).toProperty("firstName")
                .map(lastName).toProperty("lastName")
                .map(occupation).toNull()
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "insert into foo (id, first_name, last_name, occupation) "
                + "values (#{row.id,jdbcType=INTEGER}, #{row.firstName,jdbcType=VARCHAR}, #{row.lastName,jdbcType=VARCHAR}, null)";
        assertThat(insertStatement.getInsertStatement()).isEqualTo(expected);
    }

    @Test
    void testInsertStatementBuilderWithConstants() {

        TestRecord row = new TestRecord();

        InsertStatementProvider<TestRecord> insertStatement = insert(row)
                .into(foo)
                .map(id).toConstant("3")
                .map(firstName).toProperty("firstName")
                .map(lastName).toProperty("lastName")
                .map(occupation).toStringConstant("Y")
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "insert into foo (id, first_name, last_name, occupation) "
                + "values (3, #{row.firstName,jdbcType=VARCHAR}, #{row.lastName,jdbcType=VARCHAR}, 'Y')";
        assertThat(insertStatement.getInsertStatement()).isEqualTo(expected);
    }

    @Test
    void testSelectiveInsertStatementBuilder() {
        TestRecord row = new TestRecord(null, null, "jones", "dino driver");

        InsertStatementProvider<TestRecord> insertStatement = insert(row)
                .into(foo)
                .map(id).toPropertyWhenPresent("id", row::id)
                .map(firstName).toPropertyWhenPresent("firstName", row::firstName)
                .map(lastName).toPropertyWhenPresent("lastName", row::lastName)
                .map(occupation).toPropertyWhenPresent("occupation", row::occupation)
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "insert into foo (last_name, occupation) "
                + "values (#{row.lastName,jdbcType=VARCHAR}, #{row.occupation,jdbcType=VARCHAR})";
        assertThat(insertStatement.getInsertStatement()).isEqualTo(expected);
    }

    record TestRecord (@Nullable Integer id, @Nullable String firstName, @Nullable String lastName, @Nullable String occupation) {
        TestRecord() {
            this(null, null, null, null);
        }
    }
}
