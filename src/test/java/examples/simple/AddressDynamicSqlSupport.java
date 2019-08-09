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
package examples.simple;

import java.sql.JDBCType;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

public final class AddressDynamicSqlSupport {
    public static final Address address = new Address();
    public static final SqlColumn<Integer> id = address.id;
    public static final SqlColumn<String> streetAddress = address.streetAddress;
    public static final SqlColumn<String> city = address.city;
    public static final SqlColumn<String> state = address.state;
    
    public static final class Address extends SqlTable {
        public final SqlColumn<Integer> id = column("address_id", JDBCType.INTEGER);
        public final SqlColumn<String> streetAddress = column("street_address", JDBCType.VARCHAR);
        public final SqlColumn<String> city = column("city", JDBCType.VARCHAR);
        public final SqlColumn<String> state = column("state", JDBCType.VARCHAR);

        public Address() {
            super("Address");
        }
    }
}
