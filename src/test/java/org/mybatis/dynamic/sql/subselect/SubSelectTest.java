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
package org.mybatis.dynamic.sql.subselect;

import static org.mybatis.dynamic.sql.SqlBuilder.select;
import static org.mybatis.dynamic.sql.SqlConditions.*;

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
import org.mybatis.dynamic.sql.select.render.SelectSupport;

@RunWith(JUnitPlatform.class)
public class SubSelectTest {
    
    public static final SqlTable table = SqlTable.of("foo");
    public static final SqlColumn<Date> column1 = SqlColumn.of(table, "column1", JDBCType.DATE).withAlias("A_COLUMN1");
    public static final SqlColumn<Integer> column2 = SqlColumn.of(table, "column2", JDBCType.INTEGER);

    @Test
    public void testInSubSelect() {
        Date d = new Date();

        SelectSupport selectSupport = select(column1, column2)
                .from(table, "a")
                .where(column2, isIn(select(column2).from(table).where(column2, isEqualTo(3))))
                .and(column1, isLessThan(d))
                .build()
                .render(RenderingStrategy.MYBATIS3);
        

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(selectSupport.getDistinct()).isEqualTo("");
            softly.assertThat(selectSupport.getColumnList()).isEqualTo("a.column1 as A_COLUMN1, a.column2");

            String expectedWhereClause = "where a.column2 in (select column2 from foo where column2 = #{parameters.p1,jdbcType=INTEGER})"
                    + " and a.column1 < #{parameters.p2,jdbcType=DATE}";

            softly.assertThat(selectSupport.getWhereClause()).isEqualTo(expectedWhereClause);
            softly.assertThat(selectSupport.getOrderByClause()).isEqualTo("");

            String expectedFullStatement = "select " + selectSupport.getColumnList() + " from foo a "
                    + selectSupport.getWhereClause();

            softly.assertThat(selectSupport.getFullSelectStatement()).isEqualTo(expectedFullStatement);

            Map<String, Object> parameters = selectSupport.getParameters();
            softly.assertThat(parameters.get("p1")).isEqualTo(3);
            softly.assertThat(parameters.get("p2")).isEqualTo(d);
        });
    }

    @Test
    public void testNotInSubSelect() {
        Date d = new Date();

        SelectSupport selectSupport = select(column1, column2)
                .from(table, "a")
                .where(column2, isNotIn(select(column2).from(table).where(column2, isEqualTo(3))))
                .and(column1, isLessThan(d))
                .build()
                .render(RenderingStrategy.MYBATIS3);
        

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(selectSupport.getDistinct()).isEqualTo("");
            softly.assertThat(selectSupport.getColumnList()).isEqualTo("a.column1 as A_COLUMN1, a.column2");

            String expectedWhereClause = "where a.column2 not in (select column2 from foo where column2 = #{parameters.p1,jdbcType=INTEGER})"
                    + " and a.column1 < #{parameters.p2,jdbcType=DATE}";

            softly.assertThat(selectSupport.getWhereClause()).isEqualTo(expectedWhereClause);
            softly.assertThat(selectSupport.getOrderByClause()).isEqualTo("");

            String expectedFullStatement = "select " + selectSupport.getColumnList() + " from foo a "
                    + selectSupport.getWhereClause();

            softly.assertThat(selectSupport.getFullSelectStatement()).isEqualTo(expectedFullStatement);

            Map<String, Object> parameters = selectSupport.getParameters();
            softly.assertThat(parameters.get("p1")).isEqualTo(3);
            softly.assertThat(parameters.get("p2")).isEqualTo(d);
        });
    }
}
