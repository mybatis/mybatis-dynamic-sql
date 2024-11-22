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
package org.mybatis.dynamic.sql.update.render;

import java.util.Objects;
import java.util.Optional;

import org.mybatis.dynamic.sql.render.RenderedParameterInfo;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.util.AbstractColumnMapping;
import org.mybatis.dynamic.sql.util.ColumnToColumnMapping;
import org.mybatis.dynamic.sql.util.ConstantMapping;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.NullMapping;
import org.mybatis.dynamic.sql.util.SelectMapping;
import org.mybatis.dynamic.sql.util.StringConstantMapping;
import org.mybatis.dynamic.sql.util.StringUtilities;
import org.mybatis.dynamic.sql.util.UpdateMappingVisitor;
import org.mybatis.dynamic.sql.util.ValueMapping;
import org.mybatis.dynamic.sql.util.ValueOrNullMapping;
import org.mybatis.dynamic.sql.util.ValueWhenPresentMapping;

public class SetPhraseVisitor extends UpdateMappingVisitor<Optional<FragmentAndParameters>> {

    private final RenderingContext renderingContext;

    public SetPhraseVisitor(RenderingContext renderingContext) {
        this.renderingContext = Objects.requireNonNull(renderingContext);
    }

    @Override
    public Optional<FragmentAndParameters> visit(NullMapping mapping) {
        return buildNullFragment(mapping);
    }

    @Override
    public Optional<FragmentAndParameters> visit(ConstantMapping mapping) {
        String fragment = renderingContext.aliasedColumnName(mapping.column())
                + " = " + mapping.constant(); //$NON-NLS-1$
        return FragmentAndParameters.withFragment(fragment)
                .buildOptional();
    }

    @Override
    public Optional<FragmentAndParameters> visit(StringConstantMapping mapping) {
        String fragment = renderingContext.aliasedColumnName(mapping.column())
                + " = " //$NON-NLS-1$
                + StringUtilities.formatConstantForSQL(mapping.constant());

        return FragmentAndParameters.withFragment(fragment)
                .buildOptional();
    }

    @Override
    public <T> Optional<FragmentAndParameters> visit(ValueMapping<T> mapping) {
        return buildValueFragment(mapping, mapping.value());
    }

    @Override
    public <T> Optional<FragmentAndParameters> visit(ValueOrNullMapping<T> mapping) {
        return mapping.value()
                .map(v -> buildValueFragment(mapping, v))
                .orElseGet(() -> buildNullFragment(mapping));
    }

    @Override
    public <T> Optional<FragmentAndParameters> visit(ValueWhenPresentMapping<T> mapping) {
        return mapping.value().flatMap(v -> buildValueFragment(mapping, v));
    }

    @Override
    public Optional<FragmentAndParameters> visit(SelectMapping mapping) {
        return Optional.of(mapping.selectModel().renderSubQuery(renderingContext)
                .mapFragment(f -> renderingContext.aliasedColumnName(mapping.column())
                        + " = (" + f + ")")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public Optional<FragmentAndParameters> visit(ColumnToColumnMapping mapping) {
        return Optional.of(mapping.rightColumn().render(renderingContext)
                .mapFragment(f -> renderingContext.aliasedColumnName(mapping.column()) + " = " + f)); //$NON-NLS-1$
    }

    private <T> Optional<FragmentAndParameters> buildValueFragment(AbstractColumnMapping mapping, T value) {
        RenderedParameterInfo parameterInfo = renderingContext.calculateParameterInfo(mapping.column());
        String setPhrase = renderingContext.aliasedColumnName(mapping.column())
                + " = "  //$NON-NLS-1$
                + parameterInfo.renderedPlaceHolder();

        return FragmentAndParameters.withFragment(setPhrase)
                .withParameter(parameterInfo.parameterMapKey(), value)
                .buildOptional();
    }

    private Optional<FragmentAndParameters> buildNullFragment(AbstractColumnMapping mapping) {
        return FragmentAndParameters
                .withFragment(renderingContext.aliasedColumnName(mapping.column()) + " = null") //$NON-NLS-1$
                .buildOptional();
    }
}
