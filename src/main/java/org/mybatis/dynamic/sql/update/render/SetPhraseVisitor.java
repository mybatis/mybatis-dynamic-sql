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
package org.mybatis.dynamic.sql.update.render;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.render.TableAliasCalculator;
import org.mybatis.dynamic.sql.select.render.SelectRenderer;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.AbstractColumnMapping;
import org.mybatis.dynamic.sql.util.ColumnToColumnMapping;
import org.mybatis.dynamic.sql.util.ConstantMapping;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.NullMapping;
import org.mybatis.dynamic.sql.util.SelectMapping;
import org.mybatis.dynamic.sql.util.StringConstantMapping;
import org.mybatis.dynamic.sql.util.UpdateMappingVisitor;
import org.mybatis.dynamic.sql.util.ValueMapping;
import org.mybatis.dynamic.sql.util.ValueOrNullMapping;
import org.mybatis.dynamic.sql.util.ValueWhenPresentMapping;

public class SetPhraseVisitor extends UpdateMappingVisitor<Optional<FragmentAndParameters>> {

    private final AtomicInteger sequence;
    private final RenderingStrategy renderingStrategy;
    private final Function<SqlColumn<?>, String> aliasedColumnNameFunction;

    public SetPhraseVisitor(AtomicInteger sequence, RenderingStrategy renderingStrategy,
                            TableAliasCalculator tableAliasCalculator) {
        this.sequence = Objects.requireNonNull(sequence);
        this.renderingStrategy = Objects.requireNonNull(renderingStrategy);
        Objects.requireNonNull(tableAliasCalculator);
        aliasedColumnNameFunction = c -> tableAliasCalculator.aliasForColumn(c.table())
                .map(alias -> alias + "." + c.name())  //$NON-NLS-1$
                .orElseGet(c::name);
    }

    @Override
    public Optional<FragmentAndParameters> visit(NullMapping mapping) {
        return FragmentAndParameters
                .withFragment(mapping.mapColumn(aliasedColumnNameFunction) + " = null") //$NON-NLS-1$
                .buildOptional();
    }

    @Override
    public Optional<FragmentAndParameters> visit(ConstantMapping mapping) {
        String fragment = mapping.mapColumn(aliasedColumnNameFunction) + " = " + mapping.constant(); //$NON-NLS-1$
        return FragmentAndParameters.withFragment(fragment)
                .buildOptional();
    }

    @Override
    public Optional<FragmentAndParameters> visit(StringConstantMapping mapping) {
        String fragment = mapping.mapColumn(aliasedColumnNameFunction)
                + " = '" //$NON-NLS-1$
                + mapping.constant()
                + "'"; //$NON-NLS-1$

        return FragmentAndParameters.withFragment(fragment)
                .buildOptional();
    }

    @Override
    public <T> Optional<FragmentAndParameters> visit(ValueMapping<T> mapping) {
        return buildFragment(mapping, mapping.value());
    }

    @Override
    public <T> Optional<FragmentAndParameters> visit(ValueOrNullMapping<T> mapping) {
        return mapping.value()
                .map(v -> buildFragment(mapping, v))
                .orElseGet(() -> FragmentAndParameters
                        .withFragment(mapping.mapColumn(aliasedColumnNameFunction) + " = null") //$NON-NLS-1$
                        .buildOptional()
                );
    }

    @Override
    public <T> Optional<FragmentAndParameters> visit(ValueWhenPresentMapping<T> mapping) {
        return mapping.value().flatMap(v -> buildFragment(mapping, v));
    }

    @Override
    public Optional<FragmentAndParameters> visit(SelectMapping mapping) {
        SelectStatementProvider selectStatement = SelectRenderer.withSelectModel(mapping.selectModel())
                .withRenderingStrategy(renderingStrategy)
                .withSequence(sequence)
                .build()
                .render();

        String fragment = mapping.mapColumn(aliasedColumnNameFunction)
                + " = (" //$NON-NLS-1$
                + selectStatement.getSelectStatement()
                + ")"; //$NON-NLS-1$

        return FragmentAndParameters.withFragment(fragment)
                .withParameters(selectStatement.getParameters())
                .buildOptional();
    }

    @Override
    public Optional<FragmentAndParameters> visit(ColumnToColumnMapping mapping) {
        String setPhrase = mapping.mapColumn(aliasedColumnNameFunction)
                + " = "  //$NON-NLS-1$
                + mapping.rightColumn().renderWithTableAlias(TableAliasCalculator.empty());

        return FragmentAndParameters.withFragment(setPhrase)
                .buildOptional();
    }

    private <T> Optional<FragmentAndParameters> buildFragment(AbstractColumnMapping mapping, T value) {
        String mapKey = RenderingStrategy.formatParameterMapKey(sequence);

        String jdbcPlaceholder = mapping.mapColumn(c -> calculateJdbcPlaceholder(c, mapKey));
        String setPhrase = mapping.mapColumn(aliasedColumnNameFunction)
                + " = "  //$NON-NLS-1$
                + jdbcPlaceholder;

        return FragmentAndParameters.withFragment(setPhrase)
                .withParameter(mapKey, value)
                .buildOptional();
    }

    private String calculateJdbcPlaceholder(SqlColumn<?> column, String parameterName) {
        return column.renderingStrategy().orElse(renderingStrategy)
                .getFormattedJdbcPlaceholder(column, RenderingStrategy.DEFAULT_PARAMETER_PREFIX, parameterName);
    }
}
