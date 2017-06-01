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

/**
 * This class represents a mapping between a column and a constant.  The constant should be rendered
 * exactly as specified here.
 * 
 * @author Jeff Butler
 *
 */
public class ConstantMapping extends AbstractColumnMapping implements InsertMapping, UpdateMapping {
    private String constant;

    private ConstantMapping(SqlColumn<?> column) {
        super(column);
    }

    public String constant() {
        return constant;
    }

    public static ConstantMapping of(SqlColumn<?> column, String constant) {
        ConstantMapping mapping = new ConstantMapping(column);
        mapping.constant = constant;
        return mapping;
    }

    @Override
    public <R> R accept(UpdateMappingVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public <R> R accept(InsertMappingVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
