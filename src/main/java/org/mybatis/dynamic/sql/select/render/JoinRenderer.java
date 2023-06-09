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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.render.TableAliasCalculator;
import org.mybatis.dynamic.sql.select.join.JoinCriterion;
import org.mybatis.dynamic.sql.select.join.JoinModel;
import org.mybatis.dynamic.sql.select.join.JoinSpecification;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.FragmentCollector;

public class JoinRenderer {
    private final JoinModel joinModel;
    private final TableExpressionRenderer tableExpressionRenderer;
    private final TableAliasCalculator tableAliasCalculator;
    private final RenderingStrategy renderingStrategy;
    private final AtomicInteger sequence;

    private JoinRenderer(Builder builder) {
        joinModel = Objects.requireNonNull(builder.joinModel);
        tableExpressionRenderer = Objects.requireNonNull(builder.tableExpressionRenderer);
        tableAliasCalculator = Objects.requireNonNull(builder.tableAliasCalculator);
        renderingStrategy = Objects.requireNonNull(builder.renderingStrategy);
        sequence = Objects.requireNonNull(builder.sequence);
    }

    public FragmentAndParameters render() {
        FragmentCollector fc = joinModel.mapJoinSpecifications(this::renderJoinSpecification)
                .collect(FragmentCollector.collect());

        return FragmentAndParameters.withFragment(fc.fragments().collect(Collectors.joining(" "))) //$NON-NLS-1$
                .withParameters(fc.parameters())
                .build();
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
        FragmentCollector fragmentCollector = joinSpecification.mapJoinCriteria(this::renderCriterion)
                .collect(FragmentCollector.collect());

        return FragmentAndParameters
                .withFragment(fragmentCollector.fragments().collect(Collectors.joining(" "))) //$NON-NLS-1$
                .withParameters(fragmentCollector.parameters())
                .build();
    }

    private FragmentAndParameters renderCriterion(JoinCriterion joinCriterion) {
        String prefix = joinCriterion.connector()
                + spaceBefore(applyTableAlias(joinCriterion.leftColumn()));

        JoinConditionRenderer joinConditionRenderer = new JoinConditionRenderer.Builder()
                .withRenderingStrategy(renderingStrategy)
                .withSequence(sequence)
                .withTableAliasCalculator(tableAliasCalculator)
                .withLeftColumn(joinCriterion.leftColumn())
                .build();

        FragmentAndParameters suffix = joinCriterion.joinCondition().accept(joinConditionRenderer);

        return FragmentAndParameters.withFragment(prefix + spaceBefore(suffix.fragment()))
                .withParameters(suffix.parameters())
                .build();
    }

    private String applyTableAlias(BasicColumn column) {
        return column.renderWithTableAlias(tableAliasCalculator);
    }

    public static Builder withJoinModel(JoinModel joinModel) {
        return new Builder().withJoinModel(joinModel);
    }

    public static class Builder {
        private JoinModel joinModel;
        private TableExpressionRenderer tableExpressionRenderer;
        private TableAliasCalculator tableAliasCalculator;
        private RenderingStrategy renderingStrategy;
        private AtomicInteger sequence;

        public Builder withJoinModel(JoinModel joinModel) {
            this.joinModel = joinModel;
            return this;
        }

        public Builder withTableExpressionRenderer(TableExpressionRenderer tableExpressionRenderer) {
            this.tableExpressionRenderer = tableExpressionRenderer;
            return this;
        }

        public Builder withTableAliasCalculator(TableAliasCalculator tableAliasCalculator) {
            this.tableAliasCalculator = tableAliasCalculator;
            return this;
        }

        public Builder withRenderingStrategy(RenderingStrategy renderingStrategy) {
            this.renderingStrategy = renderingStrategy;
            return this;
        }

        public Builder withSequence(AtomicInteger sequence) {
            this.sequence = sequence;
            return this;
        }

        public JoinRenderer build() {
            return new JoinRenderer(this);
        }
    }
}
