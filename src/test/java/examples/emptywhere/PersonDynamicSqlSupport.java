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
package examples.emptywhere;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

public class PersonDynamicSqlSupport {
    
    public static Person person = new Person();
    public static SqlColumn<Integer> id = person.id;
    public static SqlColumn<String> firstName = person.firstName;
    public static SqlColumn<String> lastName = person.lastName;

    public static class Person extends SqlTable {
        public SqlColumn<Integer> id = column("id");
        public SqlColumn<String> firstName = column("first_name");
        public SqlColumn<String> lastName = column("last_name");
        
        public Person() {
            super("person");
        }
    }
}
