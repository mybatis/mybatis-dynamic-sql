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

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

public final class OrderLineDynamicSQLSupport {
    public static final OrderLine orderLine = new OrderLine();
    public static final SqlColumn<Integer> orderId = orderLine.orderId;
    public static final SqlColumn<Integer> itemId = orderLine.itemId;
    public static final SqlColumn<Integer> lineNumber = orderLine.lineNumber;
    public static final SqlColumn<Integer> quantity = orderLine.quantity;
    
    public static final class OrderLine extends SqlTable {
        public final SqlColumn<Integer> orderId = column("order_id", JDBCType.INTEGER);
        public final SqlColumn<Integer> itemId = column("item_id", JDBCType.INTEGER);
        public final SqlColumn<Integer> lineNumber = column("line_number", JDBCType.INTEGER);
        public final SqlColumn<Integer> quantity = column("quantity", JDBCType.INTEGER);

        public OrderLine() {
            super("OrderLine");
        }
    }
}
