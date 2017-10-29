/**
 *    Copyright 2016-2017 the original author or authors.
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
package org.mybatis.dynamic.sql.insert.render;

import java.util.function.Function;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.util.ConstantMapping;
import org.mybatis.dynamic.sql.util.InsertMappingVisitor;
import org.mybatis.dynamic.sql.util.NullMapping;
import org.mybatis.dynamic.sql.util.PropertyMapping;
import org.mybatis.dynamic.sql.util.StringConstantMapping;

public class ValuePhraseVisitor implements InsertMappingVisitor<FieldAndValue> {
    
    private RenderingStrategy renderingStrategy;
    
    public ValuePhraseVisitor(RenderingStrategy renderingStrategy) {
        this.renderingStrategy = renderingStrategy;
    }

    @Override
    public FieldAndValue visit(NullMapping mapping) {
        return FieldAndValue.of(mapping.mapColumn(SqlColumn::name), "null"); //$NON-NLS-1$
    }

    @Override
    public FieldAndValue visit(ConstantMapping mapping) {
        return FieldAndValue.of(mapping.mapColumn(SqlColumn::name), mapping.constant());
    }

    @Override
    public FieldAndValue visit(StringConstantMapping mapping) {
        return FieldAndValue.of(mapping.mapColumn(SqlColumn::name), "'" + mapping.constant() + "'"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    @Override
    public FieldAndValue visit(PropertyMapping mapping) {
        String placeholder = mapping.mapColumn(getColumnMapper(mapping.property()));
        return FieldAndValue.of(mapping.mapColumn(SqlColumn::name), placeholder);
    }
    
    private Function<SqlColumn<?>, String> getColumnMapper(String parameterName) {
        return column -> renderingStrategy.getFormattedJdbcPlaceholder(column, "record", parameterName); //$NON-NLS-1$
    }
}
