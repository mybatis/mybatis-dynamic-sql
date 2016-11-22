package org.mybatis.qbe.sql.where;

import java.util.ArrayList;
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
    
    public static class Builder {
        private List<SqlCriterion<?>> criteria = new ArrayList<>();

        public <T> Builder(SqlField<T> field, Condition<T> condition, SqlCriterion<?>...criteria) {
            this.criteria.add(SqlCriterion.of(field, condition, criteria));
        }
        
        public <T> Builder and(SqlField<T> field, Condition<T> condition, SqlCriterion<?>...criteria) {
            this.criteria.add(SqlCriterion.of("and", field, condition, criteria));
            return this;
        }
        
        public <T> Builder or(SqlField<T> field, Condition<T> condition, SqlCriterion<?>...criteria) {
            this.criteria.add(SqlCriterion.of("or", field, condition, criteria));
            return this;
        }

        public WhereClause build() {
            return new WhereClause(criteria.stream());
        }
        
        public WhereClause buildIgnoringAlias() {
            return new WhereClause(criteria.stream().map(SqlCriterion::ignoringAlias));
        }
    }
}
