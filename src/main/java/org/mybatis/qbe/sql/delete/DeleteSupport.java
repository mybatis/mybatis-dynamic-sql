/**
 *    Copyright 2016 the original author or authors.
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
package org.mybatis.qbe.sql.delete;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mybatis.qbe.sql.AbstractSqlSupport;
import org.mybatis.qbe.sql.SqlTable;

public class DeleteSupport extends AbstractSqlSupport {

    private String whereClause;
    private Map<String, Object> parameters;
    
    private DeleteSupport(String whereClause, Map<String, Object> parameters, SqlTable table) {
        super(table);
        this.whereClause = whereClause;
        this.parameters = parameters;
    }

    public String getWhereClause() {
        return whereClause;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    public String getFullDeleteStatement() {
        return Stream.of("delete from", //$NON-NLS-1$
                table().orElse(UNKNOWN_TABLE).name(),
                getWhereClause()).collect(Collectors.joining(" ")); //$NON-NLS-1$
    }

    public static DeleteSupport of(String whereClause, Map<String, Object> parameters, SqlTable table) {
        return new DeleteSupport(whereClause, parameters, table);
    }
}