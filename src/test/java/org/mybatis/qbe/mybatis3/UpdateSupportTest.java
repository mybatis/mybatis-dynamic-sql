package org.mybatis.qbe.mybatis3;

import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.hamcrest.core.Is.*;
import static org.mybatis.qbe.sql.update.UpdateSupportShortcut.*;
import static org.mybatis.qbe.sql.where.SqlConditions.*;

import java.sql.JDBCType;

import org.junit.Test;
import org.mybatis.qbe.sql.update.UpdateSupport;

public class UpdateSupportTest {

    @Test
    public void testUpdateParameter() {
        MyBatis3Field<Integer> id = MyBatis3Field.of("id", JDBCType.INTEGER);
        MyBatis3Field<String> firstName = MyBatis3Field.of("firstName", JDBCType.VARCHAR);
        MyBatis3Field<String> lastName = MyBatis3Field.of("lastName", JDBCType.VARCHAR);
        MyBatis3Field<String> occupation = MyBatis3Field.of("occupation", JDBCType.VARCHAR);

        UpdateSupport updateSupport = 
                set(firstName, "fred")
                .andSet(lastName, "jones")
                .andSetNull(occupation)
                .where(id, isEqualTo(3))
                .build();
        
        String expectedSetClause = "set firstName = #{parameters.p1,jdbcType=VARCHAR}, "
                + "lastName = #{parameters.p2,jdbcType=VARCHAR}, "
                + "occupation = #{parameters.p3,jdbcType=VARCHAR}";
                
        assertThat(updateSupport.getSetClause(), is(expectedSetClause));
        
        String expectedWhereClauses = "where id = #{parameters.p4,jdbcType=INTEGER}";
        assertThat(updateSupport.getWhereClause(), is(expectedWhereClauses));
        
        assertThat(updateSupport.getParameters().size(), is(4));
        assertThat(updateSupport.getParameters().get("p1"), is("fred"));
        assertThat(updateSupport.getParameters().get("p2"), is("jones"));
        assertThat(updateSupport.getParameters().get("p3"), is(nullValue()));
        assertThat(updateSupport.getParameters().get("p4"), is(3));
    }

    @Test
    public void testUpdateParameterStartWithNull() {
        MyBatis3Field<Integer> id = MyBatis3Field.of("id", JDBCType.INTEGER);
        MyBatis3Field<String> firstName = MyBatis3Field.of("firstName", JDBCType.VARCHAR);
        MyBatis3Field<String> lastName = MyBatis3Field.of("lastName", JDBCType.VARCHAR);
        MyBatis3Field<String> occupation = MyBatis3Field.of("occupation", JDBCType.VARCHAR);

        UpdateSupport updateSupport = 
                setNull(occupation)
                .andSet(firstName, "fred")
                .andSet(lastName, "jones")
                .where(id, isEqualTo(3))
                .and(firstName, isEqualTo("barney"))
                .build();
        
        String expectedSetClause = "set occupation = #{parameters.p1,jdbcType=VARCHAR}, "
                + "firstName = #{parameters.p2,jdbcType=VARCHAR}, "
                + "lastName = #{parameters.p3,jdbcType=VARCHAR}";
                
        assertThat(updateSupport.getSetClause(), is(expectedSetClause));
        
        String expectedWhereClauses = "where id = #{parameters.p4,jdbcType=INTEGER} "
                + "and firstName = #{parameters.p5,jdbcType=VARCHAR}";
        assertThat(updateSupport.getWhereClause(), is(expectedWhereClauses));
        
        assertThat(updateSupport.getParameters().size(), is(5));
        assertThat(updateSupport.getParameters().get("p1"), is(nullValue()));
        assertThat(updateSupport.getParameters().get("p2"), is("fred"));
        assertThat(updateSupport.getParameters().get("p3"), is("jones"));
        assertThat(updateSupport.getParameters().get("p4"), is(3));
        assertThat(updateSupport.getParameters().get("p5"), is("barney"));
    }
}
