/*
 *    Copyright 2016-2024 the original author or authors.
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
package org.mybatis.dynamic.sql;

import java.util.Optional;

import org.mybatis.dynamic.sql.exception.DynamicSqlException;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.render.TableAliasCalculator;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.util.Messages;

/**
 * Describes attributes of columns that are necessary for rendering if the column is not expected to
 * be bound as a JDBC parameter.  Columns in select lists, join expressions, and group by expressions
 * are typically not bound.
 *
 * @author Jeff Butler
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
     * @param alias
     *            the column alias to set
     *
     * @return new instance with alias set
     */
    BasicColumn as(String alias);

    /**
     * Returns a rendering of the column.
     * The rendered fragment should include the table alias based on the TableAliasCalculator
     * in the RenderingContext. The fragment could contain prepared statement parameter
     * markers and associated parameter values if desired.
     *
     * @param renderingContext the rendering context (strategy, sequence, etc.)
     * @return a rendered SQL fragment and, optionally, parameters associated with the fragment
     * @since 1.5.1
     */
    default FragmentAndParameters render(RenderingContext renderingContext) {
        // the default implementation ensures compatibility with prior releases. When the
        // deprecated renderWithTableAlias method is removed, this function can become purely abstract.
        // Also remove the method tableAliasCalculator() from RenderingContext.
        return FragmentAndParameters.fromFragment(renderWithTableAlias(renderingContext.tableAliasCalculator()));
    }

    /**
     * Returns the name of the item aliased with a table name if appropriate.
     * For example, "a.foo".  This is appropriate for where clauses and order by clauses.
     *
     * @param tableAliasCalculator the table alias calculator for the current renderer
     * @return the item name with the table alias applied
     * @deprecated Please replace this method by overriding the more general "render" method
     */
    @Deprecated
    default String renderWithTableAlias(TableAliasCalculator tableAliasCalculator) {
        throw new DynamicSqlException(Messages.getString("ERROR.36"));  //$NON-NLS-1$
    }

    /**
     * Utility method to make it easier to build column lists for methods that require an
     * array rather than the varargs method.
     *
     * @param columns list of BasicColumn
     * @return an array of BasicColumn
     */
    static BasicColumn[] columnList(BasicColumn... columns) {
        return columns;
    }
}
