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
package org.mybatis.dynamic.sql.select;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.sql.JDBCType;
import java.util.Date;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;

public class SelectStatementTest {
    
    public static final SqlTable table = SqlTable.of("foo");
    public static final SqlColumn<Date> column1 = table.column("column1", JDBCType.DATE);
    public static final SqlColumn<Integer> column2 = table.column("column2", JDBCType.INTEGER);

    @Test
    public void testSimpleCriteria() {
        Date d = new Date();

        SelectStatementProvider selectStatement = select(column1.as("A_COLUMN1"), column2)
                .from(table, "a")
                .where(column1, isEqualTo(d), and(column2, isEqualTo(33)))
                .or(column2, isEqualTo(4))
                .and(column2, isLessThan(3))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expectedFullStatement = "select a.column1 as A_COLUMN1, a.column2 "
                + "from foo a "
                + "where (a.column1 = #{parameters.p1,jdbcType=DATE} and a.column2 = #{parameters.p2,jdbcType=INTEGER}) or a.column2 = #{parameters.p3,jdbcType=INTEGER} and a.column2 < #{parameters.p4,jdbcType=INTEGER}";

        Map<String, Object> parameters = selectStatement.getParameters();

        assertAll(
                () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedFullStatement),
                () -> assertThat(parameters.get("p1")).isEqualTo(d),
                () -> assertThat(parameters.get("p2")).isEqualTo(33),
                () -> assertThat(parameters.get("p3")).isEqualTo(4),
                () -> assertThat(parameters.get("p4")).isEqualTo(3)
        );
    }

    @Test
    public void testComplexCriteria() {
        Date d = new Date();

        SelectStatementProvider selectStatement = select(column1.as("A_COLUMN1"), column2)
                .from(table, "a")
                .where(column1, isEqualTo(d))
                .or(column2, isEqualTo(4))
                .and(column2, isLessThan(3))
                .or(column2, isEqualTo(4), and(column2, isEqualTo(6)))
                .and(column2, isLessThan(3), or(column1, isEqualTo(d)))
                .build()
                .render(RenderingStrategies.MYBATIS3);
        
        String expectedFullStatement = "select a.column1 as A_COLUMN1, a.column2 "
                + "from foo a "
                + "where a.column1 = #{parameters.p1,jdbcType=DATE}"
                + " or a.column2 = #{parameters.p2,jdbcType=INTEGER}"
                + " and a.column2 < #{parameters.p3,jdbcType=INTEGER}"
                + " or (a.column2 = #{parameters.p4,jdbcType=INTEGER} and a.column2 = #{parameters.p5,jdbcType=INTEGER})"
                + " and (a.column2 < #{parameters.p6,jdbcType=INTEGER} or a.column1 = #{parameters.p7,jdbcType=DATE})";

        Map<String, Object> parameters = selectStatement.getParameters();

        assertAll(
                () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedFullStatement),
                () -> assertThat(parameters.get("p1")).isEqualTo(d),
                () -> assertThat(parameters.get("p2")).isEqualTo(4),
                () -> assertThat(parameters.get("p3")).isEqualTo(3),
                () -> assertThat(parameters.get("p4")).isEqualTo(4),
                () -> assertThat(parameters.get("p5")).isEqualTo(6),
                () -> assertThat(parameters.get("p6")).isEqualTo(3),
                () -> assertThat(parameters.get("p7")).isEqualTo(d)
        );
    }

    @Test
    public void testOrderBySingleColumnAscending() {
        Date d = new Date();

        SelectStatementProvider selectStatement = select(column1.as("A_COLUMN1"), column2)
                .from(table, "a")
                .where(column1, isEqualTo(d))
                .orderBy(column1)
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expectedFullStatement = "select a.column1 as A_COLUMN1, a.column2 "
                + "from foo a "
                + "where a.column1 = #{parameters.p1,jdbcType=DATE} "
                + "order by column1";

        Map<String, Object> parameters = selectStatement.getParameters();

        assertAll(
                () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedFullStatement),
                () -> assertThat(parameters.get("p1")).isEqualTo(d)
        );
    }

    @Test
    public void testOrderBySingleColumnDescending() {
        Date d = new Date();

        SelectStatementProvider selectStatement = select(column1.as("A_COLUMN1"), column2)
                .from(table, "a")
                .where(column1, isEqualTo(d))
                .orderBy(column2.descending())
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expectedFullStatement = "select a.column1 as A_COLUMN1, a.column2 "
                + "from foo a "
                + "where a.column1 = #{parameters.p1,jdbcType=DATE} "
                + "order by column2 DESC";
        
        Map<String, Object> parameters = selectStatement.getParameters();

        assertAll(
                () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedFullStatement),
                () -> assertThat(parameters.get("p1")).isEqualTo(d)
        );
    }

    @Test
    public void testOrderByMultipleColumns() {
        Date d = new Date();

        SelectStatementProvider selectStatement = select(column1.as("A_COLUMN1"), column2)
                .from(table, "a")
                .where(column1, isEqualTo(d))
                .orderBy(column2.descending(), column1)
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expectedFullStatement = "select a.column1 as A_COLUMN1, a.column2 "
                + "from foo a "
                + "where a.column1 = #{parameters.p1,jdbcType=DATE} "
                + "order by column2 DESC, column1";
        
        Map<String, Object> parameters = selectStatement.getParameters();

        assertAll(
                () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedFullStatement),
                () -> assertThat(parameters.get("p1")).isEqualTo(d)
        );
    }

    @Test
    public void testDistinct() {
        Date d = new Date();

        SelectStatementProvider selectStatement = selectDistinct(column1.as("A_COLUMN1"), column2)
                .from(table, "a")
                .where(column1, isEqualTo(d))
                .orderBy(column2.descending(), column1)
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expectedFullStatement = "select distinct a.column1 as A_COLUMN1, a.column2 "
                + "from foo a "
                + "where a.column1 = #{parameters.p1,jdbcType=DATE} "
                + "order by column2 DESC, column1";
        
        Map<String, Object> parameters = selectStatement.getParameters();

        assertAll(
                () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedFullStatement),
                () -> assertThat(parameters.get("p1")).isEqualTo(d)
        );
    }

    @Test
    public void testCount() {
        Date d = new Date();

        SelectStatementProvider selectStatement = select(count())
                .from(table, "a")
                .where(column1, isEqualTo(d))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expectedFullStatement = "select count(*) "
                + "from foo a "
                + "where a.column1 = #{parameters.p1,jdbcType=DATE}";
        
        Map<String, Object> parameters = selectStatement.getParameters();

        assertAll(
                () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedFullStatement),
                () -> assertThat(parameters.get("p1")).isEqualTo(d)
        );
    }

    @Test
    public void testNoWhere() {
        SelectStatementProvider selectStatement = select(count())
                .from(table, "a")
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expectedFullStatement = "select count(*) "
                + "from foo a";
        
        Map<String, Object> parameters = selectStatement.getParameters();

        assertAll(
                () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedFullStatement),
                () -> assertThat(parameters.size()).isEqualTo(0)
        );
    }
    
    @Test
    public void testGroupBySingleColumn() {
        Date d = new Date();

        SelectStatementProvider selectStatement = select(column1.as("A_COLUMN1"), column2)
                .from(table, "a")
                .where(column1, isEqualTo(d))
                .groupBy(column2)
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expectedFullStatement = "select a.column1 as A_COLUMN1, a.column2 "
                + "from foo a "
                + "where a.column1 = #{parameters.p1,jdbcType=DATE} "
                + "group by a.column2";
        
        Map<String, Object> parameters = selectStatement.getParameters();

        assertAll(
                () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedFullStatement),
                () -> assertThat(parameters.get("p1")).isEqualTo(d)
        );
    }
}
