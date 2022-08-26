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
package org.mybatis.dynamic.sql.insert.render;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.util.AbstractColumnMapping;
import org.mybatis.dynamic.sql.util.ConstantMapping;
import org.mybatis.dynamic.sql.util.GeneralInsertMappingVisitor;
import org.mybatis.dynamic.sql.util.NullMapping;
import org.mybatis.dynamic.sql.util.StringConstantMapping;
import org.mybatis.dynamic.sql.util.ValueMapping;
import org.mybatis.dynamic.sql.util.ValueOrNullMapping;
import org.mybatis.dynamic.sql.util.ValueWhenPresentMapping;

public class GeneralInsertValuePhraseVisitor extends GeneralInsertMappingVisitor<Optional<FieldAndValueAndParameters>> {

    private final RenderingStrategy renderingStrategy;
    private final AtomicInteger sequence = new AtomicInteger(1);

    public GeneralInsertValuePhraseVisitor(RenderingStrategy renderingStrategy) {
        this.renderingStrategy = renderingStrategy;
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
                .withValuePhrase("'" + mapping.constant() + "'") //$NON-NLS-1$ //$NON-NLS-2$
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
        String mapKey = RenderingStrategy.formatParameterMapKey(sequence);

        String jdbcPlaceholder = mapping.mapColumn(c -> calculateJdbcPlaceholder(c, mapKey));

        return FieldAndValueAndParameters.withFieldName(mapping.columnName())
                .withValuePhrase(jdbcPlaceholder)
                .withParameter(mapKey, value)
                .buildOptional();
    }

    private String calculateJdbcPlaceholder(SqlColumn<?> column, String parameterName) {
        return column.renderingStrategy().orElse(renderingStrategy)
                .getFormattedJdbcPlaceholder(column, RenderingStrategy.DEFAULT_PARAMETER_PREFIX, parameterName);
    }
}
