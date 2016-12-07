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
import static org.mybatis.qbe.sql.SqlConditions.and;
import static org.mybatis.qbe.sql.SqlConditions.isEqualTo;
import static org.mybatis.qbe.sql.SqlConditions.or;

import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
        AtomicInteger sequence = new AtomicInteger(1);
        List<CriterionWrapper> criteria = new ArrayList<>();

        Date currentDate = new Date();
        criteria.add(CriterionWrapper.of(SqlCriterion.of(field1, isEqualTo(currentDate)), sequence));
        criteria.add(CriterionWrapper.of(and(field2, isEqualTo(2)), sequence));
        criteria.add(CriterionWrapper.of(or(field3, isEqualTo("foo")), sequence));
        criteria.add(CriterionWrapper.of(or(field4, isEqualTo("bar")), sequence));
        criteria.add(CriterionWrapper.of(or(field5, isEqualTo(8)), sequence));
        
        WhereSupport whereSupport = criteria.parallelStream().collect(Collector.of(
                () -> new CollectorSupport(SqlField::nameIgnoringTableAlias),
                CollectorSupport::add,
                CollectorSupport::merge,
                CollectorSupport::getWhereSupport));


        // the where clause will be something like this:
        //  where field1 = {parameters.p1} and field2 = {parameters.p2} or field3 = {parameters.p3} or field4 = {parameters.p4} or field5 = {parameters.p5}
        // but the parameter numbers will vary because of the parallel stream.
        // the field names and basic logic of the where clause should always be the same, but the parameter numbers
        // are obtained by an AtomicInteger call in potentially different threads, so it is acceptable for them to vary
        
        String whereClause = whereSupport.getWhereClause();
        String[] tokens = whereClause.split(" ");
        
        // make sure the generated clause has the correct number of tokens
        assertThat(tokens.length, is(20));
        
        // make sure the basic structure of the generated clause is correct
        Pattern pattern = Pattern.compile("\\{parameters.(p[1-5])\\}");
        assertThat(tokens[0], is("where"));

        assertThat(tokens[1], is("field1"));
        assertThat(tokens[2], is("="));
        Matcher field1Matcher = pattern.matcher(tokens[3]);
        assertThat(field1Matcher.matches(), is(true));
        
        assertThat(tokens[4], is("and"));
        assertThat(tokens[5], is("field2"));
        assertThat(tokens[6], is("="));
        Matcher field2Matcher = pattern.matcher(tokens[7]);
        assertThat(field2Matcher.matches(), is(true));
        
        assertThat(tokens[8], is("or"));
        assertThat(tokens[9], is("field3"));
        assertThat(tokens[10], is("="));
        Matcher field3Matcher = pattern.matcher(tokens[11]);
        assertThat(field3Matcher.matches(), is(true));
        
        assertThat(tokens[12], is("or"));
        assertThat(tokens[13], is("field4"));
        assertThat(tokens[14], is("="));
        Matcher field4Matcher = pattern.matcher(tokens[15]);
        assertThat(field4Matcher.matches(), is(true));
        
        assertThat(tokens[16], is("or"));
        assertThat(tokens[17], is("field5"));
        assertThat(tokens[18], is("="));
        Matcher field5Matcher = pattern.matcher(tokens[19]);
        assertThat(field5Matcher.matches(), is(true));

        // check the value for field1
        String parameter = field1Matcher.group(1);
        assertThat(whereSupport.getParameters().get(parameter), is(currentDate));
        
        // check the value for field2
        parameter = field2Matcher.group(1);
        assertThat(whereSupport.getParameters().get(parameter), is(2));
        
        // check the value for field3
        parameter = field3Matcher.group(1);
        assertThat(whereSupport.getParameters().get(parameter), is("foo"));
        
        // check the value for field4
        parameter = field4Matcher.group(1);
        assertThat(whereSupport.getParameters().get(parameter), is("bar"));
        
        // check the value for field5
        parameter = field5Matcher.group(1);
        assertThat(whereSupport.getParameters().get(parameter), is(8));
    }
}
