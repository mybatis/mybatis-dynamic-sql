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
package org.mybatis.dynamic.sql.util;

import org.mybatis.dynamic.sql.SqlColumn;

public class PropertyMapping extends AbstractColumnMapping implements InsertMapping {
    private String property;
    
    private PropertyMapping(SqlColumn<?> column) {
        super(column);
    }
    
    public String property() {
        return property;
    }

    @Override
    public <S> S accept(InsertMappingVisitor<S> visitor) {
        return visitor.visit(this);
    }
    
    public static PropertyMapping of(SqlColumn<?> column, String property) {
        PropertyMapping mapping = new PropertyMapping(column);
        mapping.property = property;
        return mapping;
    }
}
