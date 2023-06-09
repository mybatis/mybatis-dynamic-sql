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

import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.render.TableAliasCalculator;
import org.mybatis.dynamic.sql.select.join.ColumnBasedJoinCondition;
import org.mybatis.dynamic.sql.select.join.JoinConditionVisitor;
import org.mybatis.dynamic.sql.select.join.TypedJoinCondition;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;

public class JoinConditionRenderer implements JoinConditionVisitor<FragmentAndParameters> {
    private final RenderingStrategy renderingStrategy;
    private final AtomicInteger sequence;
    private final BindableColumn<?> leftColumn;
    private final TableAliasCalculator tableAliasCalculator;

    private JoinConditionRenderer(Builder builder) {
        renderingStrategy = Objects.requireNonNull(builder.renderingStrategy);
        sequence = Objects.requireNonNull(builder.sequence);
        leftColumn = Objects.requireNonNull(builder.leftColumn);
        tableAliasCalculator = Objects.requireNonNull(builder.tableAliasCalculator);
    }

    @Override
    public <T> FragmentAndParameters visit(TypedJoinCondition<T> condition) {
        String mapKey = renderingStrategy.formatParameterMapKey(sequence);

        String placeHolder =  leftColumn.renderingStrategy().orElse(renderingStrategy)
                .getFormattedJdbcPlaceholder(leftColumn, RenderingStrategy.DEFAULT_PARAMETER_PREFIX, mapKey);

        return FragmentAndParameters.withFragment(condition.operator() + spaceBefore(placeHolder))
                .withParameter(mapKey, condition.value())
                .build();
    }

    @Override
    public FragmentAndParameters visit(ColumnBasedJoinCondition condition) {
        return FragmentAndParameters
                .withFragment(condition.operator() + spaceBefore(applyTableAlias(condition.rightColumn())))
                .build();
    }

    private String applyTableAlias(BasicColumn column) {
        return column.renderWithTableAlias(tableAliasCalculator);
    }

    public static class Builder {
        private RenderingStrategy renderingStrategy;
        private AtomicInteger sequence;
        private BindableColumn<?> leftColumn;
        private TableAliasCalculator tableAliasCalculator;

        public Builder withRenderingStrategy(RenderingStrategy renderingStrategy) {
            this.renderingStrategy = renderingStrategy;
            return this;
        }

        public Builder withSequence(AtomicInteger sequence) {
            this.sequence = sequence;
            return this;
        }

        public Builder withLeftColumn(BindableColumn<?> leftColumn) {
            this.leftColumn = leftColumn;
            return this;
        }

        public Builder withTableAliasCalculator(TableAliasCalculator tableAliasCalculator) {
            this.tableAliasCalculator = tableAliasCalculator;
            return this;
        }

        public JoinConditionRenderer build() {
            return new JoinConditionRenderer(this);
        }
    }
}
