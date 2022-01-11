/*
 *    Copyright 2016-2022 the original author or authors.
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
package examples.kotlin.mybatis3.general

import examples.kotlin.mybatis3.general.FooDynamicSqlSupport.A
import examples.kotlin.mybatis3.general.FooDynamicSqlSupport.B
import examples.kotlin.mybatis3.general.FooDynamicSqlSupport.C
import examples.kotlin.mybatis3.general.FooDynamicSqlSupport.foo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.util.kotlin.elements.exists
import org.mybatis.dynamic.sql.util.kotlin.elements.group
import org.mybatis.dynamic.sql.util.kotlin.elements.isEqualTo
import org.mybatis.dynamic.sql.util.kotlin.elements.isGreaterThan
import org.mybatis.dynamic.sql.util.kotlin.elements.isLessThan
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.select

object FooDynamicSqlSupport {
    class Foo : SqlTable("Foo") {
        var A = column<Int>("A")
        var B = column<Int>("B")
        var C = column<Int>("C")
    }

    val foo = Foo()
    val A = foo.A
    val B = foo.B
    val C = foo.C
}

class KGroupingTest {
    @Test
    fun testSimpleGrouping() {
        val selectStatement = select(A, B, C) {
            from(foo)
            where (A, isEqualTo(1)) {
                or(A, isEqualTo(2))
            }
            and(B, isEqualTo(3))
        }

        val expected = "select A, B, C" +
                " from Foo" +
                " where (A = #{parameters.p1} or A = #{parameters.p2}) and B = #{parameters.p3}"

        assertThat(selectStatement.selectStatement).isEqualTo(expected)
        assertThat(selectStatement.parameters).containsEntry("p1", 1)
        assertThat(selectStatement.parameters).containsEntry("p2", 2)
        assertThat(selectStatement.parameters).containsEntry("p3", 3)
    }

    @Test
    fun testComplexGrouping() {
        val selectStatement = select(A, B, C) {
            from(foo)
            where(group(A, isEqualTo(1)) {
                or(A, isGreaterThan(5))
            }) {
                and(B, isEqualTo(1))
                or(A, isLessThan(0)) {
                    and(B, isEqualTo(2))
                }
            }
            and(C, isEqualTo(1))
        }

        val expected = "select A, B, C" +
                " from Foo" +
                " where ((A = #{parameters.p1} or A > #{parameters.p2}) and B = #{parameters.p3} or (A < #{parameters.p4} and B = #{parameters.p5})) and C = #{parameters.p6}"

        assertThat(selectStatement.selectStatement).isEqualTo(expected)
        assertThat(selectStatement.parameters).containsEntry("p1", 1)
        assertThat(selectStatement.parameters).containsEntry("p2", 5)
        assertThat(selectStatement.parameters).containsEntry("p3", 1)
        assertThat(selectStatement.parameters).containsEntry("p4", 0)
        assertThat(selectStatement.parameters).containsEntry("p5", 2)
        assertThat(selectStatement.parameters).containsEntry("p6", 1)
    }

    @Test
    fun testGroupAndExists() {
        val selectStatement = select(A, B, C) {
            from(foo)
            where(group(exists {
                select(foo.allColumns()) {
                    from (foo)
                    where(A, isEqualTo(3))
                }
            }) {
                and(A, isEqualTo((1)))
                or(A, isGreaterThan(5))
            }) {
                and(B, isEqualTo(1))
                or(A, isLessThan(0)) {
                    and(B, isEqualTo(2))
                }
            }
            and(C, isEqualTo(1))
        }

        val expected = "select A, B, C" +
                " from Foo" +
                " where ((exists (select * from Foo where A = #{parameters.p1}) and A = #{parameters.p2} or A > #{parameters.p3}) and B = #{parameters.p4} or (A < #{parameters.p5} and B = #{parameters.p6})) and C = #{parameters.p7}"
        assertThat(selectStatement.selectStatement).isEqualTo(expected)
        assertThat(selectStatement.parameters).containsEntry("p1", 3)
        assertThat(selectStatement.parameters).containsEntry("p2", 1)
        assertThat(selectStatement.parameters).containsEntry("p3", 5)
        assertThat(selectStatement.parameters).containsEntry("p4", 1)
        assertThat(selectStatement.parameters).containsEntry("p5", 0)
        assertThat(selectStatement.parameters).containsEntry("p6", 2)
        assertThat(selectStatement.parameters).containsEntry("p7", 1)
    }

    @Test
    fun testNestedGrouping() {
        val selectStatement = select(A, B, C) {
            from(foo)
            where(
                group(group(A, isEqualTo(1)) {
                    or(A, isGreaterThan(5))
                }) {
                    and(A, isGreaterThan(5))
                }
            ) {
                and(group(A, isEqualTo(1)) {
                    or(A, isGreaterThan(5))
                }) {
                    or(B, isEqualTo(1))
                }
                or(group(A, isEqualTo(1)) {
                    or(A, isGreaterThan(5))
                }) {
                    and(A, isLessThan(0)) {
                        and(B, isEqualTo(2))
                    }
                }
            }
            and(C, isEqualTo(1))
        }

        val expected = "select A, B, C" +
                " from Foo" +
                " where (((A = #{parameters.p1} or A > #{parameters.p2}) and A > #{parameters.p3}) and ((A = #{parameters.p4} or A > #{parameters.p5}) or B = #{parameters.p6}) or ((A = #{parameters.p7} or A > #{parameters.p8}) and (A < #{parameters.p9} and B = #{parameters.p10}))) and C = #{parameters.p11}"

        assertThat(selectStatement.selectStatement).isEqualTo(expected)
        assertThat(selectStatement.parameters).containsEntry("p1", 1)
        assertThat(selectStatement.parameters).containsEntry("p2", 5)
        assertThat(selectStatement.parameters).containsEntry("p3", 5)
        assertThat(selectStatement.parameters).containsEntry("p4", 1)
        assertThat(selectStatement.parameters).containsEntry("p5", 5)
        assertThat(selectStatement.parameters).containsEntry("p6", 1)
        assertThat(selectStatement.parameters).containsEntry("p7", 1)
        assertThat(selectStatement.parameters).containsEntry("p8", 5)
        assertThat(selectStatement.parameters).containsEntry("p9", 0)
        assertThat(selectStatement.parameters).containsEntry("p10", 2)
        assertThat(selectStatement.parameters).containsEntry("p11", 1)
    }

    @Test
    fun testAndOrCriteriaGroups() {
        val selectStatement = select(A, B, C) {
            from(foo)
            where(A, isEqualTo(6))
            and(C, isEqualTo(1))
            and(group(A, isEqualTo(1)) {
                or(A, isGreaterThan(5))
            }) {
                or(B, isEqualTo(1))
            }
            or(group(A, isEqualTo(1)) {
                or(A, isGreaterThan(5))
            }) {
                and(A, isLessThan(0)) {
                    and(B, isEqualTo(2))
                }
            }
        }

        val expected = "select A, B, C" +
                " from Foo" +
                " where A = #{parameters.p1}" +
                " and C = #{parameters.p2}" +
                " and ((A = #{parameters.p3} or A > #{parameters.p4}) or B = #{parameters.p5})" +
                " or ((A = #{parameters.p6} or A > #{parameters.p7}) and (A < #{parameters.p8} and B = #{parameters.p9}))"

        assertThat(selectStatement.selectStatement).isEqualTo(expected)
        assertThat(selectStatement.parameters).containsEntry("p1", 6)
        assertThat(selectStatement.parameters).containsEntry("p2", 1)
        assertThat(selectStatement.parameters).containsEntry("p3", 1)
        assertThat(selectStatement.parameters).containsEntry("p4", 5)
        assertThat(selectStatement.parameters).containsEntry("p5", 1)
        assertThat(selectStatement.parameters).containsEntry("p6", 1)
        assertThat(selectStatement.parameters).containsEntry("p7", 5)
        assertThat(selectStatement.parameters).containsEntry("p8", 0)
        assertThat(selectStatement.parameters).containsEntry("p9", 2)
    }
}
