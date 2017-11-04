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

public interface SelectListItem {

    Optional<String> alias();

    Optional<SqlTable> table();

    /**
     * Returns the name of the item aliased with a table name if appropriate.
     * For example, "a.foo".  This is appropriate for where clauses and order by clauses.
     * 
     * @param tableAlias an optional table alias
     * @return the item name with the table alias applied
     */
    String applyTableAliasToName(Optional<String> tableAlias);
    
    /**
     * Returns the name of the item aliased with a table name and column alias if appropriate.
     * For example, "a.foo as bar".  This is appropriate for select list clauses.
     * 
     * @param tableAlias an optional table alias
     * @return the item name with the table and column aliases applied
     */
    default String applyTableAndColumnAliasToName(Optional<String> tableAlias) {
        String nameAndTableAlias = applyTableAliasToName(tableAlias);
        
        return alias().map(a -> nameAndTableAlias + " as " + a) //$NON-NLS-1$
                .orElse(nameAndTableAlias);
    }
}
