package org.mybatis.qbe.sql;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mybatis.qbe.sql.SqlConditions.and;
import static org.mybatis.qbe.sql.SqlConditions.isEqualTo;
import static org.mybatis.qbe.sql.SqlConditions.isLessThan;
import static org.mybatis.qbe.sql.SqlConditions.or;
import static org.mybatis.qbe.sql.render.RenderingShortcut.*;

import java.sql.JDBCType;
import java.util.Date;
import java.util.Map;

import org.junit.Test;
import org.mybatis.qbe.sql.render.RenderedWhereClause;

public class RenderedWhereClauseTest {
    public static final SqlField<Date> field1 = SqlField.of("field1", JDBCType.DATE, "a");
    public static final SqlField<Integer> field2 = SqlField.of("field2", JDBCType.INTEGER, "a");

    @Test
    public void testSimpleCriteriaWithoutAlias() {
        Date d = new Date();

        RenderedWhereClause renderedWhereClause = whereIgnoringAlias(field1, isEqualTo(d))
                .or(field2, isEqualTo(4))
                .and(field2, isLessThan(3))
                .render();

        assertThat(renderedWhereClause.getWhereClause(), is("where field1 = ? or field2 = ? and field2 < ?"));
        
        Map<String, Object> parameters = renderedWhereClause.getParameters();
        assertThat(parameters.get("p1"), is(d));
        assertThat(parameters.get("p2"), is(4));
        assertThat(parameters.get("p3"), is(3));
    }

    @Test
    public void testComplexCriteriaWithoutAlias() {
        Date d = new Date();

        RenderedWhereClause renderedWhereClause = whereIgnoringAlias(field1, isEqualTo(d))
                .or(field2, isEqualTo(4))
                .and(field2, isLessThan(3))
                .or(field2, isEqualTo(4), and(field2, isEqualTo(6)))
                .and(field2, isLessThan(3), or(field1, isEqualTo(d)))
                .render();
        

        String expected = "where field1 = ?" +
                " or field2 = ?" +
                " and field2 < ?" +
                " or (field2 = ? and field2 = ?)" +
                " and (field2 < ? or field1 = ?)";
        
        assertThat(renderedWhereClause.getWhereClause(), is(expected));
        
        Map<String, Object> parameters = renderedWhereClause.getParameters();
        assertThat(parameters.get("p1"), is(d));
        assertThat(parameters.get("p2"), is(4));
        assertThat(parameters.get("p3"), is(3));
        assertThat(parameters.get("p4"), is(4));
        assertThat(parameters.get("p5"), is(6));
        assertThat(parameters.get("p6"), is(3));
        assertThat(parameters.get("p7"), is(d));
    }

    @Test
    public void testSimpleCriteriaWithAlias() {
        Date d = new Date();

        RenderedWhereClause renderedWhereClause = where(field1, isEqualTo(d))
                .or(field2, isEqualTo(4))
                .and(field2, isLessThan(3))
                .render();

        assertThat(renderedWhereClause.getWhereClause(), is("where a.field1 = ? or a.field2 = ? and a.field2 < ?"));
        
        Map<String, Object> parameters = renderedWhereClause.getParameters();
        assertThat(parameters.get("p1"), is(d));
        assertThat(parameters.get("p2"), is(4));
        assertThat(parameters.get("p3"), is(3));
    }

    @Test
    public void testComplexCriteriaWithAlias() {
        Date d = new Date();

        RenderedWhereClause renderedWhereClause = where(field1, isEqualTo(d))
                .or(field2, isEqualTo(4))
                .and(field2, isLessThan(3))
                .or(field2, isEqualTo(4), and(field2, isEqualTo(6)))
                .and(field2, isLessThan(3), or(field1, isEqualTo(d)))
                .render();
        

        String expected = "where a.field1 = ?" +
                " or a.field2 = ?" +
                " and a.field2 < ?" +
                " or (a.field2 = ? and a.field2 = ?)" +
                " and (a.field2 < ? or a.field1 = ?)";
        
        assertThat(renderedWhereClause.getWhereClause(), is(expected));
        
        Map<String, Object> parameters = renderedWhereClause.getParameters();
        assertThat(parameters.get("p1"), is(d));
        assertThat(parameters.get("p2"), is(4));
        assertThat(parameters.get("p3"), is(3));
        assertThat(parameters.get("p4"), is(4));
        assertThat(parameters.get("p5"), is(6));
        assertThat(parameters.get("p6"), is(3));
        assertThat(parameters.get("p7"), is(d));
    }
}
