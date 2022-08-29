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

import examples.kotlin.spring.canonical.PersonDynamicSqlSupport.addressId
import examples.kotlin.spring.canonical.PersonDynamicSqlSupport.employed
import examples.kotlin.spring.canonical.PersonDynamicSqlSupport.person
import examples.kotlin.spring.canonical.PersonDynamicSqlSupport.firstName
import examples.kotlin.spring.canonical.PersonDynamicSqlSupport.id
import examples.kotlin.spring.canonical.PersonDynamicSqlSupport.lastName
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import org.mybatis.dynamic.sql.util.Messages
import org.mybatis.dynamic.sql.util.kotlin.KInvalidSQLException
import org.mybatis.dynamic.sql.util.kotlin.elements.isLike
import org.mybatis.dynamic.sql.util.kotlin.elements.stringConstant
import org.mybatis.dynamic.sql.util.kotlin.elements.upper
import org.mybatis.dynamic.sql.util.kotlin.spring.countFrom
import org.mybatis.dynamic.sql.util.kotlin.spring.delete
import org.mybatis.dynamic.sql.util.kotlin.spring.deleteFrom
import org.mybatis.dynamic.sql.util.kotlin.spring.select
import org.mybatis.dynamic.sql.util.kotlin.spring.selectList
import org.mybatis.dynamic.sql.util.kotlin.spring.selectOne
import org.mybatis.dynamic.sql.util.kotlin.spring.update
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.transaction.annotation.Transactional

@Suppress("LargeClass")
@SpringJUnitConfig(SpringConfiguration::class)
@Transactional
open class InfixElementsTest {
    @Autowired
    private lateinit var template: NamedParameterJdbcTemplate

    @Test
    fun testNull() {
        val selectStatement = select(id) {
            from(person)
            where { id.isNull() }
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select id from Person where id is null"
        )

        val value = template.selectList(selectStatement, Int::class)

        assertThat(value).isEmpty()
    }

    @Test
    fun testStringConstant() {
        val selectStatement = select(firstName) {
            from(person)
            where { firstName isEqualTo stringConstant("Fred") }
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select first_name from Person where first_name = 'Fred'"
        )

        val value = template.selectOne(selectStatement, String::class)

        assertThat(value).isEqualTo("Fred")
    }

