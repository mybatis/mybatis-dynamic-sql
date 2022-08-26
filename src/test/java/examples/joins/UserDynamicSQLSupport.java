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

public class UserDynamicSQLSupport {
    public static final User user = new User();
    public final SqlColumn<Integer> userId = user.userId;
    public final SqlColumn<String> userName = user.userName;
    public final SqlColumn<Integer> parentId = user.parentId;

    public static final class User extends AliasableSqlTable<User> {
        public final SqlColumn<Integer> userId = column("user_id", JDBCType.INTEGER);
        public final SqlColumn<String> userName = column("user_name", JDBCType.VARCHAR);
        public final SqlColumn<Integer> parentId = column("parent_id", JDBCType.INTEGER);

        public User() {
            super(() -> "User", User::new);
        }
    }
}
