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
package org.mybatis.dynamic.sql.insert.render;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.util.ConstantMapping;
import org.mybatis.dynamic.sql.util.InsertMappingVisitor;
import org.mybatis.dynamic.sql.util.NullMapping;
import org.mybatis.dynamic.sql.util.PropertyMapping;
import org.mybatis.dynamic.sql.util.StringConstantMapping;

public class MultiRowInsertValuePhraseVisitor implements InsertMappingVisitor<FieldAndMultipleValues> {
    
    private RenderingStrategy renderingStrategy;
    private int rowCount;
    
    public MultiRowInsertValuePhraseVisitor(RenderingStrategy renderingStrategy, int rowCount) {
        this.renderingStrategy = renderingStrategy;
        this.rowCount = rowCount;
    }

    @Override
    public FieldAndMultipleValues visit(NullMapping mapping) {
        return FieldAndMultipleValues.withFieldName(mapping.mapColumn(SqlColumn::name))
                .withValuePhrases(listOfRepeatedStrings("null")) //$NON-NLS-1$
                .build();
    }
    
    @Override
    public FieldAndMultipleValues visit(ConstantMapping mapping) {
        return FieldAndMultipleValues.withFieldName(mapping.mapColumn(SqlColumn::name))
                .withValuePhrases(listOfRepeatedStrings(mapping.constant()))
                .build();
    }

    @Override
    public FieldAndMultipleValues visit(StringConstantMapping mapping) {
        return FieldAndMultipleValues.withFieldName(mapping.mapColumn(SqlColumn::name))
                .withValuePhrases(listOfRepeatedStrings("'" + mapping.constant() + "'")) //$NON-NLS-1$ //$NON-NLS-2$
                .build();
    }
    
    @Override
    public FieldAndMultipleValues visit(PropertyMapping mapping) {
        return FieldAndMultipleValues.withFieldName(mapping.mapColumn(SqlColumn::name))
                .withValuePhrases(listOfRepeatedStrings(mapping))
                .build();
    }
    
    private List<String> listOfRepeatedStrings(String s) {
        return IntStream.range(0, rowCount)
                .mapToObj(i -> s)
                .collect(Collectors.toList());
    }

    private List<String> listOfRepeatedStrings(PropertyMapping mapping) {
        return IntStream.range(0, rowCount)
                .mapToObj(i -> mapping.mapColumn(toJdbcPlaceholder(mapping.property(), i)))
                .collect(Collectors.toList());
    }
    
    private Function<SqlColumn<?>, String> toJdbcPlaceholder(String parameterName, int i) {
        return column -> renderingStrategy.getFormattedJdbcPlaceholder(
                column, "records[" + i + "]", parameterName); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
