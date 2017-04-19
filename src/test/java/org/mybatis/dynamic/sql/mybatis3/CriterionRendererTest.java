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
package org.mybatis.dynamic.sql.mybatis3;

import java.sql.JDBCType;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.mybatis.dynamic.sql.MyBatis3Column;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.util.FragmentAndParameters;
import org.mybatis.dynamic.sql.where.CriterionRenderer;
import org.mybatis.dynamic.sql.where.condition.IsEqualTo;

public class CriterionRendererTest {
    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Test
    public void testAliasWithIgnore() {
        SqlTable table = SqlTable.of("foo").withAlias("a");
        MyBatis3Column<Integer> column = MyBatis3Column.of("id", JDBCType.INTEGER).inTable(table);
        
        IsEqualTo<Integer> condition = IsEqualTo.of(3);
        SqlCriterion<Integer> criterion = SqlCriterion.of(column, condition);
        AtomicInteger sequence = new AtomicInteger(1);
        CriterionRenderer renderer = CriterionRenderer.newRendererIgnoringTableAlias(sequence);
        
        FragmentAndParameters fp = renderer.render(criterion);
        softly.assertThat(fp.fragment()).isEqualTo("id = #{parameters.p1,jdbcType=INTEGER}");
        softly.assertThat(fp.parameters().size()).isEqualTo(1);
    }

    @Test
    public void testAliasWithoutIgnore() {
        SqlTable table = SqlTable.of("foo").withAlias("a");
        MyBatis3Column<Integer> column = MyBatis3Column.of("id", JDBCType.INTEGER).inTable(table);
        IsEqualTo<Integer> condition = IsEqualTo.of(3);
        SqlCriterion<Integer> criterion = SqlCriterion.of(column, condition);
        AtomicInteger sequence = new AtomicInteger(1);
        CriterionRenderer renderer = CriterionRenderer.newRendererIncludingTableAlias(sequence);
        
        FragmentAndParameters fp = renderer.render(criterion);
        softly.assertThat(fp.fragment()).isEqualTo("a.id = #{parameters.p1,jdbcType=INTEGER}");
        softly.assertThat(fp.parameters().size()).isEqualTo(1);
    }

    @Test
    public void testNoAliasWithIgnore() {
        MyBatis3Column<Integer> column = MyBatis3Column.of("id", JDBCType.INTEGER);
        IsEqualTo<Integer> condition = IsEqualTo.of(3);
        SqlCriterion<Integer> criterion = SqlCriterion.of(column, condition);
        AtomicInteger sequence = new AtomicInteger(1);
        CriterionRenderer renderer = CriterionRenderer.newRendererIgnoringTableAlias(sequence);
        
        FragmentAndParameters fp = renderer.render(criterion);
        softly.assertThat(fp.fragment()).isEqualTo("id = #{parameters.p1,jdbcType=INTEGER}");
        softly.assertThat(fp.parameters().size()).isEqualTo(1);
    }

    @Test
    public void testNoAliasWithoutIgnore() {
        MyBatis3Column<Integer> column = MyBatis3Column.of("id", JDBCType.INTEGER);
        IsEqualTo<Integer> condition = IsEqualTo.of(3);
        SqlCriterion<Integer> criterion = SqlCriterion.of(column, condition);
        AtomicInteger sequence = new AtomicInteger(1);
        CriterionRenderer renderer = CriterionRenderer.newRendererIncludingTableAlias(sequence);
        
        FragmentAndParameters fp = renderer.render(criterion);
        softly.assertThat(fp.fragment()).isEqualTo("id = #{parameters.p1,jdbcType=INTEGER}");
        softly.assertThat(fp.parameters().size()).isEqualTo(1);
    }

    @Test
    public void testTypeHandler() {
        MyBatis3Column<Date> column = MyBatis3Column.of("id", JDBCType.DATE).withTypeHandler("foo.Bar");
        IsEqualTo<Date> condition = IsEqualTo.of(new Date());
        SqlCriterion<Date> criterion = SqlCriterion.of(column, condition);
        AtomicInteger sequence = new AtomicInteger(1);
        CriterionRenderer renderer = CriterionRenderer.newRendererIgnoringTableAlias(sequence);
        
        FragmentAndParameters fp = renderer.render(criterion);
        softly.assertThat(fp.fragment()).isEqualTo("id = #{parameters.p1,jdbcType=DATE,typeHandler=foo.Bar}");
        softly.assertThat(fp.parameters().size()).isEqualTo(1);
    }

    @Test
    public void testTypeHandlerAndAlias() {
        SqlTable table = SqlTable.of("foo").withAlias("a");
        MyBatis3Column<Integer> column = MyBatis3Column.of("id", JDBCType.INTEGER).withTypeHandler("foo.Bar").inTable(table);
        IsEqualTo<Integer> condition = IsEqualTo.of(3);
        SqlCriterion<Integer> criterion = SqlCriterion.of(column, condition);
        AtomicInteger sequence = new AtomicInteger(1);
        CriterionRenderer renderer = CriterionRenderer.newRendererIncludingTableAlias(sequence);
        
        FragmentAndParameters rc = renderer.render(criterion);
        softly.assertThat(rc.fragment()).isEqualTo("a.id = #{parameters.p1,jdbcType=INTEGER,typeHandler=foo.Bar}");
        softly.assertThat(rc.parameters().size()).isEqualTo(1);
    }
}
