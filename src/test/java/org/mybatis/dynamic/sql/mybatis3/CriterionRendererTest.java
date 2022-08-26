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

import java.sql.JDBCType;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.ColumnAndConditionCriterion;
import org.mybatis.dynamic.sql.SqlBuilder;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.render.ExplicitTableAliasCalculator;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.render.TableAliasCalculator;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.where.condition.IsEqualTo;
import org.mybatis.dynamic.sql.where.render.CriterionRenderer;

class CriterionRendererTest {
    @Test
    void testAliasWithIgnore() {
        SqlTable table = SqlTable.of("foo");
        SqlColumn<Integer> column = table.column("id", JDBCType.INTEGER);

        IsEqualTo<Integer> condition = SqlBuilder.isEqualTo(() -> 3);
        ColumnAndConditionCriterion<Integer> criterion = ColumnAndConditionCriterion.withColumn(column)
                .withCondition(condition)
                .build();
        AtomicInteger sequence = new AtomicInteger(1);

        CriterionRenderer renderer = new CriterionRenderer.Builder()
                .withSequence(sequence)
                .withRenderingStrategy(RenderingStrategies.MYBATIS3)
                .withTableAliasCalculator(TableAliasCalculator.empty())
                .build();

        assertThat(criterion.accept(renderer)).hasValueSatisfying(rc -> {
            FragmentAndParameters fp = rc.fragmentAndParametersWithConnector();
            assertThat(fp.fragment()).isEqualTo("id = #{parameters.p1,jdbcType=INTEGER}");
            assertThat(fp.parameters()).hasSize(1);
        });
    }

    @Test
    void testAliasWithoutIgnore() {
        SqlTable table = SqlTable.of("foo");
        SqlColumn<Integer> column = table.column("id", JDBCType.INTEGER);
        IsEqualTo<Integer> condition = SqlBuilder.isEqualTo(() -> 3);
        ColumnAndConditionCriterion<Integer> criterion = ColumnAndConditionCriterion.withColumn(column)
                .withCondition(condition)
                .build();
        AtomicInteger sequence = new AtomicInteger(1);
        Map<SqlTable, String> tableAliases = new HashMap<>();
        tableAliases.put(table, "a");

        CriterionRenderer renderer = new CriterionRenderer.Builder()
                .withSequence(sequence)
                .withRenderingStrategy(RenderingStrategies.MYBATIS3)
                .withTableAliasCalculator(ExplicitTableAliasCalculator.of(tableAliases))
                .build();

        assertThat(criterion.accept(renderer)).hasValueSatisfying(rc -> {
            FragmentAndParameters fp = rc.fragmentAndParametersWithConnector();
            assertThat(fp.fragment()).isEqualTo("a.id = #{parameters.p1,jdbcType=INTEGER}");
            assertThat(fp.parameters()).hasSize(1);
        });
    }

    @Test
    void testTypeHandler() {
        SqlTable table = SqlTable.of("foo");
        SqlColumn<Date> column = new SqlColumn.Builder<Date>()
                .withName("id")
                .withTable(table)
                .withJdbcType(JDBCType.DATE)
                .withTypeHandler("foo.Bar")
                .build();
        IsEqualTo<Date> condition = SqlBuilder.isEqualTo(new Date());
        ColumnAndConditionCriterion<Date> criterion = ColumnAndConditionCriterion.withColumn(column)
                .withCondition(condition)
                .build();
        AtomicInteger sequence = new AtomicInteger(1);

        CriterionRenderer renderer = new CriterionRenderer.Builder()
                .withSequence(sequence)
                .withRenderingStrategy(RenderingStrategies.MYBATIS3)
                .withTableAliasCalculator(TableAliasCalculator.empty())
                .build();

        assertThat(criterion.accept(renderer)).hasValueSatisfying(rc -> {
            FragmentAndParameters fp = rc.fragmentAndParametersWithConnector();
            assertThat(fp.fragment()).isEqualTo("id = #{parameters.p1,jdbcType=DATE,typeHandler=foo.Bar}");
            assertThat(fp.parameters()).hasSize(1);
        });
    }

    @Test
    void testTypeHandlerAndAlias() {
        SqlTable table = SqlTable.of("foo");
        SqlColumn<Integer> column = table.column("id", JDBCType.INTEGER, "foo.Bar");
        IsEqualTo<Integer> condition = SqlBuilder.isEqualTo(() -> 3);
        ColumnAndConditionCriterion<Integer> criterion = ColumnAndConditionCriterion.withColumn(column)
                .withCondition(condition)
                .build();
        AtomicInteger sequence = new AtomicInteger(1);
        Map<SqlTable, String> tableAliases = new HashMap<>();
        tableAliases.put(table, "a");

        CriterionRenderer renderer = new CriterionRenderer.Builder()
                .withSequence(sequence)
                .withRenderingStrategy(RenderingStrategies.MYBATIS3)
                .withTableAliasCalculator(ExplicitTableAliasCalculator.of(tableAliases))
                .build();

        assertThat(criterion.accept(renderer)).hasValueSatisfying(rc -> {
            FragmentAndParameters fp = rc.fragmentAndParametersWithConnector();
            assertThat(fp.fragment()).isEqualTo("a.id = #{parameters.p1,jdbcType=INTEGER,typeHandler=foo.Bar}");
            assertThat(fp.parameters()).hasSize(1);
        });
    }
}