    @Test
    fun testIsEqualToWhenPresent() {
        val selectStatement = select(firstName) {
            from(person)
            where { id isEqualToWhenPresent 6 }
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
            from(person)
            where { id isEqualToWhenPresent null }
            orderBy(id)
            configureStatement { isNonRenderingWhereClauseAllowed = true }
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
            from(person)
            where { id isNotEqualToWhenPresent 6 }
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
    fun testDeleteIsNotEqualToWhenPresentNull() {
        val deleteStatement = deleteFrom(person) {
            where { id isNotEqualToWhenPresent null }
            configureStatement { isNonRenderingWhereClauseAllowed = true }
        }

        assertThat(deleteStatement.deleteStatement).isEqualTo(
            "delete from Person"
        )

        val rows = template.delete(deleteStatement)

        assertThat(rows).isEqualTo(6)
    }

    @Test
    fun testIsNotEqualToWhenPresentNull() {
        val selectStatement = select(firstName) {
            from(person)
            where { id isNotEqualToWhenPresent null }
            orderBy(id)
            configureStatement { isNonRenderingWhereClauseAllowed = true }
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
            from(person)
            where { id isGreaterThanWhenPresent 5 }
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
            from(person)
            where { id isGreaterThanWhenPresent null }
            orderBy(id)
            configureStatement { isNonRenderingWhereClauseAllowed = true }
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
            from(person)
            where { id isGreaterThanOrEqualToWhenPresent 5 }
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
            from(person)
            where { id isGreaterThanOrEqualToWhenPresent null }
            orderBy(id)
            configureStatement { isNonRenderingWhereClauseAllowed = true }
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
            from(person)
            where { id isLessThanWhenPresent 5 }
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
            from(person)
            where { id isGreaterThanWhenPresent null }
            orderBy(id)
            configureStatement { isNonRenderingWhereClauseAllowed = true }
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
            from(person)
            where { id isLessThanOrEqualTo 5 }
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
            from(person)
            where { id isLessThanOrEqualToWhenPresent 5 }
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
            from(person)
            where { id isLessThanOrEqualToWhenPresent null }
            orderBy(id)
            configureStatement { isNonRenderingWhereClauseAllowed = true }
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
            from(person)
            where { id.isInWhenPresent(1, null, 3) }
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
        val myList = listOf(1, null, 3)

        val selectStatement = select(firstName) {
            from(person)
            where { id isInWhenPresent myList }
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
            where { id.isNotInWhenPresent(1, null, 3) }
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
        val myList = listOf(1, 3)

        val selectStatement = select(firstName) {
            from(person)
            where { id isNotIn myList }
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
            where { id.isNotIn(1, 3) }
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
        val myList = listOf(1, null, 3)

        val selectStatement = select(firstName) {
            from(person)
            where { id isNotInWhenPresent myList }
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
            from(person)
            where { id isBetween 2 and 3 }
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
            from(person)
            where { id isBetweenWhenPresent 2 and 3 }
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
            where { id isBetweenWhenPresent null and 3 }
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
            where { id isBetweenWhenPresent 2 and null }
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
            where { id isBetweenWhenPresent null and null }
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
            where { id isNotBetween 2 and 3 }
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
            where { id isNotBetweenWhenPresent 2 and 3 }
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
            where { id isNotBetweenWhenPresent null and 3 }
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
            where { id isNotBetweenWhenPresent 2 and null }
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
            where { id isNotBetweenWhenPresent null and null }
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
    fun testIsLikeWhenPresent() {
        val selectStatement = select(firstName) {
            from(person)
            where { firstName isLikeWhenPresent "F%" }
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
            from(person)
            where { firstName isLikeWhenPresent null }
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
    fun testIsNotLike() {
        val selectStatement = select(firstName) {
            from(person)
            where { firstName  isNotLike "F%" }
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
    fun testIsNotLikeWhenPresent() {
        val selectStatement = select(firstName) {
            from(person)
            where { firstName  isNotLikeWhenPresent "F%" }
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
            from(person)
            where { firstName isNotLikeWhenPresent null }
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
    fun testIsLikeCaseInsensitive() {
        val selectStatement = select(firstName) {
            from(person)
            where { firstName isLikeCaseInsensitive "f%" }
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
            from(person)
            where { firstName isLikeCaseInsensitiveWhenPresent "f%" }
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
            from(person)
            where { firstName isLikeCaseInsensitiveWhenPresent null }
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
    fun testIsNotLikeCaseInsensitive() {
        val selectStatement = select(firstName) {
            from(person)
            where { firstName isNotLikeCaseInsensitive "f%" }
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
            from(person)
            where { firstName isNotLikeCaseInsensitiveWhenPresent "f%" }
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
            from(person)
            where { firstName isNotLikeCaseInsensitiveWhenPresent null }
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
    fun testIsInCaseInsensitive() {
        val selectStatement = select(firstName) {
            from(person)
            where { firstName.isInCaseInsensitive("FRED", "wilma") }
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
            where { firstName.isInCaseInsensitiveWhenPresent("FRED", null, "wilma") }
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
            where { firstName.isInCaseInsensitiveWhenPresent(null, null) }
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
            where { firstName.isNotInCaseInsensitive("FRED", "wilma") }
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
            where { firstName.isNotInCaseInsensitiveWhenPresent("FRED", null, "wilma") }
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
            where { firstName.isNotInCaseInsensitiveWhenPresent(null, null) }
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
    fun testSearchWhenThenBlank() {
        val fn = ""

        val selectStatement = select(firstName) {
            from(person)
            where {
                upper(firstName) (
                isLike(fn).filter(String::isNotBlank)
                    .map(String::uppercase)
                    .map { "%$it%" })
            }
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
    fun testSearchWhenThenNotBlank() {
        val fn = "w"

        val selectStatement = select(firstName) {
            from(person)
            where {
                upper(firstName) (isLike(fn).filter(String::isNotBlank)
                    .map(String::uppercase)
                    .map { "%$it%" })
            }
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement)
            .isEqualTo("select first_name from Person where upper(first_name) like :p1 order by id")
        assertThat(selectStatement.parameters).containsEntry("p1", "%W%")

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(1)
        assertThat(rows[0]).isEqualTo("Wilma")
    }

    @Test
    fun testThatTwoInitialCriteriaThrowsException() {
        assertThatExceptionOfType(KInvalidSQLException::class.java).isThrownBy {
            select(lastName) {
                from(person)
                where {
                    firstName isEqualTo "Fred"
                    firstName isEqualTo "Betty"
                }
            }
        }.withMessage(Messages.getString("ERROR.21")) //$NON-NLS-1$
    }

    @Test
    fun testNotNull() {
        val selectStatement = select(firstName) {
            from(person)
            where { firstName.isNotNull() }
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement)
            .isEqualTo("select first_name from Person where first_name is not null order by id")

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(6)
        assertThat(rows[0]).isEqualTo("Fred")
    }

    @Test
    fun testGreaterThan() {
        val selectStatement = select(firstName) {
            from(person)
            where { id isGreaterThan 3 }
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement)
            .isEqualTo("select first_name from Person where id > :p1 order by id")

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(3)
        assertThat(rows[0]).isEqualTo("Barney")
    }

    @Test
    fun testNotEqualTo() {
        val selectStatement = select(firstName) {
            from(person)
            where { id isNotEqualTo 3 }
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement)
            .isEqualTo("select first_name from Person where id <> :p1 order by id")

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(5)
        assertThat(rows[0]).isEqualTo("Fred")
    }

    @Test
    fun testGreaterThanColumn() {
        val selectStatement = select(firstName) {
            from(person)
            where { id isGreaterThan addressId }
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement)
            .isEqualTo("select first_name from Person where id > address_id order by id")

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(5)
        assertThat(rows[0]).isEqualTo("Wilma")
    }

    @Test
    fun testNotEqualToColumn() {
        val selectStatement = select(firstName) {
            from(person)
            where { id isNotEqualTo addressId }
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement)
            .isEqualTo("select first_name from Person where id <> address_id order by id")

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(5)
        assertThat(rows[0]).isEqualTo("Wilma")
    }

    @Test
    fun testLessThanColumn() {
        val selectStatement = select(firstName) {
            from(person)
            where { id isLessThan addressId }
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement)
            .isEqualTo("select first_name from Person where id < address_id order by id")

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(0)
    }

    @Test
    fun testLessThanOrEqualToColumn() {
        val selectStatement = select(firstName) {
            from(person)
            where { id isLessThanOrEqualTo addressId }
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement)
            .isEqualTo("select first_name from Person where id <= address_id order by id")

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(1)
        assertThat(rows[0]).isEqualTo("Fred")
    }

    @Test
    fun testGreaterThanOrEqual() {
        val selectStatement = select(firstName) {
            from(person)
            where { id isGreaterThanOrEqualTo 3 }
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement)
            .isEqualTo("select first_name from Person where id >= :p1 order by id")

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(4)
        assertThat(rows[0]).isEqualTo("Pebbles")
    }

    @Test
    fun testGreaterThanOrEqualToColumn() {
        val selectStatement = select(firstName) {
            from(person)
            where { id isGreaterThanOrEqualTo addressId }
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement)
            .isEqualTo("select first_name from Person where id >= address_id order by id")

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(6)
        assertThat(rows[0]).isEqualTo("Fred")
    }

    @Test
    fun testIsInVarArgs() {
        val selectStatement = select(firstName) {
            from(person)
            where { id.isIn(1, 2) }
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement)
            .isEqualTo("select first_name from Person where id in (:p1,:p2) order by id")

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(2)
        assertThat(rows[0]).isEqualTo("Fred")
    }

    @Test
    fun testIsInList() {
        val ids = listOf(1, 2)

        val selectStatement = select(firstName) {
            from(person)
            where { id isIn ids }
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement)
            .isEqualTo("select first_name from Person where id in (:p1,:p2) order by id")

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(2)
        assertThat(rows[0]).isEqualTo("Fred")
    }

    @Test
    fun testIsTrue() {
        val selectStatement = select(firstName) {
            from(person)
            where { employed.isTrue() }
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement)
            .isEqualTo("select first_name from Person where employed = :p1 order by id")

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(4)
        assertThat(rows[0]).isEqualTo("Fred")
    }

    @Test
    fun testIsFalse() {
        val selectStatement = select(firstName) {
            from(person)
            where { employed.isFalse() }
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement)
            .isEqualTo("select first_name from Person where employed = :p1 order by id")

        val rows = template.selectList(selectStatement, String::class)

        assertThat(rows).hasSize(2)
        assertThat(rows[0]).isEqualTo("Pebbles")
    }

    @Test
    fun testUpdate() {
        val updateStatement = update(person) {
            set(id) equalTo 1
            // following should have no impact - where clause not specified
            configureStatement { isNonRenderingWhereClauseAllowed = false }
        }

        assertThat(updateStatement.updateStatement).isEqualTo("update Person set id = :p1")
    }

    @Test
    fun testCount() {
        val selectStatement = countFrom(person) {
            // following should have no impact - where clause not specified
            configureStatement { isNonRenderingWhereClauseAllowed = false }
        }

        assertThat(selectStatement.selectStatement).isEqualTo("select count(*) from Person")
    }

    @Test
    fun testQueryExpression() {
        val selectStatement = select(id) {
            from(person)
            // following should have no impact - where clause not specified
            configureStatement { isNonRenderingWhereClauseAllowed = false }
        }

        assertThat(selectStatement.selectStatement).isEqualTo("select id from Person")
    }
}
