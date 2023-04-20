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
package examples.simple;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

import java.sql.JDBCType;

public class CompoundKeyDynamicSqlSupport {
    public static final CompoundKey compoundKey = new CompoundKey();
    public static final SqlColumn<Integer> id1 = compoundKey.id1;
    public static final SqlColumn<Integer> id2 = compoundKey.id2;

    public static final class CompoundKey extends SqlTable {
        public final SqlColumn<Integer> id1 = column("id1", JDBCType.INTEGER);
        public final SqlColumn<Integer> id2 = column("id2", JDBCType.INTEGER);

        public CompoundKey() {
            super("CompoundKey");
        }
    }
}
