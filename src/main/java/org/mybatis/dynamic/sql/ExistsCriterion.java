package org.mybatis.dynamic.sql;

import org.mybatis.dynamic.sql.where.condition.Exists;

import java.util.Objects;

public class ExistsCriterion extends SqlCriterion {
    private final Exists exists;

    private ExistsCriterion(Builder builder) {
        super(builder);
        this.exists = Objects.requireNonNull(builder.exists);
    }

    public Exists exists() {
        return exists;
    }

    @Override
    public <R> R accept(SqlCriterionVisitor<R> visitor) {
        return visitor.visit(this);
    }

    public static class Builder extends AbstractBuilder<Builder> {
        private Exists exists;

        public Builder withExists(Exists exists) {
            this.exists = exists;
            return this;
        }

        public ExistsCriterion build() {
            return new ExistsCriterion(this);
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }
}
