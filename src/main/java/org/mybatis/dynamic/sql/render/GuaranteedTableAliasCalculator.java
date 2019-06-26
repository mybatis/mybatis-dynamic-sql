/**
 *    Copyright 2016-2019 the original author or authors.
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
package org.mybatis.dynamic.sql.render;

import java.util.Map;
import java.util.Optional;

import org.mybatis.dynamic.sql.SqlTable;

/**
 * Returns the alias for a table if specified, or the table name itself.
 * This is useful for join rendering when we always want to have an alias for the table.
 * 
 * @author Jeff Butler
 * 
 */
public class GuaranteedTableAliasCalculator extends TableAliasCalculator {

    private GuaranteedTableAliasCalculator(Map<SqlTable, String> aliases) {
        super(aliases);
    }

    @Override
    public Optional<String> aliasForColumn(SqlTable table) {
        return super.aliasForColumn(table)
                .map(Optional::of)
                .orElseGet(() -> Optional.of(table.tableNameAtRuntime()));
    }
    
    public static TableAliasCalculator of(Map<SqlTable, String> aliases) {
        return new GuaranteedTableAliasCalculator(aliases);
    }
}
