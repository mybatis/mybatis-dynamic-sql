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
package org.mybatis.dynamic.sql.util.mybatis3;

import org.apache.ibatis.annotations.UpdateProvider;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;

/**
 * This is a general purpose MyBatis mapper for update statements.
 *
 * <p>This mapper can be injected as-is into a MyBatis configuration, or it can be extended with existing mappers.
 *
 * @author Jeff Butler
 */
public interface CommonUpdateMapper {
    /**
     * Execute an update statement.
     *
     * @param updateStatement
     *            the update statement
     *
     * @return the number of rows affected
     */
    @UpdateProvider(type = SqlProviderAdapter.class, method = "update")
    int update(UpdateStatementProvider updateStatement);
}
