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
package org.mybatis.dynamic.sql.exception;

import org.mybatis.dynamic.sql.configuration.GlobalConfiguration;
import org.mybatis.dynamic.sql.configuration.StatementConfiguration;
import org.mybatis.dynamic.sql.util.Messages;

/**
 * This exception is thrown when the where clause in a statement will not render.
 * This can happen if all the optional conditions in a where clause fail to
 * render - for example, if an "in" condition specifies an empty list.
 *
 * <p>By default, the framework will throw this exception if a where clause
 * fails to render. A where clause that fails to render can be very dangerous in that
 * it could cause all rows in a table to be affected by a statement - for example,
 * all rows could be deleted.
 *
 * <p>If you intend to allow a where clause to not render, then configure the
 * statement to allow it, or change the global configuration.
 *
 * @see GlobalConfiguration
 * @see StatementConfiguration
 *
 * @since 1.4.1
 *
 * @author Jeff Butler
 */
public class NonRenderingWhereClauseException extends DynamicSqlException {
    private static final long serialVersionUID = 6619119078542625135L;

    public NonRenderingWhereClauseException() {
        super(Messages.getString("ERROR.2")); //$NON-NLS-1$
    }
}
