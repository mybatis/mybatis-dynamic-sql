/*
 *    Copyright 2016-2025 the original author or authors.
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

import java.sql.JDBCType;
import java.util.Optional;

import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;

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
    FragmentAndParameters render(RenderingContext renderingContext);

    default Optional<JDBCType> jdbcType() {
        return Optional.empty();
    }

    default Optional<String> typeHandler() {
        return Optional.empty();
    }

    default Optional<RenderingStrategy> renderingStrategy() {
        return Optional.empty();
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
