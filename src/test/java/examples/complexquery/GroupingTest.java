/*
 *    Copyright 2016-2025 the original author or authors.
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
package examples.complexquery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.and;
import static org.mybatis.dynamic.sql.SqlBuilder.exists;
import static org.mybatis.dynamic.sql.SqlBuilder.group;
import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;
import static org.mybatis.dynamic.sql.SqlBuilder.isGreaterThan;
import static org.mybatis.dynamic.sql.SqlBuilder.isLessThan;
import static org.mybatis.dynamic.sql.SqlBuilder.or;
import static org.mybatis.dynamic.sql.SqlBuilder.select;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;

class GroupingTest {
    private static class Foo extends SqlTable {
        public SqlColumn<Integer> columnA = column("A");
        public SqlColumn<Integer> columnB = column("B");
        public SqlColumn<Integer> columnC = column("C");

        public Foo() {
            super("Foo");
        }
    }

    private static final Foo foo = new Foo();
    private static final SqlColumn<Integer> columnA = foo.columnA;
    private static final SqlColumn<Integer> columnB = foo.columnB;
    private static final SqlColumn<Integer> columnC = foo.columnC;

    @Test
    void testSimpleGrouping() {
        SelectStatementProvider selectStatement = select(columnA, columnB, columnC)
                .from(foo)
                .where(columnA, isEqualTo(1), or(columnA, isEqualTo(2)))
                .and(columnB, isEqualTo(3))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select A, B, C"
                + " from Foo"
                + " where (A = #{parameters.p1} or A = #{parameters.p2}) and B = #{parameters.p3}";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(selectStatement.getParameters()).containsEntry("p1", 1);
        assertThat(selectStatement.getParameters()).containsEntry("p2", 2);
        assertThat(selectStatement.getParameters()).containsEntry("p3", 3);
    }

    @Test
    void testComplexGrouping() {
        SelectStatementProvider selectStatement = select(columnA, columnB, columnC)
                .from(foo)
                .where(
                        group(columnA, isEqualTo(1), or(columnA, isGreaterThan(5))),
                        and(columnB, isEqualTo(1)),
                        or(columnA, isLessThan(0), and(columnB, isEqualTo(2)))
                )
                .and(columnC, isEqualTo(1))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select A, B, C"
                + " from Foo"
                + " where ((A = #{parameters.p1} or A > #{parameters.p2}) and B = #{parameters.p3} or (A < #{parameters.p4} and B = #{parameters.p5})) and C = #{parameters.p6}";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(selectStatement.getParameters()).containsEntry("p1", 1);
        assertThat(selectStatement.getParameters()).containsEntry("p2", 5);
        assertThat(selectStatement.getParameters()).containsEntry("p3", 1);
        assertThat(selectStatement.getParameters()).containsEntry("p4", 0);
        assertThat(selectStatement.getParameters()).containsEntry("p5", 2);
        assertThat(selectStatement.getParameters()).containsEntry("p6", 1);
    }

    @Test
    void testGroupAndExists() {
        SelectStatementProvider selectStatement = select(columnA, columnB, columnC)
                .from(foo)
                .where(
                        group(exists(select(foo.allColumns()).from(foo).where(columnA, isEqualTo(3))), and (columnA, isEqualTo(1)), or(columnA, isGreaterThan(5))),
                        and(columnB, isEqualTo(1)),
                        or(columnA, isLessThan(0), and(columnB, isEqualTo(2)))
                )
                .and(columnC, isEqualTo(1))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select A, B, C"
                + " from Foo"
                + " where ((exists (select * from Foo where A = #{parameters.p1}) and A = #{parameters.p2} or A > #{parameters.p3}) and B = #{parameters.p4} or (A < #{parameters.p5} and B = #{parameters.p6})) and C = #{parameters.p7}";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(selectStatement.getParameters()).containsEntry("p1", 3);
        assertThat(selectStatement.getParameters()).containsEntry("p2", 1);
        assertThat(selectStatement.getParameters()).containsEntry("p3", 5);
        assertThat(selectStatement.getParameters()).containsEntry("p4", 1);
        assertThat(selectStatement.getParameters()).containsEntry("p5", 0);
        assertThat(selectStatement.getParameters()).containsEntry("p6", 2);
        assertThat(selectStatement.getParameters()).containsEntry("p7", 1);
    }

    @Test
    void testNestedGrouping() {
        SelectStatementProvider selectStatement = select(columnA, columnB, columnC)
                .from(foo)
                .where(
                        group(group(columnA, isEqualTo(1), or(columnA, isGreaterThan(5))), and(columnA, isGreaterThan(5))),
                        and(group(columnA, isEqualTo(1), or(columnA, isGreaterThan(5))), or(columnB, isEqualTo(1))),
                        or(group(columnA, isEqualTo(1), or(columnA, isGreaterThan(5))), and(columnA, isLessThan(0), and(columnB, isEqualTo(2))))
                )
                .and(columnC, isEqualTo(1))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select A, B, C"
                + " from Foo"
                + " where (((A = #{parameters.p1} or A > #{parameters.p2}) and A > #{parameters.p3}) and ((A = #{parameters.p4} or A > #{parameters.p5}) or B = #{parameters.p6}) or ((A = #{parameters.p7} or A > #{parameters.p8}) and (A < #{parameters.p9} and B = #{parameters.p10}))) and C = #{parameters.p11}";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(selectStatement.getParameters()).containsEntry("p1", 1);
        assertThat(selectStatement.getParameters()).containsEntry("p2", 5);
        assertThat(selectStatement.getParameters()).containsEntry("p3", 5);
        assertThat(selectStatement.getParameters()).containsEntry("p4", 1);
        assertThat(selectStatement.getParameters()).containsEntry("p5", 5);
        assertThat(selectStatement.getParameters()).containsEntry("p6", 1);
        assertThat(selectStatement.getParameters()).containsEntry("p7", 1);
        assertThat(selectStatement.getParameters()).containsEntry("p8", 5);
        assertThat(selectStatement.getParameters()).containsEntry("p9", 0);
        assertThat(selectStatement.getParameters()).containsEntry("p10", 2);
        assertThat(selectStatement.getParameters()).containsEntry("p11", 1);
    }

    @Test
    void testAndOrCriteriaGroups() {
        SelectStatementProvider selectStatement = select(columnA, columnB, columnC)
                .from(foo)
                .where(columnA, isEqualTo(6))
                .and(columnC, isEqualTo(1))
                .and(group(columnA, isEqualTo(1), or(columnA, isGreaterThan(5))), or(columnB, isEqualTo(1)))
                .or(group(columnA, isEqualTo(1), or(columnA, isGreaterThan(5))), and(columnA, isLessThan(0), and(columnB, isEqualTo(2))))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = "select A, B, C"
                + " from Foo"
                + " where A = #{parameters.p1}"
                + " and C = #{parameters.p2}"
                + " and ((A = #{parameters.p3} or A > #{parameters.p4}) or B = #{parameters.p5})"
                + " or ((A = #{parameters.p6} or A > #{parameters.p7}) and (A < #{parameters.p8} and B = #{parameters.p9}))";

        assertThat(selectStatement.getSelectStatement()).isEqualTo(expected);
        assertThat(selectStatement.getParameters()).containsEntry("p1", 6);
        assertThat(selectStatement.getParameters()).containsEntry("p2", 1);
        assertThat(selectStatement.getParameters()).containsEntry("p3", 1);
        assertThat(selectStatement.getParameters()).containsEntry("p4", 5);
        assertThat(selectStatement.getParameters()).containsEntry("p5", 1);
        assertThat(selectStatement.getParameters()).containsEntry("p6", 1);
        assertThat(selectStatement.getParameters()).containsEntry("p7", 5);
        assertThat(selectStatement.getParameters()).containsEntry("p8", 0);
        assertThat(selectStatement.getParameters()).containsEntry("p9", 2);
    }
}
