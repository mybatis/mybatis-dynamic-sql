package examples.kotlin.spring.canonical

import examples.kotlin.mybatis3.general.FooDynamicSqlSupport.foo
import examples.kotlin.mybatis3.general.FooDynamicSqlSupport.A
import examples.kotlin.mybatis3.general.FooDynamicSqlSupport.B
import org.junit.jupiter.api.Test
import org.mybatis.dynamic.sql.AndOrCriteriaGroup
import org.mybatis.dynamic.sql.util.kotlin.spring.select
import org.assertj.core.api.Assertions.assertThat
import org.mybatis.dynamic.sql.SqlBuilder
import org.mybatis.dynamic.sql.util.kotlin.elements.isEqualTo

class KNoInitialConditionTest {

    @Test
    fun testNoInitialConditionEmptyList() {
        val conditions = listOf<AndOrCriteriaGroup>()

        val selectStatement = buildSelectStatement(conditions)

        val expected = "select A, B from Foo where A < :p1"
        assertThat(selectStatement.selectStatement).isEqualTo(expected)
    }

    @Test
    fun testNoInitialConditionSingleSub() {
        // TODO - make this Kotlin Native
        val conditions = listOf(SqlBuilder.or(B, isEqualTo(3)))

        val selectStatement = buildSelectStatement(conditions)

        val expected = "select A, B from Foo where A < :p1 and B = :p2"
        assertThat(selectStatement.selectStatement).isEqualTo(expected)
    }

    @Test
    fun testNoInitialConditionMultipleSubs() {
        // TODO - make this Kotlin Native
        val conditions = listOf(
            SqlBuilder.or(B, isEqualTo(3)),
            SqlBuilder.or(B, isEqualTo(4)),
            SqlBuilder.or(B, isEqualTo(5))
        )

        val selectStatement = buildSelectStatement(conditions)

        val expected = "select A, B from Foo where A < :p1 and (B = :p2 or B = :p3 or B = :p4)"
        assertThat(selectStatement.selectStatement).isEqualTo(expected)
    }

    private fun buildSelectStatement(conditions: List<AndOrCriteriaGroup>) =
        select(A, B) {
            from(foo)
            where {
                A isLessThan 5
            }
            and(conditions)
        }
}