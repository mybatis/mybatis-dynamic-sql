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
package org.mybatis.dynamic.sql.util;

import org.mybatis.dynamic.sql.SqlColumn;

/**
 * This class represents a mapping between a column and a string constant. The constant should be rendered surrounded by
 * single quotes for SQL.
 *
 * @author Jeff Butler
 */
public class StringConstantMapping extends AbstractColumnMapping {
    private final String constant;

    private StringConstantMapping(SqlColumn<?> column, String constant) {
        super(column);
        this.constant = constant;
    }

    public String constant() {
        return constant;
    }

    public static StringConstantMapping of(SqlColumn<?> column, String constant) {
        return new StringConstantMapping(column, constant);
    }

    @Override
    public <R> R accept(ColumnMappingVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
