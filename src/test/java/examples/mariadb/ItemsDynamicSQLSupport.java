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
package examples.mariadb;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

import java.sql.JDBCType;

public final class ItemsDynamicSQLSupport {
    public static final Items items = new Items();
    public static final SqlColumn<Integer> id = items.id;
    public static final SqlColumn<String> description = items.description;
    public static final SqlColumn<Integer> amount = items.amount;

    public static final class Items extends SqlTable {
        public final SqlColumn<Integer> id = column("id", JDBCType.INTEGER);
        public final SqlColumn<String> description = column("description", JDBCType.VARCHAR);
        public final SqlColumn<Integer> amount = column("amount", JDBCType.INTEGER);

        public Items() {
            super("items");
        }
    }
}
