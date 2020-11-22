package org.mybatis.dynamic.sql;

import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.util.Buildable;

import java.util.Objects;

public class ExistsCriterion extends SqlCriterion {
    private final Buildable<SelectModel> selectModelBuilder;

    private ExistsCriterion(Builder builder) {
        super(builder);
        this.selectModelBuilder = Objects.requireNonNull(builder.selectModelBuilder);
    }

    public SelectModel selectModel() {
        return selectModelBuilder.build();
    }

    @Override
    public <R> R accept(SqlCriterionVisitor<R> visitor) {
        return visitor.visit(this);
    }

    public static class Builder extends AbstractBuilder<Builder> {
        private Buildable<SelectModel> selectModelBuilder;

        public Builder withSelectModelBuilder(Buildable<SelectModel> selectModelBuilder) {
            this.selectModelBuilder = selectModelBuilder;
            return this;
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }
}
