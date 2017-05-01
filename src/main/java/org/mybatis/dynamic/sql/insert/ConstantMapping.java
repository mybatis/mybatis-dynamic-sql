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
package org.mybatis.dynamic.sql.insert;

import org.mybatis.dynamic.sql.SqlColumn;

public class ConstantMapping extends AbstractColumnMapping {
    private String constant;
    
    private ConstantMapping(SqlColumn<?> column) {
        super(column);
    }

    @Override
    public <S> S accept(ColumnMappingVisitor<S> visitor) {
        return visitor.visit(this);
    }
    
    public String constant() {
        return constant;
    }
    
    public static ConstantMapping of(SqlColumn<?> column, String constant) {
        ConstantMapping mapping = new ConstantMapping(column);
        mapping.constant = constant;
        return mapping;
    }
}
