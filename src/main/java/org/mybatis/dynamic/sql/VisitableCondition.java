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

import org.mybatis.dynamic.sql.render.RenderingContext;

@FunctionalInterface
public interface VisitableCondition<T> {
    <R> R accept(ConditionVisitor<T, R> visitor);

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

    /**
     * This method is called during rendering. Its purpose is to allow conditions to change
     * the value of the rendered left column. This is primarily used in the case-insensitive conditions
     * where we surround the rendered column with "upper(" and ")".
     *
     * @param renderedLeftColumn the rendered left column
     * @return the altered column - by default no change is applied
     */
    default String overrideRenderedLeftColumn(String renderedLeftColumn) {
        return renderedLeftColumn;
    }
}
