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
package org.mybatis.qbe.sql;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.sql.JDBCType;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.mybatis.qbe.sql.where.condition.IsEqualTo;
import org.mybatis.qbe.sql.where.render.CriterionRenderer;
import org.mybatis.qbe.sql.where.render.RenderedCriterion;

public class CriterionRendererTest {

    @Test
    public void testAliasWithIgnore() {
        SqlField<Integer> field = SqlField.of("id", JDBCType.INTEGER, "a");
        
        IsEqualTo<Integer> condition = IsEqualTo.of(3);
        SqlCriterion<Integer> criterion = SqlCriterion.of(field, condition);
        AtomicInteger sequence = new AtomicInteger(1);
        CriterionRenderer<Integer> renderer = CriterionRenderer.of(criterion, sequence, SqlField::nameIgnoringTableAlias);
        
        RenderedCriterion rc = renderer.render();
        assertThat(rc.whereClauseFragment(), is(" id = ?"));
        assertThat(rc.fragmentParameters().size(), is(1));
    }

    @Test
    public void testAliasWithoutIgnore() {
        SqlField<Integer> field = SqlField.of("id", JDBCType.INTEGER, "a");
        IsEqualTo<Integer> condition = IsEqualTo.of(3);
        SqlCriterion<Integer> criterion = SqlCriterion.of(field, condition);
        AtomicInteger sequence = new AtomicInteger(1);
        CriterionRenderer<Integer> renderer = CriterionRenderer.of(criterion, sequence, SqlField::nameIncludingTableAlias);
        
        RenderedCriterion rc = renderer.render();
        assertThat(rc.whereClauseFragment(), is(" a.id = ?"));
        assertThat(rc.fragmentParameters().size(), is(1));
    }

    @Test
    public void testNoAliasWithIgnore() {
        SqlField<Integer> field = SqlField.of("id", JDBCType.INTEGER);
        IsEqualTo<Integer> condition = IsEqualTo.of(3);
        SqlCriterion<Integer> criterion = SqlCriterion.of(field, condition);
        AtomicInteger sequence = new AtomicInteger(1);
        CriterionRenderer<Integer> renderer = CriterionRenderer.of(criterion, sequence, SqlField::nameIgnoringTableAlias);
        
        RenderedCriterion rc = renderer.render();
        assertThat(rc.whereClauseFragment(), is(" id = ?"));
        assertThat(rc.fragmentParameters().size(), is(1));
    }

    @Test
    public void testNoAliasWithoutIgnore() {
        SqlField<Integer> field = SqlField.of("id", JDBCType.INTEGER);
        IsEqualTo<Integer> condition = IsEqualTo.of(3);
        SqlCriterion<Integer> criterion = SqlCriterion.of(field, condition);
        AtomicInteger sequence = new AtomicInteger(1);
        CriterionRenderer<Integer> renderer = CriterionRenderer.of(criterion, sequence, SqlField::nameIncludingTableAlias);
        
        RenderedCriterion rc = renderer.render();
        assertThat(rc.whereClauseFragment(), is(" id = ?"));
        assertThat(rc.fragmentParameters().size(), is(1));
    }
}
