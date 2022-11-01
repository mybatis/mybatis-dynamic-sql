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
package org.mybatis.dynamic.sql.delete.render;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.delete.DeleteModel;
import org.mybatis.dynamic.sql.render.ExplicitTableAliasCalculator;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.render.TableAliasCalculator;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.FragmentCollector;
import org.mybatis.dynamic.sql.where.WhereModel;
import org.mybatis.dynamic.sql.where.render.WhereRenderer;

public class DeleteRenderer {
    private final DeleteModel deleteModel;
    private final RenderingStrategy renderingStrategy;
    private final TableAliasCalculator tableAliasCalculator;
    private final AtomicInteger sequence = new AtomicInteger(1);

    private DeleteRenderer(Builder builder) {
        deleteModel = Objects.requireNonNull(builder.deleteModel);
        renderingStrategy = Objects.requireNonNull(builder.renderingStrategy);
        tableAliasCalculator = builder.deleteModel.tableAlias()
                .map(a -> ExplicitTableAliasCalculator.of(deleteModel.table(), a))
                .orElseGet(TableAliasCalculator::empty);
    }

    public DeleteStatementProvider render() {
        FragmentCollector fragmentCollector = new FragmentCollector();

        fragmentCollector.add(calculateDeleteStatementStart());
        calculateWhereClause().ifPresent(fragmentCollector::add);
        calculateLimitClause().ifPresent(fragmentCollector::add);

        return fragmentCollector.map(this::toDeleteStatementProvider);
    }

    private DeleteStatementProvider toDeleteStatementProvider(FragmentCollector fragmentCollector) {
        return DefaultDeleteStatementProvider
                .withDeleteStatement(fragmentCollector.fragments().collect(Collectors.joining(" "))) //$NON-NLS-1$
                .withParameters(fragmentCollector.parameters())
                .build();
    }

    private FragmentAndParameters calculateDeleteStatementStart() {
        SqlTable table = deleteModel.table();
        String tableName = table.tableNameAtRuntime();
        String aliasedTableName = tableAliasCalculator.aliasForTable(table)
                .map(a -> tableName + " " + a).orElse(tableName); //$NON-NLS-1$

        return FragmentAndParameters.withFragment("delete from " + aliasedTableName) //$NON-NLS-1$
                .build();
    }

    private Optional<FragmentAndParameters> calculateWhereClause() {
        return deleteModel.whereModel().flatMap(this::renderWhereClause);
    }

    private Optional<FragmentAndParameters> renderWhereClause(WhereModel whereModel) {
        return WhereRenderer.withWhereModel(whereModel)
                .withRenderingStrategy(renderingStrategy)
                .withSequence(sequence)
                .withTableAliasCalculator(tableAliasCalculator)
                .build()
                .render();
    }

    private Optional<FragmentAndParameters> calculateLimitClause() {
        return deleteModel.limit().map(this::renderLimitClause);
    }

    private FragmentAndParameters renderLimitClause(Long limit) {
        String mapKey = RenderingStrategy.formatParameterMapKey(sequence);
        String jdbcPlaceholder =
                renderingStrategy.getFormattedJdbcPlaceholder(RenderingStrategy.DEFAULT_PARAMETER_PREFIX, mapKey);

        return FragmentAndParameters.withFragment("limit " + jdbcPlaceholder) //$NON-NLS-1$
                .withParameter(mapKey, limit)
                .build();
    }

    public static Builder withDeleteModel(DeleteModel deleteModel) {
        return new Builder().withDeleteModel(deleteModel);
    }

    public static class Builder {
        private DeleteModel deleteModel;
        private RenderingStrategy renderingStrategy;

        public Builder withDeleteModel(DeleteModel deleteModel) {
            this.deleteModel = deleteModel;
            return this;
        }

        public Builder withRenderingStrategy(RenderingStrategy renderingStrategy) {
            this.renderingStrategy = renderingStrategy;
            return this;
        }

        public DeleteRenderer build() {
            return new DeleteRenderer(this);
        }
    }
}
