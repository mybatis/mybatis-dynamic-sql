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
package org.mybatis.dynamic.sql.update.render;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.common.OrderByModel;
import org.mybatis.dynamic.sql.common.OrderByRenderer;
import org.mybatis.dynamic.sql.exception.InvalidSqlException;
import org.mybatis.dynamic.sql.render.ExplicitTableAliasCalculator;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.render.TableAliasCalculator;
import org.mybatis.dynamic.sql.update.UpdateModel;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.FragmentCollector;
import org.mybatis.dynamic.sql.util.Messages;
import org.mybatis.dynamic.sql.where.WhereModel;
import org.mybatis.dynamic.sql.where.render.WhereRenderer;

import static org.mybatis.dynamic.sql.util.StringUtilities.spaceBefore;

public class UpdateRenderer {
    private final UpdateModel updateModel;
    private final RenderingContext renderingContext;

    private UpdateRenderer(Builder builder) {
        updateModel = Objects.requireNonNull(builder.updateModel);
        TableAliasCalculator tableAliasCalculator = builder.updateModel.tableAlias()
                .map(a -> ExplicitTableAliasCalculator.of(updateModel.table(), a))
                .orElseGet(TableAliasCalculator::empty);
        renderingContext = RenderingContext
                .withRenderingStrategy(Objects.requireNonNull(builder.renderingStrategy))
                .withTableAliasCalculator(tableAliasCalculator)
                .build();
    }

    public UpdateStatementProvider render() {
        FragmentCollector fragmentCollector = new FragmentCollector();

        fragmentCollector.add(calculateUpdateStatementStart());
        fragmentCollector.add(calculateSetPhrase());
        calculateWhereClause().ifPresent(fragmentCollector::add);
        calculateOrderByClause().ifPresent(fragmentCollector::add);
        calculateLimitClause().ifPresent(fragmentCollector::add);

        return toUpdateStatementProvider(fragmentCollector);
    }

    private UpdateStatementProvider toUpdateStatementProvider(FragmentCollector fragmentCollector) {
        return DefaultUpdateStatementProvider
                .withUpdateStatement(fragmentCollector.collectFragments(Collectors.joining(" "))) //$NON-NLS-1$
                .withParameters(fragmentCollector.parameters())
                .build();
    }

    private FragmentAndParameters calculateUpdateStatementStart() {
        SqlTable table = updateModel.table();
        String aliasedTableName = renderingContext.tableAliasCalculator().aliasForTable(table)
                .map(a -> table.tableNameAtRuntime() + spaceBefore(a))
                .orElseGet(table::tableNameAtRuntime);
        return FragmentAndParameters.fromFragment("update " + aliasedTableName); //$NON-NLS-1$
    }

    private FragmentAndParameters calculateSetPhrase() {
        SetPhraseVisitor visitor = new SetPhraseVisitor(renderingContext);

        List<Optional<FragmentAndParameters>> fragmentsAndParameters =
                updateModel.mapColumnMappings(m -> m.accept(visitor))
                        .collect(Collectors.toList());

        if (fragmentsAndParameters.stream().noneMatch(Optional::isPresent)) {
            throw new InvalidSqlException(Messages.getString("ERROR.18")); //$NON-NLS-1$
        }

        FragmentCollector fragmentCollector = fragmentsAndParameters.stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(FragmentCollector.collect());

        return toSetPhrase(fragmentCollector);
    }

    private FragmentAndParameters toSetPhrase(FragmentCollector fragmentCollector) {
        return FragmentAndParameters.withFragment(fragmentCollector.collectFragments(
                    Collectors.joining(", ", "set ", ""))) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                .withParameters(fragmentCollector.parameters())
                .build();
    }

    private Optional<FragmentAndParameters> calculateWhereClause() {
        return updateModel.whereModel().flatMap(this::renderWhereClause);
    }

    private Optional<FragmentAndParameters> renderWhereClause(WhereModel whereModel) {
        return WhereRenderer.withWhereModel(whereModel)
                .withRenderingContext(renderingContext)
                .build()
                .render();
    }

    private Optional<FragmentAndParameters> calculateLimitClause() {
        return updateModel.limit().map(this::renderLimitClause);
    }

    private FragmentAndParameters renderLimitClause(Long limit) {
        RenderingContext.ParameterInfo parameterInfo = renderingContext.calculateParameterInfo();

        return FragmentAndParameters.withFragment("limit " + parameterInfo.renderedPlaceHolder()) //$NON-NLS-1$
                .withParameter(parameterInfo.mapKey(), limit)
                .build();
    }

    private Optional<FragmentAndParameters> calculateOrderByClause() {
        return updateModel.orderByModel().map(this::renderOrderByClause);
    }

    private FragmentAndParameters renderOrderByClause(OrderByModel orderByModel) {
        return new OrderByRenderer().render(orderByModel);
    }

    public static Builder withUpdateModel(UpdateModel updateModel) {
        return new Builder().withUpdateModel(updateModel);
    }

    public static class Builder {
        private UpdateModel updateModel;
        private RenderingStrategy renderingStrategy;

        public Builder withUpdateModel(UpdateModel updateModel) {
            this.updateModel = updateModel;
            return this;
        }

        public Builder withRenderingStrategy(RenderingStrategy renderingStrategy) {
            this.renderingStrategy = renderingStrategy;
            return this;
        }

        public UpdateRenderer build() {
            return new UpdateRenderer(this);
        }
    }
}
