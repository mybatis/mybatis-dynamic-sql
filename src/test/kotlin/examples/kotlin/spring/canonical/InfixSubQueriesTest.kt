/*
 *    Copyright 2016-2023 the original author or authors.
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

import examples.kotlin.spring.canonical.PersonDynamicSqlSupport.person
import examples.kotlin.spring.canonical.PersonDynamicSqlSupport.addressId
import examples.kotlin.spring.canonical.PersonDynamicSqlSupport.birthDate
import examples.kotlin.spring.canonical.PersonDynamicSqlSupport.employed
import examples.kotlin.spring.canonical.PersonDynamicSqlSupport.firstName
import examples.kotlin.spring.canonical.PersonDynamicSqlSupport.id
import examples.kotlin.spring.canonical.PersonDynamicSqlSupport.lastName
import examples.kotlin.spring.canonical.PersonDynamicSqlSupport.occupation
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mybatis.dynamic.sql.util.kotlin.elements.max
import org.mybatis.dynamic.sql.util.kotlin.elements.min
import org.mybatis.dynamic.sql.util.kotlin.spring.select
import org.mybatis.dynamic.sql.util.kotlin.spring.selectList
import org.mybatis.dynamic.sql.util.kotlin.spring.selectOne
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.transaction.annotation.Transactional

@Suppress("LargeClass")
@SpringJUnitConfig(classes = [SpringConfiguration::class])
@Transactional
open class InfixSubQueriesTest {
    @Autowired
    private lateinit var template: NamedParameterJdbcTemplate

    @Test
    fun testSelectEqualSubQuery() {
        val selectStatement = select(id, firstName, lastName, birthDate, employed, occupation, addressId) {
            from(person)
            where {
                id isEqualTo {
                    select(max(id)) {
                        from(person)
                    }
                }
            }
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select id, first_name, last_name, birth_date, employed, occupation, address_id " +
                "from Person where id = (select max(id) from Person)"
        )

        val row = template.selectOne(selectStatement, personRowMapper)

        assertThat(row).isNotNull
        with(row!!) {
            assertThat(id).isEqualTo(6)
            assertThat(firstName).isEqualTo("Bamm Bamm")
            assertThat(lastName!!.name).isEqualTo("Rubble")
            assertThat(birthDate).isNotNull
            assertThat(employed).isFalse
            assertThat(occupation).isNull()
            assertThat(addressId).isEqualTo(2)
        }
    }

    @Test
    fun testSelectNotEqualSubQuery() {
        val selectStatement = select(id, firstName, lastName, birthDate, employed, occupation, addressId) {
            from(person)
            where { id isNotEqualTo {
                    select(max(id)) {
                        from(person)
                    }
                }
            }
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select id, first_name, last_name, birth_date, employed, occupation, address_id " +
                "from Person where id <> (select max(id) from Person) " +
                "order by id"
        )

        val rows = template.selectList(selectStatement, personRowMapper)

        assertThat(rows).hasSize(5)
        with(rows[0]) {
            assertThat(id).isEqualTo(1)
            assertThat(firstName).isEqualTo("Fred")
            assertThat(lastName!!.name).isEqualTo("Flintstone")
            assertThat(birthDate).isNotNull
            assertThat(employed).isTrue
            assertThat(occupation).isEqualTo("Brontosaurus Operator")
            assertThat(addressId).isEqualTo(1)
        }
    }

    @Test
    fun testInSubQuery() {
        val selectStatement = select(id, firstName, lastName, birthDate, employed, occupation, addressId) {
            from(person)
            where {
                id isIn {
                    select(id) {
                        from(person)
                        where { lastName isEqualTo LastName("Rubble") }
                    }
                }
            }
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select id, first_name, last_name, birth_date, employed, occupation, address_id " +
                "from Person where id in (select id from Person where last_name = :p1) " +
                "order by id"
        )
        assertThat(selectStatement.parameters).containsEntry("p1", "Rubble")

        val rows = template.selectList(selectStatement, personRowMapper)

        assertThat(rows).hasSize(3)
        with(rows[0]) {
            assertThat(id).isEqualTo(4)
            assertThat(firstName).isEqualTo("Barney")
            assertThat(lastName!!.name).isEqualTo("Rubble")
            assertThat(birthDate).isNotNull
            assertThat(employed).isTrue
            assertThat(occupation).isEqualTo("Brontosaurus Operator")
            assertThat(addressId).isEqualTo(2)
        }
    }

    @Test
    fun testNotInSubQuery() {
        val selectStatement = select(id, firstName, lastName, birthDate, employed, occupation, addressId) {
            from(person)
            where {
                id isNotIn {
                    selectDistinct(id) {
                        from(person)
                        where { lastName isEqualTo LastName("Rubble") }
                    }
                }
            }
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select id, first_name, last_name, birth_date, employed, occupation, address_id " +
                "from Person where id not in (select distinct id from Person where last_name = :p1) " +
                "order by id"
        )
        assertThat(selectStatement.parameters).containsEntry("p1", "Rubble")

        val rows = template.selectList(selectStatement, personRowMapper)

        assertThat(rows).hasSize(3)
        with(rows[0]) {
            assertThat(id).isEqualTo(1)
            assertThat(firstName).isEqualTo("Fred")
            assertThat(lastName!!.name).isEqualTo("Flintstone")
            assertThat(birthDate).isNotNull
            assertThat(employed).isTrue
            assertThat(occupation).isEqualTo("Brontosaurus Operator")
            assertThat(addressId).isEqualTo(1)
        }
    }

    @Test
    fun testLessThanSubQuery() {
        val selectStatement = select(id, firstName, lastName, birthDate, employed, occupation, addressId) {
            from(person)
            where {
                id isLessThan {
                    select(max(id)) {
                        from(person)
                    }
                }
            }
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select id, first_name, last_name, birth_date, employed, occupation, address_id " +
                "from Person where id < (select max(id) from Person) " +
                "order by id"
        )

        val rows = template.selectList(selectStatement, personRowMapper)

        assertThat(rows).hasSize(5)
        with(rows[0]) {
            assertThat(id).isEqualTo(1)
            assertThat(firstName).isEqualTo("Fred")
            assertThat(lastName!!.name).isEqualTo("Flintstone")
            assertThat(birthDate).isNotNull
            assertThat(employed).isTrue
            assertThat(occupation).isEqualTo("Brontosaurus Operator")
            assertThat(addressId).isEqualTo(1)
        }
    }

    @Test
    fun testLessThanOrEqualSubQuery() {
        val selectStatement = select(id, firstName, lastName, birthDate, employed, occupation, addressId) {
            from(person)
            where {
                id isLessThanOrEqualTo {
                    select(max(id)) {
                        from(person)
                    }
                }
            }
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select id, first_name, last_name, birth_date, employed, occupation, address_id " +
                "from Person where id <= (select max(id) from Person) " +
                "order by id"
        )

        val rows = template.selectList(selectStatement, personRowMapper)

        assertThat(rows).hasSize(6)
        with(rows[0]) {
            assertThat(id).isEqualTo(1)
            assertThat(firstName).isEqualTo("Fred")
            assertThat(lastName!!.name).isEqualTo("Flintstone")
            assertThat(birthDate).isNotNull
            assertThat(employed).isTrue
            assertThat(occupation).isEqualTo("Brontosaurus Operator")
            assertThat(addressId).isEqualTo(1)
        }
    }

    @Test
    fun testGreaterThanSubQuery() {
        val selectStatement = select(id, firstName, lastName, birthDate, employed, occupation, addressId) {
            from(person)
            where {
                id isGreaterThan {
                    select(min(id)) {
                        from(person)
                    }
                }
            }
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select id, first_name, last_name, birth_date, employed, occupation, address_id " +
                "from Person where id > (select min(id) from Person) " +
                "order by id"
        )

        val rows = template.selectList(selectStatement, personRowMapper)

        assertThat(rows).hasSize(5)
        with(rows[0]) {
            assertThat(id).isEqualTo(2)
            assertThat(firstName).isEqualTo("Wilma")
            assertThat(lastName!!.name).isEqualTo("Flintstone")
            assertThat(birthDate).isNotNull
            assertThat(employed).isTrue
            assertThat(occupation).isEqualTo("Accountant")
            assertThat(addressId).isEqualTo(1)
        }
    }

    @Test
    fun testGreaterThanOrEqualSubQuery() {
        val selectStatement = select(id, firstName, lastName, birthDate, employed, occupation, addressId) {
            from(person)
            where {
                id isGreaterThanOrEqualTo {
                    select(min(id)) {
                        from(person)
                    }
                }
            }
            orderBy(id)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select id, first_name, last_name, birth_date, employed, occupation, address_id " +
                "from Person where id >= (select min(id) from Person) " +
                "order by id"
        )

        val rows = template.selectList(selectStatement, personRowMapper)

        assertThat(rows).hasSize(6)
        with(rows[0]) {
            assertThat(id).isEqualTo(1)
            assertThat(firstName).isEqualTo("Fred")
            assertThat(lastName!!.name).isEqualTo("Flintstone")
            assertThat(birthDate).isNotNull
            assertThat(employed).isTrue
            assertThat(occupation).isEqualTo("Brontosaurus Operator")
            assertThat(addressId).isEqualTo(1)
        }
    }
}
