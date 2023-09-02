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
package org.mybatis.dynamic.sql.select.render;

import static org.mybatis.dynamic.sql.util.StringUtilities.spaceBefore;

import java.util.Objects;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.select.join.JoinCriterion;
import org.mybatis.dynamic.sql.select.join.JoinModel;
import org.mybatis.dynamic.sql.select.join.JoinSpecification;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.FragmentCollector;

public class JoinRenderer {
    private final JoinModel joinModel;
    private final TableExpressionRenderer tableExpressionRenderer;
    private final RenderingContext renderingContext;

    private JoinRenderer(Builder builder) {
        joinModel = Objects.requireNonNull(builder.joinModel);
        tableExpressionRenderer = Objects.requireNonNull(builder.tableExpressionRenderer);
        renderingContext = Objects.requireNonNull(builder.renderingContext);
    }

    public FragmentAndParameters render() {
        return joinModel.mapJoinSpecifications(this::renderJoinSpecification)
                .collect(FragmentCollector.collect())
                .toFragmentAndParameters(Collectors.joining(" ")); //$NON-NLS-1$
    }

    private FragmentAndParameters renderJoinSpecification(JoinSpecification joinSpecification) {
        FragmentAndParameters renderedTable = joinSpecification.table().accept(tableExpressionRenderer);
        FragmentAndParameters renderedJoin = renderConditions(joinSpecification);

        String fragment = joinSpecification.joinType().type()
                + spaceBefore(renderedTable.fragment())
                + spaceBefore(renderedJoin.fragment());

        return FragmentAndParameters.withFragment(fragment)
                .withParameters(renderedTable.parameters())
                .withParameters(renderedJoin.parameters())
                .build();
    }

    private FragmentAndParameters renderConditions(JoinSpecification joinSpecification) {
        return joinSpecification.mapJoinCriteria(this::renderCriterion)
                .collect(FragmentCollector.collect())
                .toFragmentAndParameters(Collectors.joining(" ")); //$NON-NLS-1$
    }

    private <T> FragmentAndParameters renderCriterion(JoinCriterion<T> joinCriterion) {
        FragmentAndParameters renderedColumn = joinCriterion.leftColumn().render(renderingContext);

        String prefix = joinCriterion.connector()
                + spaceBefore(renderedColumn.fragment());

        JoinConditionRenderer<T> joinConditionRenderer = new JoinConditionRenderer.Builder<T>()
                .withRenderingContext(renderingContext)
                .withLeftColumn(joinCriterion.leftColumn())
                .build();

        FragmentAndParameters suffix = joinCriterion.joinCondition().accept(joinConditionRenderer);

        return FragmentAndParameters.withFragment(prefix + spaceBefore(suffix.fragment()))
                .withParameters(suffix.parameters())
                .withParameters(renderedColumn.parameters())
                .build();
    }

    public static Builder withJoinModel(JoinModel joinModel) {
        return new Builder().withJoinModel(joinModel);
    }

    public static class Builder {
        private JoinModel joinModel;
        private TableExpressionRenderer tableExpressionRenderer;
        private RenderingContext renderingContext;

        public Builder withJoinModel(JoinModel joinModel) {
            this.joinModel = joinModel;
            return this;
        }

        public Builder withTableExpressionRenderer(TableExpressionRenderer tableExpressionRenderer) {
            this.tableExpressionRenderer = tableExpressionRenderer;
            return this;
        }

        public Builder withRenderingContext(RenderingContext renderingContext) {
            this.renderingContext = renderingContext;
            return this;
        }

        public JoinRenderer build() {
            return new JoinRenderer(this);
        }
    }
}
