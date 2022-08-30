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

import examples.kotlin.spring.canonical.AddressDynamicSqlSupport.address
import examples.kotlin.spring.canonical.GeneratedAlwaysDynamicSqlSupport.generatedAlways
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
import org.mybatis.dynamic.sql.util.kotlin.elements.`as`
import org.mybatis.dynamic.sql.util.kotlin.elements.add
import org.mybatis.dynamic.sql.util.kotlin.elements.constant
import org.mybatis.dynamic.sql.util.kotlin.spring.count
import org.mybatis.dynamic.sql.util.kotlin.spring.countDistinct
import org.mybatis.dynamic.sql.util.kotlin.spring.countFrom
import org.mybatis.dynamic.sql.util.kotlin.spring.deleteFrom
import org.mybatis.dynamic.sql.util.kotlin.spring.insert
import org.mybatis.dynamic.sql.util.kotlin.spring.insertBatch
import org.mybatis.dynamic.sql.util.kotlin.spring.insertInto
import org.mybatis.dynamic.sql.util.kotlin.spring.insertMultiple
import org.mybatis.dynamic.sql.util.kotlin.spring.insertSelect
import org.mybatis.dynamic.sql.util.kotlin.spring.select
import org.mybatis.dynamic.sql.util.kotlin.spring.selectDistinct
import org.mybatis.dynamic.sql.util.kotlin.spring.selectOne
import org.mybatis.dynamic.sql.util.kotlin.spring.update
import org.mybatis.dynamic.sql.util.kotlin.spring.withKeyHolder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.DataClassRowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.transaction.annotation.Transactional
import java.util.Date

@Suppress("LargeClass")
@SpringJUnitConfig(classes = [SpringConfiguration::class])
@Transactional
open class CanonicalSpringKotlinTemplateDirectTest {
    @Autowired
    private lateinit var template: NamedParameterJdbcTemplate

    @Test
    fun testCount() {
        val rows = template.countFrom(person) {
            where { id isLessThan 4 }
        }

        assertThat(rows).isEqualTo(3)
    }

    @Test
    fun testCountAllRows() {
        val rows = template.countFrom(person) {
            allRows()
        }

        assertThat(rows).isEqualTo(6)
    }

    @Test
    fun testCountLastName() {
        val rows = template.count(lastName) {
            from(person)
        }

        assertThat(rows).isEqualTo(6)
    }

    @Test
    fun testCountDistinctLastName() {
        val rows = template.countDistinct(lastName) {
            from(person)
        }

        assertThat(rows).isEqualTo(2)
    }

    @Test
    fun testAllRows() {
        val rows = template.deleteFrom(person) {
            allRows()
        }

        assertThat(rows).isEqualTo(6)
    }

    @Test
    fun testDelete1() {
        val rows = template.deleteFrom(person) {
            where { id isLessThan 4 }
        }

        assertThat(rows).isEqualTo(3)
    }

    @Test
    fun testDelete2() {
        val rows = template.deleteFrom(person) {
            where { id isLessThan 4 }
            and { occupation.isNotNull() }
        }

        assertThat(rows).isEqualTo(2)
    }

    @Test
    fun testDelete3() {
        val rows = template.deleteFrom(person) {
            where { id isLessThan 4 }
            or { occupation.isNotNull() }
        }

        assertThat(rows).isEqualTo(5)
    }

    @Test
    fun testDelete4() {
        val rows = template.deleteFrom(person) {
            where {
                id isLessThan 4
                or { occupation.isNotNull() }
            }
            and { employed isEqualTo true }
        }

        assertThat(rows).isEqualTo(4)
    }

    @Test
    fun testDelete5() {
        val rows = template.deleteFrom(person) {
            where { id isLessThan 4 }
            or {
                occupation.isNotNull()
                and { employed isEqualTo true }
            }
        }

        assertThat(rows).isEqualTo(5)
    }

    @Test
    fun testDelete6() {
        val rows = template.deleteFrom(person) {
            where { id isLessThan 4 }
            and {
                occupation.isNotNull()
                and { employed isEqualTo true }
            }
        }

        assertThat(rows).isEqualTo(2)
    }

    @Test
    fun testInsert() {
        val record = PersonRecord(100, "Joe", LastName("Jones"), Date(), true, "Developer", 1)

        val rows = template.insert(record) {
            into(person)
            map(id) toProperty "id"
            map(firstName) toProperty "firstName"
            map(lastName) toProperty "lastNameAsString"
            map(birthDate) toProperty "birthDate"
            map(employed) toProperty "employedAsString"
            map(occupation).toPropertyWhenPresent("occupation", record::occupation)
            map(addressId) toProperty "addressId"
        }

        assertThat(rows).isEqualTo(1)
    }

