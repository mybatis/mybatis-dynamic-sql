package org.mybatis.dynamic.sql.where.condition;

import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.util.Buildable;

import java.util.Objects;

public class Exists {
    private final Buildable<SelectModel> selectModelBuilder;
    private final boolean isNotExists;

    private Exists(Builder builder) {
        this.selectModelBuilder = Objects.requireNonNull(builder.selectModelBuilder);
        this.isNotExists = builder.isNotExists;
    }

    public boolean isNotExists() {
        return isNotExists;
    }

    public Buildable<SelectModel> selectModelBuilder() {
        return selectModelBuilder;
    }

    public static class Builder {
        private Buildable<SelectModel> selectModelBuilder;
        private boolean isNotExists;

        public Builder withSelectModelBuilder(Buildable<SelectModel> selectModelBuilder) {
            this.selectModelBuilder = selectModelBuilder;
            return this;
        }

        public Builder isNotExists() {
            isNotExists = true;
            return this;
        }

        public Exists build() {
            return new Exists(this);
        }
    }
}
