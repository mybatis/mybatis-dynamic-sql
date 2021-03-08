/*
 *    Copyright 2016-2021 the original author or authors.
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
package examples.kotlin.spring.canonical

import examples.kotlin.spring.canonical.PersonDynamicSqlSupport.Person
import examples.kotlin.spring.canonical.PersonDynamicSqlSupport.Person.firstName
import examples.kotlin.spring.canonical.PersonDynamicSqlSupport.Person.id
import examples.kotlin.spring.canonical.PersonDynamicSqlSupport.Person.lastName
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mybatis.dynamic.sql.util.kotlin.elements.applyOperator
import org.mybatis.dynamic.sql.util.kotlin.elements.avg
import org.mybatis.dynamic.sql.util.kotlin.elements.concatenate
import org.mybatis.dynamic.sql.util.kotlin.elements.constant
import org.mybatis.dynamic.sql.util.kotlin.elements.count
import org.mybatis.dynamic.sql.util.kotlin.elements.countDistinct
import org.mybatis.dynamic.sql.util.kotlin.elements.divide
import org.mybatis.dynamic.sql.util.kotlin.elements.isBetween
import org.mybatis.dynamic.sql.util.kotlin.elements.isBetweenWhenPresent
import org.mybatis.dynamic.sql.util.kotlin.elements.isEqualTo
import org.mybatis.dynamic.sql.util.kotlin.elements.isEqualToWhenPresent
import org.mybatis.dynamic.sql.util.kotlin.elements.isGreaterThanOrEqualToWhenPresent
import org.mybatis.dynamic.sql.util.kotlin.elements.isGreaterThanWhenPresent
import org.mybatis.dynamic.sql.util.kotlin.elements.isInCaseInsensitive
import org.mybatis.dynamic.sql.util.kotlin.elements.isInCaseInsensitiveWhenPresent
import org.mybatis.dynamic.sql.util.kotlin.elements.isInWhenPresent
import org.mybatis.dynamic.sql.util.kotlin.elements.isLessThanOrEqualTo
import org.mybatis.dynamic.sql.util.kotlin.elements.isLessThanOrEqualToWhenPresent
import org.mybatis.dynamic.sql.util.kotlin.elements.isLessThanWhenPresent
import org.mybatis.dynamic.sql.util.kotlin.elements.isLikeCaseInsensitive
import org.mybatis.dynamic.sql.util.kotlin.elements.isLikeCaseInsensitiveWhenPresent
import org.mybatis.dynamic.sql.util.kotlin.elements.isLikeWhenPresent
import org.mybatis.dynamic.sql.util.kotlin.elements.isNotBetween
import org.mybatis.dynamic.sql.util.kotlin.elements.isNotBetweenWhenPresent
import org.mybatis.dynamic.sql.util.kotlin.elements.isNotEqualToWhenPresent
import org.mybatis.dynamic.sql.util.kotlin.elements.isNotIn
import org.mybatis.dynamic.sql.util.kotlin.elements.isNotInCaseInsensitive
import org.mybatis.dynamic.sql.util.kotlin.elements.isNotInCaseInsensitiveWhenPresent
import org.mybatis.dynamic.sql.util.kotlin.elements.isNotInWhenPresent
import org.mybatis.dynamic.sql.util.kotlin.elements.isNotLikeCaseInsensitive
import org.mybatis.dynamic.sql.util.kotlin.elements.isNotLikeCaseInsensitiveWhenPresent
import org.mybatis.dynamic.sql.util.kotlin.elements.isNotLikeWhenPresent
import org.mybatis.dynamic.sql.util.kotlin.elements.lower
import org.mybatis.dynamic.sql.util.kotlin.elements.multiply
import org.mybatis.dynamic.sql.util.kotlin.elements.stringConstant
import org.mybatis.dynamic.sql.util.kotlin.elements.substring
import org.mybatis.dynamic.sql.util.kotlin.elements.subtract
import org.mybatis.dynamic.sql.util.kotlin.elements.sum
import org.mybatis.dynamic.sql.util.kotlin.elements.upper
import org.mybatis.dynamic.sql.util.kotlin.spring.select
import org.mybatis.dynamic.sql.util.kotlin.spring.selectList
import org.mybatis.dynamic.sql.util.kotlin.spring.selectOne
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType

@Suppress("LargeClass", "MaxLineLength")
class KotlinElementsTest {
    private lateinit var template: NamedParameterJdbcTemplate

    @BeforeEach
    fun setup() {
        val db = with(EmbeddedDatabaseBuilder()) {
            setType(EmbeddedDatabaseType.HSQL)
            generateUniqueName(true)
            addScript("classpath:/examples/kotlin/spring/CreateGeneratedAlwaysDB.sql")
            addScript("classpath:/examples/kotlin/spring/CreateSimpleDB.sql")
            build()
        }
        template = NamedParameterJdbcTemplate(db)
    }

    @Test
    fun testCount() {
        val selectStatement = select(count(id)) {
            from(Person)
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
            from(Person)
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
            from(Person)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select avg(id) from Person"
        )

        val value = template.selectOne(selectStatement, Double::class)

        assertThat(value).isEqualTo(3.0)
    }

    @Test
    fun testNull() {
        val selectStatement = select(id) {
            from(Person)
            where(id, isEqualTo(55))
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
            from(Person)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select sum(id) from Person"
        )

        val value = template.selectOne(selectStatement, Double::class)

        assertThat(value).isEqualTo(21.0)
    }

    @Test
    fun testStringConstant() {
        val selectStatement = select(firstName) {
            from(Person)
            where(firstName, isEqualTo(stringConstant("Fred")))
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select first_name from Person where first_name = 'Fred'"
        )

        val value = template.selectOne(selectStatement, String::class)

        assertThat(value).isEqualTo("Fred")
    }

    @Test
    fun testDivide() {
        val selectStatement = select(divide(id, constant<Int>("2.0"))) {
            from(Person)
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
            from(Person)
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
            from(Person)
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
    fun testConcatenate() {
        val selectStatement = select(concatenate(firstName, stringConstant(" "), lastName)) {
            from(Person)
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
            from(Person)
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
            from(Person)
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
    fun testUpper() {
        val selectStatement = select(upper(firstName)) {
            from(Person)
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select upper(first_name) from Person order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(6)
        assertThat(rows[5]).isEqualTo("BAMM BAMM")
    }

    @Test
    fun testSubstring() {
        val selectStatement = select(substring(firstName, 1, 3)) {
            from(Person)
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
    fun testIsEqualToWhenPresent() {
        val selectStatement = select(firstName) {
            from(Person)
            where(id, isEqualToWhenPresent(6))
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select first_name from Person where id = :p1 order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(1)
        assertThat(rows[0]).isEqualTo("Bamm Bamm")
    }

    @Test
    fun testIsEqualToWhenPresentNull() {
        val selectStatement = select(firstName) {
            from(Person)
            where(id, isEqualToWhenPresent(null))
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select first_name from Person order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(6)
        assertThat(rows[5]).isEqualTo("Bamm Bamm")
    }

    @Test
    fun testIsNotEqualToWhenPresent() {
        val selectStatement = select(firstName) {
            from(Person)
            where(id, isNotEqualToWhenPresent(6))
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select first_name from Person where id <> :p1 order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(5)
        assertThat(rows[0]).isEqualTo("Fred")
    }

    @Test
    fun testIsNotEqualToWhenPresentNull() {
        val selectStatement = select(firstName) {
            from(Person)
            where(id, isNotEqualToWhenPresent(null))
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select first_name from Person order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(6)
        assertThat(rows[5]).isEqualTo("Bamm Bamm")
    }

    @Test
    fun testIsGreaterThanWhenPresent() {
        val selectStatement = select(firstName) {
            from(Person)
            where(id, isGreaterThanWhenPresent(5))
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select first_name from Person where id > :p1 order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(1)
        assertThat(rows[0]).isEqualTo("Bamm Bamm")
    }

    @Test
    fun testIsGreaterThanWhenPresentNull() {
        val selectStatement = select(firstName) {
            from(Person)
            where(id, isGreaterThanWhenPresent(null))
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select first_name from Person order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(6)
        assertThat(rows[5]).isEqualTo("Bamm Bamm")
    }

    @Test
    fun testIsGreaterThanOrEqualToWhenPresent() {
        val selectStatement = select(firstName) {
            from(Person)
            where(id, isGreaterThanOrEqualToWhenPresent(5))
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select first_name from Person where id >= :p1 order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(2)
        assertThat(rows[0]).isEqualTo("Betty")
    }

    @Test
    fun testIsGreaterThanOrEqualToWhenPresentNull() {
        val selectStatement = select(firstName) {
            from(Person)
            where(id, isGreaterThanOrEqualToWhenPresent(null))
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select first_name from Person order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(6)
        assertThat(rows[5]).isEqualTo("Bamm Bamm")
    }

    @Test
    fun testIsLessThanWhenPresent() {
        val selectStatement = select(firstName) {
            from(Person)
            where(id, isLessThanWhenPresent(5))
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select first_name from Person where id < :p1 order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(4)
        assertThat(rows[0]).isEqualTo("Fred")
    }

    @Test
    fun testIsLessThanWhenPresentNull() {
        val selectStatement = select(firstName) {
            from(Person)
            where(id, isGreaterThanWhenPresent(null))
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select first_name from Person order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(6)
        assertThat(rows[5]).isEqualTo("Bamm Bamm")
    }

    @Test
    fun testIsLessThanOrEqualTo() {
        val selectStatement = select(firstName) {
            from(Person)
            where(id, isLessThanOrEqualTo(5))
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select first_name from Person where id <= :p1 order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(5)
        assertThat(rows[0]).isEqualTo("Fred")
    }

    @Test
    fun testIsLessThanOrEqualToWhenPresent() {
        val selectStatement = select(firstName) {
            from(Person)
            where(id, isLessThanOrEqualToWhenPresent(5))
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select first_name from Person where id <= :p1 order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(5)
        assertThat(rows[0]).isEqualTo("Fred")
    }

    @Test
    fun testIsLessThanOrEqualToWhenPresentNull() {
        val selectStatement = select(firstName) {
            from(Person)
            where(id, isLessThanOrEqualToWhenPresent(null))
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select first_name from Person order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(6)
        assertThat(rows[5]).isEqualTo("Bamm Bamm")
    }

    @Test
    fun testIsInWhenPresent() {
        val selectStatement = select(firstName) {
            from(Person)
            where(id, isInWhenPresent(1, null, 3))
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
    fun testIsInWhenPresentWithList() {
        val myList = mutableListOf<Int?>()
        myList.add(1)
        myList.add(null)
        myList.add(3)

        val selectStatement = select(firstName) {
            from(Person)
            where(id, isInWhenPresent(myList))
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
            from(Person)
            where(id, isNotInWhenPresent(1, null, 3))
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
    fun testIsNotInWithList() {
        val myList = mutableListOf<Int>()
        myList.add(1)
        myList.add(3)

        val selectStatement = select(firstName) {
            from(Person)
            where(id, isNotIn(myList))
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
            from(Person)
            where(id, isNotIn(1, 3))
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
    fun testIsNotInWhenPresentWithList() {
        val myList = mutableListOf<Int?>()
        myList.add(1)
        myList.add(null)
        myList.add(3)

        val selectStatement = select(firstName) {
            from(Person)
            where(id, isNotInWhenPresent(myList))
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
    fun testBetween() {
        val selectStatement = select(firstName) {
            from(Person)
            where(id, isBetween(2).and(3))
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
    fun testBetweenWhenPresentBothPresent() {
        val selectStatement = select(firstName) {
            from(Person)
            where(id, isBetweenWhenPresent(2).and(3))
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
            from(Person)
            where(id, isBetweenWhenPresent<Int>(null).and(3))
            orderBy(id)
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
            from(Person)
            where(id, isBetweenWhenPresent(2).and(null))
            orderBy(id)
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
            from(Person)
            where(id, isBetweenWhenPresent<Int>(null).and(null))
            orderBy(id)
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
            from(Person)
            where(id, isNotBetween(2).and(3))
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
            from(Person)
            where(id, isNotBetweenWhenPresent(2).and(3))
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
            from(Person)
            where(id, isNotBetweenWhenPresent<Int>(null).and(3))
            orderBy(id)
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
            from(Person)
            where(id, isNotBetweenWhenPresent(2).and(null))
            orderBy(id)
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
            from(Person)
            where(id, isNotBetweenWhenPresent<Int>(null).and(null))
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select first_name from Person order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(6)
        assertThat(rows[0]).isEqualTo("Fred")
    }

    @Test
    fun testIsLikeWhenPresent() {
        val selectStatement = select(firstName) {
            from(Person)
            where(firstName, isLikeWhenPresent("F%"))
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select first_name from Person where first_name like :p1 order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(1)
        assertThat(rows[0]).isEqualTo("Fred")
    }

    @Test
    fun testIsLikeWhenPresentNull() {
        val selectStatement = select(firstName) {
            from(Person)
            where(firstName, isLikeWhenPresent(null))
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select first_name from Person order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(6)
        assertThat(rows[0]).isEqualTo("Fred")
    }

    @Test
    fun testIsNotLikeWhenPresent() {
        val selectStatement = select(firstName) {
            from(Person)
            where(firstName, isNotLikeWhenPresent("F%"))
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select first_name from Person where first_name not like :p1 order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(5)
        assertThat(rows[0]).isEqualTo("Wilma")
    }

    @Test
    fun testIsNotLikeWhenPresentNull() {
        val selectStatement = select(firstName) {
            from(Person)
            where(firstName, isNotLikeWhenPresent(null))
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select first_name from Person order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(6)
        assertThat(rows[0]).isEqualTo("Fred")
    }

    @Test
    fun testIsLikeCaseInsensitive() {
        val selectStatement = select(firstName) {
            from(Person)
            where(firstName, isLikeCaseInsensitive("f%"))
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select first_name from Person where upper(first_name) like :p1 order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(1)
        assertThat(rows[0]).isEqualTo("Fred")
    }

    @Test
    fun testIsLikeCaseInsensitiveWhenPresent() {
        val selectStatement = select(firstName) {
            from(Person)
            where(firstName, isLikeCaseInsensitiveWhenPresent("f%"))
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select first_name from Person where upper(first_name) like :p1 order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(1)
        assertThat(rows[0]).isEqualTo("Fred")
    }

    @Test
    fun testIsLikeCaseInsensitiveWhenPresentNull() {
        val selectStatement = select(firstName) {
            from(Person)
            where(firstName, isLikeCaseInsensitiveWhenPresent(null))
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select first_name from Person order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(6)
        assertThat(rows[0]).isEqualTo("Fred")
    }

    @Test
    fun testIsNotLikeCaseInsensitive() {
        val selectStatement = select(firstName) {
            from(Person)
            where(firstName, isNotLikeCaseInsensitive("f%"))
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select first_name from Person where upper(first_name) not like :p1 order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(5)
        assertThat(rows[0]).isEqualTo("Wilma")
    }

    @Test
    fun testIsNotLikeCaseInsensitiveWhenPresent() {
        val selectStatement = select(firstName) {
            from(Person)
            where(firstName, isNotLikeCaseInsensitiveWhenPresent("f%"))
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select first_name from Person where upper(first_name) not like :p1 order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(5)
        assertThat(rows[0]).isEqualTo("Wilma")
    }

    @Test
    fun testIsNotLikeCaseInsensitiveWhenPresentNull() {
        val selectStatement = select(firstName) {
            from(Person)
            where(firstName, isNotLikeCaseInsensitiveWhenPresent(null))
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select first_name from Person order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(6)
        assertThat(rows[0]).isEqualTo("Fred")
    }

    @Test
    fun testIsInCaseInsensitive() {
        val selectStatement = select(firstName) {
            from(Person)
            where(firstName, isInCaseInsensitive("FRED", "wilma"))
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
            from(Person)
            where(firstName, isInCaseInsensitiveWhenPresent("FRED", null, "wilma"))
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
            from(Person)
            where(firstName, isInCaseInsensitiveWhenPresent(null, null))
            orderBy(id)
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
            from(Person)
            where(firstName, isNotInCaseInsensitive("FRED", "wilma"))
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
            from(Person)
            where(firstName, isNotInCaseInsensitiveWhenPresent("FRED", null, "wilma"))
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
            from(Person)
            where(firstName, isNotInCaseInsensitiveWhenPresent(null, null))
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select first_name from Person order by id"
        )

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(6)
        assertThat(rows[0]).isEqualTo("Fred")
    }
}
