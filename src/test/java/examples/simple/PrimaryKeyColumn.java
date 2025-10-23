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
package examples.simple;

import org.mybatis.dynamic.sql.ParameterTypeConverter;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.render.RenderingStrategy;

public class PrimaryKeyColumn<T> extends SqlColumn<T> {
    private final boolean isPrimaryKeyColumn;

    private PrimaryKeyColumn(Builder<T> builder) {
        super(builder);
        isPrimaryKeyColumn = builder.isPrimaryKeyColumn;
    }

    public boolean isPrimaryKeyColumn() {
        return isPrimaryKeyColumn;
    }

    @Override
    public PrimaryKeyColumn<T> descending() {
        return cast(super.descending());
    }

    @Override
    public PrimaryKeyColumn<T> as(String alias) {
        return cast(super.as(alias));
    }

    @Override
    public PrimaryKeyColumn<T> qualifiedWith(String tableQualifier) {
        return cast(super.qualifiedWith(tableQualifier));
    }

    @Override
    public PrimaryKeyColumn<T> asCamelCase() {
        return cast(super.asCamelCase());
    }

    @Override
    public <S> PrimaryKeyColumn<S> withTypeHandler(String typeHandler) {
        return cast(super.withTypeHandler(typeHandler));
    }

    @Override
    public <S> PrimaryKeyColumn<S> withRenderingStrategy(RenderingStrategy renderingStrategy) {
        return cast(super.withRenderingStrategy(renderingStrategy));
    }

    @Override
    public <S> PrimaryKeyColumn<S> withParameterTypeConverter(ParameterTypeConverter<S, ?> parameterTypeConverter) {
        return cast(super.withParameterTypeConverter(parameterTypeConverter));
    }

    @Override
    public <S> PrimaryKeyColumn<S> withJavaType(Class<S> javaType) {
        return cast(super.withJavaType(javaType));
    }

    @Override
    public <S> PrimaryKeyColumn<S> withJavaProperty(String javaProperty) {
        return cast(super.withJavaProperty(javaProperty));
    }

    @Override
    protected Builder<T> copyBuilder() {
        return populateBaseBuilder(new Builder<>()).isPrimaryKeyColumn(isPrimaryKeyColumn);
    }

    public static class Builder<T> extends AbstractBuilder<T, PrimaryKeyColumn<T>, Builder<T>> {
        private boolean isPrimaryKeyColumn;

        public Builder<T> isPrimaryKeyColumn(boolean isPrimaryKeyColumn) {
            this.isPrimaryKeyColumn = isPrimaryKeyColumn;
            return this;
        }

        @Override
        public PrimaryKeyColumn<T> build() {
            return new PrimaryKeyColumn<>(this);
        }

        @Override
        protected Builder<T> getThis() {
            return this;
        }
    }
}
