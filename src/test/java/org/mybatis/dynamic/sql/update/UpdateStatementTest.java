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
package org.mybatis.dynamic.sql.update;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.sql.JDBCType;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;

@RunWith(JUnitPlatform.class)
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
                .render(RenderingStrategy.MYBATIS3);
        
        String expected = "update foo set firstName = #{parameters.up1,jdbcType=VARCHAR}, lastName = #{parameters.up2,jdbcType=VARCHAR}, occupation = null "
                + "where (id = #{parameters.p1,jdbcType=INTEGER} or id = #{parameters.p2,jdbcType=INTEGER} or id = #{parameters.p3,jdbcType=INTEGER})";
                
        assertThat(updateStatement.getUpdateStatement()).isEqualTo(expected);
        assertThat(updateStatement.getParameters().size()).isEqualTo(5);
        assertThat(updateStatement.getParameters().get("up1")).isEqualTo("fred");
        assertThat(updateStatement.getParameters().get("up2")).isEqualTo("jones");
        assertThat(updateStatement.getParameters().get("p1")).isEqualTo(3);
        assertThat(updateStatement.getParameters().get("p2")).isEqualTo(4);
        assertThat(updateStatement.getParameters().get("p3")).isEqualTo(5);
    }

    @Test
    public void testUpdateParameterWithMultipleNestedCriteria() {
        UpdateStatementProvider updateStatement = update(foo)
                .set(firstName).equalTo("fred")
                .set(lastName).equalTo("jones")
                .set(occupation).equalToNull()
                .where(id, isEqualTo(3), or(id, isEqualTo(4), or(id, isEqualTo(5))))
                .build()
                .render(RenderingStrategy.MYBATIS3);
        
        String expected = "update foo set firstName = #{parameters.up1,jdbcType=VARCHAR}, lastName = #{parameters.up2,jdbcType=VARCHAR}, occupation = null "
                + "where (id = #{parameters.p1,jdbcType=INTEGER} or (id = #{parameters.p2,jdbcType=INTEGER} or id = #{parameters.p3,jdbcType=INTEGER}))";
                
        assertThat(updateStatement.getUpdateStatement()).isEqualTo(expected);
        assertThat(updateStatement.getParameters().size()).isEqualTo(5);
        assertThat(updateStatement.getParameters().get("up1")).isEqualTo("fred");
        assertThat(updateStatement.getParameters().get("up2")).isEqualTo("jones");
        assertThat(updateStatement.getParameters().get("p1")).isEqualTo(3);
        assertThat(updateStatement.getParameters().get("p2")).isEqualTo(4);
        assertThat(updateStatement.getParameters().get("p3")).isEqualTo(5);
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
                .render(RenderingStrategy.MYBATIS3);
        
        String expected = "update foo set occupation = null, firstName = #{parameters.up1,jdbcType=VARCHAR}, lastName = #{parameters.up2,jdbcType=VARCHAR} "
                + "where id = #{parameters.p1,jdbcType=INTEGER} and firstName = #{parameters.p2,jdbcType=VARCHAR}";
                
        assertThat(updateStatement.getUpdateStatement()).isEqualTo(expected);
        assertThat(updateStatement.getParameters().size()).isEqualTo(4);
        assertThat(updateStatement.getParameters().get("up1")).isEqualTo("fred");
        assertThat(updateStatement.getParameters().get("up2")).isEqualTo("jones");
        assertThat(updateStatement.getParameters().get("p1")).isEqualTo(3);
        assertThat(updateStatement.getParameters().get("p2")).isEqualTo("barney");
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
                .render(RenderingStrategy.MYBATIS3);
        
        String expected = "update foo set occupation = 'Y', firstName = #{parameters.up1,jdbcType=VARCHAR}, lastName = #{parameters.up2,jdbcType=VARCHAR}, id = 4 "
                + "where id = #{parameters.p1,jdbcType=INTEGER} and firstName = #{parameters.p2,jdbcType=VARCHAR}";
                
        assertThat(updateStatement.getUpdateStatement()).isEqualTo(expected);
        assertThat(updateStatement.getParameters().size()).isEqualTo(4);
        assertThat(updateStatement.getParameters().get("up1")).isEqualTo("fred");
        assertThat(updateStatement.getParameters().get("up2")).isEqualTo("jones");
        assertThat(updateStatement.getParameters().get("p1")).isEqualTo(3);
        assertThat(updateStatement.getParameters().get("p2")).isEqualTo("barney");
    }
    
    @Test
    public void testFullUpdateStatement() {
        UpdateStatementProvider updateStatement = update(foo)
                .set(firstName).equalTo("fred")
                .set(lastName).equalTo("jones")
                .set(occupation).equalToNull()
                .where(id, isEqualTo(3))
                .build()
                .render(RenderingStrategy.MYBATIS3);
        
        String expectedStatement = "update foo " 
                + "set firstName = #{parameters.up1,jdbcType=VARCHAR}, lastName = #{parameters.up2,jdbcType=VARCHAR}, occupation = null "
                + "where id = #{parameters.p1,jdbcType=INTEGER}";
                
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(updateStatement.getUpdateStatement()).isEqualTo(expectedStatement);
        
            softly.assertThat(updateStatement.getParameters().size()).isEqualTo(3);
            softly.assertThat(updateStatement.getParameters().get("up1")).isEqualTo("fred");
            softly.assertThat(updateStatement.getParameters().get("up2")).isEqualTo("jones");
            softly.assertThat(updateStatement.getParameters().get("p1")).isEqualTo(3);
        });
    }

    @Test
    public void testFullUpdateStatementNoWhere() {
        UpdateStatementProvider updateStatement = update(foo)
                .set(firstName).equalTo("fred")
                .set(lastName).equalTo("jones")
                .set(occupation).equalToNull()
                .build()
                .render(RenderingStrategy.MYBATIS3);
        
        String expectedStatement = "update foo " 
                + "set firstName = #{parameters.up1,jdbcType=VARCHAR}, lastName = #{parameters.up2,jdbcType=VARCHAR}, occupation = null";
                
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(updateStatement.getUpdateStatement()).isEqualTo(expectedStatement);
        
            softly.assertThat(updateStatement.getParameters().size()).isEqualTo(2);
            softly.assertThat(updateStatement.getParameters().get("up1")).isEqualTo("fred");
            softly.assertThat(updateStatement.getParameters().get("up2")).isEqualTo("jones");
        });
    }
}
