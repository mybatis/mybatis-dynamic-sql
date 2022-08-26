/*
 *    Copyright 2016-2022 the original author or authors.
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

import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.TableExpressionVisitor;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.render.TableAliasCalculator;
import org.mybatis.dynamic.sql.select.SubQuery;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;

public class TableExpressionRenderer implements TableExpressionVisitor<FragmentAndParameters> {
    private final TableAliasCalculator tableAliasCalculator;
    private final RenderingStrategy renderingStrategy;
    private final AtomicInteger sequence;

    private TableExpressionRenderer(Builder builder) {
        tableAliasCalculator = Objects.requireNonNull(builder.tableAliasCalculator);
        renderingStrategy = Objects.requireNonNull(builder.renderingStrategy);
        sequence = Objects.requireNonNull(builder.sequence);
    }

    @Override
    public FragmentAndParameters visit(SqlTable table) {
        return FragmentAndParameters.withFragment(
                tableAliasCalculator.aliasForTable(table)
                        .map(a -> table.tableNameAtRuntime() + spaceBefore(a))
                        .orElseGet(table::tableNameAtRuntime))
                .build();
    }

    @Override
    public FragmentAndParameters visit(SubQuery subQuery) {
        SelectStatementProvider selectStatement = new SelectRenderer.Builder()
                .withSelectModel(subQuery.selectModel())
                .withRenderingStrategy(renderingStrategy)
                .withSequence(sequence)
                .build()
                .render();

        String fragment = "(" + selectStatement.getSelectStatement() + ")"; //$NON-NLS-1$ //$NON-NLS-2$

        fragment = applyAlias(fragment, subQuery);

        return FragmentAndParameters.withFragment(fragment)
                .withParameters(selectStatement.getParameters())
                .build();
    }

    private String applyAlias(String fragment, SubQuery subQuery) {
        return subQuery.alias()
                .map(a -> fragment + spaceBefore(a))
                .orElse(fragment);
    }

    public static class Builder {
        private TableAliasCalculator tableAliasCalculator;
        private RenderingStrategy renderingStrategy;
        private AtomicInteger sequence;

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

        public TableExpressionRenderer build() {
            return new TableExpressionRenderer(this);
        }
    }
}
