/**
 *    Copyright 2016-2019 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
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
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.render.SelectRenderer;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;

public class SelectModel {
    private List<QueryExpressionModel> queryExpressions;
    private OrderByModel orderByModel;
    private PagingModel pagingModel;

    private SelectModel(Builder builder) {
        queryExpressions = Objects.requireNonNull(builder.queryExpressions);
        orderByModel = builder.orderByModel;
        pagingModel = builder.pagingModel;
    }
    
    public <R> Stream<R> mapQueryExpressions(Function<QueryExpressionModel, R> mapper) {
        return queryExpressions.stream().map(mapper);
    }
    
    public Optional<OrderByModel> orderByModel() {
        return Optional.ofNullable(orderByModel);
    }
    
    public Optional<PagingModel> pagingModel() {
        return Optional.ofNullable(pagingModel);
    }

    @NotNull
    public SelectStatementProvider render(RenderingStrategy renderingStrategy) {
        return SelectRenderer.withSelectModel(this)
                .withRenderingStrategy(renderingStrategy)
                .build()
                .render();
    }
    
    public static Builder withQueryExpressions(List<QueryExpressionModel> queryExpressions) {
        return new Builder().withQueryExpressions(queryExpressions);
    }
    
    public static class Builder {
        private List<QueryExpressionModel> queryExpressions = new ArrayList<>();
        private OrderByModel orderByModel;
        private PagingModel pagingModel;
        
        public Builder withQueryExpression(QueryExpressionModel queryExpression) {
            this.queryExpressions.add(queryExpression);
            return this;
        }
        
        public Builder withQueryExpressions(List<QueryExpressionModel> queryExpressions) {
            this.queryExpressions.addAll(queryExpressions);
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
        
        public SelectModel build() {
            return new SelectModel(this);
        }
    }
}
