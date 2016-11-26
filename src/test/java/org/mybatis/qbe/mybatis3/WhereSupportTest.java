package org.mybatis.qbe.mybatis3;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mybatis.qbe.sql.SqlConditions.and;
import static org.mybatis.qbe.sql.SqlConditions.isEqualTo;
import static org.mybatis.qbe.sql.SqlConditions.isLessThan;
import static org.mybatis.qbe.sql.SqlConditions.or;
import static org.mybatis.qbe.sql.where.WhereSupportBuilder.*;

import java.sql.JDBCType;
import java.util.Date;
import java.util.Map;

import org.junit.Test;
import org.mybatis.qbe.sql.where.WhereSupport;

public class WhereSupportTest {
    public static final MyBatis3Field<Date> field1 = MyBatis3Field.of("field1", JDBCType.DATE, "a");
    public static final MyBatis3Field<Integer> field2 = MyBatis3Field.of("field2", JDBCType.INTEGER).withAlias("a");

    @Test
    public void testSimpleCriteriaWithoutAlias() {
        Date d = new Date();

        WhereSupport whereSupport = where(field1, isEqualTo(d))
                .or(field2, isEqualTo(4))
                .and(field2, isLessThan(3))
                .buildIgnoringAlias();

        assertThat(whereSupport.getWhereClause(), is("where field1 = #{parameters.p1,jdbcType=DATE} or field2 = #{parameters.p2,jdbcType=INTEGER} and field2 < #{parameters.p3,jdbcType=INTEGER}"));
        
        Map<String, Object> parameters = whereSupport.getParameters();
        assertThat(parameters.get("p1"), is(d));
        assertThat(parameters.get("p2"), is(4));
        assertThat(parameters.get("p3"), is(3));
    }

    @Test
    public void testComplexCriteriaWithoutAlias() {
        Date d = new Date();

        WhereSupport whereSupport = where(field1, isEqualTo(d))
                .or(field2, isEqualTo(4))
                .and(field2, isLessThan(3))
                .or(field2, isEqualTo(4), and(field2, isEqualTo(6)))
                .and(field2, isLessThan(3), or(field1, isEqualTo(d)))
                .buildIgnoringAlias();
        

        String expected = "where field1 = #{parameters.p1,jdbcType=DATE}" +
                " or field2 = #{parameters.p2,jdbcType=INTEGER}" +
                " and field2 < #{parameters.p3,jdbcType=INTEGER}" +
                " or (field2 = #{parameters.p4,jdbcType=INTEGER} and field2 = #{parameters.p5,jdbcType=INTEGER})" +
                " and (field2 < #{parameters.p6,jdbcType=INTEGER} or field1 = #{parameters.p7,jdbcType=DATE})";
        
        assertThat(whereSupport.getWhereClause(), is(expected));
        
        Map<String, Object> parameters = whereSupport.getParameters();
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

        WhereSupport whereSupport = where(field1, isEqualTo(d))
                .or(field2, isEqualTo(4))
                .and(field2, isLessThan(3))
                .build();

        assertThat(whereSupport.getWhereClause(), is("where a.field1 = #{parameters.p1,jdbcType=DATE} or a.field2 = #{parameters.p2,jdbcType=INTEGER} and a.field2 < #{parameters.p3,jdbcType=INTEGER}"));
        
        Map<String, Object> parameters = whereSupport.getParameters();
        assertThat(parameters.get("p1"), is(d));
        assertThat(parameters.get("p2"), is(4));
        assertThat(parameters.get("p3"), is(3));
    }

    @Test
    public void testComplexCriteriaWithAlias() {
        Date d = new Date();

        WhereSupport whereSupport = where(field1, isEqualTo(d))
                .or(field2, isEqualTo(4))
                .and(field2, isLessThan(3))
                .or(field2, isEqualTo(4), and(field2, isEqualTo(6)))
                .and(field2, isLessThan(3), or(field1, isEqualTo(d)))
                .build();
        

        String expected = "where a.field1 = #{parameters.p1,jdbcType=DATE}" +
                " or a.field2 = #{parameters.p2,jdbcType=INTEGER}" +
                " and a.field2 < #{parameters.p3,jdbcType=INTEGER}" +
                " or (a.field2 = #{parameters.p4,jdbcType=INTEGER} and a.field2 = #{parameters.p5,jdbcType=INTEGER})" +
                " and (a.field2 < #{parameters.p6,jdbcType=INTEGER} or a.field1 = #{parameters.p7,jdbcType=DATE})";
        
        assertThat(whereSupport.getWhereClause(), is(expected));
        
        Map<String, Object> parameters = whereSupport.getParameters();
        assertThat(parameters.get("p1"), is(d));
        assertThat(parameters.get("p2"), is(4));
        assertThat(parameters.get("p3"), is(3));
        assertThat(parameters.get("p4"), is(4));
        assertThat(parameters.get("p5"), is(6));
        assertThat(parameters.get("p6"), is(3));
        assertThat(parameters.get("p7"), is(d));
    }
}
