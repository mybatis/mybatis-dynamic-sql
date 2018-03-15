/**
 *    Copyright 2016-2018 the original author or authors.
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
package org.mybatis.dynamic.sql.select.render;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.mybatis.dynamic.sql.SortSpecification;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.OrderByModel;
import org.mybatis.dynamic.sql.select.QueryExpressionModel;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.util.CustomCollectors;

public class SelectRenderer {
    private SelectModel selectModel;
    private RenderingStrategy renderingStrategy;
    private AtomicInteger sequence;
    
    private SelectRenderer(Builder builder) {
        selectModel = Objects.requireNonNull(builder.selectModel);
        renderingStrategy = Objects.requireNonNull(builder.renderingStrategy);
        sequence = builder.sequence.orElse(new AtomicInteger(1));
    }
    
    public SelectStatementProvider render() {
        QueryExpressionCollector collector = selectModel
                .mapQueryExpressions(this::renderQueryExpression)
                .collect(QueryExpressionCollector.collect());
        
        return DefaultSelectStatementProvider.withQueryExpression(collector.queryExpression())
                .withParameters(collector.parameters())
                .withOrderByClause(selectModel.orderByModel().map(this::renderOrderBy))
                .build();
    }

    private QueryExpression renderQueryExpression(QueryExpressionModel queryExpressionModel) {
        return QueryExpressionRenderer.withQueryExpression(queryExpressionModel)
                .withRenderingStrategy(renderingStrategy)
                .withSequence(sequence)
                .build()
                .render();
    }

    private String renderOrderBy(OrderByModel orderByModel) {
        return orderByModel.mapColumns(this::orderByPhrase)
                .collect(CustomCollectors.joining(", ", "order by ", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    
    private String orderByPhrase(SortSpecification column) {
        String phrase = column.aliasOrName();
        if (column.isDescending()) {
            phrase = phrase + " DESC"; //$NON-NLS-1$
        }
        return phrase;
    }
    
    public static Builder withSelectModel(SelectModel selectModel) {
        return new Builder().withSelectModel(selectModel);
    }
    
    public static class Builder {
        private SelectModel selectModel;
        private RenderingStrategy renderingStrategy;
        private Optional<AtomicInteger> sequence = Optional.empty();
        
        public Builder withSelectModel(SelectModel selectModel) {
            this.selectModel = selectModel;
            return this;
        }
        
        public Builder withRenderingStrategy(RenderingStrategy renderingStrategy) {
            this.renderingStrategy = renderingStrategy;
            return this;
        }
        
        public Builder withSequence(AtomicInteger sequence) {
            this.sequence = Optional.of(sequence);
            return this;
        }
        
        public SelectRenderer build() {
            return new SelectRenderer(this);
        }
    }
}
