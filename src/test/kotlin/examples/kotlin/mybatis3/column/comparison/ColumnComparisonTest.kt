/*
 *    Copyright 2016-2024 the original author or authors.
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
package examples.kotlin.mybatis3.column.comparison

import examples.kotlin.mybatis3.column.comparison.ColumnComparisonDynamicSqlSupport.columnComparison
import examples.kotlin.mybatis3.column.comparison.ColumnComparisonDynamicSqlSupport.number1
import examples.kotlin.mybatis3.column.comparison.ColumnComparisonDynamicSqlSupport.number2
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mybatis.dynamic.sql.util.kotlin.elements.sortColumn
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.select
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig

@SpringJUnitConfig(classes = [ColumnComparisonConfiguration::class])
class ColumnComparisonTest {

    @Autowired
    private lateinit var mapper: ColumnComparisonMapper

    @Test
    fun testColumnComparisonLessThan() {
        val selectStatement = select(number1, number2) {
            from(columnComparison)
            where { number1 isLessThan number2 }
            orderBy(number1, number2)
        }

        val expected = "select number1, number2 " +
            "from ColumnComparison " +
            "where number1 < number2 " +
            "order by number1, number2"

        val records = mapper.selectMany(selectStatement)
        assertThat(selectStatement.selectStatement).isEqualTo(expected)
        assertThat(records).hasSize(5)
        assertThat(records[0].number1).isEqualTo(1)
        assertThat(records[4].number1).isEqualTo(5)
    }

    @Test
    fun testColumnComparisonLessThanOrEqual() {
        val selectStatement = select(number1, number2) {
            from(columnComparison)
            where { number1 isLessThanOrEqualTo number2 }
            orderBy(number1, number2)
        }

        val expected = "select number1, number2 " +
            "from ColumnComparison " +
            "where number1 <= number2 " +
            "order by number1, number2"

        val records = mapper.selectMany(selectStatement)
        assertThat(selectStatement.selectStatement).isEqualTo(expected)
        assertThat(records).hasSize(6)
        assertThat(records[0].number1).isEqualTo(1)
        assertThat(records[5].number1).isEqualTo(6)
    }

    @Test
    fun testColumnComparisonGreaterThan() {
        val selectStatement = select(number1, number2) {
            from(columnComparison)
            where { number1 isGreaterThan number2 }
            orderBy(number1, number2)
        }

        val expected = "select number1, number2 " +
            "from ColumnComparison " +
            "where number1 > number2 " +
            "order by number1, number2"

        val records = mapper.selectMany(selectStatement)
        assertThat(selectStatement.selectStatement).isEqualTo(expected)
        assertThat(records).hasSize(5)
        assertThat(records[0].number1).isEqualTo(7)
        assertThat(records[4].number1).isEqualTo(11)
    }

    @Test
    fun testColumnComparisonGreaterThanOrEqual() {
        val selectStatement = select(number1, number2) {
            from(columnComparison)
            where { number1 isGreaterThanOrEqualTo number2 }
            orderBy(number1, number2)
        }

        val expected = "select number1, number2 " +
            "from ColumnComparison " +
            "where number1 >= number2 " +
            "order by number1, number2"

        val records = mapper.selectMany(selectStatement)
        assertThat(selectStatement.selectStatement).isEqualTo(expected)
        assertThat(records).hasSize(6)
        assertThat(records[0].number1).isEqualTo(6)
        assertThat(records[5].number1).isEqualTo(11)
    }

    @Test
    fun testColumnComparisonEqual() {
        val selectStatement = select(number1, number2) {
            from(columnComparison)
            where { number1 isEqualTo number2 }
            orderBy(number1, number2)
        }

        val expected = "select number1, number2 " +
            "from ColumnComparison " +
            "where number1 = number2 " +
            "order by number1, number2"

        val records = mapper.selectMany(selectStatement)
        assertThat(selectStatement.selectStatement).isEqualTo(expected)
        assertThat(records).hasSize(1)
        assertThat(records[0].number1).isEqualTo(6)
    }

    @Test
    fun testColumnComparisonNotEqual() {
        val selectStatement = select(number1, number2) {
            from(columnComparison, "a")
            where { number1 isNotEqualTo number2 }
            orderBy(sortColumn("a", number1), sortColumn("a", number2))
        }

        val expected = "select a.number1, a.number2 " +
            "from ColumnComparison a " +
            "where a.number1 <> a.number2 " +
            "order by a.number1, a.number2"

        val records = mapper.selectMany(selectStatement)
        assertThat(selectStatement.selectStatement).isEqualTo(expected)
        assertThat(records).hasSize(10)
        assertThat(records[0].number1).isEqualTo(1)
        assertThat(records[9].number1).isEqualTo(11)
    }

    @Test
    fun testHelperMethod() {
        val records = mapper.select {
            where { number1 isNotEqualTo number2 }
            orderBy(number1, number2)
        }

        assertThat(records).hasSize(10)
        assertThat(records[0].number1).isEqualTo(1)
        assertThat(records[9].number1).isEqualTo(11)
    }
}
