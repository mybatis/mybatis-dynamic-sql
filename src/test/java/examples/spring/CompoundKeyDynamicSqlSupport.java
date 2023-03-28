package examples.spring;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

public class CompoundKeyDynamicSqlSupport {
    public static final CompoundKey compoundKey = new CompoundKey();
    public static final SqlColumn<Integer> id1 = compoundKey.id1;
    public static final SqlColumn<Integer> id2 = compoundKey.id2;

    public static final class CompoundKey extends SqlTable {
        public final SqlColumn<Integer> id1 = column("id1");
        public final SqlColumn<Integer> id2 = column("id2");

        public CompoundKey() {
            super("CompoundKey");
        }
    }
}
