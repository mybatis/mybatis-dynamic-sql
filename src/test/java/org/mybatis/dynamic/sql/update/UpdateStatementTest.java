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
package org.mybatis.dynamic.sql.update;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.sql.JDBCType;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;

public class UpdateStatementTest {
    private static final SqlTable foo = SqlTable.of("foo");
    private static final SqlColumn<Integer> id = foo.column("id", JDBCType.INTEGER);
    private static final SqlColumn<String> firstName = foo.column("firstName", JDBCType.VARCHAR);
    private static final SqlColumn<String> lastName = foo.column("lastName", JDBCType.VARCHAR);
    private static final SqlColumn<String> occupation = foo.column("occupation", JDBCType.VARCHAR);

    @Test
    public void testUpdateParameterWithMultipleCriteria() {
        UpdateStatementProvider updateStatement = update(foo)
                .set(firstName).equalTo("fred")
                .set(lastName).equalTo("jones")
                .set(occupation).equalToNull()
                .where(id, isEqualTo(3), or(id, isEqualTo(4)), or(id, isEqualTo(5)))
                .build()
                .render(RenderingStrategies.MYBATIS3);
        
        String expected = "update foo set firstName = #{parameters.p1,jdbcType=VARCHAR}, lastName = #{parameters.p2,jdbcType=VARCHAR}, occupation = null "
                + "where (id = #{parameters.p3,jdbcType=INTEGER} or id = #{parameters.p4,jdbcType=INTEGER} or id = #{parameters.p5,jdbcType=INTEGER})";

        assertAll(
                () -> assertThat(updateStatement.getUpdateStatement()).isEqualTo(expected),
                () -> assertThat(updateStatement.getParameters().size()).isEqualTo(5),
                () -> assertThat(updateStatement.getParameters().get("p1")).isEqualTo("fred"),
                () -> assertThat(updateStatement.getParameters().get("p2")).isEqualTo("jones"),
                () -> assertThat(updateStatement.getParameters().get("p3")).isEqualTo(3),
                () -> assertThat(updateStatement.getParameters().get("p4")).isEqualTo(4),
                () -> assertThat(updateStatement.getParameters().get("p5")).isEqualTo(5)
        );
    }

    @Test
    public void testUpdateParameterWithMultipleNestedCriteria() {
        UpdateStatementProvider updateStatement = update(foo)
                .set(firstName).equalTo("fred")
                .set(lastName).equalTo("jones")
                .set(occupation).equalToNull()
                .where(id, isEqualTo(3), or(id, isEqualTo(4), or(id, isEqualTo(5))))
                .build()
                .render(RenderingStrategies.MYBATIS3);
        
        String expected = "update foo set firstName = #{parameters.p1,jdbcType=VARCHAR}, lastName = #{parameters.p2,jdbcType=VARCHAR}, occupation = null "
                + "where (id = #{parameters.p3,jdbcType=INTEGER} or (id = #{parameters.p4,jdbcType=INTEGER} or id = #{parameters.p5,jdbcType=INTEGER}))";
                
        assertAll(
                () -> assertThat(updateStatement.getUpdateStatement()).isEqualTo(expected),
                () -> assertThat(updateStatement.getParameters().size()).isEqualTo(5),
                () -> assertThat(updateStatement.getParameters().get("p1")).isEqualTo("fred"),
                () -> assertThat(updateStatement.getParameters().get("p2")).isEqualTo("jones"),
                () -> assertThat(updateStatement.getParameters().get("p3")).isEqualTo(3),
                () -> assertThat(updateStatement.getParameters().get("p4")).isEqualTo(4),
                () -> assertThat(updateStatement.getParameters().get("p5")).isEqualTo(5)
        );
    }
    
    @Test
    public void testUpdateParameterStartWithNull() {
        UpdateStatementProvider updateStatement = update(foo)
                .set(occupation).equalToNull()
                .set(firstName).equalTo("fred")
                .set(lastName).equalTo("jones")
                .where(id, isEqualTo(3))
                .and(firstName, isEqualTo("barney"))
                .build()
                .render(RenderingStrategies.MYBATIS3);
        
        String expected = "update foo set occupation = null, firstName = #{parameters.p1,jdbcType=VARCHAR}, lastName = #{parameters.p2,jdbcType=VARCHAR} "
                + "where id = #{parameters.p3,jdbcType=INTEGER} and firstName = #{parameters.p4,jdbcType=VARCHAR}";
        
        assertAll(
                () -> assertThat(updateStatement.getUpdateStatement()).isEqualTo(expected),
                () -> assertThat(updateStatement.getParameters().size()).isEqualTo(4),
                () -> assertThat(updateStatement.getParameters().get("p1")).isEqualTo("fred"),
                () -> assertThat(updateStatement.getParameters().get("p2")).isEqualTo("jones"),
                () -> assertThat(updateStatement.getParameters().get("p3")).isEqualTo(3),
                () -> assertThat(updateStatement.getParameters().get("p4")).isEqualTo("barney")
        );
    }
    
