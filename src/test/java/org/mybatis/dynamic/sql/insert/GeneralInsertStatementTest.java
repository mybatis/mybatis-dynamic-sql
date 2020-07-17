/**
 *    Copyright 2016-2020 the original author or authors.
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
import static org.mybatis.dynamic.sql.SqlBuilder.insertInto;

import java.sql.JDBCType;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.insert.render.GeneralInsertStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategies;

public class GeneralInsertStatementTest {

    private static final SqlTable foo = SqlTable.of("foo");
    private static final SqlColumn<Integer> id = foo.column("id", JDBCType.INTEGER);
    private static final SqlColumn<String> firstName = foo.column("first_name", JDBCType.VARCHAR);
    private static final SqlColumn<String> lastName = foo.column("last_name", JDBCType.VARCHAR);
    private static final SqlColumn<String> occupation = foo.column("occupation", JDBCType.VARCHAR);
    
    @Test
    public void testFullInsertStatementBuilder() {

        GeneralInsertStatementProvider insertStatement = insertInto(foo)
                .set(id).equalTo(2)
                .set(firstName).equalTo("Jones")
                .set(occupation).equalTo("dino driver")
                .build()
                .render(RenderingStrategies.MYBATIS3);
        
        String expectedStatement = "insert into foo "
                + "(id, first_name, occupation) "
                + "values (#{parameters.p1,jdbcType=INTEGER}, #{parameters.p2,jdbcType=VARCHAR}, #{parameters.p3,jdbcType=VARCHAR})";
        
        assertThat(insertStatement.getInsertStatement()).isEqualTo(expectedStatement);
    }

    @Test
    public void testInsertStatementBuilderWithNulls() {

        GeneralInsertStatementProvider insertStatement = insertInto(foo)
                .set(id).equalTo(1)
                .set(firstName).equalTo("Fred")
                .set(lastName).equalTo("Smith")
                .set(occupation).equalToNull()
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "insert into foo (id, first_name, last_name, occupation) "
                + "values (#{parameters.p1,jdbcType=INTEGER}, #{parameters.p2,jdbcType=VARCHAR}, #{parameters.p3,jdbcType=VARCHAR}, null)";
        assertThat(insertStatement.getInsertStatement()).isEqualTo(expected);
    }

    @Test
    public void testInsertStatementBuilderWithConstants() {

        GeneralInsertStatementProvider insertStatement = insertInto(foo)
                .set(id).equalToConstant("3")
                .set(firstName).equalTo("Fred")
                .set(lastName).equalTo("Jones")
                .set(occupation).equalToStringConstant("Y")
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "insert into foo (id, first_name, last_name, occupation) "
                + "values (3, #{parameters.p1,jdbcType=VARCHAR}, #{parameters.p2,jdbcType=VARCHAR}, 'Y')";
        assertThat(insertStatement.getInsertStatement()).isEqualTo(expected);
    }
    
    @Test
    public void testSelectiveInsertStatementBuilder() {
        Integer myId = null;
        String myFirstName = null;
        String myLastName = "jones";
        String myOccupation = "dino driver";
        
        GeneralInsertStatementProvider insertStatement = insertInto(foo)
                .set(id).equalToWhenPresent(() -> myId)
                .set(firstName).equalToWhenPresent(myFirstName)
                .set(lastName).equalToWhenPresent(() -> myLastName)
                .set(occupation).equalToWhenPresent(myOccupation)
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "insert into foo (last_name, occupation) "
                + "values (#{parameters.p1,jdbcType=VARCHAR}, #{parameters.p2,jdbcType=VARCHAR})";
        assertThat(insertStatement.getInsertStatement()).isEqualTo(expected);
    }
}
