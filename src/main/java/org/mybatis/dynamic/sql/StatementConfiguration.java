/*
 *    Copyright 2016-2022 the original author or authors.
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

/**
 * This class can be used to change some behaviors of the framework. Every configurable statement
 * contains a unique instance of this class, so changes here will only impact a single statement.
 * If you intend to change the behavior for all statements, use the {@link GlobalConfiguration}
 * configuration class.
 *
 * <dl>
 *     <dt>unrenderableWhereClauseAllowed</dt>
 *     <dd>If false (default), the framework will throw an
 *         {@link org.mybatis.dynamic.sql.exception.UnrenderableWhereClauseException}
 *         if a where clause is specified in the statement, but it fails to render because all
 *         optional conditions do not render. For example, if an "in" condition specifies an
 *         empty list of values.
 *     </dd>
 * </dl>
 *
 * @see GlobalConfiguration
 * @since 1.4.1
 * @author Jeff Butler
 */
public class StatementConfiguration {
    private boolean unrenderableWhereClauseAllowed = GlobalConfiguration.getUnrenderableWhereClauseAllowed();

    public boolean getUnrenderableWhereClauseAllowed() {
        return unrenderableWhereClauseAllowed;
    }

    public void setUnrenderableWhereClauseAllowed(boolean unrenderableWhereClauseAllowed) {
        this.unrenderableWhereClauseAllowed = unrenderableWhereClauseAllowed;
    }
}
