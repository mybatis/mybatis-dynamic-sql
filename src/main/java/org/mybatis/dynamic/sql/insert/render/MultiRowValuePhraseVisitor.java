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

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.util.ConstantMapping;
import org.mybatis.dynamic.sql.util.MultiRowInsertMappingVisitor;
import org.mybatis.dynamic.sql.util.NullMapping;
import org.mybatis.dynamic.sql.util.PropertyMapping;
import org.mybatis.dynamic.sql.util.RowMapping;
import org.mybatis.dynamic.sql.util.StringConstantMapping;
import org.mybatis.dynamic.sql.util.StringUtilities;

public class MultiRowValuePhraseVisitor extends MultiRowInsertMappingVisitor<FieldAndValueAndParameters> {
    protected final RenderingStrategy renderingStrategy;
    protected final String prefix;

    protected MultiRowValuePhraseVisitor(RenderingStrategy renderingStrategy, String prefix) {
        this.renderingStrategy = renderingStrategy;
        this.prefix = prefix;
    }

    @Override
    public FieldAndValueAndParameters visit(NullMapping mapping) {
        return FieldAndValueAndParameters.withFieldName(mapping.columnName())
                .withValuePhrase("null") //$NON-NLS-1$
                .build();
    }

    @Override
    public FieldAndValueAndParameters visit(ConstantMapping mapping) {
        return FieldAndValueAndParameters.withFieldName(mapping.columnName())
                .withValuePhrase(mapping.constant())
                .build();
    }

    @Override
    public FieldAndValueAndParameters visit(StringConstantMapping mapping) {
        return FieldAndValueAndParameters.withFieldName(mapping.columnName())
                .withValuePhrase(StringUtilities.formatConstantForSQL(mapping.constant()))
                .build();
    }

    @Override
    public FieldAndValueAndParameters visit(PropertyMapping mapping) {
        return FieldAndValueAndParameters.withFieldName(mapping.columnName())
                .withValuePhrase(calculateJdbcPlaceholder(mapping.column(), mapping.property()))
                .build();
    }

    @Override
    public FieldAndValueAndParameters visit(RowMapping mapping) {
        return FieldAndValueAndParameters.withFieldName(mapping.columnName())
                .withValuePhrase(calculateJdbcPlaceholder(mapping.column()))
                .build();
    }

    private String calculateJdbcPlaceholder(SqlColumn<?> column) {
        return column.renderingStrategy().orElse(renderingStrategy).getRecordBasedInsertBinding(column, prefix);
    }

    private String calculateJdbcPlaceholder(SqlColumn<?> column, String parameterName) {
        return column.renderingStrategy().orElse(renderingStrategy)
                .getRecordBasedInsertBinding(column, prefix, parameterName);
    }
}
