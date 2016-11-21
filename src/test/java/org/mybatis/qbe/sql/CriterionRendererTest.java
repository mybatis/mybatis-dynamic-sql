package org.mybatis.qbe.sql;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.sql.JDBCType;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.mybatis.qbe.Field;
import org.mybatis.qbe.sql.render.CriterionRenderer;
import org.mybatis.qbe.sql.render.RenderedCriterion;

public class CriterionRendererTest {

    @Test
    public void testAliasWithIgnore() {
        SqlField<Integer> field = SqlField.of("id", JDBCType.INTEGER, "a").ignoringAlias();
        
        IsEqualToCondition<Integer> condition = IsEqualToCondition.of(3);
        SqlCriterion<Integer> criterion = SqlCriterion.of(field, condition);
        AtomicInteger sequence = new AtomicInteger(1);
        CriterionRenderer<Integer> renderer = CriterionRenderer.of(criterion, sequence);
        
        RenderedCriterion rc = renderer.render();
        assertThat(rc.whereClauseFragment(), is(" id = ?"));
        assertThat(rc.fragmentParameters().size(), is(1));
    }

    @Test
    public void testAliasWithoutIgnore() {
        SqlField<Integer> field = SqlField.of("id", JDBCType.INTEGER, "a");
        IsEqualToCondition<Integer> condition = IsEqualToCondition.of(3);
        SqlCriterion<Integer> criterion = SqlCriterion.of(field, condition);
        AtomicInteger sequence = new AtomicInteger(1);
        CriterionRenderer<Integer> renderer = CriterionRenderer.of(criterion, sequence);
        
        RenderedCriterion rc = renderer.render();
        assertThat(rc.whereClauseFragment(), is(" a.id = ?"));
        assertThat(rc.fragmentParameters().size(), is(1));
    }

    @Test
    public void testNoAliasWithIgnore() {
        SqlField<Integer> field = SqlField.of("id", JDBCType.INTEGER);
        IsEqualToCondition<Integer> condition = IsEqualToCondition.of(3);
        SqlCriterion<Integer> criterion = SqlCriterion.of(field, condition);
        AtomicInteger sequence = new AtomicInteger(1);
        CriterionRenderer<Integer> renderer = CriterionRenderer.of(criterion, sequence);
        
        RenderedCriterion rc = renderer.render();
        assertThat(rc.whereClauseFragment(), is(" id = ?"));
        assertThat(rc.fragmentParameters().size(), is(1));
    }

    @Test
    public void testNoAliasWithoutIgnore() {
        SqlField<Integer> field = SqlField.of("id", JDBCType.INTEGER);
        IsEqualToCondition<Integer> condition = IsEqualToCondition.of(3);
        SqlCriterion<Integer> criterion = SqlCriterion.of(field, condition);
        AtomicInteger sequence = new AtomicInteger(1);
        CriterionRenderer<Integer> renderer = CriterionRenderer.of(criterion, sequence);
        
        RenderedCriterion rc = renderer.render();
        assertThat(rc.whereClauseFragment(), is(" id = ?"));
        assertThat(rc.fragmentParameters().size(), is(1));
    }

    @Test
    public void testCustomCondition() {
        SqlField<String> field = SqlField.of("description", JDBCType.VARCHAR, "a");
        
        IsLikeCondition condition = IsLikeCaseInsensitiveCondition.of("fred");
        SqlCriterion<String> criterion = SqlCriterion.of(field, condition);
        AtomicInteger sequence = new AtomicInteger(1);
        CriterionRenderer<String> renderer = CriterionRenderer.of(criterion, sequence);
        
        RenderedCriterion rc = renderer.render();
        assertThat(rc.whereClauseFragment(), is(" upper(a.description) like ?"));
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
