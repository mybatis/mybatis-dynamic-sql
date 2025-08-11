/*
 *    Copyright 2016-2025 the original author or authors.
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

import java.sql.JDBCType;
import java.util.Date;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

public final class PersonDynamicSqlSupport {
    public static final Person person = new Person();
    public static final SqlColumn<Integer> id = person.id;
    public static final SqlColumn<String> firstName = person.firstName;
    public static final SqlColumn<LastName> lastName = person.lastName;
    public static final SqlColumn<Date> birthDate = person.birthDate;
    public static final SqlColumn<Boolean> employed = person.employed;
    public static final SqlColumn<String> occupation = person.occupation;
    public static final SqlColumn<Integer> addressId = person.addressId;

    public static final class Person extends SqlTable {
        public final SqlColumn<Integer> id = column("id", JDBCType.INTEGER).withJavaProperty("id");
        public final SqlColumn<String> firstName = column("first_name", JDBCType.VARCHAR)
                .withJavaProperty("firstName");
        public final SqlColumn<LastName> lastName =
                column("last_name", JDBCType.VARCHAR, "examples.simple.LastNameTypeHandler")
                        .withJavaProperty("lastName");
        public final SqlColumn<Date> birthDate = column("birth_date", JDBCType.DATE)
                .withJavaProperty("birthDate");
        public final SqlColumn<Boolean> employed =
                column("employed", JDBCType.VARCHAR, "examples.simple.YesNoTypeHandler")
                        .withJavaProperty("employed");
        public final SqlColumn<String> occupation = column("occupation", JDBCType.VARCHAR)
                .withJavaProperty("occupation");
        public final SqlColumn<Integer> addressId = column("address_id", JDBCType.INTEGER)
                .withJavaProperty("addressId");

        public Person() {
            super("Person");
        }
    }
}
