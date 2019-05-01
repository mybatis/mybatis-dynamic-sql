/**
 *    Copyright 2016-2019 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.dynamic.sql.update.render;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.render.TableAliasCalculator;
import org.mybatis.dynamic.sql.select.render.SelectRenderer;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.ColumnMapping;
import org.mybatis.dynamic.sql.util.ConstantMapping;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.NullMapping;
import org.mybatis.dynamic.sql.util.SelectMapping;
import org.mybatis.dynamic.sql.util.StringConstantMapping;
import org.mybatis.dynamic.sql.util.UpdateMappingVisitor;
import org.mybatis.dynamic.sql.util.ValueMapping;

public class SetPhraseVisitor implements UpdateMappingVisitor<FragmentAndParameters> {
    
    private AtomicInteger sequence;
    private RenderingStrategy renderingStrategy;
    
    public SetPhraseVisitor(AtomicInteger sequence, RenderingStrategy renderingStrategy) {
        this.sequence = Objects.requireNonNull(sequence);
        this.renderingStrategy = Objects.requireNonNull(renderingStrategy);
    }

    @Override
    public FragmentAndParameters visit(NullMapping mapping) {
        return FragmentAndParameters.withFragment(mapping.mapColumn(SqlColumn::name) + " = null") //$NON-NLS-1$
                .build();
    }

    @Override
    public FragmentAndParameters visit(ConstantMapping mapping) {
        String fragment = mapping.mapColumn(SqlColumn::name) + " = " + mapping.constant(); //$NON-NLS-1$
        return FragmentAndParameters.withFragment(fragment)
                .build();
    }

    @Override
    public FragmentAndParameters visit(StringConstantMapping mapping) {
        String fragment = mapping.mapColumn(SqlColumn::name)
                + " = '" //$NON-NLS-1$
                + mapping.constant()
                + "'"; //$NON-NLS-1$
        
        return FragmentAndParameters.withFragment(fragment)
                .build();
    }
    
    @Override
    public <T> FragmentAndParameters visit(ValueMapping<T> mapping) {
        String mapKey = "p" + sequence.getAndIncrement(); //$NON-NLS-1$

        String jdbcPlaceholder = mapping.mapColumn(toJdbcPlaceholder(mapKey));
        String setPhrase = mapping.mapColumn(SqlColumn::name)
                + " = "  //$NON-NLS-1$
                + jdbcPlaceholder;
        
        return FragmentAndParameters.withFragment(setPhrase)
                .withParameter(mapKey, mapping.value())
                .build();
    }

    @Override
    public FragmentAndParameters visit(SelectMapping mapping) {
        SelectStatementProvider selectStatement = SelectRenderer.withSelectModel(mapping.selectModel())
                .withRenderingStrategy(renderingStrategy)
                .withSequence(sequence)
                .build()
                .render();
        
        String fragment = mapping.mapColumn(SqlColumn::name)
                + " = (" //$NON-NLS-1$
                + selectStatement.getSelectStatement()
                + ")"; //$NON-NLS-1$
        
        return FragmentAndParameters.withFragment(fragment)
                .withParameters(selectStatement.getParameters())
                .build();
    }

    @Override
    public FragmentAndParameters visit(ColumnMapping mapping) {
        String setPhrase = mapping.mapColumn(SqlColumn::name)
                + " = "  //$NON-NLS-1$
                + mapping.rightColumn().renderWithTableAlias(TableAliasCalculator.empty());
        
        return FragmentAndParameters.withFragment(setPhrase)
                .build();
    }
    
    private Function<SqlColumn<?>, String> toJdbcPlaceholder(String parameterName) {
        return column -> renderingStrategy
                .getFormattedJdbcPlaceholder(column, RenderingStrategy.DEFAULT_PARAMETER_PREFIX, parameterName);
    }
}
