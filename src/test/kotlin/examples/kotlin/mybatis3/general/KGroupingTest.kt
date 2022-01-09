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
}
