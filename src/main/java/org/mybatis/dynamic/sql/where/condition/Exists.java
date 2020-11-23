package org.mybatis.dynamic.sql.where.condition;

import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.util.Buildable;

import java.util.Objects;

public class Exists {
    private final Buildable<SelectModel> selectModelBuilder;
    private final String operator;

    private Exists(Builder builder) {
        this.selectModelBuilder = Objects.requireNonNull(builder.selectModelBuilder);
        this.operator = builder.operator;
    }

    public String operator() {
        return operator;
    }

    public Buildable<SelectModel> selectModelBuilder() {
        return selectModelBuilder;
    }

    public static Builder exists() {
        return new Builder();
    }

    public static Builder notExists() {
        return new Builder().notExists();
    }

    public static class Builder {
        private Buildable<SelectModel> selectModelBuilder;
        private String operator = "exists"; //$NON-NLS-1$

        public Builder withSelectModelBuilder(Buildable<SelectModel> selectModelBuilder) {
            this.selectModelBuilder = selectModelBuilder;
            return this;
        }

        private Builder notExists() {
            operator = "not exists"; //$NON-NLS-1$
            return this;
        }

        public Exists build() {
            return new Exists(this);
        }
    }
}
