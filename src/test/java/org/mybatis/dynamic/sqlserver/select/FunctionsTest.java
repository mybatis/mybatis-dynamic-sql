package org.mybatis.dynamic.sqlserver.select;

import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;
import static org.mybatis.dynamic.sql.SqlBuilder.select;
import static org.mybatis.dynamic.sqlserver.SQLServerBuilder.ascii;

import java.sql.JDBCType;
import java.util.Date;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;

@RunWith(JUnitPlatform.class)
public class FunctionsTest {

	public static final SqlTable table = SqlTable.of("foo");
    public static final SqlColumn<Date> column1 = table.column("column1", JDBCType.DATE);
    public static final SqlColumn<Integer> column2 = table.column("column2", JDBCType.INTEGER);
    public static final SqlColumn<String> column3 = table.column("column3", JDBCType.VARCHAR);
    
    @Test
    public void testAscii() {
        SelectStatementProvider selectStatement = select(ascii(column3).as("A_COLUMN3"))
                .from(table, "a")
                .where(ascii(column3), isEqualTo("A"))
                .build()
                .render(RenderingStrategy.MYBATIS3);

        SoftAssertions.assertSoftly(softly -> {
            String expectedFullStatement = "select ASCII(a.column3) as A_COLUMN3 "
                    + "from foo a "
                    + "where ASCII(a.column3) = #{parameters.p1,jdbcType=VARCHAR}";

            softly.assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedFullStatement);
        });
    }
}
