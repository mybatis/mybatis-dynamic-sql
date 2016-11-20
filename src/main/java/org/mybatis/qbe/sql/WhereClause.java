package org.mybatis.qbe.sql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.mybatis.qbe.Condition;

public class WhereClause {
    private List<SqlCriterion<?>> criteria = new ArrayList<>();

    private WhereClause(Stream<SqlCriterion<?>> criteria) {
        criteria.forEach(this.criteria::add);
    }
    
    public void visitCriteria(Consumer<SqlCriterion<?>> consumer) {
        criteria.stream().forEach(consumer);
    }
    
    public abstract static class AbstractBuilder<T extends AbstractBuilder<T>> {
        private List<SqlCriterion<?>> criteria = new ArrayList<>();
        
        public <S> AbstractBuilder(SqlField<S> field, Condition<S> condition, SqlCriterion<?>...criteria) {
            this.criteria.add(SqlCriterion.of(field, condition, criteria));
        }
        
        public <S> T and(SqlField<S> field, Condition<S> condition, SqlCriterion<?>...criteria) {
            this.criteria.add(SqlCriterion.of("and", field, condition, criteria));
            return getThis();
        }
        
        public <S> T or(SqlField<S> field, Condition<S> condition, SqlCriterion<?>...criteria) {
            this.criteria.add(SqlCriterion.of("or", field, condition, criteria));
            return getThis();
        }
        
        public WhereClause build() {
            return new WhereClause(criteria.stream());
        }
        
        public abstract T getThis();
    }
    
    public static class Builder extends AbstractBuilder<Builder> {
        public <T> Builder(SqlField<T> field, Condition<T> condition, SqlCriterion<?>...criteria) {
            super(field, condition, criteria);
        }
        
        @Override
        public Builder getThis() {
            return this;
        }
    }

    public abstract static class AbstractAliasIgnoringBuilder<T extends AbstractAliasIgnoringBuilder<T>> {
        private List<SqlCriterion<?>> criteria = new ArrayList<>();
        
        public <S> AbstractAliasIgnoringBuilder(SqlField<S> field, Condition<S> condition, SqlCriterion<?>...criteria) {
            this.criteria.add(SqlCriterion.of(null, field.ignoringAlias(), condition,
                    Arrays.stream(criteria).map(SqlCriterion::ignoringAlias)));
        }
        
        public <S> T and(SqlField<S> field, Condition<S> condition, SqlCriterion<?>...criteria) {
            this.criteria.add(SqlCriterion.of("and", field.ignoringAlias(), condition,
                    Arrays.stream(criteria).map(SqlCriterion::ignoringAlias)));
            return getThis();
        }
        
        public <S> T or(SqlField<S> field, Condition<S> condition, SqlCriterion<?>...criteria) {
            this.criteria.add(SqlCriterion.of("or", field.ignoringAlias(), condition,
                    Arrays.stream(criteria).map(SqlCriterion::ignoringAlias)));
            return getThis();
        }
        
        public WhereClause build() {
            return new WhereClause(criteria.stream());
        }
        
        public abstract T getThis();
    }

    public static class AliasIgnoringBuilder extends AbstractAliasIgnoringBuilder<AliasIgnoringBuilder> {
        public <T> AliasIgnoringBuilder(SqlField<T> field, Condition<T> condition, SqlCriterion<?>...criteria) {
            super(field, condition, criteria);
        }
        
        @Override
        public AliasIgnoringBuilder getThis() {
            return this;
        }
    }
}
