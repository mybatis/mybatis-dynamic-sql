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

public class InsertColumnMapping {
    private SqlColumn<?> column;
    private String valuePhrase;
    
    private InsertColumnMapping() {
        super();
    }
    
    public String columnName() {
        return column.name();
    }
    
    public String valuePhrase() {
        return valuePhrase;
    }
    
    public static InsertColumnMapping ofConstantMap(SqlColumn<?> column, String constant) {
        InsertColumnMapping mapping = new InsertColumnMapping();
        mapping.column = column;
        mapping.valuePhrase = constant;
        return mapping;
    }
    
    public static InsertColumnMapping ofPropertyMap(SqlColumn<?> column, String property) {
        InsertColumnMapping mapping = new InsertColumnMapping();
        mapping.column = column;
        mapping.valuePhrase = column.getFormattedJdbcPlaceholder("record", property); //$NON-NLS-1$
        return mapping;
    }
}