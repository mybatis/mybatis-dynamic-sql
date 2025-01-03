/*
 *    Copyright 2016-2025 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.dynamic.sql.select.function;

import java.util.Objects;
import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;

public class Cast implements BasicColumn {
    private final BasicColumn column;
    private final String targetType;
    private final @Nullable String alias;

    private Cast(Builder builder) {
        column = Objects.requireNonNull(builder.column);
        targetType = Objects.requireNonNull(builder.targetType);
        alias = builder.alias;
    }

    @Override
    public Optional<String> alias() {
        return Optional.ofNullable(alias);
    }

    @Override
    public Cast as(String alias) {
        return new Builder().withColumn(column)
                .withTargetType(targetType)
                .withAlias(alias)
                .build();
    }

    @Override
    public FragmentAndParameters render(RenderingContext renderingContext) {
        return column.render(renderingContext).mapFragment(this::applyCast);
    }

    private String applyCast(String in) {
        return "cast(" + in + " as " + targetType + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public static class Builder {
        private @Nullable BasicColumn column;
        private @Nullable String targetType;
        private @Nullable String alias;

        public Builder withColumn(BasicColumn column) {
            this.column = column;
            return this;
        }

        public Builder withTargetType(String targetType) {
            this.targetType = targetType;
            return this;
        }

        public Builder withAlias(String alias) {
            this.alias = alias;
            return this;
        }

        public Cast build() {
            return new Cast(this);
        }
    }
}
