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
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import org.mybatis.dynamic.sql.exception.InvalidSqlException
import org.mybatis.dynamic.sql.util.Messages
import org.mybatis.dynamic.sql.util.kotlin.KInvalidSQLException
import org.mybatis.dynamic.sql.util.kotlin.elements.`as`
import org.mybatis.dynamic.sql.util.kotlin.elements.add
import org.mybatis.dynamic.sql.util.kotlin.elements.constant
import org.mybatis.dynamic.sql.util.kotlin.elements.insert
import org.mybatis.dynamic.sql.util.kotlin.elements.insertBatch
import org.mybatis.dynamic.sql.util.kotlin.elements.insertMultiple
import org.mybatis.dynamic.sql.util.kotlin.elements.isLikeWhenPresent
import org.mybatis.dynamic.sql.util.kotlin.elements.max
import org.mybatis.dynamic.sql.util.kotlin.elements.sortColumn
import org.mybatis.dynamic.sql.util.kotlin.elements.upper
import org.mybatis.dynamic.sql.util.kotlin.spring.count
import org.mybatis.dynamic.sql.util.kotlin.spring.countDistinct
import org.mybatis.dynamic.sql.util.kotlin.spring.countFrom
import org.mybatis.dynamic.sql.util.kotlin.spring.delete
import org.mybatis.dynamic.sql.util.kotlin.spring.deleteFrom
import org.mybatis.dynamic.sql.util.kotlin.spring.generalInsert
import org.mybatis.dynamic.sql.util.kotlin.spring.insert
import org.mybatis.dynamic.sql.util.kotlin.spring.insertBatch
import org.mybatis.dynamic.sql.util.kotlin.spring.insertInto
import org.mybatis.dynamic.sql.util.kotlin.spring.insertMultiple
import org.mybatis.dynamic.sql.util.kotlin.spring.insertSelect
import org.mybatis.dynamic.sql.util.kotlin.spring.into
import org.mybatis.dynamic.sql.util.kotlin.spring.select
import org.mybatis.dynamic.sql.util.kotlin.spring.selectDistinct
import org.mybatis.dynamic.sql.util.kotlin.spring.selectList
import org.mybatis.dynamic.sql.util.kotlin.spring.selectOne
import org.mybatis.dynamic.sql.util.kotlin.spring.update
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.transaction.annotation.Transactional
import java.util.Date

@Suppress("LargeClass")
@SpringJUnitConfig(classes = [SpringConfiguration::class])
@Transactional
open class CanonicalSpringKotlinTest {
    @Autowired
    private lateinit var template: NamedParameterJdbcTemplate

    @Test
    fun testRawCount() {
        val countStatement = countFrom(person) {
            where { id isLessThan 4 }
        }

        assertThat(countStatement.selectStatement).isEqualTo(
            "select count(*) from Person where id < :p1"
        )

        val rows = template.count(countStatement)

        assertThat(rows).isEqualTo(3)
    }

    @Test
    fun testRawCountAllRows() {
        val countStatement = countFrom(person) {
            allRows()
        }

        assertThat(countStatement.selectStatement).isEqualTo("select count(*) from Person")

        val rows = template.count(countStatement)

        assertThat(rows).isEqualTo(6)
    }

    @Test
    fun testRawCountLastName() {
        val countStatement = count(lastName) {
            from(person)
        }

        assertThat(countStatement.selectStatement).isEqualTo("select count(last_name) from Person")

        val rows = template.count(countStatement)

        assertThat(rows).isEqualTo(6)
    }

    @Test
    fun testRawCountDistinctLastName() {
        val countStatement = countDistinct(lastName) {
            from(person)
        }

        assertThat(countStatement.selectStatement).isEqualTo("select count(distinct last_name) from Person")

        val rows = template.count(countStatement)

        assertThat(rows).isEqualTo(2)
    }

    @Test
    fun testRawDelete1() {
        val deleteStatement = deleteFrom(person) {
            where { id isLessThan 4 }
        }

        assertThat(deleteStatement.deleteStatement).isEqualTo(
            "delete from Person where id < :p1"
        )

        val rows = template.delete(deleteStatement)

        assertThat(rows).isEqualTo(3)
    }

    @Test
    fun testRawDelete2() {
        val deleteStatement = deleteFrom(person) {
            where { id isLessThan 4 }
            and { occupation.isNotNull() }
        }

        assertThat(deleteStatement.deleteStatement).isEqualTo(
            "delete from Person where id < :p1 and occupation is not null"
        )

        val rows = template.delete(deleteStatement)

        assertThat(rows).isEqualTo(2)
    }

    @Test
    fun testRawDelete3() {

        val deleteStatement = deleteFrom(person) {
            where { id isLessThan 4 }
            or { occupation.isNotNull() }
        }

        assertThat(deleteStatement.deleteStatement).isEqualTo(
            "delete from Person where id < :p1 or occupation is not null"
        )

        val rows = template.delete(deleteStatement)

        assertThat(rows).isEqualTo(5)
    }

    @Test
    fun testRawDelete4() {

        val deleteStatement = deleteFrom(person) {
            where {
                id isLessThan 4
                or { occupation.isNotNull() }
            }
            and { employed isEqualTo true }
        }

        val expected = "delete from Person" +
            " where (id < :p1 or occupation is not null)" +
            " and employed = :p2"

        assertThat(deleteStatement.deleteStatement).isEqualTo(expected)
        assertThat(deleteStatement.parameters).containsEntry("p1", 4)
        assertThat(deleteStatement.parameters).containsEntry("p2", "Yes")

        val rows = template.delete(deleteStatement)

        assertThat(rows).isEqualTo(4)
    }

