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

import java.util.function.Function;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.util.PropertyMapping;

public class MultiRowValuePhraseVisitor extends ValuePhraseVisitor {

    public MultiRowValuePhraseVisitor(RenderingStrategy renderingStrategy) {
        super(renderingStrategy);
    }

    @Override
    public FieldAndValue visit(PropertyMapping mapping) {
        return FieldAndValue.withFieldName(mapping.mapColumn(SqlColumn::name))
                .withValuePhrase(mapping.mapColumn(toMultiRowJdbcPlaceholder(mapping.property())))
                .build();
    }

    private Function<SqlColumn<?>, String> toMultiRowJdbcPlaceholder(String parameterName) {
        return column -> renderingStrategy.getFormattedJdbcPlaceholder(column, "records[%s]", //$NON-NLS-1$
                parameterName);
    }
}
