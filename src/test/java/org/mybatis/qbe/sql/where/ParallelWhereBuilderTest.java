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
package org.mybatis.qbe.sql.where;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mybatis.qbe.sql.SqlConditions.*;

import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collector;

import org.junit.Test;
import org.mybatis.qbe.sql.SqlCriterion;
import org.mybatis.qbe.sql.SqlField;
import org.mybatis.qbe.sql.SqlTable;
import org.mybatis.qbe.sql.where.AbstractWhereBuilder.CollectorSupport;
import org.mybatis.qbe.sql.where.AbstractWhereBuilder.CriterionWrapper;

public class ParallelWhereBuilderTest {
    public static final SqlTable table = SqlTable.of("foo").withAlias("a");
    public static final SqlField<Date> field1 = SqlField.of("field1", JDBCType.DATE).inTable(table);
    public static final SqlField<Integer> field2 = SqlField.of("field2", JDBCType.INTEGER).inTable(table);
    public static final SqlField<String> field3 = SqlField.of("field3", JDBCType.VARCHAR).inTable(table);
    public static final SqlField<String> field4 = SqlField.of("field4", JDBCType.VARCHAR).inTable(table);
    public static final SqlField<Integer> field5 = SqlField.of("field5", JDBCType.INTEGER).inTable(table);

    @Test
    public void testParallelStream() {
        int idStartValue = 1;
        List<CriterionWrapper> criteria = new ArrayList<>();

        Date currentDate = new Date();
        CriterionWrapper wrapper = CriterionWrapper.of(SqlCriterion.of(field1, isEqualTo(currentDate)), idStartValue);
        idStartValue += wrapper.criterion.valueCount();
        criteria.add(wrapper);
        
        wrapper = CriterionWrapper.of(and(field2, isEqualTo(2)), idStartValue);
        idStartValue += wrapper.criterion.valueCount();
        criteria.add(wrapper);
        
        wrapper = CriterionWrapper.of(or(field3, isEqualTo("foo")), idStartValue);
        idStartValue += wrapper.criterion.valueCount();
        criteria.add(wrapper);

        wrapper = CriterionWrapper.of(or(field4, isEqualTo("bar")), idStartValue);
        idStartValue += wrapper.criterion.valueCount();
        criteria.add(wrapper);

        wrapper = CriterionWrapper.of(or(field5, isEqualTo(8)), idStartValue);
        idStartValue += wrapper.criterion.valueCount();
        criteria.add(wrapper);
        
        WhereSupport whereSupport = criteria.parallelStream().collect(Collector.of(
                () -> new CollectorSupport(SqlField::nameIgnoringTableAlias),
                CollectorSupport::add,
                CollectorSupport::merge,
                CollectorSupport::getWhereSupport));

        String expected = "where field1 = {parameters.p1}"
                + " and field2 = {parameters.p2}"
                + " or field3 = {parameters.p3}"
                + " or field4 = {parameters.p4}"
                + " or field5 = {parameters.p5}";
        
        assertThat(whereSupport.getWhereClause(), is(expected));
        assertThat(whereSupport.getParameters().size(), is(5));
        assertThat(whereSupport.getParameters().get("p1"), is(currentDate));
        assertThat(whereSupport.getParameters().get("p2"), is(2));
        assertThat(whereSupport.getParameters().get("p3"), is("foo"));
        assertThat(whereSupport.getParameters().get("p4"), is("bar"));
        assertThat(whereSupport.getParameters().get("p5"), is(8));
    }

    @Test
    public void testParallelStreamComplex() {
        int idStartValue = 1;
        List<CriterionWrapper> criteria = new ArrayList<>();

        Date currentDate = new Date();
        CriterionWrapper wrapper = CriterionWrapper.of(SqlCriterion.of(field1, isEqualTo(currentDate)), idStartValue);
        idStartValue += wrapper.criterion.valueCount();
        criteria.add(wrapper);
        
        wrapper = CriterionWrapper.of(and(field2, isEqualTo(2), and(field1, isNull())), idStartValue);
        idStartValue += wrapper.criterion.valueCount();
        criteria.add(wrapper);
        
        wrapper = CriterionWrapper.of(or(field3, isEqualTo("foo"), or(field2, isIn(2, 3, 4))), idStartValue);
        idStartValue += wrapper.criterion.valueCount();
        criteria.add(wrapper);

        wrapper = CriterionWrapper.of(or(field4, isEqualTo("bar")), idStartValue);
        idStartValue += wrapper.criterion.valueCount();
        criteria.add(wrapper);

        wrapper = CriterionWrapper.of(or(field5, isEqualTo(8)), idStartValue);
        idStartValue += wrapper.criterion.valueCount();
        criteria.add(wrapper);
        
        WhereSupport whereSupport = criteria.parallelStream().collect(Collector.of(
                () -> new CollectorSupport(SqlField::nameIgnoringTableAlias),
                CollectorSupport::add,
                CollectorSupport::merge,
                CollectorSupport::getWhereSupport));

        String expected = "where field1 = {parameters.p1}"
                + " and (field2 = {parameters.p2} and field1 is null)"
                + " or (field3 = {parameters.p3} or field2 in ({parameters.p4},{parameters.p5},{parameters.p6}))"
                + " or field4 = {parameters.p7}"
                + " or field5 = {parameters.p8}";
        
        assertThat(whereSupport.getWhereClause(), is(expected));
        assertThat(whereSupport.getParameters().size(), is(8));
        assertThat(whereSupport.getParameters().get("p1"), is(currentDate));
        assertThat(whereSupport.getParameters().get("p2"), is(2));
        assertThat(whereSupport.getParameters().get("p3"), is("foo"));
        assertThat(whereSupport.getParameters().get("p4"), is(2));
        assertThat(whereSupport.getParameters().get("p5"), is(3));
        assertThat(whereSupport.getParameters().get("p6"), is(4));
        assertThat(whereSupport.getParameters().get("p7"), is("bar"));
        assertThat(whereSupport.getParameters().get("p8"), is(8));
    }
}
