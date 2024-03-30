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
import org.mybatis.dynamic.sql.configuration.StatementConfiguration;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.MultiSelectModel;
import org.mybatis.dynamic.sql.select.PagingModel;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.select.UnionQuery;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.FragmentCollector;

public class MultiSelectRenderer {
    private final MultiSelectModel multiSelectModel;
    private final RenderingContext renderingContext;

    private MultiSelectRenderer(Builder builder) {
        renderingContext = RenderingContext
                .withRenderingStrategy(builder.renderingStrategy)
                .withStatementConfiguration(builder.statementConfiguration)
                .build();
        multiSelectModel = Objects.requireNonNull(builder.multiSelectModel);
    }

    public SelectStatementProvider render() {
        FragmentAndParameters initialSelect = renderSelect(multiSelectModel.initialSelect());

        FragmentCollector fragmentCollector = multiSelectModel
                .unionQueries()
                .map(this::renderSelect)
                .collect(FragmentCollector.collect(initialSelect));

        renderOrderBy().ifPresent(fragmentCollector::add);
        renderPagingModel().ifPresent(fragmentCollector::add);

        return toSelectStatementProvider(fragmentCollector);
    }

    private SelectStatementProvider toSelectStatementProvider(FragmentCollector fragmentCollector) {
        return DefaultSelectStatementProvider
                .withSelectStatement(fragmentCollector.collectFragments(Collectors.joining(" "))) //$NON-NLS-1$
                .withParameters(fragmentCollector.parameters())
                .build();
    }

    private FragmentAndParameters renderSelect(SelectModel selectModel) {
        SelectStatementProvider selectStatement = selectModel.render(renderingContext);

        return FragmentAndParameters
                .withFragment("(" + selectStatement.getSelectStatement() + ")") //$NON-NLS-1$ //$NON-NLS-2$
                .withParameters(selectStatement.getParameters())
                .build();
    }

    private FragmentAndParameters renderSelect(UnionQuery unionQuery) {
        SelectStatementProvider selectStatement = unionQuery.selectModel().render(renderingContext);

        return FragmentAndParameters.withFragment(
                unionQuery.connector() + " (" + selectStatement.getSelectStatement() + ")") //$NON-NLS-1$ //$NON-NLS-2$
                .withParameters(selectStatement.getParameters())
                .build();
    }

    private Optional<FragmentAndParameters> renderOrderBy() {
        return multiSelectModel.orderByModel().map(this::renderOrderBy);
    }

    private FragmentAndParameters renderOrderBy(OrderByModel orderByModel) {
        return new OrderByRenderer().render(orderByModel);
    }

    private Optional<FragmentAndParameters> renderPagingModel() {
        return multiSelectModel.pagingModel().map(this::renderPagingModel);
    }

    private FragmentAndParameters renderPagingModel(PagingModel pagingModel) {
        return new PagingModelRenderer.Builder()
                .withPagingModel(pagingModel)
                .withRenderingContext(renderingContext)
                .build()
                .render();
    }

    public static class Builder {
        private RenderingStrategy renderingStrategy;
        private MultiSelectModel multiSelectModel;
        private StatementConfiguration statementConfiguration;

        public Builder withRenderingStrategy(RenderingStrategy renderingStrategy) {
            this.renderingStrategy = renderingStrategy;
            return this;
        }

        public Builder withMultiSelectModel(MultiSelectModel multiSelectModel) {
            this.multiSelectModel = multiSelectModel;
            return this;
        }

        public Builder withStatementConfiguration(StatementConfiguration statementConfiguration) {
            this.statementConfiguration = statementConfiguration;
            return this;
        }

        public MultiSelectRenderer build() {
            return new MultiSelectRenderer(this);
        }
    }
}
