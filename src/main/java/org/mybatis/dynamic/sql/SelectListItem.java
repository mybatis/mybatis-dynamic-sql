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
package org.mybatis.dynamic.sql;

import java.util.Optional;

import org.mybatis.dynamic.sql.select.render.AliasMap;

public interface SelectListItem {

    Optional<String> alias();

    /**
     * Returns the name of the item aliased with a table name if appropriate.
     * For example, "a.foo".  This is appropriate for where clauses and order by clauses.
     * 
     * @param aliasMap the map of tables to aliases for the current statement
     * @return the item name with the table alias applied
     */
    String applyTableAliasToName(AliasMap aliasMap);
    
    /**
     * Returns the name of the item aliased with a table name and column alias if appropriate.
     * For example, "a.foo as bar".  This is appropriate for select list clauses.
     * 
     * @param aliasMap the map of tables to aliases for the current statement
     * @return the item name with the table and column aliases applied
     */
    default String applyTableAndColumnAliasToName(AliasMap aliasMap) {
        String nameAndTableAlias = applyTableAliasToName(aliasMap);
        
        return alias().map(a -> nameAndTableAlias + " as " + a) //$NON-NLS-1$
                .orElse(nameAndTableAlias);
    }
}
