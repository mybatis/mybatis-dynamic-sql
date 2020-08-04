/**
 *    Copyright 2016-2020 the original author or authors.
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

import examples.kotlin.spring.canonical.AddressDynamicSqlSupport.Address
import examples.kotlin.spring.canonical.PersonDynamicSqlSupport.Person
import examples.kotlin.spring.canonical.PersonDynamicSqlSupport.Person.addressId
import examples.kotlin.spring.canonical.PersonDynamicSqlSupport.Person.birthDate
import examples.kotlin.spring.canonical.PersonDynamicSqlSupport.Person.employed
import examples.kotlin.spring.canonical.PersonDynamicSqlSupport.Person.firstName
import examples.kotlin.spring.canonical.PersonDynamicSqlSupport.Person.id
import examples.kotlin.spring.canonical.PersonDynamicSqlSupport.Person.lastName
import examples.kotlin.spring.canonical.PersonDynamicSqlSupport.Person.occupation
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mybatis.dynamic.sql.SqlBuilder.*
import org.mybatis.dynamic.sql.util.kotlin.spring.*
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType
import java.util.*

class CanonicalSpringKotlinTemplateDirectTest {
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
        val rows = template.countFrom(Person) {
            where(id, isLessThan(4))
        }

        assertThat(rows).isEqualTo(3)
    }

    @Test
    fun testCountAllRows() {
        val rows = template.countFrom(Person) {
            allRows()
        }

        assertThat(rows).isEqualTo(6)
    }

    @Test
    fun testCountLastName() {
        val rows = template.count(lastName).from(Person) {
            allRows()
        }

        assertThat(rows).isEqualTo(6)
    }

    @Test
    fun testCountDistinctLastName() {
        val rows = template.countDistinct(lastName).from(Person) {
            allRows()
        }

        assertThat(rows).isEqualTo(2)
    }

    @Test
    fun testAllRows() {
        val rows = template.deleteFrom(Person) {
            allRows()
        }

        assertThat(rows).isEqualTo(6)
    }

    @Test
    fun testDelete1() {
        val rows = template.deleteFrom(Person) {
            where(id, isLessThan(4))
        }

        assertThat(rows).isEqualTo(3)
    }

    @Test
    fun testDelete2() {
        val rows = template.deleteFrom(Person) {
            where(id, isLessThan(4))
            and(occupation, isNotNull())
        }

        assertThat(rows).isEqualTo(2)
    }

    @Test
    fun testDelete3() {
        val rows = template.deleteFrom(Person) {
            where(id, isLessThan(4))
            or(occupation, isNotNull())
        }

        assertThat(rows).isEqualTo(5)
    }

    @Test
    fun testDelete4() {
        val rows = template.deleteFrom(Person) {
            where(id, isLessThan(4)) {
                or(occupation, isNotNull())
            }
            and(employed, isEqualTo(true))
        }

        assertThat(rows).isEqualTo(4)
    }

    @Test
    fun testDelete5() {
        val rows = template.deleteFrom(Person) {
            where(id, isLessThan(4))
            or(occupation, isNotNull()) {
                and(employed, isEqualTo(true))
            }
        }

        assertThat(rows).isEqualTo(5)
    }

    @Test
    fun testDelete6() {
        val rows = template.deleteFrom(Person) {
            where(id, isLessThan(4))
            and(occupation, isNotNull()) {
                and(employed, isEqualTo(true))
            }
        }

        assertThat(rows).isEqualTo(2)
    }

    @Test
    fun testDeprecatedInsert() {
        val record = PersonRecord(100, "Joe", LastName("Jones"), Date(), true, "Developer", 1)

        val rows = template.insert(record, Person) {
            map(id).toProperty("id")
            map(firstName).toProperty("firstName")
            map(lastName).toProperty("lastNameAsString")
            map(birthDate).toProperty("birthDate")
            map(employed).toProperty("employedAsString")
            map(occupation).toPropertyWhenPresent("occupation", record::occupation)
            map(addressId).toProperty("addressId")
        }

        assertThat(rows).isEqualTo(1)
    }

    @Test
    fun testInsert() {
        val record = PersonRecord(100, "Joe", LastName("Jones"), Date(), true, "Developer", 1)

        val rows = template.insert(record).into(Person) {
            map(id).toProperty("id")
            map(firstName).toProperty("firstName")
            map(lastName).toProperty("lastNameAsString")
            map(birthDate).toProperty("birthDate")
            map(employed).toProperty("employedAsString")
            map(occupation).toPropertyWhenPresent("occupation", record::occupation)
            map(addressId).toProperty("addressId")
        }

        assertThat(rows).isEqualTo(1)
    }

    @Test
    fun testGeneralInsert() {
        val rows = template.insertInto(Person) {
            set(id).toValue(100)
            set(firstName).toValue("Joe")
            set(lastName).toValue(LastName("Jones"))
            set(birthDate).toValue(Date())
            set(employed).toValue(true)
            set(occupation).toValue("Developer")
            set(addressId).toValue(1)
        }

        assertThat(rows).isEqualTo(1)
    }

    @Test
    fun testMultiRowInsert() {
        val record1 = PersonRecord(100, "Joe", LastName("Jones"), Date(), true, "Developer", 1)
        val record2 = PersonRecord(101, "Sarah", LastName("Smith"), Date(), true, "Architect", 2)

        val rows = template.insertMultiple(record1, record2).into(Person) {
            map(id).toProperty("id")
            map(firstName).toProperty("firstName")
            map(lastName).toProperty("lastNameAsString")
            map(birthDate).toProperty("birthDate")
            map(employed).toProperty("employedAsString")
            map(occupation).toProperty("occupation")
            map(addressId).toProperty("addressId")
        }

        assertThat(rows).isEqualTo(2)
    }

    fun testBatchInsert() {
        TODO()
    }

    fun testGeneralInsertWithGeneratedKey() {
        TODO()
    }

    fun testInsertWithGeneratedKey() {
        TODO()
    }

    fun testMultiRowInsertWithGeneratedKey() {
        TODO()
    }

    @Test
    fun testSelectAll() {
        val rows = template.select(id, firstName, lastName, birthDate, employed, occupation, addressId)
            .from(Person) {
                allRows()
                orderBy(id)
            }.withRowMapper(::personRowMapper)

        assertThat(rows).hasSize(6)
    }

    @Test
    fun testSelect() {
        val rows = template.select(
                id.`as`("A_ID"), firstName, lastName, birthDate, employed, occupation, addressId)
            .from(Person) {
                where(id, isLessThan(4)) {
                    and(occupation, isNotNull())
                }
                and(occupation, isNotNull())
                orderBy(id)
                limit(3)
            }.withRowMapper(::personRowMapper)

        assertThat(rows).hasSize(2)
        with(rows[0]) {
            assertThat(id).isEqualTo(1)
            assertThat(firstName).isEqualTo("Fred")
            assertThat(lastName!!.name).isEqualTo("Flintstone")
            assertThat(birthDate).isNotNull()
            assertThat(employed).isTrue()
            assertThat(occupation).isEqualTo("Brontosaurus Operator")
            assertThat(addressId).isEqualTo(1)
        }
    }

    @Test
    fun testSelectWithUnion() {
        val rows = template.select(
            id, firstName, lastName, birthDate, employed, occupation, addressId)
            .from(Person) {
                where(id, isEqualTo(1))
                union {
                    select(id, firstName, lastName, birthDate, employed, occupation, addressId)
                        .from(Person) {
                            where(id, isEqualTo(2))
                        }
                }
                union {
                    select(id, firstName, lastName, birthDate, employed, occupation, addressId)
                        .from(Person) {
                            where(id, isEqualTo(2))
                        }
                }
            }.withRowMapper(::personRowMapper)

        assertThat(rows).hasSize(2)
        with(rows[0]) {
            assertThat(id).isEqualTo(1)
            assertThat(firstName).isEqualTo("Fred")
            assertThat(lastName!!.name).isEqualTo("Flintstone")
            assertThat(birthDate).isNotNull()
            assertThat(employed).isTrue()
            assertThat(occupation).isEqualTo("Brontosaurus Operator")
            assertThat(addressId).isEqualTo(1)
        }
    }

    @Test
    fun testSelectWithUnionAll() {
        val rows = template.select(
            id, firstName, lastName, birthDate, employed, occupation, addressId)
            .from(Person) {
                where(id, isEqualTo(1))
                union {
                    select(id, firstName, lastName, birthDate, employed, occupation, addressId)
                        .from(Person) {
                            where(id, isEqualTo(2))
                        }
                }
                unionAll {
                    select(id, firstName, lastName, birthDate, employed, occupation, addressId)
                        .from(Person) {
                            where(id, isEqualTo(2))
                        }
                }
            }.withRowMapper(::personRowMapper)

        assertThat(rows).hasSize(3)
        with(rows[0]) {
            assertThat(id).isEqualTo(1)
            assertThat(firstName).isEqualTo("Fred")
            assertThat(lastName!!.name).isEqualTo("Flintstone")
            assertThat(birthDate).isNotNull()
            assertThat(employed).isTrue()
            assertThat(occupation).isEqualTo("Brontosaurus Operator")
            assertThat(addressId).isEqualTo(1)
        }
    }

    @Test
    fun testSelectByPrimaryKey() {
        val record = template.selectOne(
                id.`as`("A_ID"), firstName, lastName, birthDate, employed, occupation, addressId)
            .from(Person) {
                where(id, isEqualTo(1))
            }.withRowMapper(::personRowMapper)

        with(record!!) {
            assertThat(id).isEqualTo(1)
            assertThat(firstName).isEqualTo("Fred")
            assertThat(lastName!!.name).isEqualTo("Flintstone")
            assertThat(birthDate).isNotNull()
            assertThat(employed).isTrue()
            assertThat(occupation).isEqualTo("Brontosaurus Operator")
            assertThat(addressId).isEqualTo(1)
        }
    }

    @Test
    fun testSelectOneWithAlias() {
        val name = template.selectOne(firstName)
            .from(Person, "p") {
                where(id, isEqualTo(1))
            }.withRowMapper { rs, _ ->
                rs.getString(1)
            }

        assertThat(name).isEqualTo("Fred")
    }

    @Test
    fun testSelectDistinct() {
        val rows = template.selectDistinct(lastName)
            .from(Person) {
                orderBy(lastName)
            }.withRowMapper { rs, _ ->
                rs.getString(1)
            }

        assertThat(rows).hasSize(2)
    }

    @Test
    fun testSelectDistinctWithAlias() {
        val rows = template.selectDistinct(lastName)
            .from(Person, "p") {
                orderBy(lastName)
            }.withRowMapper { rs, _ ->
                rs.getString(1)
            }

        assertThat(rows).hasSize(2)
    }

    @Test
    fun testSelectWithJoin() {
        val rows = template.select(
                id.`as`("A_ID"), firstName, lastName, birthDate, employed, occupation,
            Address.id, Address.streetAddress, Address.city, Address.state)
            .from(Person, "p") {
                join(Address, "a") {
                    on(addressId, equalTo(Address.id))
                }
                where(id, isLessThan(4))
                orderBy(id)
                limit(3)
            }.withRowMapper(::personWithAddressRowMapper)


        assertThat(rows).hasSize(3)
        with(rows[0]) {
            assertThat(id).isEqualTo(1)
            assertThat(firstName).isEqualTo("Fred")
            assertThat(lastName!!.name).isEqualTo("Flintstone")
            assertThat(birthDate).isNotNull()
            assertThat(employed).isTrue()
            assertThat(occupation).isEqualTo("Brontosaurus Operator")
            assertThat(address?.id).isEqualTo(1)
            assertThat(address?.streetAddress).isEqualTo("123 Main Street")
            assertThat(address?.city).isEqualTo("Bedrock")
            assertThat(address?.state).isEqualTo("IN")
        }
    }

    @Test
    fun testSelectWithComplexWhere1() {
        val rows = template.select(
                id.`as`("A_ID"), firstName, lastName, birthDate, employed, occupation, addressId)
            .from(Person) {
                where(id, isLessThan(5))
                and(id, isLessThan(4)) {
                    and(id, isLessThan(3)) {
                        and(id, isLessThan(2))
                    }
                }
                orderBy(id)
                limit(3)
            }.withRowMapper(::personRowMapper)

        assertThat(rows).hasSize(1)
        with(rows[0]) {
            assertThat(id).isEqualTo(1)
            assertThat(firstName).isEqualTo("Fred")
            assertThat(lastName!!.name).isEqualTo("Flintstone")
            assertThat(birthDate).isNotNull()
            assertThat(employed).isTrue()
            assertThat(occupation).isEqualTo("Brontosaurus Operator")
            assertThat(addressId).isEqualTo(1)
        }
    }

    @Test
    fun testSelectWithComplexWhere2() {
        val rows = template.select(
                id.`as`("A_ID"), firstName, lastName, birthDate, employed, occupation, addressId)
            .from(Person) {
                where(id, isEqualTo(5))
                or(id, isEqualTo(4)) {
                    or(id, isEqualTo(3)) {
                        or(id, isEqualTo(2))
                    }
                }
                orderBy(id)
                limit(3)
            }.withRowMapper(::personRowMapper)

        assertThat(rows).hasSize(3)
        with(rows[2]) {
            assertThat(id).isEqualTo(4)
            assertThat(firstName).isEqualTo("Barney")
            assertThat(lastName!!.name).isEqualTo("Rubble")
            assertThat(birthDate).isNotNull()
            assertThat(employed).isTrue()
            assertThat(occupation).isEqualTo("Brontosaurus Operator")
            assertThat(addressId).isEqualTo(2)
        }
    }

    @Test
    fun testUpdate1() {
        val rows = template.update(Person) {
            set(firstName).equalTo("Sam")
            where(firstName, isEqualTo("Fred"))
        }

        assertThat(rows).isEqualTo(1)
    }

    @Test
    fun testUpdate2() {
        val rows = template.update(Person) {
            set(firstName).equalTo("Sam")
            where(firstName, isEqualTo("Fred")) {
                or(id, isGreaterThan(3))
            }
        }

        assertThat(rows).isEqualTo(4)
    }

    @Test
    fun testUpdate3() {
        val rows = template.update(Person) {
            set(firstName).equalTo("Sam")
            where(firstName, isEqualTo("Fred"))
            or(id, isEqualTo(5)) {
                or(id, isEqualTo(6))
            }
        }

        assertThat(rows).isEqualTo(3)
    }

    @Test
    fun testUpdate4() {
        val rows = template.update(Person) {
            set(firstName).equalTo("Sam")
            where(firstName, isEqualTo("Fred"))
            and(id, isEqualTo(1)) {
                or(id, isEqualTo(6))
            }
        }

        assertThat(rows).isEqualTo(1)
    }

    @Test
    fun testUpdate5() {
        val rows = template.update(Person) {
            set(firstName).equalTo("Sam")
            where(firstName, isEqualTo("Fred"))
            or(id, isEqualTo(3))
        }

        assertThat(rows).isEqualTo(2)
    }
}
