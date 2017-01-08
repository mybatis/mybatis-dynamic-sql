/**
 *    Copyright 2016 the original author or authors.
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
package org.mybatis.qbe.mybatis3;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.sql.JDBCType;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.mybatis.qbe.sql.SqlCriterion;
import org.mybatis.qbe.sql.SqlColumn;
import org.mybatis.qbe.sql.SqlTable;
import org.mybatis.qbe.sql.where.condition.IsEqualTo;
import org.mybatis.qbe.sql.where.render.CriterionRenderer;
import org.mybatis.qbe.sql.where.render.RenderedCriterion;

public class CriterionRendererTest {

    @Test
    public void testAliasWithIgnore() {
        SqlTable table = SqlTable.of("foo").withAlias("a");
        MyBatis3Column<Integer> column = MyBatis3Column.of("id", JDBCType.INTEGER).inTable(table);
        
        IsEqualTo<Integer> condition = IsEqualTo.of(3);
        SqlCriterion<Integer> criterion = SqlCriterion.of(column, condition);
        AtomicInteger sequence = new AtomicInteger(1);
        CriterionRenderer<Integer> renderer = CriterionRenderer.of(criterion, sequence, SqlColumn::name);
        
        RenderedCriterion rc = renderer.render();
        assertThat(rc.whereClauseFragment(), is(" id = #{parameters.p1,jdbcType=INTEGER}"));
        assertThat(rc.fragmentParameters().size(), is(1));
    }

    @Test
    public void testAliasWithoutIgnore() {
        SqlTable table = SqlTable.of("foo").withAlias("a");
        MyBatis3Column<Integer> column = MyBatis3Column.of("id", JDBCType.INTEGER).inTable(table);
        IsEqualTo<Integer> condition = IsEqualTo.of(3);
        SqlCriterion<Integer> criterion = SqlCriterion.of(column, condition);
        AtomicInteger sequence = new AtomicInteger(1);
        CriterionRenderer<Integer> renderer = CriterionRenderer.of(criterion, sequence, SqlColumn::nameIncludingTableAlias);
        
        RenderedCriterion rc = renderer.render();
        assertThat(rc.whereClauseFragment(), is(" a.id = #{parameters.p1,jdbcType=INTEGER}"));
        assertThat(rc.fragmentParameters().size(), is(1));
    }

    @Test
    public void testNoAliasWithIgnore() {
        MyBatis3Column<Integer> column = MyBatis3Column.of("id", JDBCType.INTEGER);
        IsEqualTo<Integer> condition = IsEqualTo.of(3);
        SqlCriterion<Integer> criterion = SqlCriterion.of(column, condition);
        AtomicInteger sequence = new AtomicInteger(1);
        CriterionRenderer<Integer> renderer = CriterionRenderer.of(criterion, sequence, SqlColumn::name);
        
        RenderedCriterion rc = renderer.render();
        assertThat(rc.whereClauseFragment(), is(" id = #{parameters.p1,jdbcType=INTEGER}"));
        assertThat(rc.fragmentParameters().size(), is(1));
    }

    @Test
    public void testNoAliasWithoutIgnore() {
        MyBatis3Column<Integer> column = MyBatis3Column.of("id", JDBCType.INTEGER);
        IsEqualTo<Integer> condition = IsEqualTo.of(3);
        SqlCriterion<Integer> criterion = SqlCriterion.of(column, condition);
        AtomicInteger sequence = new AtomicInteger(1);
        CriterionRenderer<Integer> renderer = CriterionRenderer.of(criterion, sequence, SqlColumn::nameIncludingTableAlias);
        
        RenderedCriterion rc = renderer.render();
        assertThat(rc.whereClauseFragment(), is(" id = #{parameters.p1,jdbcType=INTEGER}"));
        assertThat(rc.fragmentParameters().size(), is(1));
    }

    @Test
    public void testTypeHandler() {
        MyBatis3Column<Date> column = MyBatis3Column.of("id", JDBCType.DATE).withTypeHandler("foo.Bar");
        IsEqualTo<Date> condition = IsEqualTo.of(new Date());
        SqlCriterion<Date> criterion = SqlCriterion.of(column, condition);
        AtomicInteger sequence = new AtomicInteger(1);
        CriterionRenderer<Date> renderer = CriterionRenderer.of(criterion, sequence, SqlColumn::name);
        
        RenderedCriterion rc = renderer.render();
        assertThat(rc.whereClauseFragment(), is(" id = #{parameters.p1,jdbcType=DATE,typeHandler=foo.Bar}"));
        assertThat(rc.fragmentParameters().size(), is(1));
    }

    @Test
    public void testTypeHandlerAndAlias() {
        SqlTable table = SqlTable.of("foo").withAlias("a");
        MyBatis3Column<Integer> column = MyBatis3Column.of("id", JDBCType.INTEGER).withTypeHandler("foo.Bar").inTable(table);
        IsEqualTo<Integer> condition = IsEqualTo.of(3);
        SqlCriterion<Integer> criterion = SqlCriterion.of(column, condition);
        AtomicInteger sequence = new AtomicInteger(1);
        CriterionRenderer<Integer> renderer = CriterionRenderer.of(criterion, sequence, SqlColumn::nameIncludingTableAlias);
        
        RenderedCriterion rc = renderer.render();
        assertThat(rc.whereClauseFragment(), is(" a.id = #{parameters.p1,jdbcType=INTEGER,typeHandler=foo.Bar}"));
        assertThat(rc.fragmentParameters().size(), is(1));
    }
}
