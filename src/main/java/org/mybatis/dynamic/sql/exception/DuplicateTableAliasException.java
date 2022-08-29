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

import java.util.Objects;

import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.util.Messages;

/**
 * This exception is thrown when a query is built that attempts to specify more than one
 * alias for the same instance of an SqlTable object. That error that would produce a select
 * statement that doesn't work.
 *
 * <p>This error usually occurs when building a self-join query. The workaround is to create
 * a second instance of the SqlTable object to use in the self-join.
 *
 * @since 1.3.1
 *
 * @author Jeff Butler
 */
public class DuplicateTableAliasException extends DynamicSqlException {

    private static final long serialVersionUID = -2631664872557787391L;

    public DuplicateTableAliasException(SqlTable table, String newAlias, String existingAlias) {
        super(generateMessage(Objects.requireNonNull(table),
                Objects.requireNonNull(newAlias),
                Objects.requireNonNull(existingAlias)));
    }

    private static String generateMessage(SqlTable table, String newAlias, String existingAlias) {
        return Messages.getString("ERROR.1", table.tableNameAtRuntime(), newAlias, existingAlias); //$NON-NLS-1$
    }
}
