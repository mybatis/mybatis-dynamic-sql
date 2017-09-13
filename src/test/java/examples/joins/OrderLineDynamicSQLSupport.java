package examples.joins;

import java.sql.JDBCType;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

public class OrderLineDynamicSQLSupport {
    public static final OrderLine orderLine = new OrderLine();
    public static final SqlColumn<Integer> orderId = orderLine.orderId;
    public static final SqlColumn<Integer> itemId = orderLine.itemId;
    public static final SqlColumn<Integer> quantity = orderLine.quantity;
    
    public static class OrderLine extends SqlTable {
        public OrderLine() {
            super("OrderLine");
        }
        
        public final SqlColumn<Integer> orderId = column("order_id", JDBCType.INTEGER);
        public final SqlColumn<Integer> itemId = column("item_id", JDBCType.INTEGER);
        public final SqlColumn<Integer> quantity = column("quantity", JDBCType.INTEGER);
    }
}
