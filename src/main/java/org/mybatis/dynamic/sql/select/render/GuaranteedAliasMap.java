/**
 *    Copyright 2016-2017 the original author or authors.
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
package org.mybatis.dynamic.sql.select.render;

import java.util.HashMap;
import java.util.Map;

import org.mybatis.dynamic.sql.SqlTable;

/**
 * This specialized HashMap will return the user specified alias for a table if there
 * is one, else it will return the table name.  This is used when there is is a select
 * with a join.  In that case we always alias the columns and tables regardless
 * of whether the caller specified and alias or not.  If they did not specify an alias,
 * then we will use the table name itself as the alias.
 * 
 * @author Jeff Butler
 *
 */
public class GuaranteedAliasMap extends HashMap<SqlTable, String> {
    
    private static final long serialVersionUID = -2185625759618131743L;

    public GuaranteedAliasMap(Map<SqlTable, String> entries) {
        putAll(entries);
    }

    @Override
    public String get(Object key) {
        SqlTable table = (SqlTable) key;
        return getOrDefault(table, table.name());
    }
}
