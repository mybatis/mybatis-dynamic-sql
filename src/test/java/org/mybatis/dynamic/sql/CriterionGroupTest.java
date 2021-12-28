/*
 *    Copyright 2016-2021 the original author or authors.
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
package org.mybatis.dynamic.sql;

import java.sql.JDBCType;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.QueryExpressionDSL;
import org.mybatis.dynamic.sql.select.QueryExpressionDSL.QueryExpressionWhereBuilder;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

/**
 * @author <a href="mailto:daonan.zhan@gmail.com">Joshua</a>
 */
class CriterionGroupTest {

    static final SqlTable table = SqlTable.of("foo");
    static final SqlColumn<Integer> column1 = table.column("col1", JDBCType.INTEGER);
    static final SqlColumn<Integer> column2 = table.column("col2", JDBCType.INTEGER);
    static final SqlColumn<Integer> column3 = table.column("col3", JDBCType.INTEGER);

    @Test
    void testWhere() {
        SelectStatementProvider selectStatement = select(column1, column2)
            .from(table)
            .where(or(column1, isEqualTo(1)), or(column2, isEqualTo(2)))
            .and(column3, isEqualTo(3))
            .build()
            .render(RenderingStrategies.MYBATIS3);

        String expectedFullStatement = "select col1, col2 from foo "
            + "where (col1 = #{parameters.p1,jdbcType=INTEGER} or col2 = #{parameters.p2,jdbcType=INTEGER}) "
            + "and col3 = #{parameters.p3,jdbcType=INTEGER}";

        Map<String, Object> parameters = selectStatement.getParameters();

        assertAll(
            () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedFullStatement),
            () -> assertThat(parameters).containsEntry("p1", 1),
            () -> assertThat(parameters).containsEntry("p2", 2),
            () -> assertThat(parameters).containsEntry("p3", 3)
        );
    }

    static class Param {
        Integer cond1;
        Integer cond2;
        Integer cond3;
    }


    @Test
    void demo() {
        Param param = new Param();
        param.cond1 = 1;
        param.cond2 = 2;
        param.cond3 = 3;

        List<SqlCriterion> conditions = new LinkedList<>();
        QueryExpressionDSL<SelectModel> dsl = select(column1, column2).from(table);

        QueryExpressionWhereBuilder builder = dsl.where();
        Optional.ofNullable(param.cond1).map(c -> or(column1, isEqualTo(c))).ifPresent(conditions::add);
        Optional.ofNullable(param.cond2).map(c -> or(column2, isEqualTo(c))).ifPresent(conditions::add);
        builder.and(conditions);

        if (null != param.cond3) {
            builder.and(column3, isEqualTo(param.cond3));
        }

        SelectStatementProvider selectStatement = dsl.build().render(RenderingStrategies.MYBATIS3);

        System.out.println(selectStatement.getSelectStatement());

    }

    @Test
    void testAndOr() {
        SelectStatementProvider selectStatement = select(column1, column2)
            .from(table)
            .where()
            .and(or(column1, isEqualTo(1)), or(column2, isEqualTo(2)))
            .or(and(column2, isEqualTo(2)), or(column3, isEqualTo(3)))
            .build()
            .render(RenderingStrategies.MYBATIS3);

        // Connector of first SqlCriterion in subCriteria list is ignored.
        String expectedFullStatement = "select col1, col2 from foo "
            + "where (col1 = #{parameters.p1,jdbcType=INTEGER} or col2 = #{parameters.p2,jdbcType=INTEGER}) "
            + "or (col2 = #{parameters.p3,jdbcType=INTEGER} or col3 = #{parameters.p4,jdbcType=INTEGER})";

        Map<String, Object> parameters = selectStatement.getParameters();

        assertAll(
            () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedFullStatement),
            () -> assertThat(parameters).containsEntry("p1", 1),
            () -> assertThat(parameters).containsEntry("p2", 2),
            () -> assertThat(parameters).containsEntry("p3", 2),
            () -> assertThat(parameters).containsEntry("p4", 3)
        );
    }

    @Test
    void testEmptySubCriteria() {
        SelectStatementProvider selectStatement = select(column1, column2)
            .from(table)
            .where(Collections.emptyList())
            .or(Collections.emptyList())
            .and(Collections.singletonList(or(column1, isEqualTo(1))))
            .build()
            .render(RenderingStrategies.MYBATIS3);

        // Empty subCriteria list is ignored and subCriteria list with single SqlCriterion doesn't have parentheses.
        String expectedFullStatement = "select col1, col2 from foo "
            + "where col1 = #{parameters.p1,jdbcType=INTEGER}";

        Map<String, Object> parameters = selectStatement.getParameters();

        assertAll(
            () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedFullStatement),
            () -> assertThat(parameters).containsEntry("p1", 1)
        );
    }

    @Test
    void testNest() {
        SelectStatementProvider selectStatement = select(column1, column2).from(table)
            .where(or(and(
                or(column1, isEqualTo(1))
                , or(column2, isEqualTo(2))
                , or(
                    and(column2, isEqualTo(2))
                    , and(column3, isEqualTo(3))
                )
            )))
            .build().render(RenderingStrategies.MYBATIS3);

        // CriterionGroup within CriterionGroup
        String expectedFullStatement = "select col1, col2 from foo "
            + "where col1 = #{parameters.p1,jdbcType=INTEGER} or col2 = #{parameters.p2,jdbcType=INTEGER} "
            + "or (col2 = #{parameters.p3,jdbcType=INTEGER} and col3 = #{parameters.p4,jdbcType=INTEGER})";

        Map<String, Object> parameters = selectStatement.getParameters();

        assertAll(
            () -> assertThat(selectStatement.getSelectStatement()).isEqualTo(expectedFullStatement),
            () -> assertThat(parameters).containsEntry("p1", 1),
            () -> assertThat(parameters).containsEntry("p2", 2),
            () -> assertThat(parameters).containsEntry("p3", 2),
            () -> assertThat(parameters).containsEntry("p4", 3)
        );
    }


}