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
package org.mybatis.dynamic.sql.insert.render;

import java.util.Objects;
import java.util.Optional;

import org.mybatis.dynamic.sql.render.RenderedParameterInfo;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.util.AbstractColumnMapping;
import org.mybatis.dynamic.sql.util.ConstantMapping;
import org.mybatis.dynamic.sql.util.GeneralInsertMappingVisitor;
import org.mybatis.dynamic.sql.util.NullMapping;
import org.mybatis.dynamic.sql.util.StringConstantMapping;
import org.mybatis.dynamic.sql.util.StringUtilities;
import org.mybatis.dynamic.sql.util.ValueMapping;
import org.mybatis.dynamic.sql.util.ValueOrNullMapping;
import org.mybatis.dynamic.sql.util.ValueWhenPresentMapping;

public class GeneralInsertValuePhraseVisitor extends GeneralInsertMappingVisitor<Optional<FieldAndValueAndParameters>> {

    private final RenderingContext renderingContext;

    public GeneralInsertValuePhraseVisitor(RenderingContext renderingContext) {
        this.renderingContext = Objects.requireNonNull(renderingContext);
    }

    @Override
    public Optional<FieldAndValueAndParameters> visit(NullMapping mapping) {
        return buildNullFragment(mapping);
    }

    @Override
    public Optional<FieldAndValueAndParameters> visit(ConstantMapping mapping) {
        return FieldAndValueAndParameters.withFieldName(mapping.columnName())
                .withValuePhrase(mapping.constant())
                .buildOptional();
    }

    @Override
    public Optional<FieldAndValueAndParameters> visit(StringConstantMapping mapping) {
        return FieldAndValueAndParameters.withFieldName(mapping.columnName())
                .withValuePhrase(StringUtilities.formatConstantForSQL(mapping.constant()))
                .buildOptional();
    }

    @Override
    public <T> Optional<FieldAndValueAndParameters> visit(ValueMapping<T> mapping) {
        return buildValueFragment(mapping, mapping.value());
    }

    @Override
    public <T> Optional<FieldAndValueAndParameters> visit(ValueOrNullMapping<T> mapping) {
        return mapping.value().map(v -> buildValueFragment(mapping, v))
                .orElseGet(() -> buildNullFragment(mapping));
    }

    @Override
    public <T> Optional<FieldAndValueAndParameters> visit(ValueWhenPresentMapping<T> mapping) {
        return mapping.value().flatMap(v -> buildValueFragment(mapping, v));
    }

    private Optional<FieldAndValueAndParameters> buildValueFragment(AbstractColumnMapping mapping,
            Object value) {
        return buildFragment(mapping, value);
    }

    private Optional<FieldAndValueAndParameters> buildNullFragment(AbstractColumnMapping mapping) {
        return FieldAndValueAndParameters.withFieldName(mapping.columnName())
                .withValuePhrase("null") //$NON-NLS-1$
                .buildOptional();
    }

    private Optional<FieldAndValueAndParameters> buildFragment(AbstractColumnMapping mapping, Object value) {
        RenderedParameterInfo parameterInfo = renderingContext.calculateParameterInfo(mapping.column());

        return FieldAndValueAndParameters.withFieldName(mapping.columnName())
                .withValuePhrase(parameterInfo.renderedPlaceHolder())
                .withParameter(parameterInfo.parameterMapKey(), value)
                .buildOptional();
    }
}
