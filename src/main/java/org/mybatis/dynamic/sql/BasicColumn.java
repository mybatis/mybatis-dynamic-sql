/**
 *    Copyright 2016-2019 the original author or authors.
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

import org.mybatis.dynamic.sql.render.TableAliasCalculator;

/**
 * Describes attributes of columns that are necessary for rendering if the column is not expected to
 * be bound as a JDBC parameter.  Columns in select lists, join expressions, and group by expressions
 * are typically not bound.
 * 
 * @author Jeff Butler
 *
 */
public interface BasicColumn {

    /**
     * Returns the columns alias if one has been specified.
     * 
     * @return the column alias
     */
    Optional<String> alias();
    
    /**
     * Returns a new instance of a BasicColumn with the alias set.
     * 
     * @param alias the column alias to set
     * @return new instance with alias set
     */
    BasicColumn as(String alias);
    
    /**
     * Returns the name of the item aliased with a table name if appropriate.
     * For example, "a.foo".  This is appropriate for where clauses and order by clauses.
     * 
     * @param tableAliasCalculator the table alias calculator for the current renderer
     * @return the item name with the table alias applied
     */
    String renderWithTableAlias(TableAliasCalculator tableAliasCalculator);
    
    /**
     * Returns the name of the item aliased with a table name and column alias if appropriate.
     * For example, "a.foo as bar".  This is appropriate for select list clauses.
     * 
     * @param tableAliasCalculator the table alias calculator for the current renderer
     * @return the item name with the table and column aliases applied
     */
    default String renderWithTableAndColumnAlias(TableAliasCalculator tableAliasCalculator) {
        String nameAndTableAlias = renderWithTableAlias(tableAliasCalculator);
        
        return alias().map(a -> nameAndTableAlias + " as " + a) //$NON-NLS-1$
                .orElse(nameAndTableAlias);
    }
    
    /**
     * Utility method to make it easier to build column lists for methods that require an
     * array rather than the varargs method.
     * 
     * @param columns list of BasicColumn
     * @return an array of BasicColumn
     */
    static BasicColumn[] columnList(BasicColumn...columns) {
        return columns;
    }
}
