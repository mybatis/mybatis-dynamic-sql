/*
 *    Copyright 2016-2023 the original author or authors.
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
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.mybatis.dynamic.sql.common.OrderByModel;
import org.mybatis.dynamic.sql.exception.InvalidSqlException;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.render.MultiSelectRenderer;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.Messages;

public class MultiSelectModel {
    private final SelectModel initialSelect;
    private final List<UnionQuery> unionQueries;
    private final OrderByModel orderByModel;
    private final PagingModel pagingModel;

    private MultiSelectModel(Builder builder) {
        initialSelect = Objects.requireNonNull(builder.initialSelect);
        unionQueries = builder.unionQueries;
        orderByModel = builder.orderByModel;
        pagingModel = builder.pagingModel;
        if (unionQueries.isEmpty()) {
            throw new InvalidSqlException(Messages.getString("ERROR.35")); //$NON-NLS-1$
        }
    }

    public SelectModel initialSelect() {
        return initialSelect;
    }

    public <R> Stream<R> mapUnionQueries(Function<UnionQuery, R> mapper) {
        return unionQueries.stream().map(mapper);
    }

    public Optional<OrderByModel> orderByModel() {
        return Optional.ofNullable(orderByModel);
    }

    public Optional<PagingModel> pagingModel() {
        return Optional.ofNullable(pagingModel);
    }

    @NotNull
    public SelectStatementProvider render(RenderingStrategy renderingStrategy) {
        return new MultiSelectRenderer.Builder()
                .withMultiSelectModel(this)
                .withRenderingStrategy(renderingStrategy)
                .build()
                .render();
    }

    public static class Builder {
        private SelectModel initialSelect;
        private final List<UnionQuery> unionQueries = new ArrayList<>();
        private OrderByModel orderByModel;
        private PagingModel pagingModel;

        public Builder withInitialSelect(SelectModel initialSelect) {
            this.initialSelect = initialSelect;
            return this;
        }

        public Builder withUnionQueries(List<UnionQuery> unionQueries) {
            this.unionQueries.addAll(unionQueries);
            return this;
        }

        public Builder withOrderByModel(OrderByModel orderByModel) {
            this.orderByModel = orderByModel;
            return this;
        }

        public Builder withPagingModel(PagingModel pagingModel) {
            this.pagingModel = pagingModel;
            return this;
        }

        public MultiSelectModel build() {
            return new MultiSelectModel(this);
        }
    }

    public static class UnionQuery {
        private final String connector;
        private final SelectModel selectModel;

        public UnionQuery(String connector, SelectModel selectModel) {
            this.connector = Objects.requireNonNull(connector);
            this.selectModel = Objects.requireNonNull(selectModel);
        }

        public String connector() {
            return connector;
        }

        public SelectModel selectModel() {
            return selectModel;
        }
    }
}
