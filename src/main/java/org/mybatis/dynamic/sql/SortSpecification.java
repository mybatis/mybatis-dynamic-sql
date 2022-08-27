/*
 *    Copyright 2016-2022 the original author or authors.
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
     * Return the phrase that should be written into a rendered order by clause. This should
     * NOT include the "DESC" word for descending sort specifications.
     *
     * @return the order by phrase
     */
    String orderByName();

    /**
     * Return true if the sort order is descending.
     *
     * @return true if the SortSpecification should render as descending
     */
    boolean isDescending();
}
