/**
 *    Copyright 2016-2020 the original author or authors.
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

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.util.ConstantMapping;
import org.mybatis.dynamic.sql.util.GeneralInsertMappingVisitor;
import org.mybatis.dynamic.sql.util.NullMapping;
import org.mybatis.dynamic.sql.util.StringConstantMapping;
import org.mybatis.dynamic.sql.util.ValueMapping;
import org.mybatis.dynamic.sql.util.ValueWhenPresentMapping;

public class GeneralInsertValuePhraseVisitor extends GeneralInsertMappingVisitor<Optional<FieldAndValueAndParameters>> {
    
    private RenderingStrategy renderingStrategy;
    private AtomicInteger sequence = new AtomicInteger(1);
    
    public GeneralInsertValuePhraseVisitor(RenderingStrategy renderingStrategy) {
        this.renderingStrategy = renderingStrategy;
    }

    @Override
    public Optional<FieldAndValueAndParameters> visit(NullMapping mapping) {
        return FieldAndValueAndParameters.withFieldName(mapping.mapColumn(SqlColumn::name))
                .withValuePhrase("null") //$NON-NLS-1$
                .buildOptional();
    }

    @Override
    public Optional<FieldAndValueAndParameters> visit(ConstantMapping mapping) {
        return FieldAndValueAndParameters.withFieldName(mapping.mapColumn(SqlColumn::name))
                .withValuePhrase(mapping.constant())
                .buildOptional();
    }

    @Override
    public Optional<FieldAndValueAndParameters> visit(StringConstantMapping mapping) {
        return FieldAndValueAndParameters.withFieldName(mapping.mapColumn(SqlColumn::name))
                .withValuePhrase("'" + mapping.constant() + "'") //$NON-NLS-1$ //$NON-NLS-2$
                .buildOptional();
    }
    
    @Override
    public <R> Optional<FieldAndValueAndParameters> visit(ValueMapping<R> mapping) {
        String mapKey = RenderingStrategy.formatParameterMapKey(sequence);

        String jdbcPlaceholder = mapping.mapColumn(toJdbcPlaceholder(mapKey));
        
        return FieldAndValueAndParameters.withFieldName(mapping.mapColumn(SqlColumn::name))
                .withValuePhrase(jdbcPlaceholder)
                .withParameter(mapKey, mapping.value())
                .buildOptional();
    }

    @Override
    public <R> Optional<FieldAndValueAndParameters> visit(ValueWhenPresentMapping<R> mapping) {
        return mapping.value().flatMap(v -> {
            String mapKey = RenderingStrategy.formatParameterMapKey(sequence);

            String jdbcPlaceholder = mapping.mapColumn(toJdbcPlaceholder(mapKey));
            
            return FieldAndValueAndParameters.withFieldName(mapping.mapColumn(SqlColumn::name))
                    .withValuePhrase(jdbcPlaceholder)
                    .withParameter(mapKey, v)
                    .buildOptional();
        });
    }

    private Function<SqlColumn<?>, String> toJdbcPlaceholder(String parameterName) {
        return column -> column.renderingStrategy().orElse(renderingStrategy)
                .getFormattedJdbcPlaceholder(column, RenderingStrategy.DEFAULT_PARAMETER_PREFIX, parameterName);
    }
}
