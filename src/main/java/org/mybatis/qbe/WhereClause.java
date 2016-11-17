package org.mybatis.qbe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.mybatis.qbe.condition.Condition;
import org.mybatis.qbe.condition.Conditions;
import org.mybatis.qbe.field.Field;

public class WhereClause {
    private List<Criterion<?>> criteria = new ArrayList<>();

    private WhereClause(Stream<Criterion<?>> criteria) {
        criteria.forEach(this.criteria::add);
    }
    
    public void visitCriteria(Consumer<Criterion<?>> consumer) {
        criteria.stream().forEach(consumer);
    }
    
    public static <T> Builder of(Field<T> field, Condition<T> condition, Criterion<?>...criteria) {
        return new Builder(field, condition, criteria);
    }
    
    public abstract static class AbstractBuilder<T extends AbstractBuilder<T>> {
        private List<Criterion<?>> criteria = new ArrayList<>();
        
        public <S> AbstractBuilder(Field<S> field, Condition<S> condition, Criterion<?>...criteria) {
            this.criteria.add(Criterion.of(field, condition, criteria));
        }
        
        public <S> T and(Field<S> field, Condition<S> condition, Criterion<?>...criteria) {
            this.criteria.add(Conditions.and(field, condition, criteria));
            return getThis();
        }
        
        public <S> T or(Field<S> field, Condition<S> condition, Criterion<?>...criteria) {
            this.criteria.add(Conditions.or(field, condition, criteria));
            return getThis();
        }
        
        public WhereClause build() {
            return new WhereClause(criteria.stream());
        }
        
        public abstract T getThis();
    }
    
    public static class Builder extends AbstractBuilder<Builder> {
        public <T> Builder(Field<T> field, Condition<T> condition, Criterion<?>...criteria) {
            super(field, condition, criteria);
        }
        
        @Override
        public Builder getThis() {
            return this;
        }
    }
}
