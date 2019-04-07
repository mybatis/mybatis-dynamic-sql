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
package org.mybatis.dynamic.sql.select.render;

import static org.mybatis.dynamic.sql.util.StringUtilities.spaceBefore;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.SortSpecification;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.OrderByModel;
import org.mybatis.dynamic.sql.select.QueryExpressionModel;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.util.CustomCollectors;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.FragmentCollector;

public class SelectRenderer {
    private static final String LIMIT_PARAMETER = "_limit"; //$NON-NLS-1$
    private static final String OFFSET_PARAMETER = "_offset"; //$NON-NLS-1$
    private SelectModel selectModel;
    private RenderingStrategy renderingStrategy;
    private AtomicInteger sequence;
    
    private SelectRenderer(Builder builder) {
        selectModel = Objects.requireNonNull(builder.selectModel);
        renderingStrategy = Objects.requireNonNull(builder.renderingStrategy);
        sequence = builder.sequence.orElse(new AtomicInteger(1));
    }
    
    public SelectStatementProvider render() {
        FragmentCollector queryExpressionCollector = selectModel
                .mapQueryExpressions(this::renderQueryExpression)
                .collect(FragmentCollector.collect());
        
        Map<String, Object> parameters = queryExpressionCollector.parameters();
        
        String selectStatement = queryExpressionCollector.fragments().collect(Collectors.joining(" ")) //$NON-NLS-1$
                + spaceBefore(renderOrderBy())
                + spaceBefore(renderLimit(parameters))
                + spaceBefore(renderOffset(parameters));
        
        return DefaultSelectStatementProvider.withSelectStatement(selectStatement)
                .withParameters(parameters)
                .build();
    }

    private FragmentAndParameters renderQueryExpression(QueryExpressionModel queryExpressionModel) {
        return QueryExpressionRenderer.withQueryExpression(queryExpressionModel)
                .withRenderingStrategy(renderingStrategy)
                .withSequence(sequence)
                .build()
                .render();
    }

    private Optional<String> renderOrderBy() {
        return selectModel.orderByModel()
                .map(this::renderOrderBy);
    }
    
    private String renderOrderBy(OrderByModel orderByModel) {
        return orderByModel.mapColumns(this::calculateOrderByPhrase)
                .collect(CustomCollectors.joining(", ", "order by ", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    
    private String calculateOrderByPhrase(SortSpecification column) {
        String phrase = column.aliasOrName();
        if (column.isDescending()) {
            phrase = phrase + " DESC"; //$NON-NLS-1$
        }
        return phrase;
    }
    
    private Optional<String> renderLimit(Map<String, Object> parameters) {
        return selectModel.limit().map(l -> renderLimit(parameters, l));
    }
    
    private String renderLimit(Map<String, Object> parameters, Long limit) {
        String placeholder = renderingStrategy.getFormattedJdbcPlaceholder(RenderingStrategy.DEFAULT_PARAMETER_PREFIX,
                LIMIT_PARAMETER); 
        parameters.put(LIMIT_PARAMETER, limit);
        return "limit " + placeholder; //$NON-NLS-1$
    }
    
    private Optional<String> renderOffset(Map<String, Object> parameters) {
        return selectModel.offset().map(o -> renderOffset(parameters, o));
    }
    
    private String renderOffset(Map<String, Object> parameters, Long offset) {
        String placeholder = renderingStrategy.getFormattedJdbcPlaceholder(RenderingStrategy.DEFAULT_PARAMETER_PREFIX,
                OFFSET_PARAMETER);
        parameters.put(OFFSET_PARAMETER, offset);
        return "offset " + placeholder; //$NON-NLS-1$
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
