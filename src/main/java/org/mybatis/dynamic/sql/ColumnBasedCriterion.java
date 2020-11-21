package org.mybatis.dynamic.sql;

import java.util.Objects;

public class ColumnBasedCriterion<T> extends SqlCriterion {
    private final BindableColumn<T> column;
    private final VisitableCondition<T> condition;

    private ColumnBasedCriterion(Builder<T> builder) {
        super(builder);
        column = Objects.requireNonNull(builder.column);
        condition = Objects.requireNonNull(builder.condition);
    }

    public BindableColumn<T> column() {
        return column;
    }

    public VisitableCondition<T> condition() {
        return condition;
    }

    @Override
    public <R> R accept(SqlCriterionVisitor<R> visitor) {
        return visitor.visit(this);
    }

    public static <T> Builder<T> withColumn(BindableColumn<T> column) {
        return new Builder<T>().withColumn(column);
    }

    public static class Builder<T> extends AbstractBuilder<Builder<T>> {
        private BindableColumn<T> column;
        private VisitableCondition<T> condition;

        public Builder<T> withColumn(BindableColumn<T> column) {
            this.column = column;
            return this;
        }

        public Builder<T> withCondition(VisitableCondition<T> condition) {
            this.condition = condition;
            return this;
        }

        @Override
        protected Builder<T> getThis() {
            return this;
        }

        public ColumnBasedCriterion<T> build() {
            return new ColumnBasedCriterion<>(this);
        }
    }
}
