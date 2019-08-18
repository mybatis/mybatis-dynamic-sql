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
package org.mybatis.dynamic.sql.subselect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.sql.JDBCType;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;

public class SubSelectTest {
    
    public static final SqlTable table = SqlTable.of("foo");
    public static final SqlColumn<Date> column1 = table.column("column1", JDBCType.DATE);
    public static final SqlColumn<Integer> column2 = table.column("column2", JDBCType.INTEGER);

    @Test
    public void testInSubSelect() {
        Date d = new Date();

        SelectStatementProvider selectStatement = select(column1.as("A_COLUMN1"), column2)
                .from(table, "a")
                .where(column2, isIn(select(column2).from(table).where(column2, isEqualTo(3))))
                .and(column1, isLessThan(d))
                .build()
                .render(RenderingStrategies.MYBATIS3);
        
        String expectedFullStatement = "select a.column1 as A_COLUMN1, a.column2 "
                + "from foo a "
                + "where a.column2 in (select column2 from foo where column2 = #{parameters.p1,jdbcType=INTEGER}) "
                + "and a.column1 < #{parameters.p2,jdbcType=DATE}";

        assertAll(
                () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedFullStatement),
                () -> assertThat(selectStatement.getParameters().get("p1")).isEqualTo(3),
                () -> assertThat(selectStatement.getParameters().get("p2")).isEqualTo(d)
        );
    }

    @Test
    public void testNotInSubSelect() {
        Date d = new Date();

        SelectStatementProvider selectStatement = select(column1.as("A_COLUMN1"), column2)
                .from(table, "a")
                .where(column2, isNotIn(select(column2).from(table).where(column2, isEqualTo(3))))
                .and(column1, isLessThan(d))
                .build()
                .render(RenderingStrategies.MYBATIS3);
        
        String expectedFullStatement = "select a.column1 as A_COLUMN1, a.column2 "
                + "from foo a "
                + "where a.column2 not in (select column2 from foo where column2 = #{parameters.p1,jdbcType=INTEGER})"
                + " and a.column1 < #{parameters.p2,jdbcType=DATE}";

        assertAll(
                () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedFullStatement),
                () -> assertThat(selectStatement.getParameters().get("p1")).isEqualTo(3),
                () -> assertThat(selectStatement.getParameters().get("p2")).isEqualTo(d)
        );
    }
}
