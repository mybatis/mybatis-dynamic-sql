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
package examples.joins;

import java.sql.JDBCType;

import org.mybatis.dynamic.sql.AliasableSqlTable;
import org.mybatis.dynamic.sql.SqlColumn;

public final class ItemMasterDynamicSQLSupport {
    public static final ItemMaster itemMaster = new ItemMaster();
    public static final SqlColumn<Integer> itemId = itemMaster.itemId;
    public static final SqlColumn<String> description = itemMaster.description;

    public static final class ItemMaster extends AliasableSqlTable<ItemMaster> {
        public final SqlColumn<Integer> itemId = column("item_id", JDBCType.INTEGER);
        public final SqlColumn<String> description = column("description", JDBCType.VARCHAR);

        public ItemMaster() {
            super("ItemMaster", ItemMaster::new);
        }
    }
}
