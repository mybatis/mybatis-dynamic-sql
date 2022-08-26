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

import org.apache.ibatis.annotations.SelectProvider;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;

/**
 * This is a general purpose MyBatis mapper for count statements. Count statements are select statements that always
 * return a long.
 *
 * <p>This mapper can be injected as-is into a MyBatis configuration, or it can be extended with existing mappers.
 *
 * @author Jeff Butler
 */
public interface CommonCountMapper {
    /**
     * Execute a select statement that returns a long (typically a select(count(*)) statement). This mapper
     * assumes the statement returns a single row with a single column that cen be retrieved as a long.
     *
     * @param selectStatement the select statement
     * @return the long value
     */
    @SelectProvider(type = SqlProviderAdapter.class, method = "select")
    long count(SelectStatementProvider selectStatement);
}
