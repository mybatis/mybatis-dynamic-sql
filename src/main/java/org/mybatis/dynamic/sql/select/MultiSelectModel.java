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
package org.mybatis.dynamic.sql.select;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.render.MultiSelectRenderer;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.Validator;

public class MultiSelectModel extends AbstractSelectModel {
    private final SelectModel initialSelect;
    private final List<UnionQuery> unionQueries;

    private MultiSelectModel(Builder builder) {
        super(builder);
        initialSelect = Objects.requireNonNull(builder.initialSelect);
        unionQueries = builder.unionQueries;
        Validator.assertNotEmpty(unionQueries, "ERROR.35"); //$NON-NLS-1$
    }

    public SelectModel initialSelect() {
        return initialSelect;
    }

    public Stream<UnionQuery> unionQueries() {
        return unionQueries.stream();
    }

    public SelectStatementProvider render(RenderingStrategy renderingStrategy) {
        return MultiSelectRenderer.withMultiSelectModel(this)
                .withRenderingStrategy(renderingStrategy)
                .build()
                .render();
    }

    public static class Builder extends AbstractBuilder<Builder> {
        private @Nullable SelectModel initialSelect;
        private final List<UnionQuery> unionQueries = new ArrayList<>();

        public Builder withInitialSelect(SelectModel initialSelect) {
            this.initialSelect = initialSelect;
            return this;
        }

        public Builder withUnionQueries(List<UnionQuery> unionQueries) {
            this.unionQueries.addAll(unionQueries);
            return this;
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        public MultiSelectModel build() {
            return new MultiSelectModel(this);
        }
    }
}
