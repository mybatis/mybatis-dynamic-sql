package examples.joins;

import java.sql.JDBCType;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

public class OrderDetailDynamicSQLSupport {
    public static final OrderDetail orderDetail = new OrderDetail();
    public static final SqlColumn<Integer> orderId = orderDetail.orderId;
    public static final SqlColumn<Integer> lineNumber = orderDetail.lineNumber;
    public static final SqlColumn<String> description = orderDetail.description;
    public static final SqlColumn<Integer> quantity = orderDetail.quantity;
    
    public static class OrderDetail extends SqlTable {
        public OrderDetail() {
            super("OrderDetail");
        }

        public final SqlColumn<Integer> orderId = column("order_id", JDBCType.INTEGER);
        public final SqlColumn<Integer> lineNumber = column("line_number", JDBCType.INTEGER);
        public final SqlColumn<String> description = column("description", JDBCType.VARCHAR);
        public final SqlColumn<Integer> quantity = column("quantity", JDBCType.INTEGER);
    }
}
