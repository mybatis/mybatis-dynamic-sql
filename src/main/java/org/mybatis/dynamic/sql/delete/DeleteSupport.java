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
package org.mybatis.dynamic.sql.delete;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.mybatis.dynamic.sql.AbstractSqlSupport;
import org.mybatis.dynamic.sql.SqlTable;

public class DeleteSupport extends AbstractSqlSupport {

    private String whereClause;
    private Map<String, Object> parameters = new HashMap<>();
    
    private DeleteSupport(SqlTable table) {
        super(table);
    }
    
    private DeleteSupport(String whereClause, Map<String, Object> parameters, SqlTable table) {
        super(table);
        this.whereClause = whereClause;
        this.parameters.putAll(parameters);
    }

    public String getWhereClause() {
        return whereClause().orElse(EMPTY_STRING);
    }

    public Optional<String> whereClause() {
        return Optional.ofNullable(whereClause);
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    public String getFullDeleteStatement() {
        return "delete from " //$NON-NLS-1$
                + tableName()
                + whereClause().map(w -> ONE_SPACE + w).orElse(EMPTY_STRING);
    }

    public static DeleteSupport of(SqlTable table) {
        return new DeleteSupport(table);
    }
    
    public static DeleteSupport of(String whereClause, Map<String, Object> parameters, SqlTable table) {
        return new DeleteSupport(whereClause, parameters, table);
    }
}