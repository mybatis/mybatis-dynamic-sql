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
package org.mybatis.dynamic.sql.render;

import static org.mybatis.dynamic.sql.util.StringUtilities.spaceBefore;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.mybatis.dynamic.sql.BindableColumn;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.configuration.StatementConfiguration;

/**
 * This class encapsulates all the supporting items related to rendering, and contains many utility methods
 * used during the rendering process.
 *
 * @since 1.5.1
 * @author Jeff Butler
 */
public class RenderingContext {

    private final RenderingStrategy renderingStrategy;
    private final AtomicInteger sequence;
    private final TableAliasCalculator tableAliasCalculator;
    private final String configuredParameterName;
    private final String calculatedParameterName;
    private final StatementConfiguration statementConfiguration;

    private RenderingContext(Builder builder) {
        renderingStrategy = Objects.requireNonNull(builder.renderingStrategy);
        configuredParameterName = builder.parameterName;
        tableAliasCalculator = Objects.requireNonNull(builder.tableAliasCalculator);
        statementConfiguration = Objects.requireNonNull(builder.statementConfiguration);

        // reasonable defaults
        sequence = builder.sequence == null ? new AtomicInteger(1) : builder.sequence;
        calculatedParameterName = builder.parameterName == null ? RenderingStrategy.DEFAULT_PARAMETER_PREFIX
                : builder.parameterName + "." + RenderingStrategy.DEFAULT_PARAMETER_PREFIX;  //$NON-NLS-1$
    }

    public TableAliasCalculator tableAliasCalculator() {
        // this method can be removed when the renderWithTableAlias method is removed from BasicColumn
        return tableAliasCalculator;
    }

    private String nextMapKey() {
        return renderingStrategy.formatParameterMapKey(sequence);
    }

    private String renderedPlaceHolder(String mapKey) {
        return renderingStrategy.getFormattedJdbcPlaceholder(calculatedParameterName, mapKey);
    }

    private <T> String renderedPlaceHolder(String mapKey, BindableColumn<T> column) {
        return  column.renderingStrategy().orElse(renderingStrategy)
                .getFormattedJdbcPlaceholder(column, calculatedParameterName, mapKey);
    }

    public RenderedParameterInfo calculateParameterInfo() {
        String mapKey = nextMapKey();
        return new RenderedParameterInfo(mapKey, renderedPlaceHolder(mapKey));
    }

    public <T> RenderedParameterInfo calculateParameterInfo(BindableColumn<T> column) {
        String mapKey = nextMapKey();
        return new RenderedParameterInfo(mapKey, renderedPlaceHolder(mapKey, column));
    }

    public <T> String aliasedColumnName(SqlColumn<T> column) {
        return tableAliasCalculator.aliasForColumn(column.table())
                .map(alias -> aliasedColumnName(column, alias))
                .orElseGet(column::name);
    }

    public <T> String aliasedColumnName(SqlColumn<T> column, String explicitAlias) {
        return explicitAlias + "." + column.name();  //$NON-NLS-1$
    }

    public String aliasedTableName(SqlTable table) {
        return tableAliasCalculator.aliasForTable(table)
                .map(a -> table.tableNameAtRuntime() + spaceBefore(a))
                .orElseGet(table::tableNameAtRuntime);
    }

    /**
     * Create a new rendering context based on this, with the table alias calculator modified to include the
     * specified child table alias calculator. This is used by the query expression renderer when the alias calculator
     * may change during rendering.
     *
     * @param childTableAliasCalculator the child table alias calculator
     * @return a new rendering context whose table alias calculator is composed of the former calculator as parent, and
     *     the new child calculator
     */
    public RenderingContext withChildTableAliasCalculator(TableAliasCalculator childTableAliasCalculator) {
        TableAliasCalculator tac = new TableAliasCalculatorWithParent.Builder()
                .withParent(tableAliasCalculator)
                .withChild(childTableAliasCalculator)
                .build();

        return new Builder()
                .withRenderingStrategy(this.renderingStrategy)
                .withSequence(this.sequence)
                .withParameterName(this.configuredParameterName)
                .withTableAliasCalculator(tac)
                .withStatementConfiguration(statementConfiguration)
                .build();
    }

    public static Builder withRenderingStrategy(RenderingStrategy renderingStrategy) {
        return new Builder().withRenderingStrategy(renderingStrategy);
    }

    public static class Builder {
        private RenderingStrategy renderingStrategy;
        private AtomicInteger sequence;
        private TableAliasCalculator tableAliasCalculator = TableAliasCalculator.empty();
        private String parameterName;
        private StatementConfiguration statementConfiguration;

        public Builder withRenderingStrategy(RenderingStrategy renderingStrategy) {
            this.renderingStrategy = renderingStrategy;
            return this;
        }

        public Builder withSequence(AtomicInteger sequence) {
            this.sequence = sequence;
            return this;
        }

        public Builder withTableAliasCalculator(TableAliasCalculator tableAliasCalculator) {
            this.tableAliasCalculator = tableAliasCalculator;
            return this;
        }

        public Builder withParameterName(String parameterName) {
            this.parameterName = parameterName;
            return this;
        }

        public Builder withStatementConfiguration(StatementConfiguration statementConfiguration) {
            this.statementConfiguration = statementConfiguration;
            return this;
        }

        public RenderingContext build() {
            return new RenderingContext(this);
        }
    }
}
