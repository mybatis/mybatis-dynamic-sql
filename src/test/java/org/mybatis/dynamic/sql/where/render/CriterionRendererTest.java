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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.sql.JDBCType;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.where.condition.IsEqualTo;
import org.mybatis.dynamic.sql.where.render.CriterionRenderer;
import org.mybatis.dynamic.sql.where.render.RenderedCriterion;

public class CriterionRendererTest {

    @Test
    public void testAliasWithIgnore() {
        SqlTable table = SqlTable.of("foo").withAlias("a");
        SqlColumn<Integer> column = SqlColumn.of("id", JDBCType.INTEGER).inTable(table);
        
        IsEqualTo<Integer> condition = IsEqualTo.of(3);
        SqlCriterion<Integer> criterion = SqlCriterion.of(column, condition);
        AtomicInteger sequence = new AtomicInteger(1);
        CriterionRenderer<Integer> renderer = CriterionRenderer.of(criterion, sequence, SqlColumn::name);
        
        RenderedCriterion rc = renderer.render();
        assertThat(rc.whereClauseFragment(), is(" id = {parameters.p1}"));
        assertThat(rc.fragmentParameters().size(), is(1));
        assertThat(rc.fragmentParameters().get("p1"), is(3));
    }

    @Test
    public void testAliasWithoutIgnore() {
        SqlTable table = SqlTable.of("foo").withAlias("a");
        SqlColumn<Integer> column = SqlColumn.of("id", JDBCType.INTEGER).inTable(table);
        IsEqualTo<Integer> condition = IsEqualTo.of(3);
        SqlCriterion<Integer> criterion = SqlCriterion.of(column, condition);
        AtomicInteger sequence = new AtomicInteger(1);
        CriterionRenderer<Integer> renderer = CriterionRenderer.of(criterion, sequence, SqlColumn::nameIncludingTableAlias);
        
        RenderedCriterion rc = renderer.render();
        assertThat(rc.whereClauseFragment(), is(" a.id = {parameters.p1}"));
        assertThat(rc.fragmentParameters().size(), is(1));
        assertThat(rc.fragmentParameters().get("p1"), is(3));
    }

    @Test
    public void testNoAliasWithIgnore() {
        SqlColumn<Integer> column = SqlColumn.of("id", JDBCType.INTEGER);
        IsEqualTo<Integer> condition = IsEqualTo.of(3);
        SqlCriterion<Integer> criterion = SqlCriterion.of(column, condition);
        AtomicInteger sequence = new AtomicInteger(1);
        CriterionRenderer<Integer> renderer = CriterionRenderer.of(criterion, sequence, SqlColumn::name);
        
        RenderedCriterion rc = renderer.render();
        assertThat(rc.whereClauseFragment(), is(" id = {parameters.p1}"));
        assertThat(rc.fragmentParameters().size(), is(1));
        assertThat(rc.fragmentParameters().get("p1"), is(3));
    }

    @Test
    public void testNoAliasWithoutIgnore() {
        SqlColumn<Integer> column = SqlColumn.of("id", JDBCType.INTEGER);
        IsEqualTo<Integer> condition = IsEqualTo.of(3);
        SqlCriterion<Integer> criterion = SqlCriterion.of(column, condition);
        AtomicInteger sequence = new AtomicInteger(1);
        CriterionRenderer<Integer> renderer = CriterionRenderer.of(criterion, sequence, SqlColumn::nameIncludingTableAlias);
        
        RenderedCriterion rc = renderer.render();
        assertThat(rc.whereClauseFragment(), is(" id = {parameters.p1}"));
        assertThat(rc.fragmentParameters().size(), is(1));
        assertThat(rc.fragmentParameters().get("p1"), is(3));
    }
}
