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

import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;

@FunctionalInterface
public interface RenderableCondition<T> {
    /**
     * Render a condition - typically a condition in a WHERE clause.
     *
     * <p>A rendered condition includes an SQL fragment, and any associated parameters. For example,
     * the <code>isEqual</code> condition should be rendered as "= ?" where "?" is a properly formatted
     * parameter marker (the parameter marker can be computed from the <code>RenderingContext</code>).
     * Note that a rendered condition should NOT include the left side of the phrase - that is rendered
     * by the {@link RenderableCondition#renderLeftColumn(RenderingContext, BindableColumn)} method.
     *
     * @param renderingContext the current rendering context
     * @param leftColumn the column related to this condition in a where clause
     * @return the rendered condition. Should NOT include the column.
     */
    FragmentAndParameters renderCondition(RenderingContext renderingContext, BindableColumn<T> leftColumn);

    /**
     * Render the column in a column and condition phrase - typically in a WHERE clause.
     *
     * <p>By default, the column will be rendered as the column alias if it exists, or the column name.
     * This can be complicated if the column has a table qualifier, or if the "column" is a function or
     * part of a CASE expression. Columns know how to render themselves, so we just call their "render"
     * methods.
     *
     * @param renderingContext the current rendering context
     * @param leftColumn the column related to this condition in a where clause
     * @return the rendered column
     */
    default FragmentAndParameters renderLeftColumn(RenderingContext renderingContext, BindableColumn<T> leftColumn) {
        return leftColumn.alias()
                .map(FragmentAndParameters::fromFragment)
                .orElseGet(() -> leftColumn.render(renderingContext));
    }

    /**
     * Subclasses can override this to inform the renderer if the condition should not be included
     * in the rendered SQL.  Typically, conditions will not render if they are empty.
     *
     * @return true if the condition should render.
     */
    default boolean shouldRender(RenderingContext renderingContext) {
        return !isEmpty();
    }

    /**
     * Subclasses can override this to indicate whether the condition is considered empty. This is primarily used in
     * map and filter operations - the map and filter functions will not be applied if the condition is empty.
     *
     * @return true if the condition is empty.
     */
    default boolean isEmpty() {
        return false;
    }

    /**
     * This method will be called during rendering when {@link RenderableCondition#shouldRender(RenderingContext)}
     * returns false.
     */
    default void renderingSkipped() {}
}
