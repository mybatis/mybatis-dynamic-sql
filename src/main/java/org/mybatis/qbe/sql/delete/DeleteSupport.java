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

public class DeleteSupport {

    private String whereClause;
    private Map<String, Object> parameters;
    
    private DeleteSupport(String whereClause, Map<String, Object> parameters) {
        this.whereClause = whereClause;
        this.parameters = parameters;
    }

    public String getWhereClause() {
        return whereClause;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    public static DeleteSupport of(String whereClause, Map<String, Object> parameters) {
        return new DeleteSupport(whereClause, parameters);
    }
}
