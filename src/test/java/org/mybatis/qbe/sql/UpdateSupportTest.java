package org.mybatis.qbe.sql;

import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.hamcrest.core.Is.*;
import static org.mybatis.qbe.sql.SqlConditions.*;
import static org.mybatis.qbe.sql.update.UpdateSupportBuilder.*;

import java.sql.JDBCType;

import org.junit.Test;
import org.mybatis.qbe.sql.update.UpdateSupport;

public class UpdateSupportTest {

    @Test
    public void testUpdateParameter() {
        SqlField<Integer> id = SqlField.of("id", JDBCType.INTEGER);
        SqlField<String> firstName = SqlField.of("firstName", JDBCType.VARCHAR);
        SqlField<String> lastName = SqlField.of("lastName", JDBCType.VARCHAR);
        SqlField<String> occupation = SqlField.of("occupation", JDBCType.VARCHAR);

        UpdateSupport updateSupport = updateSupport()
                .set(firstName, "fred")
                .set(lastName, "jones")
                .setNull(occupation)
                .where(id, isEqualTo(3))
                .build();
        
        String expectedSetClause = "set firstName = ?, lastName = ?, occupation = ?";
                
        assertThat(updateSupport.getSetClause(), is(expectedSetClause));
        
        String expectedWhereClauses = "where id = ?";
        assertThat(updateSupport.getWhereClause(), is(expectedWhereClauses));
        
        assertThat(updateSupport.getParameters().size(), is(4));
        assertThat(updateSupport.getParameters().get("p1"), is("fred"));
        assertThat(updateSupport.getParameters().get("p2"), is("jones"));
        assertThat(updateSupport.getParameters().get("p3"), is(nullValue()));
        assertThat(updateSupport.getParameters().get("p4"), is(3));
    }

    @Test
    public void testUpdateParameterStartWithNull() {
        SqlField<Integer> id = SqlField.of("id", JDBCType.INTEGER);
        SqlField<String> firstName = SqlField.of("firstName", JDBCType.VARCHAR);
        SqlField<String> lastName = SqlField.of("lastName", JDBCType.VARCHAR);
        SqlField<String> occupation = SqlField.of("occupation", JDBCType.VARCHAR);

        UpdateSupport updateSupport = updateSupport()
                .setNull(occupation)
                .set(firstName, "fred")
                .set(lastName, "jones")
                .where(id, isEqualTo(3))
                .and(firstName, isEqualTo("barney"))
                .build();
        
        String expectedSetClause = "set occupation = ?, firstName = ?, lastName = ?";
                
        assertThat(updateSupport.getSetClause(), is(expectedSetClause));
        
        String expectedWhereClauses = "where id = ? and firstName = ?";
        assertThat(updateSupport.getWhereClause(), is(expectedWhereClauses));
        
        assertThat(updateSupport.getParameters().size(), is(5));
        assertThat(updateSupport.getParameters().get("p1"), is(nullValue()));
        assertThat(updateSupport.getParameters().get("p2"), is("fred"));
        assertThat(updateSupport.getParameters().get("p3"), is("jones"));
        assertThat(updateSupport.getParameters().get("p4"), is(3));
        assertThat(updateSupport.getParameters().get("p5"), is("barney"));
    }
}
