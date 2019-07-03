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
package issues.gh105;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

public final class PersonDynamicSqlSupport {
    public static final Person person = new Person();
    public static final SqlColumn<Integer> id = person.id;
    public static final SqlColumn<String> firstName = person.firstName;
    public static final SqlColumn<String> lastName = person.lastName;
    public static final SqlColumn<Integer> age = person.age;

    public static final class Person extends SqlTable {
        public final SqlColumn<Integer> id = column("person_id");
        public final SqlColumn<String> firstName = column("first_name");
        public final SqlColumn<String> lastName = column("last_name");
        public final SqlColumn<Integer> age = column("age");

        public Person() {
            super("Person");
        }
    }
}
