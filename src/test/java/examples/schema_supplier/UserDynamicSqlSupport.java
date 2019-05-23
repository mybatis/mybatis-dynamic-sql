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
package examples.schema_supplier;

import java.sql.JDBCType;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

public class UserDynamicSqlSupport {
    public static final User user = new User();
    public static final SqlColumn<Integer> id = user.id;
    public static final SqlColumn<String> name = user.name;

    public static final class User extends SqlTable {
        public final SqlColumn<Integer> id = column("user_id", JDBCType.INTEGER);
        public final SqlColumn<String> name = column("user_name", JDBCType.VARCHAR);

        public User() {
            super(SchemaSupplier::schemaPropertyReader, "User");
        }
    }
}
