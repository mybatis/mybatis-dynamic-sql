/**
 *    Copyright 2016-2017 the original author or authors.
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
package examples.joins;

import java.sql.JDBCType;
import java.util.Date;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

public final class OrderMasterDynamicSQLSupport {
    public static final OrderMaster orderMaster = new OrderMaster();
    public static final SqlColumn<Integer> orderId = orderMaster.orderId;
    public static final SqlColumn<Date> orderDate = orderMaster.orderDate;
    
    public static final class OrderMaster extends SqlTable {
        public final SqlColumn<Integer> orderId = column("order_id", JDBCType.INTEGER);
        public final SqlColumn<Date> orderDate = column("order_date", JDBCType.DATE);

        public OrderMaster() {
            super("OrderMaster");
        }
    }
}
