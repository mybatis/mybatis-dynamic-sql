/*
 *    Copyright 2016-2024 the original author or authors.
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
package org.mybatis.dynamic.sql.where.render;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.sql.JDBCType;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.ColumnAndConditionCriterion;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.render.ExplicitTableAliasCalculator;
import org.mybatis.dynamic.sql.render.RenderingContext;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.where.condition.IsEqualTo;

class CriterionRendererTest {

    @Test
    void testAliasWithIgnore() {
        SqlTable table = SqlTable.of("foo");
        SqlColumn<Integer> column = table.column("id", JDBCType.INTEGER);

        IsEqualTo<Integer> condition = IsEqualTo.of(3);
        ColumnAndConditionCriterion<Integer> criterion = ColumnAndConditionCriterion.withColumn(column)
                .withCondition(condition)
                .build();

        RenderingContext renderingContext = RenderingContext
                .withRenderingStrategy(RenderingStrategies.MYBATIS3)
                .build();

        CriterionRenderer renderer = new CriterionRenderer(renderingContext);

        assertThat(criterion.accept(renderer)).hasValueSatisfying(rc -> {
            FragmentAndParameters fp = rc.fragmentAndParameters();
            assertThat(fp.fragment()).isEqualTo("id = #{parameters.p1,jdbcType=INTEGER}");
            assertThat(fp.parameters()).containsExactly(entry("p1", 3));
        });
    }

    @Test
    void testAliasWithoutIgnore() {
        SqlTable table = SqlTable.of("foo");
        SqlColumn<Integer> column = table.column("id", JDBCType.INTEGER);
        IsEqualTo<Integer> condition = IsEqualTo.of(3);
        ColumnAndConditionCriterion<Integer> criterion = ColumnAndConditionCriterion.withColumn(column)
                .withCondition(condition)
                .build();

        Map<SqlTable, String> tableAliases = new HashMap<>();
        tableAliases.put(table, "a");

        RenderingContext renderingContext = RenderingContext
                .withRenderingStrategy(RenderingStrategies.MYBATIS3)
                .withTableAliasCalculator(ExplicitTableAliasCalculator.of(tableAliases))
                .build();

        CriterionRenderer renderer = new CriterionRenderer(renderingContext);

        assertThat(criterion.accept(renderer)).hasValueSatisfying(rc -> {
            FragmentAndParameters fp = rc.fragmentAndParameters();
            assertThat(fp.fragment()).isEqualTo("a.id = #{parameters.p1,jdbcType=INTEGER}");
            assertThat(fp.parameters()).containsExactly(entry("p1", 3));
        });
    }
}
