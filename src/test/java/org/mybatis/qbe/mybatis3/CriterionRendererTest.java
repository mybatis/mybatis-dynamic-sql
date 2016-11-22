package org.mybatis.qbe.mybatis3;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.sql.JDBCType;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.mybatis.qbe.Field;
import org.mybatis.qbe.mybatis3.MyBatis3Field;
import org.mybatis.qbe.sql.where.IsEqualToCondition;
import org.mybatis.qbe.sql.where.IsLikeCondition;
import org.mybatis.qbe.sql.where.SqlCriterion;
import org.mybatis.qbe.sql.where.render.CriterionRenderer;
import org.mybatis.qbe.sql.where.render.RenderedCriterion;

public class CriterionRendererTest {

    @Test
    public void testAliasWithIgnore() {
        MyBatis3Field<Integer> field = MyBatis3Field.of("id", JDBCType.INTEGER, "a").ignoringAlias();
        
        IsEqualToCondition<Integer> condition = IsEqualToCondition.of(3);
        SqlCriterion<Integer> criterion = SqlCriterion.of(field, condition);
        AtomicInteger sequence = new AtomicInteger(1);
        CriterionRenderer<Integer> renderer = CriterionRenderer.of(criterion, sequence);
        
        RenderedCriterion rc = renderer.render();
        assertThat(rc.whereClauseFragment(), is(" id = #{parameters.p1,jdbcType=INTEGER}"));
        assertThat(rc.fragmentParameters().size(), is(1));
    }

    @Test
    public void testAliasWithoutIgnore() {
        MyBatis3Field<Integer> field = MyBatis3Field.of("id", JDBCType.INTEGER, "a");
        IsEqualToCondition<Integer> condition = IsEqualToCondition.of(3);
        SqlCriterion<Integer> criterion = SqlCriterion.of(field, condition);
        AtomicInteger sequence = new AtomicInteger(1);
        CriterionRenderer<Integer> renderer = CriterionRenderer.of(criterion, sequence);
        
        RenderedCriterion rc = renderer.render();
        assertThat(rc.whereClauseFragment(), is(" a.id = #{parameters.p1,jdbcType=INTEGER}"));
        assertThat(rc.fragmentParameters().size(), is(1));
    }

    @Test
    public void testNoAliasWithIgnore() {
        MyBatis3Field<Integer> field = MyBatis3Field.of("id", JDBCType.INTEGER);
        IsEqualToCondition<Integer> condition = IsEqualToCondition.of(3);
        SqlCriterion<Integer> criterion = SqlCriterion.of(field, condition);
        AtomicInteger sequence = new AtomicInteger(1);
        CriterionRenderer<Integer> renderer = CriterionRenderer.of(criterion, sequence);
        
        RenderedCriterion rc = renderer.render();
        assertThat(rc.whereClauseFragment(), is(" id = #{parameters.p1,jdbcType=INTEGER}"));
        assertThat(rc.fragmentParameters().size(), is(1));
    }

    @Test
    public void testNoAliasWithoutIgnore() {
        MyBatis3Field<Integer> field = MyBatis3Field.of("id", JDBCType.INTEGER);
        IsEqualToCondition<Integer> condition = IsEqualToCondition.of(3);
        SqlCriterion<Integer> criterion = SqlCriterion.of(field, condition);
        AtomicInteger sequence = new AtomicInteger(1);
        CriterionRenderer<Integer> renderer = CriterionRenderer.of(criterion, sequence);
        
        RenderedCriterion rc = renderer.render();
        assertThat(rc.whereClauseFragment(), is(" id = #{parameters.p1,jdbcType=INTEGER}"));
        assertThat(rc.fragmentParameters().size(), is(1));
    }

    @Test
    public void testTypeHandler() {
        MyBatis3Field<Date> field = MyBatis3Field.of("id", JDBCType.DATE).withTypeHandler("foo.Bar");
        IsEqualToCondition<Date> condition = IsEqualToCondition.of(new Date());
        SqlCriterion<Date> criterion = SqlCriterion.of(field, condition);
        AtomicInteger sequence = new AtomicInteger(1);
        CriterionRenderer<Date> renderer = CriterionRenderer.of(criterion, sequence);
        
        RenderedCriterion rc = renderer.render();
        assertThat(rc.whereClauseFragment(), is(" id = #{parameters.p1,jdbcType=DATE,typeHandler=foo.Bar}"));
        assertThat(rc.fragmentParameters().size(), is(1));
    }

    @Test
    public void testTypeHandlerAndAlias() {
        MyBatis3Field<Integer> field = MyBatis3Field.of("id", JDBCType.INTEGER).withTypeHandler("foo.Bar").withAlias("a");
        IsEqualToCondition<Integer> condition = IsEqualToCondition.of(3);
        SqlCriterion<Integer> criterion = SqlCriterion.of(field, condition);
        AtomicInteger sequence = new AtomicInteger(1);
        CriterionRenderer<Integer> renderer = CriterionRenderer.of(criterion, sequence);
        
        RenderedCriterion rc = renderer.render();
        assertThat(rc.whereClauseFragment(), is(" a.id = #{parameters.p1,jdbcType=INTEGER,typeHandler=foo.Bar}"));
        assertThat(rc.fragmentParameters().size(), is(1));
    }
    
    @Test
    public void testCustomCondition() {
        MyBatis3Field<String> field = MyBatis3Field.of("description", JDBCType.VARCHAR).withAlias("a").withTypeHandler("foo.Bar");
        
        IsLikeCondition condition = IsLikeCaseInsensitiveCondition.of("fred");
        SqlCriterion<String> criterion = SqlCriterion.of(field, condition);
        AtomicInteger sequence = new AtomicInteger(1);
        CriterionRenderer<String> renderer = CriterionRenderer.of(criterion, sequence);
        
        RenderedCriterion rc = renderer.render();
        assertThat(rc.whereClauseFragment(), is(" upper(a.description) like #{parameters.p1,jdbcType=VARCHAR,typeHandler=foo.Bar}"));
        assertThat(rc.fragmentParameters().size(), is(1));
        assertThat(rc.fragmentParameters().get("p1"), is("FRED"));
    }
    
    public static class IsLikeCaseInsensitiveCondition extends IsLikeCondition {
        private IsLikeCaseInsensitiveCondition(String value) {
            super(value);
        }
        
        public static IsLikeCaseInsensitiveCondition of(String value) {
            return new IsLikeCaseInsensitiveCondition(value);
        }
        
        @Override
        public String renderField(Field<String> field) {
            return String.format("upper(%s)", field.render());
        }
        
        @Override
        public String value() {
            return super.value().toUpperCase();
        }
    }
}
