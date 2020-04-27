package examples.custom_render;

import java.sql.JDBCType;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

public class JsonTestDynamicSqlSupport {
    public static JsonTest jsonTest = new JsonTest();
    public static SqlColumn<Integer> id = jsonTest.column("id", JDBCType.INTEGER);
    public static SqlColumn<String> description = jsonTest.column("description", JDBCType.VARCHAR);
    public static SqlColumn<String> info = jsonTest.column("info", JDBCType.VARCHAR)
            .withRenderingStrategy(new JsonRenderingStrategy()); 

    public static class JsonTest extends SqlTable {
        public JsonTest() {
            super("JsonTest");
        }
    }
}