    @Test
    fun testDeprecatedInsert() {
        val record = PersonRecord(100, "Joe", LastName("Jones"), Date(), true, "Developer", 1)

        val rows = template.insert(record).into(person) {
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
        val rows = template.insertInto(person) {
            set(id) toValue 100
            set(firstName) toValue "Joe"
            set(lastName) toValue LastName("Jones")
            set(birthDate) toValue Date()
            set(employed) toValue true
            set(occupation) toValue "Developer"
            set(addressId) toValue 1
        }

        assertThat(rows).isEqualTo(1)
    }

    @Test
    fun testMultiRowInsert() {
        val record1 = PersonRecord(100, "Joe", LastName("Jones"), Date(), true, "Developer", 1)
        val record2 = PersonRecord(101, "Sarah", LastName("Smith"), Date(), true, "Architect", 2)

        val rows = template.insertMultiple(record1, record2) {
            into(person)
            map(id) toProperty "id"
            map(firstName) toProperty "firstName"
            map(lastName) toProperty "lastNameAsString"
            map(birthDate) toProperty "birthDate"
            map(employed) toProperty "employedAsString"
            map(occupation) toProperty "occupation"
            map(addressId) toProperty "addressId"
        }

        assertThat(rows).isEqualTo(2)
    }

    @Test
    fun testDeprecatedMultiRowInsert() {
        val record1 = PersonRecord(100, "Joe", LastName("Jones"), Date(), true, "Developer", 1)
        val record2 = PersonRecord(101, "Sarah", LastName("Smith"), Date(), true, "Architect", 2)

        val rows = template.insertMultiple(record1, record2).into(person) {
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

    @Test
    fun testBatchInsert() {
        val record1 = PersonRecord(100, "Joe", LastName("Jones"), Date(), true, "Developer", 1)
        val record2 = PersonRecord(101, "Sarah", LastName("Smith"), Date(), true, "Architect", 2)

        val rows = template.insertBatch(record1, record2) {
            into(person)
            map(id) toProperty "id"
            map(firstName) toProperty "firstName"
            map(lastName) toProperty "lastNameAsString"
            map(birthDate) toProperty "birthDate"
            map(employed) toProperty "employedAsString"
            map(occupation) toProperty "occupation"
            map(addressId) toProperty "addressId"
        }

        assertThat(rows).hasSize(2)
        assertThat(rows[0]).isEqualTo(1)
        assertThat(rows[1]).isEqualTo(1)
    }

    @Test
    fun testDeprecatedBatchInsert() {
        val record1 = PersonRecord(100, "Joe", LastName("Jones"), Date(), true, "Developer", 1)
        val record2 = PersonRecord(101, "Sarah", LastName("Smith"), Date(), true, "Architect", 2)

        val rows = template.insertBatch(record1, record2).into(person) {
            map(id).toProperty("id")
            map(firstName).toProperty("firstName")
            map(lastName).toProperty("lastNameAsString")
            map(birthDate).toProperty("birthDate")
            map(employed).toProperty("employedAsString")
            map(occupation).toProperty("occupation")
            map(addressId).toProperty("addressId")
        }

        assertThat(rows).hasSize(2)
        assertThat(rows[0]).isEqualTo(1)
        assertThat(rows[1]).isEqualTo(1)
    }

    @Test
    fun testInsertSelect() {
        val rows = template.insertSelect {
            into(person)
            columns(id, firstName, lastName, birthDate, employed, occupation, addressId)
            select(
                add(id, constant<Int>("100")), firstName, lastName, birthDate, employed, occupation, addressId
            ) {
                from(person)
                orderBy(id)
            }
        }

        assertThat(rows).isEqualTo(6)

        val records = template.select(id, firstName, lastName, birthDate, employed, occupation, addressId) {
            from(person)
            where { id isGreaterThanOrEqualTo 100 }
            orderBy(id)
        }.withRowMapper(personRowMapper)

        assertThat(records).hasSize(6)
        with(records[1]) {
            assertThat(id).isEqualTo(102)
            assertThat(firstName).isEqualTo("Wilma")
            assertThat(lastName).isEqualTo(LastName("Flintstone"))
            assertThat(birthDate).isNotNull
            assertThat(employed).isTrue
            assertThat(occupation).isEqualTo("Accountant")
            assertThat(addressId).isEqualTo(1)
        }
    }

    @Test
    fun testDeprecatedInsertSelect() {
        val rows = template.insertSelect(person) {
            columns(id, firstName, lastName, birthDate, employed, occupation, addressId)
            select(
                add(id, constant<Int>("100")), firstName, lastName, birthDate, employed, occupation, addressId
            ) {
                from(person)
                orderBy(id)
            }
        }

        assertThat(rows).isEqualTo(6)

        val records = template.select(id, firstName, lastName, birthDate, employed, occupation, addressId) {
            from(person)
            where { id isGreaterThanOrEqualTo 100 }
            orderBy(id)
        }.withRowMapper(personRowMapper)

        assertThat(records).hasSize(6)
        with(records[1]) {
            assertThat(id).isEqualTo(102)
            assertThat(firstName).isEqualTo("Wilma")
            assertThat(lastName).isEqualTo(LastName("Flintstone"))
            assertThat(birthDate).isNotNull
            assertThat(employed).isTrue
            assertThat(occupation).isEqualTo("Accountant")
            assertThat(addressId).isEqualTo(1)
        }
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    fun testGeneralInsertWithGeneratedKey() {
        val keyHolder = GeneratedKeyHolder()

        val rows = template.withKeyHolder(keyHolder) {
            insertInto(generatedAlways) {
                set(generatedAlways.firstName) toValue "Fred"
                set(generatedAlways.lastName) toValue "Flintstone"
            }
        }

        assertThat(rows).isEqualTo(1)
        assertThat(keyHolder.keys).containsEntry("ID", 22)
        assertThat(keyHolder.keys).containsEntry("FULL_NAME", "Fred Flintstone")
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    fun testInsertWithGeneratedKey() {
        val command = GeneratedAlwaysCommand(firstName = "Fred", lastName = "Flintstone")

        val keyHolder = GeneratedKeyHolder()

        val rows = template.withKeyHolder(keyHolder) {
            insert(command) {
                into(generatedAlways)
                map(generatedAlways.firstName) toProperty "firstName"
                map(generatedAlways.lastName) toProperty "lastName"
            }
        }

        assertThat(rows).isEqualTo(1)
        assertThat(keyHolder.keys).containsEntry("ID", 22)
        assertThat(keyHolder.keys).containsEntry("FULL_NAME", "Fred Flintstone")
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    fun testDeprecatedInsertWithGeneratedKey() {
        val command = GeneratedAlwaysCommand(firstName = "Fred", lastName = "Flintstone")

        val keyHolder = GeneratedKeyHolder()

        val rows = template.withKeyHolder(keyHolder) {
            insert(command).into(generatedAlways) {
                map(generatedAlways.firstName).toProperty("firstName")
                map(generatedAlways.lastName).toProperty("lastName")
            }
        }

        assertThat(rows).isEqualTo(1)
        assertThat(keyHolder.keys).containsEntry("ID", 22)
        assertThat(keyHolder.keys).containsEntry("FULL_NAME", "Fred Flintstone")
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    fun testMultiRowInsertWithGeneratedKey() {
        val command1 = GeneratedAlwaysCommand(firstName = "Fred", lastName = "Flintstone")
        val command2 = GeneratedAlwaysCommand(firstName = "Barney", lastName = "Rubble")

        val keyHolder = GeneratedKeyHolder()

        val rows = template.withKeyHolder(keyHolder) {
            insertMultiple(command1, command2) {
                into(generatedAlways)
                map(generatedAlways.firstName) toProperty "firstName"
                map(generatedAlways.lastName) toProperty "lastName"
            }
        }

        assertThat(rows).isEqualTo(2)
        assertThat(keyHolder.keyList[0]).containsEntry("ID", 22)
        assertThat(keyHolder.keyList[0]).containsEntry("FULL_NAME", "Fred Flintstone")
        assertThat(keyHolder.keyList[1]).containsEntry("ID", 23)
        assertThat(keyHolder.keyList[1]).containsEntry("FULL_NAME", "Barney Rubble")
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    fun testDeprecatedMultiRowInsertWithGeneratedKey() {
        val command1 = GeneratedAlwaysCommand(firstName = "Fred", lastName = "Flintstone")
        val command2 = GeneratedAlwaysCommand(firstName = "Barney", lastName = "Rubble")

        val keyHolder = GeneratedKeyHolder()

        val rows = template.withKeyHolder(keyHolder) {
            insertMultiple(command1, command2).into(generatedAlways) {
                map(generatedAlways.firstName).toProperty("firstName")
                map(generatedAlways.lastName).toProperty("lastName")
            }
        }

        assertThat(rows).isEqualTo(2)
        assertThat(keyHolder.keyList[0]).containsEntry("ID", 22)
        assertThat(keyHolder.keyList[0]).containsEntry("FULL_NAME", "Fred Flintstone")
        assertThat(keyHolder.keyList[1]).containsEntry("ID", 23)
        assertThat(keyHolder.keyList[1]).containsEntry("FULL_NAME", "Barney Rubble")
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    fun testInsertSelectWithGeneratedKey() {
        val keyHolder = GeneratedKeyHolder()

        val rows = template.withKeyHolder(keyHolder) {
            insertSelect(generatedAlways) {
                columns(generatedAlways.firstName, generatedAlways.lastName)
                select(person.firstName, person.lastName) {
                    from(person)
                }
            }
        }

        assertThat(rows).isEqualTo(6)
        assertThat(keyHolder.keyList).hasSize(6)
        assertThat(keyHolder.keyList[0]).containsEntry("ID", 22)
        assertThat(keyHolder.keyList[0]).containsEntry("FULL_NAME", "Fred Flintstone")
        assertThat(keyHolder.keyList[5]).containsEntry("ID", 27)
        assertThat(keyHolder.keyList[5]).containsEntry("FULL_NAME", "Bamm Bamm Rubble")
    }

    @Test
    fun testSelectAll() {
        val rows = template.select(id, firstName, lastName, birthDate, employed, occupation, addressId) {
            from(person)
            orderBy(id)
        }.withRowMapper(personRowMapper)

        assertThat(rows).hasSize(6)
    }

    @Test
    fun testSelectAllWithSelectStar() {
        val rows = template.select(person.allColumns()) {
            from(person)
            orderBy(id)
        }.withRowMapper(personRowMapper)

        assertThat(rows).hasSize(6)
    }

    @Test
    fun testSelect() {
        val rows = template.select(id `as` "A_ID", firstName, lastName, birthDate, employed, occupation, addressId) {
            from(person)
            where {
                id isLessThan 4
                and { occupation.isNotNull() }
            }
            and { occupation.isNotNull() }
            orderBy(id)
            limit(3)
        }.withRowMapper(personRowMapper)

        assertThat(rows).hasSize(2)
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
    fun testSelectWithUnion() {
        val rows = template.select(id, firstName, lastName, birthDate, employed, occupation, addressId) {
            from(person)
            where { id isEqualTo 1 }
            union {
                select(id, firstName, lastName, birthDate, employed, occupation, addressId) {
                    from(person)
                    where { id isEqualTo 2 }
                }
            }
            union {
                select(id, firstName, lastName, birthDate, employed, occupation, addressId) {
                    from(person)
                    where { id isEqualTo 2 }
                }
            }
        }.withRowMapper(personRowMapper)

        assertThat(rows).hasSize(2)
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
    fun testSelectWithUnionAll() {
        val rows = template.select(id, firstName, lastName, birthDate, employed, occupation, addressId) {
            from(person)
            where { id isEqualTo 1 }
            union {
                select(id, firstName, lastName, birthDate, employed, occupation, addressId) {
                    from(person)
                    where { id isEqualTo 2 }
                }
            }
            unionAll {
                select(id, firstName, lastName, birthDate, employed, occupation, addressId) {
                    from(person)
                    where { id isEqualTo 2 }
                }
            }
        }.withRowMapper(personRowMapper)

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
    fun testSelectByPrimaryKey() {
        val record = template.selectOne(
            id `as` "A_ID", firstName, lastName, birthDate, employed, occupation, addressId
        ) {
            from(person)
            where { id isEqualTo 1 }
        }.withRowMapper(personRowMapper)

        with(record!!) {
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
    fun testSelectOneWithAlias() {
        val name = template.selectOne(firstName) {
            from(person, "p")
            where { id isEqualTo 1 }
        }.withRowMapper { rs, _ ->
            rs.getString(1)
        }

        assertThat(name).isEqualTo("Fred")
    }

    @Test
    fun testSelectDistinct() {
        val rows = template.selectDistinct(lastName) {
            from(person)
            orderBy(lastName)
        }.withRowMapper { rs, _ ->
            rs.getString(1)
        }

        assertThat(rows).hasSize(2)
    }

    @Test
    fun testSelectDistinctWithAlias() {
        val rows = template.selectDistinct(lastName) {
            from(person, "p")
            orderBy(lastName)
        }.withRowMapper { rs, _ ->
            rs.getString(1)
        }

        assertThat(rows).hasSize(2)
    }

    @Test
    fun testAutoMapping() {
        val rows = template.select(address.id.`as`("id"), address.streetAddress, address.city, address.state) {
            from(address)
            orderBy(address.id)
        }.withRowMapper(DataClassRowMapper(AddressRecord::class.java))

        assertThat(rows).hasSize(2)
        with(rows[0]) {
            assertThat(id).isEqualTo(1)
            assertThat(streetAddress).isEqualTo("123 Main Street")
            assertThat(city).isEqualTo("Bedrock")
            assertThat(state).isEqualTo("IN")
        }
    }

    @Test
    fun testAutoMappingOneRow() {
        val row = template.selectOne(address.id.`as`("id"), address.streetAddress, address.city, address.state) {
            from(address)
            where { address.id isEqualTo 1 }
            orderBy(address.id)
        }.withRowMapper(DataClassRowMapper(AddressRecord::class.java))

        assertThat(row).isNotNull
        with(row!!) {
            assertThat(id).isEqualTo(1)
            assertThat(streetAddress).isEqualTo("123 Main Street")
            assertThat(city).isEqualTo("Bedrock")
            assertThat(state).isEqualTo("IN")
        }
    }

    @Test
    fun testSelectWithJoin() {
        val rows = template.select(
            id `as` "A_ID", firstName, lastName, birthDate, employed, occupation,
            address.id, address.streetAddress, address.city, address.state
        ) {
            from(person, "p")
            join(address, "a") {
                on(addressId) equalTo address.id
            }
            where { id isLessThan 4 }
            orderBy(id)
            limit(3)
        }.withRowMapper(personWithAddressRowMapper)

        assertThat(rows).hasSize(3)
        with(rows[0]) {
            assertThat(id).isEqualTo(1)
            assertThat(firstName).isEqualTo("Fred")
            assertThat(lastName!!.name).isEqualTo("Flintstone")
            assertThat(birthDate).isNotNull
            assertThat(employed).isTrue
            assertThat(occupation).isEqualTo("Brontosaurus Operator")
            assertThat(address?.id).isEqualTo(1)
            assertThat(address?.streetAddress).isEqualTo("123 Main Street")
            assertThat(address?.city).isEqualTo("Bedrock")
            assertThat(address?.state).isEqualTo("IN")
        }
    }

    @Test
    fun testSelectWithComplexWhere1() {
        val rows = template.select(id `as` "A_ID", firstName, lastName, birthDate, employed, occupation, addressId) {
            from(person)
            where { id isLessThan 5 }
            and {
                id isLessThan 4
                and {
                    id isLessThan 3
                    and { id isLessThan 2 }
                }
            }
            orderBy(id)
            limit(3)
        }.withRowMapper(personRowMapper)

        assertThat(rows).hasSize(1)
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
    fun testSelectWithComplexWhere2() {
        val rows = template.select(id `as` "A_ID", firstName, lastName, birthDate, employed, occupation, addressId) {
            from(person)
            where { id isEqualTo 5 }
            or {
                id isEqualTo 4
                or {
                    id isEqualTo 3
                    or { id isEqualTo 2 }
                }
            }
            orderBy(id)
            limit(3)
        }.withRowMapper(personRowMapper)

        assertThat(rows).hasSize(3)
        with(rows[2]) {
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
    fun testUpdate1() {
        val rows = template.update(person) {
            set(firstName) equalTo "Sam"
            where { firstName isEqualTo "Fred" }
        }

        assertThat(rows).isEqualTo(1)
    }

    @Test
    fun testUpdate2() {
        val rows = template.update(person) {
            set(firstName) equalTo "Sam"
            where {
                firstName isEqualTo "Fred"
                or { id isGreaterThan 3 }
            }
        }

        assertThat(rows).isEqualTo(4)
    }

    @Test
    fun testUpdate3() {
        val rows = template.update(person) {
            set(firstName) equalTo "Sam"
            where { firstName isEqualTo "Fred" }
            or {
                id isEqualTo 5
                or { id isEqualTo 6 }
            }
        }

        assertThat(rows).isEqualTo(3)
    }

    @Test
    fun testUpdate4() {
        val rows = template.update(person) {
            set(firstName) equalTo  "Sam"
            where { firstName isEqualTo "Fred" }
            and {
                id isEqualTo 1
                or { id isEqualTo 6 }
            }
        }

        assertThat(rows).isEqualTo(1)
    }

    @Test
    fun testUpdate5() {
        val rows = template.update(person) {
            set(firstName) equalTo "Sam"
            where { firstName isEqualTo "Fred" }
            or { id isEqualTo 3 }
        }

        assertThat(rows).isEqualTo(2)
    }
}
