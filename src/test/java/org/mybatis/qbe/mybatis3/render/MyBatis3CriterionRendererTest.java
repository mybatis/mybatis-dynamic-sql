package org.mybatis.qbe.mybatis3.render;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.sql.JDBCType;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.mybatis.qbe.Criterion;
import org.mybatis.qbe.condition.IsEqualToCondition;
import org.mybatis.qbe.field.Field;
import org.mybatis.qbe.mybatis3.WhereClauseAndParameters;

public class MyBatis3CriterionRendererTest {

    @Test
    public void testAliasWithIgnore() {
        Field<Integer> field = Field.of("id", JDBCType.INTEGER).withAlias("a");
        IsEqualToCondition<Integer> condition = IsEqualToCondition.of(3);
        Criterion<Integer> criterion = Criterion.of(field, condition);
        AtomicInteger sequence = new AtomicInteger(1);
        MyBatis3CriterionRenderer renderer = MyBatis3CriterionRenderer.of(criterion, sequence);
        
        WhereClauseAndParameters rc = renderer.render(true);
        assertThat(rc.getWhereClause(), is(" id = #{parameters.p1,jdbcType=INTEGER}"));
        assertThat(rc.getParameters().size(), is(1));
    }

    @Test
    public void testAliasWithoutIgnore() {
        Field<Integer> field = Field.of("id", JDBCType.INTEGER).withAlias("a");
        IsEqualToCondition<Integer> condition = IsEqualToCondition.of(3);
        Criterion<Integer> criterion = Criterion.of(field, condition);
        AtomicInteger sequence = new AtomicInteger(1);
        MyBatis3CriterionRenderer renderer = MyBatis3CriterionRenderer.of(criterion, sequence);
        
        WhereClauseAndParameters rc = renderer.render(false);
        assertThat(rc.getWhereClause(), is(" a.id = #{parameters.p1,jdbcType=INTEGER}"));
        assertThat(rc.getParameters().size(), is(1));
    }

    @Test
    public void testNoAliasWithIgnore() {
        Field<Integer> field = Field.of("id", JDBCType.INTEGER);
        IsEqualToCondition<Integer> condition = IsEqualToCondition.of(3);
        Criterion<Integer> criterion = Criterion.of(field, condition);
        AtomicInteger sequence = new AtomicInteger(1);
        MyBatis3CriterionRenderer renderer = MyBatis3CriterionRenderer.of(criterion, sequence);
        
        WhereClauseAndParameters rc = renderer.render(true);
        assertThat(rc.getWhereClause(), is(" id = #{parameters.p1,jdbcType=INTEGER}"));
        assertThat(rc.getParameters().size(), is(1));
    }

    @Test
    public void testNoAliasWithoutIgnore() {
        Field<Integer> field = Field.of("id", JDBCType.INTEGER);
        IsEqualToCondition<Integer> condition = IsEqualToCondition.of(3);
        Criterion<Integer> criterion = Criterion.of(field, condition);
        AtomicInteger sequence = new AtomicInteger(1);
        MyBatis3CriterionRenderer renderer = MyBatis3CriterionRenderer.of(criterion, sequence);
        
        WhereClauseAndParameters rc = renderer.render(false);
        assertThat(rc.getWhereClause(), is(" id = #{parameters.p1,jdbcType=INTEGER}"));
        assertThat(rc.getParameters().size(), is(1));
    }

    @Test
    public void testTypeHandler() {
        Field<Date> field = Field.of("id", JDBCType.DATE).withTypeHandler("foo.Bar");
        IsEqualToCondition<Date> condition = IsEqualToCondition.of(new Date());
        Criterion<Date> criterion = Criterion.of(field, condition);
        AtomicInteger sequence = new AtomicInteger(1);
        MyBatis3CriterionRenderer renderer = MyBatis3CriterionRenderer.of(criterion, sequence);
        
        WhereClauseAndParameters rc = renderer.render(false);
        assertThat(rc.getWhereClause(), is(" id = #{parameters.p1,jdbcType=DATE,typeHandler=foo.Bar}"));
        assertThat(rc.getParameters().size(), is(1));
    }

    @Test
    public void testTypeHandlerAndAlias() {
        Field<Integer> field = Field.of("id", JDBCType.INTEGER).withTypeHandler("foo.Bar").withAlias("a");
        IsEqualToCondition<Integer> condition = IsEqualToCondition.of(3);
        Criterion<Integer> criterion = Criterion.of(field, condition);
        AtomicInteger sequence = new AtomicInteger(1);
        MyBatis3CriterionRenderer renderer = MyBatis3CriterionRenderer.of(criterion, sequence);
        
        WhereClauseAndParameters rc = renderer.render(false);
        assertThat(rc.getWhereClause(), is(" a.id = #{parameters.p1,jdbcType=INTEGER,typeHandler=foo.Bar}"));
        assertThat(rc.getParameters().size(), is(1));
    }
}
