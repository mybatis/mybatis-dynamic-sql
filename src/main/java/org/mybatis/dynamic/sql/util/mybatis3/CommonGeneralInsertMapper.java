/*
 *    Copyright 2016-2023 the original author or authors.
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
package org.mybatis.dynamic.sql.util.mybatis3;

import org.apache.ibatis.annotations.InsertProvider;
import org.mybatis.dynamic.sql.insert.render.GeneralInsertStatementProvider;
import org.mybatis.dynamic.sql.insert.render.InsertSelectStatementProvider;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;

/**
 * This is a general purpose mapper for executing various non-typed insert statements (general inserts and insert
 * selects). This mapper is appropriate for insert statements that do NOT expect generated keys.
 */
public interface CommonGeneralInsertMapper {
    /**
     * Execute an insert statement with input fields supplied directly.
     *
     * @param insertStatement
     *            the insert statement
     *
     * @return the number of rows affected
     */
    @InsertProvider(type = SqlProviderAdapter.class, method = "generalInsert")
    int generalInsert(GeneralInsertStatementProvider insertStatement);

    /**
     * Execute an insert statement with input fields supplied by a select statement.
     *
     * @param insertSelectStatement
     *            the insert statement
     *
     * @return the number of rows affected
     */
    @InsertProvider(type = SqlProviderAdapter.class, method = "insertSelect")
    int insertSelect(InsertSelectStatementProvider insertSelectStatement);
}
