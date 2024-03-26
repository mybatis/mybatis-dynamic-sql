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
package org.mybatis.dynamic.sql.configuration;

import org.mybatis.dynamic.sql.exception.NonRenderingWhereClauseException;

/**
 * This class can be used to change some behaviors of the framework. Every configurable statement
 * contains a unique instance of this class, so changes here will only impact a single statement.
 * If you intend to change the behavior for all statements, use the {@link GlobalConfiguration}.
 * Initial values for this class in each statement are set from the {@link GlobalConfiguration}.
 * Configurable behaviors are detailed below:
 *
 * <dl>
 *     <dt>emptyListConditionRenderingAllowed</dt>
 *     <dd>If false (default), the framework will not render list conditions that are empty in a where clause.
 *         This is beneficial in that it will not allow the library to generate invalid SQL, but it has a
 *         potentially dangerous side effect where a statement could be generated that impacts more rows
 *         then expected. If true, an empty list will be rendered as "in ()", "not in ()", etc. which will likely
 *         cause an SQLException at runtime.
 *     </dd>
 *     <dt>nonRenderingWhereClauseAllowed</dt>
 *     <dd>If false (default), the framework will throw a {@link NonRenderingWhereClauseException}
 *         if a where clause is specified in the statement, but it fails to render because all
 *         optional conditions do not render. For example, if an "in" condition specifies an
 *         empty list of values. If no criteria are specified in a where clause, the framework
 *         assumes that no where clause was intended and will not throw an exception.
 *     </dd>
 * </dl>
 *
 * @see GlobalConfiguration
 *
 * @since 1.4.1
 *
 * @author Jeff Butler
 */
public class StatementConfiguration {
    private boolean isNonRenderingWhereClauseAllowed =
            GlobalContext.getConfiguration().isIsNonRenderingWhereClauseAllowed();

    private boolean isEmptyListConditionRenderingAllowed =
            GlobalContext.getConfiguration().isEmptyListConditionRenderingAllowed();

    public boolean isNonRenderingWhereClauseAllowed() {
        return isNonRenderingWhereClauseAllowed;
    }

    public StatementConfiguration setNonRenderingWhereClauseAllowed(boolean nonRenderingWhereClauseAllowed) {
        isNonRenderingWhereClauseAllowed = nonRenderingWhereClauseAllowed;
        return this;
    }

    public boolean isEmptyListConditionRenderingAllowed() {
        return isEmptyListConditionRenderingAllowed;
    }

    public StatementConfiguration setEmptyListConditionRenderingAllowed(boolean emptyListConditionRenderingAllowed) {
        isEmptyListConditionRenderingAllowed = emptyListConditionRenderingAllowed;
        return this;
    }
}
