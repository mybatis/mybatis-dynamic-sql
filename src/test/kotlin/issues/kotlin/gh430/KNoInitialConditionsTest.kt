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
package issues.kotlin.gh430

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mybatis.dynamic.sql.AndOrCriteriaGroup
import org.mybatis.dynamic.sql.subselect.FooDynamicSqlSupport.foo
import org.mybatis.dynamic.sql.subselect.FooDynamicSqlSupport.column1
import org.mybatis.dynamic.sql.subselect.FooDynamicSqlSupport.column2
import org.mybatis.dynamic.sql.util.kotlin.elements.and
import org.mybatis.dynamic.sql.util.kotlin.elements.or
import org.mybatis.dynamic.sql.util.kotlin.spring.select
import java.util.*

class KNoInitialConditionsTest {
    @Test
    fun testNoInitialConditionEmptyList() {
        val criteria = listOf<AndOrCriteriaGroup>()
        val selectStatement = buildSelectStatement(criteria)
        val expected = "select column1, column2 from foo where column1 < :p1"
        assertThat(selectStatement.selectStatement).isEqualTo(expected)
    }

    @Test
    fun testNoInitialConditionSingleSub() {
        val criteria = listOf(
            or { column2 isEqualTo 3 }
        )

        val selectStatement = buildSelectStatement(criteria)
        val expected = "select column1, column2 from foo where column1 < :p1 " +
                "and column2 = :p2"
        assertThat(selectStatement.selectStatement).isEqualTo(expected)
    }

    @Test
    fun testNoInitialConditionMultipleSubs() {
        val criteria = listOf(
            or { column2 isEqualTo 3 },
            or { column2 isEqualTo 4 },
            or { column2 isEqualTo 5 }
        )

        val selectStatement = buildSelectStatement(criteria)
        val expected = "select column1, column2 from foo where column1 < :p1 " +
                "and (column2 = :p2 or column2 = :p3 or column2 = :p4)"
        assertThat(selectStatement.selectStatement).isEqualTo(expected)
    }

    @Test
    fun testNoInitialConditionWhereMultipleSubs() {
        val criteria = listOf(
            or { column2 isEqualTo 3 },
            or { column2 isEqualTo 4 },
            or { column2 isEqualTo 5 }
        )

        val selectStatement = select(column1, column2) {
            from(foo)
            where(criteria)
        }

        val expected = "select column1, column2 from foo where " +
                "column2 = :p1 or column2 = :p2 or column2 = :p3"
        assertThat(selectStatement.selectStatement).isEqualTo(expected)
    }

    @Test
    fun testNoInitialConditionWhereNotMultipleSubs() {
        val criteria = listOf(
            or { column2 isEqualTo 3 },
            or { column2 isEqualTo 4 },
            or { column2 isEqualTo 5 }
        )

        val selectStatement = select(column1, column2) {
            from(foo)
            where {
                not(criteria)
                and { column1 isLessThan Date() }
            }
        }

        val expected = "select column1, column2 from foo where not " +
                "(column2 = :p1 or column2 = :p2 or column2 = :p3) " +
                "and column1 < :p4"
        assertThat(selectStatement.selectStatement).isEqualTo(expected)
    }

    @Test
    fun testNoInitialConditionWhereGroupMultipleSubs() {
        val criteria = listOf(
            or { column2 isEqualTo 3 },
            or { column2 isEqualTo 4 },
            or { column2 isEqualTo 5 }
        )

        val selectStatement = select(column1, column2) {
            from(foo)
            where {
                group(criteria)
                and { column1 isLessThan Date() }
            }
        }

        val expected = "select column1, column2 from foo where " +
                "(column2 = :p1 or column2 = :p2 or column2 = :p3) " +
                "and column1 < :p4"
        assertThat(selectStatement.selectStatement).isEqualTo(expected)
    }

    @Test
    fun testNoInitialConditionWhereCCAndMultipleSubs() {
        val criteria = listOf(
            or { column2 isEqualTo 3 },
            or { column2 isEqualTo 4 },
            or { column2 isEqualTo 5 }
        )

        val selectStatement = select(column1, column2) {
            from(foo)
            where {
                column1 isLessThan Date()
                and(criteria)
            }
        }

        val expected = "select column1, column2 from foo where " +
                "column1 < :p1 and (column2 = :p2 or column2 = :p3 or column2 = :p4)"
        assertThat(selectStatement.selectStatement).isEqualTo(expected)
    }

    @Test
    fun testNoInitialConditionWhereCCOrMultipleSubs() {
        val criteria = listOf(
            or { column2 isEqualTo 3 },
            or { column2 isEqualTo 4 },
            or { column2 isEqualTo 5 }
        )

        val selectStatement = select(column1, column2) {
            from(foo)
            where {
                column1 isLessThan Date()
                or(criteria)
            }
        }

        val expected = "select column1, column2 from foo where " +
                "column1 < :p1 or (column2 = :p2 or column2 = :p3 or column2 = :p4)"
        assertThat(selectStatement.selectStatement).isEqualTo(expected)
    }

    @Test
    fun testNoInitialConditionWhereOrMultipleSubs() {
        val criteria = listOf(
            and { column2 isEqualTo 3 },
            and { column2 isEqualTo 4 },
            and { column2 isEqualTo 5 }
        )

        val selectStatement = select(column1, column2) {
            from(foo)
            where {
                column1 isLessThan Date()
                or(criteria)
            }
        }

        val expected = "select column1, column2 from foo where column1 < :p1 " +
                "or (column2 = :p2 and column2 = :p3 and column2 = :p4)"
        assertThat(selectStatement.selectStatement).isEqualTo(expected)
    }

    private fun buildSelectStatement(criteria: List<AndOrCriteriaGroup>) =
        select(column1, column2) {
            from(foo)
            where {
                column1 isLessThan Date()
                and(criteria)
            }
        }
}
