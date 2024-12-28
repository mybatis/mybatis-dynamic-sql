/*
 *    Copyright 2016-2024 the original author or authors.
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
package org.mybatis.dynamic.sql.select.render;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.common.OrderByModel;
import org.mybatis.dynamic.sql.common.OrderByRenderer;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.select.PagingModel;
import org.mybatis.dynamic.sql.select.QueryExpressionModel;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.FragmentCollector;

public class SubQueryRenderer {
    private final SelectModel selectModel;
    private final RenderingContext renderingContext;
    private final String prefix;
    private final String suffix;

    private SubQueryRenderer(Builder builder) {
        selectModel = Objects.requireNonNull(builder.selectModel);
        renderingContext = Objects.requireNonNull(builder.renderingContext);
        prefix = builder.prefix == null ? "" : builder.prefix; //$NON-NLS-1$
        suffix = builder.suffix == null ? "" : builder.suffix; //$NON-NLS-1$
    }

    public FragmentAndParameters render() {
        FragmentCollector fragmentCollector = selectModel
                .queryExpressions()
                .map(this::renderQueryExpression)
                .collect(FragmentCollector.collect());

        renderOrderBy().ifPresent(fragmentCollector::add);
        renderPagingModel().ifPresent(fragmentCollector::add);

        return fragmentCollector.toFragmentAndParameters(Collectors.joining(" ", prefix, suffix)); //$NON-NLS-1$
    }

    private FragmentAndParameters renderQueryExpression(QueryExpressionModel queryExpressionModel) {
        return QueryExpressionRenderer.withQueryExpression(queryExpressionModel)
                .withRenderingContext(renderingContext)
                .build()
                .render();
    }

    private Optional<FragmentAndParameters> renderOrderBy() {
        return selectModel.orderByModel().map(this::renderOrderBy);
    }

    private FragmentAndParameters renderOrderBy(OrderByModel orderByModel) {
        return new OrderByRenderer(renderingContext).render(orderByModel);
    }

    private Optional<FragmentAndParameters> renderPagingModel() {
        return selectModel.pagingModel().map(this::renderPagingModel);
    }

    private FragmentAndParameters renderPagingModel(PagingModel pagingModel) {
        return new PagingModelRenderer.Builder()
                .withPagingModel(pagingModel)
                .withRenderingContext(renderingContext)
                .build()
                .render();
    }

    public static Builder withSelectModel(SelectModel selectModel) {
        return new Builder().withSelectModel(selectModel);
    }

    public static class Builder {
        private SelectModel selectModel;
        private RenderingContext renderingContext;
        private String prefix;
        private String suffix;

        public Builder withRenderingContext(RenderingContext renderingContext) {
            this.renderingContext = renderingContext;
            return this;
        }

        public Builder withSelectModel(SelectModel selectModel) {
            this.selectModel = selectModel;
            return this;
        }

        public Builder withPrefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public Builder withSuffix(String suffix) {
            this.suffix = suffix;
            return this;
        }

        public SubQueryRenderer build() {
            return new SubQueryRenderer(this);
        }
    }
}