    @Test
    fun testRawDelete5() {
        val deleteStatement = deleteFrom(person) {
            where { id isLessThan 4 }
            or {
                occupation.isNotNull()
                and { employed isEqualTo true }
            }
        }

        val expected = "delete from Person" +
            " where id < :p1 or (occupation is not null" +
            " and employed =" +
            " :p2)"

        assertThat(deleteStatement.deleteStatement).isEqualTo(expected)

        val rows = template.delete(deleteStatement)

        assertThat(rows).isEqualTo(5)
    }

    @Test
    fun testRawDelete6() {
        val deleteStatement = deleteFrom(person) {
            where { id isLessThan 4 }
            and {
                occupation.isNotNull()
                and { employed isEqualTo true }
            }
        }

        val expected = "delete from Person where id < :p1" +
            " and (occupation is not null and" +
            " employed = :p2)"

        assertThat(deleteStatement.deleteStatement).isEqualTo(expected)

        val rows = template.delete(deleteStatement)

        assertThat(rows).isEqualTo(2)
    }

    @Test
    fun testInsert() {

        val record = PersonRecord(100, "Joe", LastName("Jones"), Date(), true, "Developer", 1)

        val insertStatement = insert(record) {
            into(person)
            map(id).toProperty("id")
            map(firstName).toProperty("firstName")
            map(lastName).toProperty("lastNameAsString")
            map(birthDate).toProperty("birthDate")
            map(employed).toProperty("employedAsString")
            map(occupation).toProperty("occupation")
            map(addressId).toProperty("addressId")
        }

        val expected =
            "insert into Person (id, first_name, last_name, birth_date, employed, occupation, address_id)" +
                " values" +
                " (:id, :firstName," +
                " :lastNameAsString," +
                " :birthDate, :employedAsString," +
                " :occupation, :addressId)"

        assertThat(insertStatement.insertStatement).isEqualTo(expected)

        val rows = template.insert(insertStatement)

        assertThat(rows).isEqualTo(1)
    }

    @Test
    fun testDeprecatedInsert() {

        val record = PersonRecord(100, "Joe", LastName("Jones"), Date(), true, "Developer", 1)

        val insertStatement = insert(record).into(person) {
            map(id).toProperty("id")
            map(firstName).toProperty("firstName")
            map(lastName).toProperty("lastNameAsString")
            map(birthDate).toProperty("birthDate")
            map(employed).toProperty("employedAsString")
            map(occupation).toProperty("occupation")
            map(addressId).toProperty("addressId")
        }

        val expected =
            "insert into Person (id, first_name, last_name, birth_date, employed, occupation, address_id)" +
                    " values" +
                    " (:id, :firstName," +
                    " :lastNameAsString," +
                    " :birthDate, :employedAsString," +
                    " :occupation, :addressId)"

        assertThat(insertStatement.insertStatement).isEqualTo(expected)

        val rows = template.insert(insertStatement)

        assertThat(rows).isEqualTo(1)
    }

    @Test
    fun testGeneralInsert() {

        val insertStatement = insertInto(person) {
            set(id) toConstant "100"
            set(firstName) toStringConstant "Joe"
            set(lastName) toValue LastName("Jones")
            set(birthDate) toValue Date()
            set(employed) toValue true
            set(occupation).toNull()
            set(addressId) toValue 1
        }

        val expected =
            "insert into Person (id, first_name, last_name, birth_date, employed, occupation, address_id)" +
                " values (100, 'Joe', :p1, :p2, :p3, null, :p4)"

        assertThat(insertStatement.insertStatement).isEqualTo(expected)

        val rows = template.generalInsert(insertStatement)
        val record = template.selectOne(id, firstName, lastName, birthDate, employed, occupation, addressId) {
            from(person)
            where { id isEqualTo 100 }
        }.withRowMapper(personRowMapper)

        assertThat(rows).isEqualTo(1)
        with(record!!) {
            assertThat(id).isEqualTo(100)
            assertThat(firstName).isEqualTo("Joe")
            assertThat(lastName!!.name).isEqualTo("Jones")
            assertThat(birthDate).isNotNull
            assertThat(employed).isTrue
            assertThat(occupation).isNull()
            assertThat(addressId).isEqualTo(1)
        }
    }

    @Test
    fun testGeneralInsertSpecialConditions() {

        val insertStatement = insertInto(person) {
            set(id) toConstant "100"
            set(firstName) toStringConstant "Joe"
            set(lastName) toValue LastName("Jones")
            set(birthDate) toValue Date()
            set(employed) toValueOrNull true
            set(occupation) toValueWhenPresent null
            set(addressId) toValue 1
        }

        val expected =
            "insert into Person (id, first_name, last_name, birth_date, employed, address_id)" +
                    " values (100, 'Joe', :p1, :p2, :p3, :p4)"

        assertThat(insertStatement.insertStatement).isEqualTo(expected)

        val rows = template.generalInsert(insertStatement)
        val record = template.selectOne(id, firstName, lastName, birthDate, employed, occupation, addressId) {
            from(person)
            where { id isEqualTo 100 }
        }.withRowMapper(personRowMapper)

        assertThat(rows).isEqualTo(1)
        with(record!!) {
            assertThat(id).isEqualTo(100)
            assertThat(firstName).isEqualTo("Joe")
            assertThat(lastName!!.name).isEqualTo("Jones")
            assertThat(birthDate).isNotNull
            assertThat(employed).isTrue
            assertThat(occupation).isNull()
            assertThat(addressId).isEqualTo(1)
        }
    }

