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
package org.mybatis.dynamic.sql;

import java.util.Optional;

public class SqlTable {

    private String name;
    private Optional<String> alias = Optional.empty();
    
    private SqlTable(String name) {
        this.name = name;
    }
    
    public String name() {
        return name;
    }
    
    public String nameIncludingAlias() {
        return alias.map(a -> name + " " + a).orElse(name); //$NON-NLS-1$
    }
    
    public Optional<String> alias() {
        return alias;
    }
    
    public SqlTable withAlias(String alias) {
        SqlTable sqlTable = new SqlTable(name);
        sqlTable.alias = Optional.of(alias);
        return sqlTable;
    }

    public static SqlTable of(String name) {
        return new SqlTable(name);
    }
}
