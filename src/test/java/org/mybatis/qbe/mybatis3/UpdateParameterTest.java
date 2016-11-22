package org.mybatis.qbe.mybatis3;

import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.hamcrest.core.Is.*;
import static org.mybatis.qbe.mybatis3.UpdateParameterShortcut.*;
import static org.mybatis.qbe.sql.where.SqlConditions.*;

import java.sql.JDBCType;

import org.junit.Test;

public class UpdateParameterTest {

    @Test
    public void testUpdateParameter() {
        MyBatis3Field<Integer> id = MyBatis3Field.of("id", JDBCType.INTEGER);
        MyBatis3Field<String> firstName = MyBatis3Field.of("firstName", JDBCType.VARCHAR);
        MyBatis3Field<String> lastName = MyBatis3Field.of("lastName", JDBCType.VARCHAR);
        MyBatis3Field<String> occupation = MyBatis3Field.of("occupation", JDBCType.VARCHAR);

        UpdateParameter parameter = 
                set(firstName, "fred")
                .set(lastName, "jones")
                .setNull(occupation)
                .where(id, isEqualTo(3))
                .render();
        
        String expectedSetClause = "set firstName = #{parameters.p1,jdbcType=VARCHAR}, "
                + "lastName = #{parameters.p2,jdbcType=VARCHAR}, "
                + "occupation = #{parameters.p3,jdbcType=VARCHAR}";
                
        assertThat(parameter.getSetClause(), is(expectedSetClause));
        
        String expectedWhereClauses = "where id = #{parameters.p4,jdbcType=INTEGER}";
        assertThat(parameter.getWhereClause(), is(expectedWhereClauses));
        
        assertThat(parameter.getParameters().size(), is(4));
        assertThat(parameter.getParameters().get("p1"), is("fred"));
        assertThat(parameter.getParameters().get("p2"), is("jones"));
        assertThat(parameter.getParameters().get("p3"), is(nullValue()));
        assertThat(parameter.getParameters().get("p4"), is(3));
    }

    @Test
    public void testUpdateParameterStartWithNull() {
        MyBatis3Field<Integer> id = MyBatis3Field.of("id", JDBCType.INTEGER);
        MyBatis3Field<String> firstName = MyBatis3Field.of("firstName", JDBCType.VARCHAR);
        MyBatis3Field<String> lastName = MyBatis3Field.of("lastName", JDBCType.VARCHAR);
        MyBatis3Field<String> occupation = MyBatis3Field.of("occupation", JDBCType.VARCHAR);

        UpdateParameter parameter = 
                setNull(occupation)
                .set(firstName, "fred")
                .set(lastName, "jones")
                .where(id, isEqualTo(3))
                .and(firstName, isEqualTo("barney"))
                .render();
        
        String expectedSetClause = "set occupation = #{parameters.p1,jdbcType=VARCHAR}, "
                + "firstName = #{parameters.p2,jdbcType=VARCHAR}, "
                + "lastName = #{parameters.p3,jdbcType=VARCHAR}";
                
        assertThat(parameter.getSetClause(), is(expectedSetClause));
        
        String expectedWhereClauses = "where id = #{parameters.p4,jdbcType=INTEGER} "
                + "and firstName = #{parameters.p5,jdbcType=VARCHAR}";
        assertThat(parameter.getWhereClause(), is(expectedWhereClauses));
        
        assertThat(parameter.getParameters().size(), is(5));
        assertThat(parameter.getParameters().get("p1"), is(nullValue()));
        assertThat(parameter.getParameters().get("p2"), is("fred"));
        assertThat(parameter.getParameters().get("p3"), is("jones"));
        assertThat(parameter.getParameters().get("p4"), is(3));
        assertThat(parameter.getParameters().get("p5"), is("barney"));
    }
}
