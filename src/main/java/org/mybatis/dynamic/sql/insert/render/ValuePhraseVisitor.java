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

import org.mybatis.dynamic.sql.insert.ColumnMappingVisitor;
import org.mybatis.dynamic.sql.insert.ConstantMapping;
import org.mybatis.dynamic.sql.insert.NullMapping;
import org.mybatis.dynamic.sql.insert.PropertyMapping;

public class ValuePhraseVisitor implements ColumnMappingVisitor<String> {

    @Override
    public String visit(NullMapping mapping) {
        return "null"; //$NON-NLS-1$
    }

    @Override
    public String visit(ConstantMapping mapping) {
        return mapping.constant();
    }

    @Override
    public String visit(PropertyMapping mapping) {
        return mapping.getFormattedJdbcPlaceholder("record"); //$NON-NLS-1$
    }
}