    @Test
    fun testMultiRowInsert() {
        val record1 = PersonRecord(100, "Joe", LastName("Jones"), Date(), true, "Developer", 1)
        val record2 = PersonRecord(101, "Sarah", LastName("Smith"), Date(), true, "Architect", 2)

        val insertStatement = insertMultiple(listOf(record1, record2)) {
            into(person)
            map(id).toProperty("id")
            map(firstName).toProperty("firstName")
            map(lastName).toProperty("lastNameAsString")
            map(birthDate).toProperty("birthDate")
            map(employed).toProperty("employedAsString")
            map(occupation).toProperty("occupation")
            map(addressId).toProperty("addressId")
        }

        assertThat(insertStatement.insertStatement).isEqualTo(
            "insert into Person (id, first_name, last_name, birth_date, employed, occupation, address_id) " +
                "values (:records[0].id, :records[0].firstName, :records[0].lastNameAsString, " +
                ":records[0].birthDate, :records[0].employedAsString, " +
                ":records[0].occupation, :records[0].addressId), " +
                "(:records[1].id, :records[1].firstName, :records[1].lastNameAsString, " +
                ":records[1].birthDate, :records[1].employedAsString, :records[1].occupation, :records[1].addressId)"
        )

        val rows = template.insertMultiple(insertStatement)
        assertThat(rows).isEqualTo(2)
    }

    @Test
    fun testDeprecatedMultiRowInsert() {
        val record1 = PersonRecord(100, "Joe", LastName("Jones"), Date(), true, "Developer", 1)
        val record2 = PersonRecord(101, "Sarah", LastName("Smith"), Date(), true, "Architect", 2)

        val insertStatement = insertMultiple(record1, record2).into(person) {
            map(id).toProperty("id")
            map(firstName).toProperty("firstName")
            map(lastName).toProperty("lastNameAsString")
            map(birthDate).toProperty("birthDate")
            map(employed).toProperty("employedAsString")
            map(occupation).toProperty("occupation")
            map(addressId).toProperty("addressId")
        }

        assertThat(insertStatement.insertStatement).isEqualTo(
            "insert into Person (id, first_name, last_name, birth_date, employed, occupation, address_id) " +
                    "values (:records[0].id, :records[0].firstName, :records[0].lastNameAsString, " +
                    ":records[0].birthDate, :records[0].employedAsString, " +
                    ":records[0].occupation, :records[0].addressId), " +
                    "(:records[1].id, :records[1].firstName, :records[1].lastNameAsString, " +
                    ":records[1].birthDate, :records[1].employedAsString, :records[1].occupation, " +
                    ":records[1].addressId)"
        )

        val rows = template.insertMultiple(insertStatement)
        assertThat(rows).isEqualTo(2)
    }

    @Test
    fun testBatchInsert() {
        val record1 = PersonRecord(
            100,
            "Joe",
            LastName("Jones"),
            Date(),
            true,
            "Developer",
            1
        )
        val record2 = PersonRecord(
            101,
            "Sarah",
            LastName("Smith"),
            Date(),
            true,
            "Architect",
            2
        )

        val insertStatement = insertBatch(listOf(record1, record2)) {
            into(person)
            map(id) toProperty "id"
            map(firstName) toProperty "firstName"
            map(lastName) toProperty "lastNameAsString"
            map(birthDate) toProperty "birthDate"
            map(employed) toProperty "employedAsString"
            map(occupation) toProperty "occupation"
            map(addressId) toProperty "addressId"
        }

        val rows = template.insertBatch(insertStatement)
        assertThat(rows).hasSize(2)
        assertThat(rows[0]).isEqualTo(1)
        assertThat(rows[1]).isEqualTo(1)
    }

    @Test
    fun testDeprecatedBatchInsert() {
        val record1 = PersonRecord(
            100,
            "Joe",
            LastName("Jones"),
            Date(),
            true,
            "Developer",
            1
        )
        val record2 = PersonRecord(
            101,
            "Sarah",
            LastName("Smith"),
            Date(),
            true,
            "Architect",
            2
        )

        val insertStatement = insertBatch(record1, record2).into(person) {
            map(id).toProperty("id")
            map(firstName).toProperty("firstName")
            map(lastName).toProperty("lastNameAsString")
            map(birthDate).toProperty("birthDate")
            map(employed).toProperty("employedAsString")
            map(occupation).toProperty("occupation")
            map(addressId).toProperty("addressId")
        }

        val rows = template.insertBatch(insertStatement)
        assertThat(rows).hasSize(2)
        assertThat(rows[0]).isEqualTo(1)
        assertThat(rows[1]).isEqualTo(1)
    }

