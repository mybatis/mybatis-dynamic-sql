package examples.joins;

import java.sql.JDBCType;
import java.util.Date;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

public class ItemMasterDynamicSQLSupport {
    public static final ItemMaster itemMaster = new ItemMaster();
    public static final SqlColumn<Integer> itemId = itemMaster.itemId;
    public static final SqlColumn<Date> description = itemMaster.description;
    
    public static class ItemMaster extends SqlTable {
        public ItemMaster() {
            super("ItemMaster");
        }
        
        public final SqlColumn<Integer> itemId = column("item_id", JDBCType.INTEGER);
        public final SqlColumn<Date> description = column("description", JDBCType.DATE);
    }
}
