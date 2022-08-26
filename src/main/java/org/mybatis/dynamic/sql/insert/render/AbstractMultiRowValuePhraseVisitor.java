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

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.util.ConstantMapping;
import org.mybatis.dynamic.sql.util.MultiRowInsertMappingVisitor;
import org.mybatis.dynamic.sql.util.NullMapping;
import org.mybatis.dynamic.sql.util.PropertyMapping;
import org.mybatis.dynamic.sql.util.StringConstantMapping;

public abstract class AbstractMultiRowValuePhraseVisitor extends MultiRowInsertMappingVisitor<FieldAndValue> {

    protected final RenderingStrategy renderingStrategy;
    protected final String prefix;

    protected AbstractMultiRowValuePhraseVisitor(RenderingStrategy renderingStrategy, String prefix) {
        this.renderingStrategy = renderingStrategy;
        this.prefix = prefix;
    }

    @Override
    public FieldAndValue visit(NullMapping mapping) {
        return FieldAndValue.withFieldName(mapping.columnName())
                .withValuePhrase("null") //$NON-NLS-1$
                .build();
    }

    @Override
    public FieldAndValue visit(ConstantMapping mapping) {
        return FieldAndValue.withFieldName(mapping.columnName())
                .withValuePhrase(mapping.constant())
                .build();
    }

    @Override
    public FieldAndValue visit(StringConstantMapping mapping) {
        return FieldAndValue.withFieldName(mapping.columnName())
                .withValuePhrase("'" + mapping.constant() + "'") //$NON-NLS-1$ //$NON-NLS-2$
                .build();
    }

    @Override
    public FieldAndValue visit(PropertyMapping mapping) {
        return FieldAndValue.withFieldName(mapping.columnName())
                .withValuePhrase(mapping.mapColumn(c -> calculateJdbcPlaceholder(c, mapping.property())))
                .build();
    }

    abstract String calculateJdbcPlaceholder(SqlColumn<?> column, String parameterName);
}
