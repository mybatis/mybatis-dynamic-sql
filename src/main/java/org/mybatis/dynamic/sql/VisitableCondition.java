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
public interface VisitableCondition<T> {
    FragmentAndParameters renderCondition(RenderingContext renderingContext, BindableColumn<T> leftColumn);

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
     * This method will be called during rendering when {@link VisitableCondition#shouldRender(RenderingContext)}
     * returns false.
     */
    default void renderingSkipped() {}
}