    @Test
    fun testInsertSelect() {
        val insertStatement = insertSelect {
            into(person)
            columns(id, firstName, lastName, birthDate, employed, occupation, addressId)
            select(add(id, constant<Int>("100")), firstName, lastName, birthDate, employed, occupation, addressId) {
                from(person)
                orderBy(id)
            }
        }

        assertThat(insertStatement.insertStatement).isEqualTo(
            "insert into Person (id, first_name, last_name, birth_date, employed, occupation, address_id) " +
                "select (id + 100), first_name, last_name, birth_date, employed, occupation, address_id " +
                "from Person " +
                "order by id"
        )
        val rows = template.insertSelect(insertStatement)
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
    fun testInsertSelectNoTable() {
        assertThatExceptionOfType(KInvalidSQLException::class.java).isThrownBy {
            insertSelect {
                columns(id, firstName, lastName, birthDate, employed, occupation, addressId)
                select(add(id, constant<Int>("100")), firstName, lastName, birthDate, employed, occupation, addressId) {
                    from(person)
                    orderBy(id)
                }
            }
        }.withMessage(Messages.getString("ERROR.29"))
    }

    @Test
    fun testDeprecatedInsertSelect() {
        val insertStatement = insertSelect(person) {
            columns(id, firstName, lastName, birthDate, employed, occupation, addressId)
            select(add(id, constant<Int>("100")), firstName, lastName, birthDate, employed, occupation, addressId) {
                from(person)
                orderBy(id)
            }
        }

        assertThat(insertStatement.insertStatement).isEqualTo(
            "insert into Person (id, first_name, last_name, birth_date, employed, occupation, address_id) " +
                    "select (id + 100), first_name, last_name, birth_date, employed, occupation, address_id " +
                    "from Person " +
                    "order by id"
        )
        val rows = template.insertSelect(insertStatement)
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
    fun testInsertSelectNoColumns() {
        val insertStatement = insertSelect(person) {
            select(add(id, constant<Int>("100")), firstName, lastName, birthDate, employed, occupation, addressId) {
                from(person)
                orderBy(id)
            }
        }

        assertThat(insertStatement.insertStatement).isEqualTo(
            "insert into Person " +
                    "select (id + 100), first_name, last_name, birth_date, employed, occupation, address_id " +
                    "from Person " +
                    "order by id"
        )
        val rows = template.insertSelect(insertStatement)
        assertThat(rows).isEqualTo(6)
    }

    @Test
    fun testInsertSelectEmptyColumnList() {
        assertThatExceptionOfType(InvalidSqlException::class.java).isThrownBy {
            insertSelect(person) {
                columns()
                select(add(id, constant<Int>("100")), firstName, lastName, birthDate, employed, occupation, addressId) {
                    from(person)
                    orderBy(id)
                }
            }
        }.withMessage(Messages.getString("ERROR.4")) //$NON-NLS-1$
    }

    @Test
    fun testInsertSelectNoSelectStatement() {
        assertThatExceptionOfType(KInvalidSQLException::class.java).isThrownBy {
            insertSelect(person) {
                columns(id, firstName, lastName, birthDate, employed, occupation, addressId)
            }
        }.withMessage(Messages.getString("ERROR.28")) //$NON-NLS-1$
    }

    @Test
    fun testBatchInsertNoTable() {
        val record1 = PersonRecord(100, "Joe", LastName("Jones"), Date(), true, "Developer", 1)
        val record2 = PersonRecord(101, "Sarah", LastName("Smith"), Date(), true, "Architect", 1)

        assertThatExceptionOfType(KInvalidSQLException::class.java).isThrownBy {
            insertBatch(listOf(record1, record2)) {
                map(person.firstName) toProperty "firstName"
            }
        }.withMessage(Messages.getString("ERROR.23")) //$NON-NLS-1$
    }

    @Test
    fun testInsertNoTable() {
        val record = PersonRecord(100, "Joe", LastName("Jones"), Date(), true, "Developer", 1)

        assertThatExceptionOfType(KInvalidSQLException::class.java).isThrownBy {
            insert(record) {
                map(person.firstName) toProperty "firstName"
            }
        }.withMessage(Messages.getString("ERROR.25")) //$NON-NLS-1$
    }

    @Test
    fun testMultiRowInsertNoTable() {
        val record1 = PersonRecord(100, "Joe", LastName("Jones"), Date(), true, "Developer", 1)
        val record2 = PersonRecord(101, "Sarah", LastName("Smith"), Date(), true, "Architect", 1)

        assertThatExceptionOfType(KInvalidSQLException::class.java).isThrownBy {
            insertMultiple(listOf(record1, record2)) {
                map(person.firstName) toProperty "firstName"
            }
        }.withMessage(Messages.getString("ERROR.26")) //$NON-NLS-1$
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    fun testGeneralInsertWithGeneratedKey() {
        val insertStatement = insertInto(generatedAlways) {
            set(generatedAlways.firstName) toValue "Fred"
            set(generatedAlways.lastName) toValue "Flintstone"
        }

        val keyHolder = GeneratedKeyHolder()

        val rows = template.generalInsert(insertStatement, keyHolder)
        assertThat(rows).isEqualTo(1)
        assertThat(keyHolder.keys).containsEntry("ID", 22)
        assertThat(keyHolder.keys).containsEntry("FULL_NAME", "Fred Flintstone")
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    fun testInsertWithGeneratedKey() {
        val command = GeneratedAlwaysCommand(firstName = "Fred", lastName = "Flintstone")

        val insertStatement = insert(command) {
            into(generatedAlways)
            map(generatedAlways.firstName) toProperty "firstName"
            map(generatedAlways.lastName) toProperty "lastName"
        }

        val keyHolder = GeneratedKeyHolder()

        val rows = template.insert(insertStatement, keyHolder)
        assertThat(rows).isEqualTo(1)
        assertThat(keyHolder.keys).containsEntry("ID", 22)
        assertThat(keyHolder.keys).containsEntry("FULL_NAME", "Fred Flintstone")
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    fun testMultiRowInsertWithGeneratedKey() {
        val command1 = GeneratedAlwaysCommand(firstName = "Fred", lastName = "Flintstone")
        val command2 = GeneratedAlwaysCommand(firstName = "Barney", lastName = "Rubble")

        val insertStatement = insertMultiple(listOf(command1, command2)) {
            into(generatedAlways)
            map(generatedAlways.firstName) toProperty "firstName"
            map(generatedAlways.lastName) toProperty "lastName"
        }

        val keyHolder = GeneratedKeyHolder()

        val rows = template.insertMultiple(insertStatement, keyHolder)
        assertThat(rows).isEqualTo(2)
        assertThat(keyHolder.keyList[0]).containsEntry("ID", 22)
        assertThat(keyHolder.keyList[0]).containsEntry("FULL_NAME", "Fred Flintstone")
        assertThat(keyHolder.keyList[1]).containsEntry("ID", 23)
        assertThat(keyHolder.keyList[1]).containsEntry("FULL_NAME", "Barney Rubble")
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    fun testInsertSelectWithGeneratedKey() {
        val insertStatement = insertSelect(generatedAlways) {
            columns(generatedAlways.firstName, generatedAlways.lastName)
            select(person.firstName, person.lastName) {
                from(person)
            }
        }

        val keyHolder = GeneratedKeyHolder()

        val rows = template.insertSelect(insertStatement, keyHolder)
        assertThat(rows).isEqualTo(6)
        assertThat(keyHolder.keyList).hasSize(6)
        assertThat(keyHolder.keyList[0]).containsEntry("ID", 22)
        assertThat(keyHolder.keyList[0]).containsEntry("FULL_NAME", "Fred Flintstone")
        assertThat(keyHolder.keyList[5]).containsEntry("ID", 27)
        assertThat(keyHolder.keyList[5]).containsEntry("FULL_NAME", "Bamm Bamm Rubble")
    }

    @Test
    fun testRawSelect() {
        val selectStatement = select(
            id `as` "A_ID", firstName, lastName, birthDate, employed, occupation, addressId
        ) {
            from(person)
            where {
                id isLessThan 4
                and { occupation.isNotNull() }
            }
            and { occupation.isNotNull() }
            orderBy(id)
            limit(3)
        }

        val rows = template.selectList(selectStatement, personRowMapper)

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
    fun testRawSelectDistinct() {
        val selectStatement = selectDistinct(lastName) {
            from(person)
        }

        val rows = template.selectList(selectStatement) { rs, _ ->
            rs.getString(1)
        }

        assertThat(rows).hasSize(2)
        assertThat(rows[0]).isEqualTo("Flintstone")
        assertThat(rows[1]).isEqualTo("Rubble")
    }

    @Test
    fun testRawSelectWithMissingRecord() {
        val selectStatement = select(
            id `as` "A_ID", firstName, lastName, birthDate, employed, occupation, addressId
        ) {
            from(person)
            where { id isEqualTo 300 }
        }

        val record = template.selectOne(selectStatement, personRowMapper)

        assertThat(record).isNull()
    }

    @Test
    fun testRawSelectByPrimaryKey() {
        val selectStatement = select(
            id `as` "A_ID", firstName, lastName, birthDate, employed, occupation, addressId
        ) {
            from(person)
            where { id isEqualTo 1 }
        }

        val record = template.selectOne(selectStatement, personRowMapper)

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
    fun testRawSelectWithUnion() {
        val selectStatement = select(
            id `as` "A_ID", firstName, lastName, birthDate, employed, occupation, addressId
        ) {
            from(person)
            where { id isEqualTo 1 }
            union {
                select(id `as` "A_ID", firstName, lastName, birthDate, employed, occupation, addressId) {
                    from(person)
                    where { id isEqualTo 2 }
                }
            }
            union {
                select(id `as` "A_ID", firstName, lastName, birthDate, employed, occupation, addressId) {
                    from(person)
                    where { id isEqualTo 3 }
                }
            }
        }

        val expected = "select id as A_ID, first_name, last_name, birth_date, employed, occupation, address_id " +
            "from Person " +
            "where id = :p1 " +
            "union " +
            "select id as A_ID, first_name, last_name, birth_date, employed, occupation, address_id " +
            "from Person " +
            "where id = :p2 " +
            "union " +
            "select id as A_ID, first_name, last_name, birth_date, employed, occupation, address_id " +
            "from Person " +
            "where id = :p3"

        assertThat(selectStatement.selectStatement).isEqualTo(expected)

        val records = template.selectList(selectStatement, personRowMapper)

        assertThat(records).hasSize(3)
        with(records[0]) {
            assertThat(id).isEqualTo(1)
            assertThat(firstName).isEqualTo("Fred")
            assertThat(lastName!!.name).isEqualTo("Flintstone")
            assertThat(birthDate).isNotNull
            assertThat(employed).isTrue
            assertThat(occupation).isEqualTo("Brontosaurus Operator")
            assertThat(addressId).isEqualTo(1)
        }

        with(records[2]) {
            assertThat(id).isEqualTo(3)
            assertThat(firstName).isEqualTo("Pebbles")
            assertThat(lastName!!.name).isEqualTo("Flintstone")
            assertThat(birthDate).isNotNull
            assertThat(employed).isFalse
            assertThat(occupation).isNull()
            assertThat(addressId).isEqualTo(1)
        }
    }

    @Test
    fun testRawSelectWithUnionAndAlias() {
        val selectStatement = select(
            id `as` "A_ID", firstName, lastName, birthDate, employed, occupation, addressId
        ) {
            from(person)
            where { id isEqualTo 1 }
            union {
                select(id `as` "A_ID", firstName, lastName, birthDate, employed, occupation, addressId) {
                    from(person)
                    where { id isEqualTo 2 }
                }
            }
            union {
                select(id `as` "A_ID", firstName, lastName, birthDate, employed, occupation, addressId) {
                    from(person, "p")
                    where { id isEqualTo 3 }
                }
            }
        }

        val expected = "select id as A_ID, first_name, last_name, birth_date, employed, occupation, address_id " +
            "from Person " +
            "where id = :p1 " +
            "union " +
            "select id as A_ID, first_name, last_name, birth_date, employed, occupation, address_id " +
            "from Person " +
            "where id = :p2 " +
            "union " +
            "select p.id as A_ID, p.first_name, p.last_name, p.birth_date, p.employed, p.occupation, " +
            "p.address_id " +
            "from Person p " +
            "where p.id = :p3"

        assertThat(selectStatement.selectStatement).isEqualTo(expected)

        val records = template.selectList(selectStatement, personRowMapper)

        assertThat(records).hasSize(3)
        with(records[0]) {
            assertThat(id).isEqualTo(1)
            assertThat(firstName).isEqualTo("Fred")
            assertThat(lastName!!.name).isEqualTo("Flintstone")
            assertThat(birthDate).isNotNull
            assertThat(employed).isTrue
            assertThat(occupation).isEqualTo("Brontosaurus Operator")
            assertThat(addressId).isEqualTo(1)
        }

        with(records[2]) {
            assertThat(id).isEqualTo(3)
            assertThat(firstName).isEqualTo("Pebbles")
            assertThat(lastName!!.name).isEqualTo("Flintstone")
            assertThat(birthDate).isNotNull
            assertThat(employed).isFalse
            assertThat(occupation).isNull()
            assertThat(addressId).isEqualTo(1)
        }
    }

    @Test
    fun testRawSelectWithUnionAndDistinct() {
        val selectStatement = select(
            id `as` "A_ID", firstName, lastName, birthDate, employed, occupation, addressId
        ) {
            from(person)
            where { id isEqualTo 1 }
            union {
                select(id `as` "A_ID", firstName, lastName, birthDate, employed, occupation, addressId) {
                    from(person)
                    where { id isEqualTo 2 }
                }
            }
            union {
                selectDistinct(id `as` "A_ID", firstName, lastName, birthDate, employed, occupation, addressId) {
                    from(person, "p")
                    where { id isEqualTo 3 }
                }
            }
        }

        val expected = "select id as A_ID, first_name, last_name, birth_date, employed, occupation, address_id " +
            "from Person " +
            "where id = :p1 " +
            "union " +
            "select id as A_ID, first_name, last_name, birth_date, employed, occupation, address_id " +
            "from Person " +
            "where id = :p2 " +
            "union " +
            "select distinct p.id as A_ID, p.first_name, p.last_name, p.birth_date, p.employed, p.occupation, " +
            "p.address_id " +
            "from Person p " +
            "where p.id = :p3"

        assertThat(selectStatement.selectStatement).isEqualTo(expected)

        val records = template.selectList(selectStatement, personRowMapper)

        assertThat(records).hasSize(3)
        with(records[0]) {
            assertThat(id).isEqualTo(1)
            assertThat(firstName).isEqualTo("Fred")
            assertThat(lastName!!.name).isEqualTo("Flintstone")
            assertThat(birthDate).isNotNull
            assertThat(employed).isTrue
            assertThat(occupation).isEqualTo("Brontosaurus Operator")
            assertThat(addressId).isEqualTo(1)
        }

        with(records[2]) {
            assertThat(id).isEqualTo(3)
            assertThat(firstName).isEqualTo("Pebbles")
            assertThat(lastName!!.name).isEqualTo("Flintstone")
            assertThat(birthDate).isNotNull
            assertThat(employed).isFalse
            assertThat(occupation).isNull()
            assertThat(addressId).isEqualTo(1)
        }
    }

    @Test
    fun testRawSelectWithUnionAllAndDistinct() {
        val selectStatement = select(
            id `as` "A_ID" , firstName, lastName, birthDate, employed, occupation, addressId
        ) {
            from(person)
            where { id isEqualTo 1 }
            union {
                select(id `as` "A_ID", firstName, lastName, birthDate, employed, occupation, addressId) {
                    from(person)
                    where { id isEqualTo 2 }
                }
            }
            unionAll {
                selectDistinct(id `as` "A_ID", firstName, lastName, birthDate, employed, occupation, addressId) {
                    from(person, "p")
                    allRows()
                }
            }
            orderBy(sortColumn("A_ID"))
        }

        val expected = "select id as A_ID, first_name, last_name, birth_date, employed, occupation, address_id " +
            "from Person " +
            "where id = :p1 " +
            "union " +
            "select id as A_ID, first_name, last_name, birth_date, employed, occupation, address_id " +
            "from Person " +
            "where id = :p2 " +
            "union all " +
            "select distinct p.id as A_ID, p.first_name, p.last_name, p.birth_date, p.employed, p.occupation, " +
            "p.address_id " +
            "from Person p " +
            "order by A_ID"

        assertThat(selectStatement.selectStatement).isEqualTo(expected)

        val records = template.selectList(selectStatement, personRowMapper)

        assertThat(records).hasSize(8)
        with(records[0]) {
            assertThat(id).isEqualTo(1)
            assertThat(firstName).isEqualTo("Fred")
            assertThat(lastName!!.name).isEqualTo("Flintstone")
            assertThat(birthDate).isNotNull
            assertThat(employed).isTrue
            assertThat(occupation).isEqualTo("Brontosaurus Operator")
            assertThat(addressId).isEqualTo(1)
        }

        with(records[2]) {
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
    fun testRawSelectWithJoin() {
        val selectStatement = select(
            id `as` "A_ID", firstName, lastName, birthDate, employed, occupation, address.id,
            address.streetAddress, address.city, address.state
        ) {
            from(person, "p")
            join(address, "a") {
                on(addressId) equalTo address.id
            }
            where { id isLessThan 4 }
            orderBy(id)
            limit(3)
        }

        val expected = "select p.id as A_ID, p.first_name, p.last_name, p.birth_date, p.employed," +
            " p.occupation, a.address_id, a.street_address, a.city, a.state" +
            " from Person p join Address a on p.address_id = a.address_id" +
            " where p.id < :p1 order by id limit :p2"

        assertThat(selectStatement.selectStatement).isEqualTo(expected)

        val rows = template.selectList(selectStatement, personWithAddressRowMapper)

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
    fun testRawSelectWithComplexWhere1() {
        val selectStatement = select(
            id `as` "A_ID", firstName, lastName, birthDate, employed, occupation, addressId
        ) {
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
        }

        val expected = "select id as A_ID, first_name, last_name, birth_date, employed, occupation, address_id" +
            " from Person" +
            " where id < :p1" +
            " and (id < :p2" +
            " and (id < :p3 and id < :p4))" +
            " order by id limit :p5"

        assertThat(selectStatement.selectStatement).isEqualTo(expected)

        val rows = template.selectList(selectStatement, personRowMapper)

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
    fun testRawSelectWithComplexWhere2() {
        val selectStatement = select(
            id `as` "A_ID", firstName, lastName, birthDate, employed, occupation, addressId
        ) {
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
        }

        val expected = "select id as A_ID, first_name, last_name, birth_date, employed, occupation, address_id" +
            " from Person" +
            " where id = :p1" +
            " or (id = :p2" +
            " or (id = :p3 or id = :p4))" +
            " order by id limit :p5"

        assertThat(selectStatement.selectStatement).isEqualTo(expected)

        val rows = template.selectList(selectStatement, personRowMapper)

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
    fun testRawUpdate1() {
        val updateStatement = update(person) {
            set(lastName) equalTo LastName("Smith")
            where { firstName isEqualTo "Fred" }
        }

        assertThat(updateStatement.updateStatement).isEqualTo(
            "update Person" +
                " set last_name = :p1" +
                " where first_name = :p2"
        )
        assertThat(updateStatement.parameters).containsEntry("p1", "Smith")
        assertThat(updateStatement.parameters).containsEntry("p2", "Fred")

        val rows = template.update(updateStatement)

        assertThat(rows).isEqualTo(1)
    }

    @Test
    fun testRawUpdate2() {
        val updateStatement = update(person) {
            set(firstName) equalTo "Sam"
            where {
                firstName isEqualTo "Fred"
                or { id isGreaterThan 3 }
            }
        }

        assertThat(updateStatement.updateStatement).isEqualTo(
            "update Person" +
                " set first_name = :p1" +
                " where (first_name = :p2 or id > :p3)"
        )

        val rows = template.update(updateStatement)

        assertThat(rows).isEqualTo(4)
    }

    @Test
    fun testRawUpdate3() {
        val updateStatement = update(person) {
            set(firstName) equalTo "Sam"
            where { firstName isEqualTo "Fred" }
            or {
                id isEqualTo 5
                or { id isEqualTo 6 }
            }
        }

        assertThat(updateStatement.updateStatement).isEqualTo(
            "update Person" +
                " set first_name = :p1" +
                " where first_name = :p2" +
                " or (id = :p3 or id = :p4)"
        )

        val rows = template.update(updateStatement)

        assertThat(rows).isEqualTo(3)
    }

    @Test
    fun testRawUpdate4() {
        val updateStatement = update(person) {
            set(firstName) equalTo "Sam"
            where { firstName isEqualTo "Fred" }
            and {
                id isEqualTo 1
                or { id isEqualTo 6 }
            }
        }

        assertThat(updateStatement.updateStatement).isEqualTo(
            "update Person" +
                " set first_name = :p1" +
                " where first_name = :p2" +
                " and (id = :p3 or id = :p4)"
        )

        val rows = template.update(updateStatement)

        assertThat(rows).isEqualTo(1)
    }

    @Test
    fun testRawUpdate5() {
        val updateStatement = update(person) {
            set(firstName) equalTo "Sam"
            where { firstName isEqualTo "Fred" }
            or { id isEqualTo 3 }
        }

        assertThat(updateStatement.updateStatement).isEqualTo(
            "update Person" +
                " set first_name = :p1" +
                " where first_name = :p2" +
                " or id = :p3"
        )

        val rows = template.update(updateStatement)

        assertThat(rows).isEqualTo(2)
    }

    @Test
    fun testRawUpdate6() {
        val updateStatement = update(person) {
            set(occupation) equalToOrNull  null
            where { firstName isEqualTo "Fred" }
            or { id isEqualTo 3 }
        }

        assertThat(updateStatement.updateStatement).isEqualTo(
            "update Person" +
                    " set occupation = null" +
                    " where first_name = :p1" +
                    " or id = :p2"
        )

        val rows = template.update(updateStatement)

        assertThat(rows).isEqualTo(2)
    }

    @Test
    fun testUpdateWithTypeConverterAndNullValue() {
        val updateStatement = update(person) {
            set(firstName) equalTo "Sam"
            set(lastName).equalToNull()
            where { id isEqualTo 3 }
        }

        assertThat(updateStatement.updateStatement).isEqualTo(
            "update Person" +
                " set first_name = :p1," +
                " last_name = null" +
                " where id = :p2"
        )

        assertThat(updateStatement.parameters).containsEntry("p1", "Sam")
        assertThat(updateStatement.parameters).containsEntry("p2", 3)

        val rows = template.update(updateStatement)

        assertThat(rows).isEqualTo(1)

        val selectStatement = select(id, firstName, lastName, birthDate, employed, occupation, addressId) {
            from(person)
            where { id isEqualTo 3 }
        }

        val returnedRecord = template.selectOne(selectStatement, personRowMapper)
        assertThat(returnedRecord).isNotNull
        assertThat(returnedRecord!!.lastName).isNull()
    }

    @Test
    fun testUpdateWithTypeConverterAndNonNullValue() {
        val updateStatement = update(person) {
            set(firstName) equalTo "Sam"
            set(lastName) equalTo LastName("Smith")
            where { id isEqualTo 3 }
        }

        assertThat(updateStatement.updateStatement).isEqualTo(
            "update Person" +
                " set first_name = :p1," +
                " last_name = :p2" +
                " where id = :p3"
        )

        assertThat(updateStatement.parameters).containsEntry("p1", "Sam")
        assertThat(updateStatement.parameters).containsEntry("p2", "Smith")
        assertThat(updateStatement.parameters).containsEntry("p3", 3)

        val rows = template.update(updateStatement)

        assertThat(rows).isEqualTo(1)

        val selectStatement = select(id, firstName, lastName, birthDate, employed, occupation, addressId) {
            from(person)
            where { id isEqualTo 3 }
        }

        val returnedRecord = template.selectOne(selectStatement, personRowMapper)
        assertThat(returnedRecord).isNotNull
        assertThat(returnedRecord!!.lastName?.name).isEqualTo("Smith")
    }

    @Test
    fun testUpdateSetNull() {
        val updateStatement = update(person) {
            set(addressId).equalToNull()
            where { id isEqualTo 3 }
        }

        assertThat(updateStatement.updateStatement).isEqualTo(
            "update Person" +
                " set address_id = null" +
                " where id = :p1"
        )

        assertThat(updateStatement.parameters).containsEntry("p1", 3)

        val rows = template.update(updateStatement)

        assertThat(rows).isEqualTo(1)

        val selectStatement = select(id, firstName, lastName, birthDate, employed, occupation, addressId) {
            from(person)
            where { addressId.isNull() }
        }

        val returnedRecord = template.selectOne(selectStatement, personRowMapper)
        assertThat(returnedRecord).isNotNull
        assertThat(returnedRecord!!.addressId).isNull()
    }

    @Test
    fun testUpdateSetToConstant() {
        val updateStatement = update(person) {
            set(addressId) equalToConstant "5"
            where { id isEqualTo 3 }
        }

        assertThat(updateStatement.updateStatement).isEqualTo(
            "update Person" +
                " set address_id = 5" +
                " where id = :p1"
        )

        assertThat(updateStatement.parameters).containsEntry("p1", 3)

        val rows = template.update(updateStatement)

        assertThat(rows).isEqualTo(1)

        val selectStatement = select(id, firstName, lastName, birthDate, employed, occupation, addressId) {
            from(person)
            where { id isEqualTo 3 }
        }

        val returnedRecord = template.selectOne(selectStatement, personRowMapper)
        assertThat(returnedRecord).isNotNull
        assertThat(returnedRecord!!.addressId).isEqualTo(5)
    }

    @Test
    fun testUpdateSetToColumn() {
        val updateStatement = update(person) {
            set(addressId) equalTo id
            where { id isEqualTo 3 }
        }

        assertThat(updateStatement.updateStatement).isEqualTo(
            "update Person" +
                " set address_id = id" +
                " where id = :p1"
        )

        assertThat(updateStatement.parameters).containsEntry("p1", 3)

        val rows = template.update(updateStatement)

        assertThat(rows).isEqualTo(1)

        val selectStatement = select(id, firstName, lastName, birthDate, employed, occupation, addressId) {
            from(person)
            where { id isEqualTo 3 }
        }

        val returnedRecord = template.selectOne(selectStatement, personRowMapper)
        assertThat(returnedRecord).isNotNull
        assertThat(returnedRecord!!.addressId).isEqualTo(3)
    }

    @Test
    fun testUpdateSetToSubQuery() {
        val updateStatement = update(person) {
            set(addressId) equalToQueryResult {
                select(add(max(addressId), constant<Int>("1"))) {
                    from(person)
                }
            }
            where { id isEqualTo 3 }
        }

        assertThat(updateStatement.updateStatement).isEqualTo(
            "update Person" +
                " set address_id = (select (max(address_id) + 1) from Person)" +
                " where id = :p1"
        )

        assertThat(updateStatement.parameters).containsEntry("p1", 3)

        val rows = template.update(updateStatement)

        assertThat(rows).isEqualTo(1)

        val selectStatement = select(id, firstName, lastName, birthDate, employed, occupation, addressId) {
            from(person)
            where { id isEqualTo 3 }
        }

        val returnedRecord = template.selectOne(selectStatement, personRowMapper)
        assertThat(returnedRecord).isNotNull
        assertThat(returnedRecord!!.addressId).isEqualTo(3)
    }

    @Test
    fun testUpdateSetEqualToWhenPresent() {
        val updateStatement = update(person) {
            set(addressId) equalTo 5
            set(firstName) equalToWhenPresent null
            where { id isEqualTo 3 }
        }

        assertThat(updateStatement.updateStatement).isEqualTo(
            "update Person" +
                " set address_id = :p1" +
                " where id = :p2"
        )

        assertThat(updateStatement.parameters).containsEntry("p1", 5)
        assertThat(updateStatement.parameters).containsEntry("p2", 3)

        val rows = template.update(updateStatement)

        assertThat(rows).isEqualTo(1)

        val selectStatement = select(id, firstName, lastName, birthDate, employed, occupation, addressId) {
            from(person)
            where { id isEqualTo 3 }
        }

        val returnedRecord = template.selectOne(selectStatement, personRowMapper)
        assertThat(returnedRecord).isNotNull
        assertThat(returnedRecord!!.addressId).isEqualTo(5)
    }

    @Test
    fun testComplexSearch() {
        data class SearchParameters(
            val id: Int?,
            val firstName: String?,
            val lastName: String?
        )

        val search1 = SearchParameters(id = null, firstName = "f", lastName = null)

        val selectStatement = select(
            id, firstName, lastName, birthDate, employed, occupation, addressId
        ) {
            from(person)
            where { id isEqualToWhenPresent search1.id }
            and {
                upper(firstName) (isLikeWhenPresent(search1.firstName)
                        .map(String::trim)
                        .filter(String::isNotEmpty)
                        .map(String::uppercase)
                        .map { "%$it%" }
                )
            }
            and {
                upper(lastName) (isLikeWhenPresent(search1.lastName)
                        .map(String::trim)
                        .filter(String::isNotEmpty)
                        .map(String::uppercase)
                        .map { LastName("%$it%") })
            }
            orderBy(id)
            limit(3)
        }

        val expected = "select id, first_name, last_name, birth_date, employed, occupation, address_id" +
                " from Person" +
                " where upper(first_name) like :p1" +
                " order by id limit :p2"

        assertThat(selectStatement.selectStatement).isEqualTo(expected)
        assertThat(selectStatement.parameters).containsEntry("p1", "%F%")
        assertThat(selectStatement.parameters).containsEntry("p2", 3L)

        val rows = template.selectList(selectStatement, personRowMapper)

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
}
