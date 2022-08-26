/*
 *    Copyright 2016-2022 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.dynamic.sql.mybatis3;

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

class SelectStatementTest {
    static final SqlTable table = SqlTable.of("foo");
    static final SqlColumn<Date> column1 = table.column("column1", JDBCType.DATE);
    static final SqlColumn<Integer> column2 = table.column("column2", JDBCType.INTEGER);

    @Test
    void testSimpleCriteriaWithoutAlias() {
        Date d = new Date();

        SelectStatementProvider selectStatement = select(column1, column2)
                .from(table, "a")
                .where(column1, isEqualTo(d))
                .or(column2, isEqualTo(4))
                .and(column2, isLessThan(3))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        assertThat(selectStatement.getSelectStatement()).isEqualTo(
                "select a.column1, a.column2 from foo a where a.column1 = #{parameters.p1,jdbcType=DATE} or a.column2 = #{parameters.p2,jdbcType=INTEGER} and a.column2 < #{parameters.p3,jdbcType=INTEGER}");

        Map<String, Object> parameters = selectStatement.getParameters();

        assertAll(
                () -> assertThat(parameters).containsEntry("p1", d),
                () -> assertThat(parameters).containsEntry("p2", 4),
                () -> assertThat(parameters).containsEntry("p3", 3)
        );
    }

    @Test
    void testComplexCriteriaWithoutAlias() {
        Date d = new Date();

        SelectStatementProvider selectStatement = select(column1, column2)
                .from(table, "a")
                .where(column1, isEqualTo(d))
                .or(column2, isEqualTo(4))
                .and(column2, isLessThan(3))
                .or(column2, isEqualTo(4), and(column2, isEqualTo(6)))
                .and(column2, isLessThan(3), or(column1, isEqualTo(d)))
                .build()
                .render(RenderingStrategies.MYBATIS3);


        String expected = "select a.column1, a.column2 "
                + "from foo a "
                + "where a.column1 = #{parameters.p1,jdbcType=DATE}"
                + " or a.column2 = #{parameters.p2,jdbcType=INTEGER}"
                + " and a.column2 < #{parameters.p3,jdbcType=INTEGER}"
                + " or (a.column2 = #{parameters.p4,jdbcType=INTEGER} and a.column2 = #{parameters.p5,jdbcType=INTEGER})"
                + " and (a.column2 < #{parameters.p6,jdbcType=INTEGER} or a.column1 = #{parameters.p7,jdbcType=DATE})";

        Map<String, Object> parameters = selectStatement.getParameters();

        assertAll(
                () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
                () -> assertThat(parameters).containsEntry("p1", d),
                () -> assertThat(parameters).containsEntry("p2", 4),
                () -> assertThat(parameters).containsEntry("p3", 3),
                () -> assertThat(parameters).containsEntry("p4", 4),
                () -> assertThat(parameters).containsEntry("p5", 6),
                () -> assertThat(parameters).containsEntry("p6", 3),
                () -> assertThat(parameters).containsEntry("p7", d)
        );
    }

    @Test
    void testSimpleCriteriaWithAlias() {
        Date d = new Date();

        SelectStatementProvider selectStatement = select(column1, column2)
                .from(table, "a")
                .where(column1, isEqualTo(d))
                .or(column2, isEqualTo(4))
                .and(column2, isLessThan(3))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        Map<String, Object> parameters = selectStatement.getParameters();

        assertAll(
                () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(
                    "select a.column1, a.column2 from foo a where a.column1 = #{parameters.p1,jdbcType=DATE} or a.column2 = #{parameters.p2,jdbcType=INTEGER} and a.column2 < #{parameters.p3,jdbcType=INTEGER}"),
                () -> assertThat(parameters).containsEntry("p1", d),
                () -> assertThat(parameters).containsEntry("p2", 4),
                () -> assertThat(parameters).containsEntry("p3", 3)
        );
    }

    @Test
    void testComplexCriteriaWithAlias() {
        Date d = new Date();

        SelectStatementProvider selectStatement = select(column1, column2)
                .from(table, "a")
                .where(column1, isEqualTo(d))
                .or(column2, isEqualTo(4))
                .and(column2, isLessThan(3))
                .or(column2, isEqualTo(4), and(column2, isEqualTo(6), or(column2, isEqualTo(7))))
                .and(column2, isLessThan(3), or(column1, isEqualTo(d), and(column2, isEqualTo(88))))
                .build()
                .render(RenderingStrategies.MYBATIS3);


        String expected = "select a.column1, a.column2 "
                + "from foo a "
                + "where a.column1 = #{parameters.p1,jdbcType=DATE}"
                + " or a.column2 = #{parameters.p2,jdbcType=INTEGER}"
                + " and a.column2 < #{parameters.p3,jdbcType=INTEGER}"
                + " or (a.column2 = #{parameters.p4,jdbcType=INTEGER} and (a.column2 = #{parameters.p5,jdbcType=INTEGER} or a.column2 = #{parameters.p6,jdbcType=INTEGER}))"
                + " and (a.column2 < #{parameters.p7,jdbcType=INTEGER} or (a.column1 = #{parameters.p8,jdbcType=DATE} and a.column2 = #{parameters.p9,jdbcType=INTEGER}))";

        Map<String, Object> parameters = selectStatement.getParameters();

        assertAll(
            () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expected),
            () -> assertThat(parameters).containsEntry("p1", d),
            () -> assertThat(parameters).containsEntry("p2", 4),
            () -> assertThat(parameters).containsEntry("p3", 3),
            () -> assertThat(parameters).containsEntry("p4", 4),
            () -> assertThat(parameters).containsEntry("p5", 6),
            () -> assertThat(parameters).containsEntry("p6", 7),
            () -> assertThat(parameters).containsEntry("p7", 3),
            () -> assertThat(parameters).containsEntry("p8", d),
            () -> assertThat(parameters).containsEntry("p9", 88)
        );
    }
}
