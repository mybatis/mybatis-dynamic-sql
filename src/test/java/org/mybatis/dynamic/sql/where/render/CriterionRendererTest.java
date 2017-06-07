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
package org.mybatis.dynamic.sql.where.render;

import static org.assertj.core.api.Assertions.*;

import java.sql.JDBCType;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.render.RenderingUtilities;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.where.condition.IsEqualTo;

public class CriterionRendererTest {

    @Test
    public void testAliasWithIgnore() {
        SqlTable table = SqlTable.of("foo").withAlias("a");
        SqlColumn<Integer> column = SqlColumn.of("id", JDBCType.INTEGER).inTable(table);
        
        IsEqualTo<Integer> condition = IsEqualTo.of(3);
        SqlCriterion<Integer> criterion = new SqlCriterion.Builder<Integer>()
                .withColumn(column)
                .withCondition(condition)
                .build();
        AtomicInteger sequence = new AtomicInteger(1);
        CriterionRenderer renderer = CriterionRenderer.of(sequence, RenderingStrategy.MYBATIS3, SqlColumn::name);
        
        FragmentAndParameters fp = renderer.render(criterion);
        assertThat(fp.fragment()).isEqualTo("id = #{parameters.p1,jdbcType=INTEGER}");
        assertThat(fp.parameters().size()).isEqualTo(1);
        assertThat(fp.parameters().get("p1")).isEqualTo(3);
    }

    @Test
    public void testAliasWithoutIgnore() {
        SqlTable table = SqlTable.of("foo").withAlias("a");
        SqlColumn<Integer> column = SqlColumn.of("id", JDBCType.INTEGER).inTable(table);
        IsEqualTo<Integer> condition = IsEqualTo.of(3);
        SqlCriterion<Integer> criterion = new SqlCriterion.Builder<Integer>()
                .withColumn(column)
                .withCondition(condition)
                .build();
        AtomicInteger sequence = new AtomicInteger(1);
        CriterionRenderer renderer = CriterionRenderer.of(sequence, RenderingStrategy.MYBATIS3, RenderingUtilities::nameIncludingTableAlias);
        
        FragmentAndParameters fp = renderer.render(criterion);
        assertThat(fp.fragment()).isEqualTo("a.id = #{parameters.p1,jdbcType=INTEGER}");
        assertThat(fp.parameters().size()).isEqualTo(1);
        assertThat(fp.parameters().get("p1")).isEqualTo(3);
    }

    @Test
    public void testNoAliasWithIgnore() {
        SqlColumn<Integer> column = SqlColumn.of("id", JDBCType.INTEGER);
        IsEqualTo<Integer> condition = IsEqualTo.of(3);
        SqlCriterion<Integer> criterion = new SqlCriterion.Builder<Integer>()
                .withColumn(column)
                .withCondition(condition)
                .build();
        AtomicInteger sequence = new AtomicInteger(1);
        CriterionRenderer renderer = CriterionRenderer.of(sequence, RenderingStrategy.MYBATIS3, SqlColumn::name);
        
        FragmentAndParameters fp = renderer.render(criterion);
        assertThat(fp.fragment()).isEqualTo("id = #{parameters.p1,jdbcType=INTEGER}");
        assertThat(fp.parameters().size()).isEqualTo(1);
        assertThat(fp.parameters().get("p1")).isEqualTo(3);
    }

    @Test
    public void testNoAliasWithoutIgnore() {
        SqlColumn<Integer> column = SqlColumn.of("id", JDBCType.INTEGER);
        IsEqualTo<Integer> condition = IsEqualTo.of(3);
        SqlCriterion<Integer> criterion = new SqlCriterion.Builder<Integer>()
                .withColumn(column)
                .withCondition(condition)
                .build();
        AtomicInteger sequence = new AtomicInteger(1);
        CriterionRenderer renderer = CriterionRenderer.of(sequence, RenderingStrategy.MYBATIS3, RenderingUtilities::nameIncludingTableAlias);
        
        FragmentAndParameters fp = renderer.render(criterion);
        assertThat(fp.fragment()).isEqualTo("id = #{parameters.p1,jdbcType=INTEGER}");
        assertThat(fp.parameters().size()).isEqualTo(1);
        assertThat(fp.parameters().get("p1")).isEqualTo(3);
    }
}
