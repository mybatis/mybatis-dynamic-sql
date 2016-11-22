package org.mybatis.qbe.sql.set;

import static org.junit.Assert.assertThat;
import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.IsNull.nullValue;

import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mybatis.qbe.mybatis3.MyBatis3Field;
import org.mybatis.qbe.sql.set.SetClause;
import org.mybatis.qbe.sql.set.render.RenderedSetClause;
import org.mybatis.qbe.sql.set.render.SetClauseRenderer;
import org.mybatis.qbe.sql.where.SqlField;

public class SetClauseTest {

    @Test
    public void testBasicSetClause() {
        SqlField<Integer> id = SqlField.of("id", JDBCType.INTEGER);
        SqlField<String> firstName = SqlField.of("firstName", JDBCType.VARCHAR);
        SqlField<String> lastName = SqlField.of("lastName", JDBCType.VARCHAR);
        SqlField<String> occupation = SqlField.of("occupation", JDBCType.VARCHAR);
        
        SetClause sc = new SetClause.Builder(firstName, "fred")
                .set(lastName, "jones")
                .setNull(occupation)
                .set(id, 3)
                .build();
        
        List<Object> values = new ArrayList<>();
        List<String> phrases = new ArrayList<>();
        sc.visitPhrases(p -> {
            values.add(p.getValue());
            phrases.add(p.getField().render());
        });

        assertThat(values.size(), is(4));
        assertThat(values.get(0), is("fred"));
        assertThat(values.get(1), is("jones"));
        assertThat(values.get(2), is(nullValue()));
        assertThat(values.get(3), is(3));

        assertThat(phrases.size(), is(4));
        assertThat(phrases.get(0), is(firstName.render()));
        assertThat(phrases.get(1), is(lastName.render()));
        assertThat(phrases.get(2), is(occupation.render()));
        assertThat(phrases.get(3), is(id.render()));
    }

    @Test
    public void testSetClauseRenderer() {
        SqlField<Integer> id = SqlField.of("id", JDBCType.INTEGER);
        SqlField<String> firstName = SqlField.of("firstName", JDBCType.VARCHAR);
        SqlField<String> lastName = SqlField.of("lastName", JDBCType.VARCHAR);
        SqlField<String> occupation = SqlField.of("occupation", JDBCType.VARCHAR);
        
        SetClause sc = new SetClause.Builder(firstName, "fred")
                .set(lastName, "jones")
                .setNull(occupation)
                .set(id, 3)
                .build();
        
        RenderedSetClause rsc = SetClauseRenderer.of(sc).render();

        String expectedSetClause = "set firstName = ?, lastName = ?, occupation = ?, id = ?";
        
        assertThat(rsc.getSetClause(), is(expectedSetClause));

        assertThat(rsc.getParameters().size(), is(4));
        assertThat(rsc.getParameters().get("p1"), is("fred"));
        assertThat(rsc.getParameters().get("p2"), is("jones"));
        assertThat(rsc.getParameters().get("p3"), is(nullValue()));
        assertThat(rsc.getParameters().get("p4"), is(3));
    }

    @Test
    public void testSetClauseRendererStartWithNull() {
        SqlField<Integer> id = SqlField.of("id", JDBCType.INTEGER);
        SqlField<String> firstName = SqlField.of("firstName", JDBCType.VARCHAR);
        SqlField<String> lastName = SqlField.of("lastName", JDBCType.VARCHAR);
        SqlField<String> occupation = SqlField.of("occupation", JDBCType.VARCHAR);
        
        SetClause sc = new SetClause.Builder(occupation)
                .set(firstName, "fred")
                .set(lastName, "jones")
                .set(id, 3)
                .build();
        
        RenderedSetClause rsc = SetClauseRenderer.of(sc).render();

        String expectedSetClause = "set occupation = ?, firstName = ?, lastName = ?, id = ?";
        
        assertThat(rsc.getSetClause(), is(expectedSetClause));

        assertThat(rsc.getParameters().size(), is(4));
        assertThat(rsc.getParameters().get("p1"), is(nullValue()));
        assertThat(rsc.getParameters().get("p2"), is("fred"));
        assertThat(rsc.getParameters().get("p3"), is("jones"));
        assertThat(rsc.getParameters().get("p4"), is(3));
    }

    @Test
    public void testSetClauseRendererForMyBatis() {
        MyBatis3Field<Integer> id = MyBatis3Field.of("id", JDBCType.INTEGER);
        MyBatis3Field<String> firstName = MyBatis3Field.of("firstName", JDBCType.VARCHAR);
        MyBatis3Field<String> lastName = MyBatis3Field.of("lastName", JDBCType.VARCHAR);
        MyBatis3Field<String> occupation = MyBatis3Field.of("occupation", JDBCType.VARCHAR);
        
        SetClause sc = new SetClause.Builder(firstName, "fred")
                .set(lastName, "jones")
                .setNull(occupation)
                .set(id, 3)
                .build();
        
        RenderedSetClause rsc = SetClauseRenderer.of(sc).render();

        String expectedSetClause = "set firstName = #{parameters.p1,jdbcType=VARCHAR}, " 
                + "lastName = #{parameters.p2,jdbcType=VARCHAR}, "
                + "occupation = #{parameters.p3,jdbcType=VARCHAR}, "
                + "id = #{parameters.p4,jdbcType=INTEGER}";
        
        assertThat(rsc.getSetClause(), is(expectedSetClause));

        assertThat(rsc.getParameters().size(), is(4));
        assertThat(rsc.getParameters().get("p1"), is("fred"));
        assertThat(rsc.getParameters().get("p2"), is("jones"));
        assertThat(rsc.getParameters().get("p3"), is(nullValue()));
        assertThat(rsc.getParameters().get("p4"), is(3));
    }
}
