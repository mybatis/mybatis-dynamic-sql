/**
 *    Copyright 2016-2018 the original author or authors.
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

import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.sql.JDBCType;
import java.util.Date;
import java.util.Map;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;

@RunWith(JUnitPlatform.class)
public class SelectStatementTest {
    
    public static final SqlTable table = SqlTable.of("foo");
    public static final SqlColumn<Date> column1 = table.column("column1", JDBCType.DATE);
    public static final SqlColumn<Integer> column2 = table.column("column2", JDBCType.INTEGER);
    public static final SqlColumn<Integer> column3 = table.column("column3", JDBCType.INTEGER);
    public static final SqlColumn<Integer> column4 = table.column("column4", JDBCType.INTEGER);

    @Test
    public void testSimpleCriteria() {
        Date d = new Date();

        SelectStatementProvider selectStatement = select(column1.as("A_COLUMN1"), column2)
                .from(table, "a")
                .where(column1, isEqualTo(d), and(column2, isEqualTo(33)))
                .or(column2, isEqualTo(4))
                .and(column2, isLessThan(3))
                .build()
                .render(RenderingStrategy.MYBATIS3);

        SoftAssertions.assertSoftly(softly -> {
            String expectedFullStatement = "select a.column1 as A_COLUMN1, a.column2 "
                    + "from foo a "
                    + "where (a.column1 = #{parameters.p1,jdbcType=DATE} and a.column2 = #{parameters.p2,jdbcType=INTEGER}) or a.column2 = #{parameters.p3,jdbcType=INTEGER} and a.column2 < #{parameters.p4,jdbcType=INTEGER}";

            softly.assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedFullStatement);

            Map<String, Object> parameters = selectStatement.getParameters();
            softly.assertThat(parameters.get("p1")).isEqualTo(d);
            softly.assertThat(parameters.get("p2")).isEqualTo(33);
            softly.assertThat(parameters.get("p3")).isEqualTo(4);
            softly.assertThat(parameters.get("p4")).isEqualTo(3);
        });
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
                .render(RenderingStrategy.MYBATIS3);
        

        SoftAssertions.assertSoftly(softly -> {
            String expectedFullStatement = "select a.column1 as A_COLUMN1, a.column2 "
                    + "from foo a "
                    + "where a.column1 = #{parameters.p1,jdbcType=DATE}"
                    + " or a.column2 = #{parameters.p2,jdbcType=INTEGER}"
                    + " and a.column2 < #{parameters.p3,jdbcType=INTEGER}"
                    + " or (a.column2 = #{parameters.p4,jdbcType=INTEGER} and a.column2 = #{parameters.p5,jdbcType=INTEGER})"
                    + " and (a.column2 < #{parameters.p6,jdbcType=INTEGER} or a.column1 = #{parameters.p7,jdbcType=DATE})";

            softly.assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedFullStatement);

            Map<String, Object> parameters = selectStatement.getParameters();
            softly.assertThat(parameters.get("p1")).isEqualTo(d);
            softly.assertThat(parameters.get("p2")).isEqualTo(4);
            softly.assertThat(parameters.get("p3")).isEqualTo(3);
            softly.assertThat(parameters.get("p4")).isEqualTo(4);
            softly.assertThat(parameters.get("p5")).isEqualTo(6);
            softly.assertThat(parameters.get("p6")).isEqualTo(3);
            softly.assertThat(parameters.get("p7")).isEqualTo(d);
        });
    }

    @Test
    public void testOrderBySingleColumnAscending() {
        Date d = new Date();

        SelectStatementProvider selectStatement = select(column1.as("A_COLUMN1"), column2)
                .from(table, "a")
                .where(column1, isEqualTo(d))
                .orderBy(column1)
                .build()
                .render(RenderingStrategy.MYBATIS3);

        SoftAssertions.assertSoftly(softly -> {
            String expectedFullStatement = "select a.column1 as A_COLUMN1, a.column2 "
                    + "from foo a "
                    + "where a.column1 = #{parameters.p1,jdbcType=DATE} "
                    + "order by column1";

            softly.assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedFullStatement);

            Map<String, Object> parameters = selectStatement.getParameters();
            softly.assertThat(parameters.get("p1")).isEqualTo(d);
        });
    }

    @Test
    public void testOrderBySingleColumnDescending() {
        Date d = new Date();

        SelectStatementProvider selectStatement = select(column1.as("A_COLUMN1"), column2)
                .from(table, "a")
                .where(column1, isEqualTo(d))
                .orderBy(column2.descending())
                .build()
                .render(RenderingStrategy.MYBATIS3);

        SoftAssertions.assertSoftly(softly -> {
            String expectedFullStatement = "select a.column1 as A_COLUMN1, a.column2 "
                    + "from foo a "
                    + "where a.column1 = #{parameters.p1,jdbcType=DATE} "
                    + "order by column2 DESC";

            softly.assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedFullStatement);
        
            Map<String, Object> parameters = selectStatement.getParameters();
            softly.assertThat(parameters.get("p1")).isEqualTo(d);
        });
    }

    @Test
    public void testOrderByMultipleColumns() {
        Date d = new Date();

        SelectStatementProvider selectStatement = select(column1.as("A_COLUMN1"), column2)
                .from(table, "a")
                .where(column1, isEqualTo(d))
                .orderBy(column2.descending(), column1)
                .build()
                .render(RenderingStrategy.MYBATIS3);

        SoftAssertions.assertSoftly(softly -> {
            String expectedFullStatement = "select a.column1 as A_COLUMN1, a.column2 "
                    + "from foo a "
                    + "where a.column1 = #{parameters.p1,jdbcType=DATE} "
                    + "order by column2 DESC, column1";

            softly.assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedFullStatement);
        
            Map<String, Object> parameters = selectStatement.getParameters();
            softly.assertThat(parameters.get("p1")).isEqualTo(d);
        });
    }

    @Test
    public void testDistinct() {
        Date d = new Date();

        SelectStatementProvider selectStatement = selectDistinct(column1.as("A_COLUMN1"), column2)
                .from(table, "a")
                .where(column1, isEqualTo(d))
                .orderBy(column2.descending(), column1)
                .build()
                .render(RenderingStrategy.MYBATIS3);

        SoftAssertions.assertSoftly(softly -> {
            String expectedFullStatement = "select distinct a.column1 as A_COLUMN1, a.column2 "
                    + "from foo a "
                    + "where a.column1 = #{parameters.p1,jdbcType=DATE} "
                    + "order by column2 DESC, column1";

            softly.assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedFullStatement);
        
            Map<String, Object> parameters = selectStatement.getParameters();
            softly.assertThat(parameters.get("p1")).isEqualTo(d);
        });
    }

    @Test
    public void testCount() {
        Date d = new Date();

        SelectStatementProvider selectStatement = select(count())
                .from(table, "a")
                .where(column1, isEqualTo(d))
                .build()
                .render(RenderingStrategy.MYBATIS3);

        SoftAssertions.assertSoftly(softly -> {
            String expectedFullStatement = "select count(*) "
                    + "from foo a "
                    + "where a.column1 = #{parameters.p1,jdbcType=DATE}";

            softly.assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedFullStatement);
        
            Map<String, Object> parameters = selectStatement.getParameters();
            softly.assertThat(parameters.get("p1")).isEqualTo(d);
        });
    }

    @Test
    public void testNoWhere() {
        SelectStatementProvider selectStatement = select(count())
                .from(table, "a")
                .build()
                .render(RenderingStrategy.MYBATIS3);

        SoftAssertions.assertSoftly(softly -> {
            String expectedFullStatement = "select count(*) "
                    + "from foo a";

            softly.assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedFullStatement);
        
            Map<String, Object> parameters = selectStatement.getParameters();
            softly.assertThat(parameters.size()).isEqualTo(0);
        });
    }
    
    @Test
    public void testArithmeticFunctions() {
        SelectStatementProvider selectStatement = select(divide(add(column2, column3, substract(column3, column4, multiply(column2, column4))), column3).as("addedColumns"))
                .from(table, "a")
                .build()
                .render(RenderingStrategy.MYBATIS3);

        System.out.println(selectStatement.getSelectStatement());
        
        SoftAssertions.assertSoftly(softly -> {
            String expectedFullStatement = "select ((a.column2 + a.column3 + (a.column3 - a.column4 - (a.column2 * a.column4))) / a.column3) as addedColumns "
                    + "from foo a";

            softly.assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedFullStatement);
        });
    }
}
