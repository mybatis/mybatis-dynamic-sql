/*
 *    Copyright 2016-2022 the original author or authors.
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
package examples.kotlin.spring.canonical

import examples.kotlin.spring.canonical.PersonDynamicSqlSupport.employed
import examples.kotlin.spring.canonical.PersonDynamicSqlSupport.person
import examples.kotlin.spring.canonical.PersonDynamicSqlSupport.firstName
import examples.kotlin.spring.canonical.PersonDynamicSqlSupport.id
import examples.kotlin.spring.canonical.PersonDynamicSqlSupport.lastName
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mybatis.dynamic.sql.util.kotlin.elements.applyOperator
import org.mybatis.dynamic.sql.util.kotlin.elements.avg
import org.mybatis.dynamic.sql.util.kotlin.elements.concat
import org.mybatis.dynamic.sql.util.kotlin.elements.concatenate
import org.mybatis.dynamic.sql.util.kotlin.elements.constant
import org.mybatis.dynamic.sql.util.kotlin.elements.count
import org.mybatis.dynamic.sql.util.kotlin.elements.countDistinct
import org.mybatis.dynamic.sql.util.kotlin.elements.divide
import org.mybatis.dynamic.sql.util.kotlin.elements.isBetweenWhenPresent
import org.mybatis.dynamic.sql.util.kotlin.elements.isEqualTo
import org.mybatis.dynamic.sql.util.kotlin.elements.isFalse
import org.mybatis.dynamic.sql.util.kotlin.elements.isInCaseInsensitive
import org.mybatis.dynamic.sql.util.kotlin.elements.isInCaseInsensitiveWhenPresent
import org.mybatis.dynamic.sql.util.kotlin.elements.isInWhenPresent
import org.mybatis.dynamic.sql.util.kotlin.elements.isNotBetween
import org.mybatis.dynamic.sql.util.kotlin.elements.isNotBetweenWhenPresent
import org.mybatis.dynamic.sql.util.kotlin.elements.isNotIn
import org.mybatis.dynamic.sql.util.kotlin.elements.isNotInCaseInsensitive
import org.mybatis.dynamic.sql.util.kotlin.elements.isNotInCaseInsensitiveWhenPresent
import org.mybatis.dynamic.sql.util.kotlin.elements.isNotInWhenPresent
import org.mybatis.dynamic.sql.util.kotlin.elements.isTrue
import org.mybatis.dynamic.sql.util.kotlin.elements.lower
import org.mybatis.dynamic.sql.util.kotlin.elements.multiply
import org.mybatis.dynamic.sql.util.kotlin.elements.stringConstant
import org.mybatis.dynamic.sql.util.kotlin.elements.substring
import org.mybatis.dynamic.sql.util.kotlin.elements.subtract
import org.mybatis.dynamic.sql.util.kotlin.elements.sum
import org.mybatis.dynamic.sql.util.kotlin.spring.select
import org.mybatis.dynamic.sql.util.kotlin.spring.selectList
import org.mybatis.dynamic.sql.util.kotlin.spring.selectOne
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.transaction.annotation.Transactional

@Suppress("LargeClass", "MaxLineLength")
@SpringJUnitConfig(SpringConfiguration::class)
@Transactional
open class KotlinElementsTest {
    @Autowired
    private lateinit var template: NamedParameterJdbcTemplate

    @Test
    fun testCount() {
        val selectStatement = select(count(id)) {
            from(person)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select count(id) from Person"
        )

        val value = template.selectOne(selectStatement, Int::class)
        assertThat(value).isEqualTo(6)
    }

    @Test
    fun testCountDistinct() {
        val selectStatement = select(countDistinct(lastName)) {
            from(person)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select count(distinct last_name) from Person"
        )

        val value = template.selectOne(selectStatement, Int::class)

        assertThat(value).isEqualTo(2)
    }

    @Test
    fun testAverage() {
        val selectStatement = select(avg(id)) {
            from(person)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select avg(id) from Person"
        )

        val value = template.selectOne(selectStatement, Double::class)

        assertThat(value).isEqualTo(3.0)
    }

    @Test
    fun testSelectOneNoResult() {
        val selectStatement = select(id) {
            from(person)
            where { id (isEqualTo(55)) }
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select id from Person where id = :p1"
        )

        val value = template.selectOne(selectStatement, Int::class)

        assertThat(value).isNull()
    }

    @Test
    fun testSum() {
        val selectStatement = select(sum(id)) {
            from(person)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select sum(id) from Person"
        )

        val value = template.selectOne(selectStatement, Double::class)

        assertThat(value).isEqualTo(21.0)
    }

    @Test
    fun testDivide() {
        val selectStatement = select(divide(id, constant<Int>("2.0"))) {
            from(person)
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select (id / 2.0) from Person order by id"
        )

        val rows = template.selectList(selectStatement, Float::class)

        assertThat(rows).hasSize(6)
        assertThat(rows[5]).isEqualTo(3.0f)
    }

    @Test
    fun testMultiply() {
        val selectStatement = select(multiply(id, constant<Int>("2.0"))) {
            from(person)
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select (id * 2.0) from Person order by id"
        )

        val rows = template.selectList(selectStatement, Float::class)

        assertThat(rows).hasSize(6)
        assertThat(rows[5]).isEqualTo(12.0f)
    }

    @Test
    fun testSubtract() {
        val selectStatement = select(subtract(id, constant<Int>("2"))) {
            from(person)
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select (id - 2) from Person order by id"
        )

        val rows = template.selectList(selectStatement, Int::class)

        assertThat(rows).hasSize(6)
        assertThat(rows[5]).isEqualTo(4)
    }

    @Test
    fun testConcat() {
        val selectStatement = select(concat(firstName, stringConstant(" "), lastName)) {
            from(person)
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select concat(first_name, ' ', last_name) from Person order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(6)
        assertThat(rows[5]).isEqualTo("Bamm Bamm Rubble")
    }

    @Test
    fun testConcatenate() {
        val selectStatement = select(concatenate(firstName, stringConstant(" "), lastName)) {
            from(person)
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select (first_name || ' ' || last_name) from Person order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(6)
        assertThat(rows[5]).isEqualTo("Bamm Bamm Rubble")
    }

    @Test
    fun testApplyOperator() {
        val selectStatement = select(applyOperator("||", firstName, stringConstant(" "), lastName)) {
            from(person)
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select (first_name || ' ' || last_name) from Person order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(6)
        assertThat(rows[5]).isEqualTo("Bamm Bamm Rubble")
    }

    @Test
    fun testLower() {
        val selectStatement = select(lower(firstName)) {
            from(person)
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select lower(first_name) from Person order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(6)
        assertThat(rows[5]).isEqualTo("bamm bamm")
    }

    @Test
    fun testSubstring() {
        val selectStatement = select(substring(firstName, 1, 3)) {
            from(person)
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select substring(first_name, 1, 3) from Person order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(6)
        assertThat(rows[5]).isEqualTo("Bam")
    }

    @Test
    fun testIsInWhenPresent() {
        val selectStatement = select(firstName) {
            from(person)
            where { id (isInWhenPresent(1, null, 3)) }
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select first_name from Person where id in (:p1,:p2) order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(2)
        assertThat(rows[0]).isEqualTo("Fred")
    }

    @Test
    fun testIsNotInWhenPresent() {
        val selectStatement = select(firstName) {
            from(person)
            where { id (isNotInWhenPresent(1, null, 3)) }
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select first_name from Person where id not in (:p1,:p2) order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(4)
        assertThat(rows[0]).isEqualTo("Wilma")
    }

    @Test
    fun testIsNotIn() {
        val selectStatement = select(firstName) {
            from(person)
            where { id (isNotIn(1, 3)) }
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select first_name from Person where id not in (:p1,:p2) order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(4)
        assertThat(rows[0]).isEqualTo("Wilma")
    }

    @Test
    fun testBetweenWhenPresentBothPresent() {
        val selectStatement = select(firstName) {
            from(person)
            where { id (isBetweenWhenPresent(2).and(3)) }
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select first_name from Person where id between :p1 and :p2 order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(2)
        assertThat(rows[0]).isEqualTo("Wilma")
    }

    @Test
    fun testBetweenWhenPresentFirstMissing() {
        val selectStatement = select(firstName) {
            from(person)
            where { id (isBetweenWhenPresent<Int>(null).and(3)) }
            orderBy(id)
            configureStatement { isNonRenderingWhereClauseAllowed = true }
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select first_name from Person order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(6)
        assertThat(rows[0]).isEqualTo("Fred")
    }

    @Test
    fun testBetweenWhenPresentSecondMissing() {
        val selectStatement = select(firstName) {
            from(person)
            where { id (isBetweenWhenPresent(2).and(null)) }
            orderBy(id)
            configureStatement { isNonRenderingWhereClauseAllowed = true }
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select first_name from Person order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(6)
        assertThat(rows[0]).isEqualTo("Fred")
    }

    @Test
    fun testBetweenWhenPresentBothMissing() {
        val selectStatement = select(firstName) {
            from(person)
            where { id (isBetweenWhenPresent<Int>(null).and(null)) }
            orderBy(id)
            configureStatement { isNonRenderingWhereClauseAllowed = true }
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select first_name from Person order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(6)
        assertThat(rows[0]).isEqualTo("Fred")
    }

    @Test
    fun testNotBetween() {
        val selectStatement = select(firstName) {
            from(person)
            where { id (isNotBetween(2).and(3)) }
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select first_name from Person where id not between :p1 and :p2 order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(4)
        assertThat(rows[0]).isEqualTo("Fred")
    }

    @Test
    fun testNotBetweenWhenPresentBothPresent() {
        val selectStatement = select(firstName) {
            from(person)
            where { id (isNotBetweenWhenPresent(2).and(3)) }
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select first_name from Person where id not between :p1 and :p2 order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(4)
        assertThat(rows[0]).isEqualTo("Fred")
    }

    @Test
    fun testNotBetweenWhenPresentFirstMissing() {
        val selectStatement = select(firstName) {
            from(person)
            where { id (isNotBetweenWhenPresent<Int>(null).and(3)) }
            orderBy(id)
            configureStatement { isNonRenderingWhereClauseAllowed = true }
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select first_name from Person order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(6)
        assertThat(rows[0]).isEqualTo("Fred")
    }

    @Test
    fun testNotBetweenWhenPresentSecondMissing() {
        val selectStatement = select(firstName) {
            from(person)
            where { id (isNotBetweenWhenPresent(2).and(null)) }
            orderBy(id)
            configureStatement { isNonRenderingWhereClauseAllowed = true }
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select first_name from Person order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(6)
        assertThat(rows[0]).isEqualTo("Fred")
    }

    @Test
    fun testNotBetweenWhenPresentBothMissing() {
        val selectStatement = select(firstName) {
            from(person)
            where { id (isNotBetweenWhenPresent<Int>(null).and(null)) }
            orderBy(id)
            configureStatement { isNonRenderingWhereClauseAllowed = true }
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select first_name from Person order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(6)
        assertThat(rows[0]).isEqualTo("Fred")
    }

    @Test
    fun testIsTrue() {
        val selectStatement = select(firstName) {
            from(person)
            where { employed (isTrue()) }
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select first_name from Person where employed = :p1 order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(4)
        assertThat(rows[0]).isEqualTo("Fred")
    }

    @Test
    fun testIsFalse() {
        val selectStatement = select(firstName) {
            from(person)
            where { employed (isFalse()) }
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select first_name from Person where employed = :p1 order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(2)
        assertThat(rows[0]).isEqualTo("Pebbles")
    }

    @Test
    fun testIsInCaseInsensitive() {
        val selectStatement = select(firstName) {
            from(person)
            where { firstName (isInCaseInsensitive("FRED", "wilma")) }
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select first_name from Person where upper(first_name) in (:p1,:p2) order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(2)
        assertThat(rows[0]).isEqualTo("Fred")
    }

    @Test
    fun testIsInCaseInsensitiveWhenPresent() {
        val selectStatement = select(firstName) {
            from(person)
            where { firstName (isInCaseInsensitiveWhenPresent("FRED", null, "wilma")) }
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select first_name from Person where upper(first_name) in (:p1,:p2) order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(2)
        assertThat(rows[0]).isEqualTo("Fred")
    }

    @Test
    fun testIsInCaseInsensitiveWhenPresentAllNull() {
        val selectStatement = select(firstName) {
            from(person)
            where { firstName (isInCaseInsensitiveWhenPresent(null, null)) }
            orderBy(id)
            configureStatement { isNonRenderingWhereClauseAllowed = true }
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select first_name from Person order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(6)
        assertThat(rows[0]).isEqualTo("Fred")
    }

    @Test
    fun testIsNotInCaseInsensitive() {
        val selectStatement = select(firstName) {
            from(person)
            where { firstName (isNotInCaseInsensitive("FRED", "wilma")) }
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select first_name from Person where upper(first_name) not in (:p1,:p2) order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(4)
        assertThat(rows[0]).isEqualTo("Pebbles")
    }

    @Test
    fun testIsNotInCaseInsensitiveWhenPresent() {
        val selectStatement = select(firstName) {
            from(person)
            where { firstName (isNotInCaseInsensitiveWhenPresent("FRED", null, "wilma")) }
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select first_name from Person where upper(first_name) not in (:p1,:p2) order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(4)
        assertThat(rows[0]).isEqualTo("Pebbles")
    }

    @Test
    fun testIsNotInCaseInsensitiveWhenPresentAllNull() {
        val selectStatement = select(firstName) {
            from(person)
            where { firstName (isNotInCaseInsensitiveWhenPresent(null, null)) }
            orderBy(id)
            configureStatement { isNonRenderingWhereClauseAllowed = true }
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select first_name from Person order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(6)
        assertThat(rows[0]).isEqualTo("Fred")
    }
}
