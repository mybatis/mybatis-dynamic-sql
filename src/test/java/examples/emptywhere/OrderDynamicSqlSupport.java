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

import java.util.Date;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

public class OrderDynamicSqlSupport {
    
    public static Order order = new Order();
    public static SqlColumn<Integer> personId = order.personId;
    public static SqlColumn<Date> orderDate = order.orderDate;

    public static class Order extends SqlTable {
        public SqlColumn<Integer> personId = column("person_id");
        public SqlColumn<Date> orderDate = column("order_date");
        
        public Order() {
            super("order");
        }
    }
}
