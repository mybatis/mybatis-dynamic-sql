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

import java.util.Optional;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.util.ConstantMapping;
import org.mybatis.dynamic.sql.util.InsertMappingVisitor;
import org.mybatis.dynamic.sql.util.NullMapping;
import org.mybatis.dynamic.sql.util.PropertyMapping;
import org.mybatis.dynamic.sql.util.PropertyWhenPresentMapping;
import org.mybatis.dynamic.sql.util.RowMapping;
import org.mybatis.dynamic.sql.util.StringConstantMapping;

public class ValuePhraseVisitor extends InsertMappingVisitor<Optional<FieldAndValueAndParameters>> {

    protected final RenderingStrategy renderingStrategy;

    public ValuePhraseVisitor(RenderingStrategy renderingStrategy) {
        this.renderingStrategy = renderingStrategy;
    }

    @Override
    public Optional<FieldAndValueAndParameters> visit(NullMapping mapping) {
        return FieldAndValueAndParameters.withFieldName(mapping.columnName())
                .withValuePhrase("null") //$NON-NLS-1$
                .buildOptional();
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
    public Optional<FieldAndValueAndParameters> visit(PropertyMapping mapping) {
        return FieldAndValueAndParameters.withFieldName(mapping.columnName())
                .withValuePhrase(mapping.mapColumn(c -> calculateJdbcPlaceholder(c, mapping.property())))
                .buildOptional();
    }

    @Override
    public Optional<FieldAndValueAndParameters> visit(PropertyWhenPresentMapping mapping) {
        if (mapping.shouldRender()) {
            return visit((PropertyMapping) mapping);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<FieldAndValueAndParameters> visit(RowMapping mapping) {
        return FieldAndValueAndParameters.withFieldName(mapping.columnName())
                .withValuePhrase(mapping.mapColumn(this::calculateJdbcPlaceholder))
                .buildOptional();
    }

    private String calculateJdbcPlaceholder(SqlColumn<?> column) {
        return column.renderingStrategy().orElse(renderingStrategy)
                .getRecordBasedInsertBinding(column, "row"); //$NON-NLS-1$
    }

    private String calculateJdbcPlaceholder(SqlColumn<?> column, String parameterName) {
        return column.renderingStrategy().orElse(renderingStrategy)
                .getRecordBasedInsertBinding(column, "row", parameterName); //$NON-NLS-1$
    }
}
