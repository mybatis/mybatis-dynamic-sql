package org.mybatis.qbe.mybatis3.render;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.sql.JDBCType;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.mybatis.qbe.Criterion;
import org.mybatis.qbe.condition.IsEqualToCondition;
import org.mybatis.qbe.condition.IsLikeCondition;
import org.mybatis.qbe.field.Field;
import org.mybatis.qbe.mybatis3.WhereClauseAndParameters;

public class CriterionRendererTest {

    @Test
    public void testAliasWithIgnore() {
        Field<Integer> field = Field.of("id", JDBCType.INTEGER).withAlias("a");
        IsEqualToCondition<Integer> condition = IsEqualToCondition.of(3);
        Criterion<Integer> criterion = Criterion.of(field, condition);
        AtomicInteger sequence = new AtomicInteger(1);
        CriterionRenderer<Integer> renderer = CriterionRenderer.of(criterion, sequence);
        
        WhereClauseAndParameters rc = renderer.renderWithoutTableAlias();
        assertThat(rc.getWhereClause(), is(" id = #{parameters.p1,jdbcType=INTEGER}"));
        assertThat(rc.getParameters().size(), is(1));
    }

    @Test
    public void testAliasWithoutIgnore() {
        Field<Integer> field = Field.of("id", JDBCType.INTEGER).withAlias("a");
        IsEqualToCondition<Integer> condition = IsEqualToCondition.of(3);
        Criterion<Integer> criterion = Criterion.of(field, condition);
        AtomicInteger sequence = new AtomicInteger(1);
        CriterionRenderer<Integer> renderer = CriterionRenderer.of(criterion, sequence);
        
        WhereClauseAndParameters rc = renderer.render();
        assertThat(rc.getWhereClause(), is(" a.id = #{parameters.p1,jdbcType=INTEGER}"));
        assertThat(rc.getParameters().size(), is(1));
    }

    @Test
    public void testNoAliasWithIgnore() {
        Field<Integer> field = Field.of("id", JDBCType.INTEGER);
        IsEqualToCondition<Integer> condition = IsEqualToCondition.of(3);
        Criterion<Integer> criterion = Criterion.of(field, condition);
        AtomicInteger sequence = new AtomicInteger(1);
        CriterionRenderer<Integer> renderer = CriterionRenderer.of(criterion, sequence);
        
        WhereClauseAndParameters rc = renderer.renderWithoutTableAlias();
        assertThat(rc.getWhereClause(), is(" id = #{parameters.p1,jdbcType=INTEGER}"));
        assertThat(rc.getParameters().size(), is(1));
    }

    @Test
    public void testNoAliasWithoutIgnore() {
        Field<Integer> field = Field.of("id", JDBCType.INTEGER);
        IsEqualToCondition<Integer> condition = IsEqualToCondition.of(3);
        Criterion<Integer> criterion = Criterion.of(field, condition);
        AtomicInteger sequence = new AtomicInteger(1);
        CriterionRenderer<Integer> renderer = CriterionRenderer.of(criterion, sequence);
        
        WhereClauseAndParameters rc = renderer.render();
        assertThat(rc.getWhereClause(), is(" id = #{parameters.p1,jdbcType=INTEGER}"));
        assertThat(rc.getParameters().size(), is(1));
    }

    @Test
    public void testTypeHandler() {
        Field<Date> field = Field.of("id", JDBCType.DATE).withTypeHandler("foo.Bar");
        IsEqualToCondition<Date> condition = IsEqualToCondition.of(new Date());
        Criterion<Date> criterion = Criterion.of(field, condition);
        AtomicInteger sequence = new AtomicInteger(1);
        CriterionRenderer<Date> renderer = CriterionRenderer.of(criterion, sequence);
        
        WhereClauseAndParameters rc = renderer.render();
        assertThat(rc.getWhereClause(), is(" id = #{parameters.p1,jdbcType=DATE,typeHandler=foo.Bar}"));
        assertThat(rc.getParameters().size(), is(1));
    }

    @Test
    public void testTypeHandlerAndAlias() {
        Field<Integer> field = Field.of("id", JDBCType.INTEGER).withTypeHandler("foo.Bar").withAlias("a");
        IsEqualToCondition<Integer> condition = IsEqualToCondition.of(3);
        Criterion<Integer> criterion = Criterion.of(field, condition);
        AtomicInteger sequence = new AtomicInteger(1);
        CriterionRenderer<Integer> renderer = CriterionRenderer.of(criterion, sequence);
        
        WhereClauseAndParameters rc = renderer.render();
        assertThat(rc.getWhereClause(), is(" a.id = #{parameters.p1,jdbcType=INTEGER,typeHandler=foo.Bar}"));
        assertThat(rc.getParameters().size(), is(1));
    }
    
    @Test
    public void testCustomCallbacks() {
        Field<String> field = Field.of("description", JDBCType.VARCHAR)
                .withTypeHandler("foo.Bar")
                .withAlias("a");
        
        IsLikeCondition condition = IsLikeCaseInsensitiveCondition.of("fred");
        Criterion<String> criterion = Criterion.of(field, condition);
        AtomicInteger sequence = new AtomicInteger(1);
        CriterionRenderer<String> renderer = CriterionRenderer.of(criterion, sequence);
        
        WhereClauseAndParameters rc = renderer.render();
        assertThat(rc.getWhereClause(), is(" upper(a.description) like #{parameters.p1,jdbcType=VARCHAR,typeHandler=foo.Bar}"));
        assertThat(rc.getParameters().size(), is(1));
        assertThat(rc.getParameters().get("p1"), is("FRED"));
        
        // make sure that we didn't destroy the field definition with our custom renderer above
        condition = IsLikeCondition.of("fred");
        criterion = Criterion.of(field, condition);
        sequence = new AtomicInteger(1);
        renderer = CriterionRenderer.of(criterion, sequence);
        rc = renderer.render();
        assertThat(rc.getWhereClause(), is(" a.description like #{parameters.p1,jdbcType=VARCHAR,typeHandler=foo.Bar}"));
        assertThat(rc.getParameters().size(), is(1));
        assertThat(rc.getParameters().get("p1"), is("fred"));
    }
    
    public static class IsLikeCaseInsensitiveCondition extends IsLikeCondition {
        private IsLikeCaseInsensitiveCondition(String value) {
            super(value);
        }
        
        public static IsLikeCaseInsensitiveCondition of(String value) {
            return new IsLikeCaseInsensitiveCondition(value);
        }
        
        @Override
        public String fieldName(Field<String> field) {
            return String.format("upper(%s)", field.aliasedName());
        }

        @Override
        public String fieldNameWithoutAlias(Field<String> field) {
            return String.format("upper(%s)", field.name());
        }
        
        @Override
        public String value() {
            return super.value().toUpperCase();
        }
    }
}
