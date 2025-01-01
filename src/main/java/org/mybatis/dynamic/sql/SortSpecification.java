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

/**
 * Defines attributes of columns that are necessary for rendering an order by expression.
 *
 * @author Jeff Butler
 */
public interface SortSpecification {
    /**
     * Returns a new instance of the SortSpecification that should render as descending in an
     * ORDER BY clause.
     *
     * @return new instance of SortSpecification
     */
    SortSpecification descending();

    /**
     * Return a fragment rendered for use in an ORDER BY clause. The fragment should include "DESC" if a
     * descending order is desired.
     *
     * @param renderingContext the current rendering context
     * @return a rendered fragment and  parameters if applicable
     * @since 2.0.0
     */
    FragmentAndParameters renderForOrderBy(RenderingContext renderingContext);
}