    @Test
    public void testUpdateParameterStartWithConstant() {
        UpdateStatementProvider updateStatement = update(foo)
                .set(occupation).equalToStringConstant("Y")
                .set(firstName).equalTo("fred")
                .set(lastName).equalTo("jones")
                .set(id).equalToConstant("4")
                .where(id, isEqualTo(3))
                .and(firstName, isEqualTo("barney"))
                .build()
                .render(RenderingStrategies.MYBATIS3);
        
        String expected = "update foo set occupation = 'Y', firstName = #{parameters.p1,jdbcType=VARCHAR}, lastName = #{parameters.p2,jdbcType=VARCHAR}, id = 4 "
                + "where id = #{parameters.p3,jdbcType=INTEGER} and firstName = #{parameters.p4,jdbcType=VARCHAR}";
        
        assertAll(
                () -> assertThat(updateStatement.getUpdateStatement()).isEqualTo(expected),
                () -> assertThat(updateStatement.getParameters().size()).isEqualTo(4),
                () -> assertThat(updateStatement.getParameters().get("p1")).isEqualTo("fred"),
                () -> assertThat(updateStatement.getParameters().get("p2")).isEqualTo("jones"),
                () -> assertThat(updateStatement.getParameters().get("p3")).isEqualTo(3),
                () -> assertThat(updateStatement.getParameters().get("p4")).isEqualTo("barney")
        );
    }
    
    @Test
    public void testFullUpdateStatement() {
        UpdateStatementProvider updateStatement = update(foo)
                .set(firstName).equalTo("fred")
                .set(lastName).equalTo("jones")
                .set(occupation).equalToNull()
                .where(id, isEqualTo(3))
                .build()
                .render(RenderingStrategies.MYBATIS3);
        
        String expectedStatement = "update foo " 
                + "set firstName = #{parameters.p1,jdbcType=VARCHAR}, lastName = #{parameters.p2,jdbcType=VARCHAR}, occupation = null "
                + "where id = #{parameters.p3,jdbcType=INTEGER}";
                
        assertAll(
                () -> assertThat(updateStatement.getUpdateStatement()).isEqualTo(expectedStatement),
                () -> assertThat(updateStatement.getParameters().size()).isEqualTo(3),
                () -> assertThat(updateStatement.getParameters().get("p1")).isEqualTo("fred"),
                () -> assertThat(updateStatement.getParameters().get("p2")).isEqualTo("jones"),
                () -> assertThat(updateStatement.getParameters().get("p3")).isEqualTo(3)
        );
    }

    @Test
    public void testFullUpdateStatementNoWhere() {
        UpdateStatementProvider updateStatement = update(foo)
                .set(firstName).equalTo("fred")
                .set(lastName).equalTo("jones")
                .set(occupation).equalToNull()
                .build()
                .render(RenderingStrategies.MYBATIS3);
        
        String expectedStatement = "update foo " 
                + "set firstName = #{parameters.p1,jdbcType=VARCHAR}, lastName = #{parameters.p2,jdbcType=VARCHAR}, occupation = null";
                
        assertAll(
                () -> assertThat(updateStatement.getUpdateStatement()).isEqualTo(expectedStatement),
                () -> assertThat(updateStatement.getParameters().size()).isEqualTo(2),
                () -> assertThat(updateStatement.getParameters().get("p1")).isEqualTo("fred"),
                () -> assertThat(updateStatement.getParameters().get("p2")).isEqualTo("jones")
        );
    }
    
    @Test
    public void testUpdateStatementArithmeticOperation() {
        UpdateStatementProvider updateStatement = update(foo)
                .set(id).equalTo(add(id, constant("1")))
                .set(id).equalTo(subtract(id, constant("2")))
                .set(id).equalTo(multiply(id, constant("3")))
                .set(id).equalTo(divide(id, constant("4")))
                .build()
                .render(RenderingStrategies.MYBATIS3);
        
        String expectedStatement = "update foo " 
                + "set id = (id + 1), "
                + "id = (id - 2), "
                + "id = (id * 3), "
                + "id = (id / 4)";
                
        assertAll(
                () -> assertThat(updateStatement.getUpdateStatement()).isEqualTo(expectedStatement),
                () -> assertThat(updateStatement.getParameters().size()).isEqualTo(0)
        );
    }

    @Test
    public void testUpdateSelectStatement() {
        UpdateStatementProvider updateStatement = update(foo)
                .set(lastName).equalTo("Jones")
                .set(firstName).equalTo(select(firstName).from(foo).where(id, isEqualTo(4)))
                .where(id, isEqualTo(3))
                .build()
                .render(RenderingStrategies.MYBATIS3);
        
        String expectedStatement = "update foo " 
                + "set lastName = #{parameters.p1,jdbcType=VARCHAR}, firstName = (select firstName from foo where id = #{parameters.p2,jdbcType=INTEGER}) "
                + "where id = #{parameters.p3,jdbcType=INTEGER}";

        assertAll(
                () -> assertThat(updateStatement.getUpdateStatement()).isEqualTo(expectedStatement),
                () -> assertThat(updateStatement.getParameters().size()).isEqualTo(3),
                () -> assertThat(updateStatement.getParameters().get("p1")).isEqualTo("Jones"),
                () -> assertThat(updateStatement.getParameters().get("p2")).isEqualTo(4),
                () -> assertThat(updateStatement.getParameters().get("p3")).isEqualTo(3)
        );
    }
}

