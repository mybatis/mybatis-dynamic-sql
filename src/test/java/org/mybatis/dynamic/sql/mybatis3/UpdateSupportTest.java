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
package org.mybatis.dynamic.sql.mybatis3;

import static org.junit.Assert.assertThat;
import static org.hamcrest.core.Is.*;
import static org.mybatis.dynamic.sql.SqlConditions.*;
import static org.mybatis.dynamic.sql.update.UpdateSupportBuilder.*;

import java.sql.JDBCType;

import org.junit.Test;
import org.mybatis.dynamic.sql.MyBatis3Column;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.update.UpdateSupport;

public class UpdateSupportTest {
    private static final SqlTable foo = SqlTable.of("foo");
    private static final MyBatis3Column<Integer> id = MyBatis3Column.of("id", JDBCType.INTEGER);
    private static final MyBatis3Column<String> firstName = MyBatis3Column.of("firstName", JDBCType.VARCHAR);
    private static final MyBatis3Column<String> lastName = MyBatis3Column.of("lastName", JDBCType.VARCHAR);
    private static final MyBatis3Column<String> occupation = MyBatis3Column.of("occupation", JDBCType.VARCHAR);

    @Test
    public void testUpdateParameter() {
        UpdateSupport updateSupport = update(foo)
                .set(firstName).equalTo("fred")
                .set(lastName).equalTo("jones")
                .set(occupation).equalToNull()
                .where(id, isEqualTo(3))
                .build();
        
        String expectedSetClause = "set firstName = #{parameters.up1,jdbcType=VARCHAR}, "
                + "lastName = #{parameters.up2,jdbcType=VARCHAR}, "
                + "occupation = null";
                
        assertThat(updateSupport.getSetClause(), is(expectedSetClause));
        
        String expectedWhereClauses = "where id = #{parameters.p1,jdbcType=INTEGER}";
        assertThat(updateSupport.getWhereClause(), is(expectedWhereClauses));
        
        assertThat(updateSupport.getParameters().size(), is(3));
        assertThat(updateSupport.getParameters().get("up1"), is("fred"));
        assertThat(updateSupport.getParameters().get("up2"), is("jones"));
        assertThat(updateSupport.getParameters().get("p1"), is(3));
    }

    @Test
    public void testUpdateParameterStartWithNull() {
        UpdateSupport updateSupport = update(foo)
                .set(occupation).equalToNull()
                .set(firstName).equalTo("fred")
                .set(lastName).equalTo("jones")
                .where(id, isEqualTo(3))
                .and(firstName, isEqualTo("barney"))
                .build();
        
        String expectedSetClause = "set occupation = null, "
                + "firstName = #{parameters.up1,jdbcType=VARCHAR}, "
                + "lastName = #{parameters.up2,jdbcType=VARCHAR}";
                
        assertThat(updateSupport.getSetClause(), is(expectedSetClause));
        
        String expectedWhereClauses = "where id = #{parameters.p1,jdbcType=INTEGER} "
                + "and firstName = #{parameters.p2,jdbcType=VARCHAR}";
        assertThat(updateSupport.getWhereClause(), is(expectedWhereClauses));
        
        assertThat(updateSupport.getParameters().size(), is(4));
        assertThat(updateSupport.getParameters().get("up1"), is("fred"));
        assertThat(updateSupport.getParameters().get("up2"), is("jones"));
        assertThat(updateSupport.getParameters().get("p1"), is(3));
        assertThat(updateSupport.getParameters().get("p2"), is("barney"));
    }
}
