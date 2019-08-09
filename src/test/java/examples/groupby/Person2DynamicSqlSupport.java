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
package examples.groupby;

import java.sql.JDBCType;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

public final class Person2DynamicSqlSupport {
    public static final Person2 person2 = new Person2();
    public static final SqlColumn<Integer> id = person2.id;
    public static final SqlColumn<String> firstName = person2.firstName;
    public static final SqlColumn<String> lastName = person2.lastName;
    public static final SqlColumn<String> gender = person2.gender;
    public static final SqlColumn<String> human = person2.human;
    public static final SqlColumn<Integer> age = person2.age;
    public static final SqlColumn<Integer> addressId = person2.addressId;

    public static final class Person2 extends SqlTable {
        public final SqlColumn<Integer> id = column("person_id", JDBCType.INTEGER);
        public final SqlColumn<String> firstName = column("first_name", JDBCType.VARCHAR);
        public final SqlColumn<String> lastName = column("last_name", JDBCType.VARCHAR);
        public final SqlColumn<String> gender = column("gender", JDBCType.VARCHAR);
        public final SqlColumn<String> human = column("human_flag", JDBCType.VARCHAR);
        public final SqlColumn<Integer> age = column("age", JDBCType.INTEGER);
        public final SqlColumn<Integer> addressId = column("address_id", JDBCType.INTEGER);

        public Person2() {
            super("Person2");
        }
    }
}
