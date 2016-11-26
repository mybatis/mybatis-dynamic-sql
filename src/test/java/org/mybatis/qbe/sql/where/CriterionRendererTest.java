package org.mybatis.qbe.sql.where;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.sql.JDBCType;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.mybatis.qbe.Field;
import org.mybatis.qbe.sql.SqlCriterion;
import org.mybatis.qbe.sql.SqlField;
import org.mybatis.qbe.sql.where.condition.IsEqualTo;
import org.mybatis.qbe.sql.where.condition.IsLike;
import org.mybatis.qbe.sql.where.render.CriterionRenderer;
import org.mybatis.qbe.sql.where.render.RenderedCriterion;

public class CriterionRendererTest {

    @Test
    public void testAliasWithIgnore() {
        SqlField<Integer> field = SqlField.of("id", JDBCType.INTEGER, "a").ignoringAlias();
        
        IsEqualTo<Integer> condition = IsEqualTo.of(3);
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
        IsEqualTo<Integer> condition = IsEqualTo.of(3);
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
        IsEqualTo<Integer> condition = IsEqualTo.of(3);
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
        IsEqualTo<Integer> condition = IsEqualTo.of(3);
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
        
        IsLike condition = IsLikeCaseInsensitive.of("fred");
        SqlCriterion<String> criterion = SqlCriterion.of(field, condition);
        AtomicInteger sequence = new AtomicInteger(1);
        CriterionRenderer<String> renderer = CriterionRenderer.of(criterion, sequence);
        
        RenderedCriterion rc = renderer.render();
        assertThat(rc.whereClauseFragment(), is(" upper(a.description) like ?"));
        assertThat(rc.fragmentParameters().size(), is(1));
        assertThat(rc.fragmentParameters().get("p1"), is("FRED"));
    }
    
    public static class IsLikeCaseInsensitive extends IsLike {
        private IsLikeCaseInsensitive(String value) {
            super(value);
        }
        
        public static IsLikeCaseInsensitive of(String value) {
            return new IsLikeCaseInsensitive(value);
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
