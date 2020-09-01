/*
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
import examples.kotlin.spring.canonical.GeneratedAlwaysDynamicSqlSupport.GeneratedAlways
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
import org.springframework.jdbc.support.GeneratedKeyHolder
import java.util.*

@Suppress("LargeClass", "MaxLineLength")
class CanonicalSpringKotlinTest {
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
    fun testRawCount() {
        val countStatement = countFrom(Person) {
            where(id, isLessThan(4))
        }

        assertThat(countStatement.selectStatement).isEqualTo(
            "select count(*) from Person" +
                    " where id < :p1"
        )

        val rows = template.count(countStatement)

        assertThat(rows).isEqualTo(3)
    }

    @Test
    fun testRawCountAllRows() {
        val countStatement = countFrom(Person) {
            allRows()
        }

        assertThat(countStatement.selectStatement).isEqualTo("select count(*) from Person")

        val rows = template.count(countStatement)

        assertThat(rows).isEqualTo(6)
    }

    @Test
    fun testRawCountLastName() {
        val countStatement = countColumn(lastName).from(Person) {
            allRows()
        }

        assertThat(countStatement.selectStatement).isEqualTo("select count(last_name) from Person")

        val rows = template.count(countStatement)

        assertThat(rows).isEqualTo(6)
    }

    @Test
    fun testRawCountDistinctLastName() {
        val countStatement = countDistinctColumn(lastName).from(Person) {
            allRows()
        }

        assertThat(countStatement.selectStatement).isEqualTo("select count(distinct last_name) from Person")

        val rows = template.count(countStatement)

        assertThat(rows).isEqualTo(2)
    }

    @Test
    fun testRawDelete1() {
        val deleteStatement = deleteFrom(Person) {
            where(id, isLessThan(4))
        }

        assertThat(deleteStatement.deleteStatement).isEqualTo(
            "delete from Person" +
                    " where id < :p1"
        )

        val rows = template.delete(deleteStatement)

        assertThat(rows).isEqualTo(3)
    }

    @Test
    fun testRawDelete2() {
        val deleteStatement = deleteFrom(Person) {
            where(id, isLessThan(4))
            and(occupation, isNotNull())
        }

        assertThat(deleteStatement.deleteStatement).isEqualTo(
            "delete from Person" +
                    " where id < :p1 and occupation is not null"
        )

        val rows = template.delete(deleteStatement)

        assertThat(rows).isEqualTo(2)
    }

    @Test
    fun testRawDelete3() {

        val deleteStatement = deleteFrom(Person) {
            where(id, isLessThan(4))
            or(occupation, isNotNull())
        }

        assertThat(deleteStatement.deleteStatement).isEqualTo(
            "delete from Person" +
                    " where id < :p1 or occupation is not null"
        )

        val rows = template.delete(deleteStatement)

        assertThat(rows).isEqualTo(5)
    }

    @Test
    fun testRawDelete4() {

        val deleteStatement = deleteFrom(Person) {
            where(id, isLessThan(4)) {
                or(occupation, isNotNull())
            }
            and(employed, isEqualTo(true))
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
        val deleteStatement = deleteFrom(Person) {
            where(id, isLessThan(4))
            or(occupation, isNotNull()) {
                and(employed, isEqualTo(true))
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
        val deleteStatement = deleteFrom(Person) {
            where(id, isLessThan(4))
            and(occupation, isNotNull()) {
                and(employed, isEqualTo(true))
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

        val insertStatement = insert(record).into(Person) {
            map(id).toProperty("id")
            map(firstName).toProperty("firstName")
            map(lastName).toProperty("lastNameAsString")
            map(birthDate).toProperty("birthDate")
            map(employed).toProperty("employedAsString")
            map(occupation).toProperty("occupation")
            map(addressId).toProperty("addressId")
        }

        val expected = "insert into Person (id, first_name, last_name, birth_date, employed, occupation, address_id)" +
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

        val insertStatement = insertInto(Person) {
            set(id).toValue(100)
            set(firstName).toValue("Joe")
            set(lastName).toValue(LastName("Jones"))
            set(birthDate).toValue(Date())
            set(employed).toValue(true)
            set(occupation).toValue("Developer")
            set(addressId).toValue(1)
        }

        val expected = "insert into Person (id, first_name, last_name, birth_date, employed, occupation, address_id)" +
                " values (:p1, :p2, :p3, :p4, :p5, :p6, :p7)"

        assertThat(insertStatement.insertStatement).isEqualTo(expected)

        val rows = template.generalInsert(insertStatement)
        val record = template.selectOne(id, firstName, lastName, birthDate, employed, occupation, addressId)
            .from(Person) {
                where(id, isEqualTo(100))
            }.withRowMapper(personRowMapper)

        assertThat(rows).isEqualTo(1)
        with(record!!) {
            assertThat(id).isEqualTo(100)
            assertThat(firstName).isEqualTo("Joe")
            assertThat(lastName!!.name).isEqualTo("Jones")
            assertThat(birthDate).isNotNull()
            assertThat(employed).isTrue()
            assertThat(occupation).isEqualTo("Developer")
            assertThat(addressId).isEqualTo(1)
        }
    }

    @Test
    fun testMultiRowInsert() {
        val record1 = PersonRecord(100, "Joe", LastName("Jones"), Date(), true, "Developer", 1)
        val record2 = PersonRecord(101, "Sarah", LastName("Smith"), Date(), true, "Architect", 2)

        val insertStatement = insertMultiple(record1, record2).into(Person) {
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
                    ":records[0].birthDate, :records[0].employedAsString, :records[0].occupation, :records[0].addressId), " +
                    "(:records[1].id, :records[1].firstName, :records[1].lastNameAsString, " +
                    ":records[1].birthDate, :records[1].employedAsString, :records[1].occupation, :records[1].addressId)"
        )

        val rows = template.insertMultiple(insertStatement)
        assertThat(rows).isEqualTo(2)
    }

    @Test
    fun testBatchInsert() {
        val record1 = PersonRecord(100, "Joe", LastName("Jones"), Date(), true, "Developer", 1)
        val record2 = PersonRecord(101, "Sarah", LastName("Smith"), Date(), true, "Architect", 2)

        val insertStatement = insertBatch(record1, record2).into(Person) {
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
    fun testGeneralInsertWithGeneratedKey() {
        val insertStatement = insertInto(GeneratedAlways) {
            set(GeneratedAlways.firstName).toValue("Fred")
            set(GeneratedAlways.lastName).toValue("Flintstone")
        }

        val keyHolder = GeneratedKeyHolder()

        val rows = template.generalInsert(insertStatement, keyHolder)
        assertThat(rows).isEqualTo(1)
        assertThat(keyHolder.keys).containsEntry("ID", 22)
        assertThat(keyHolder.keys).containsEntry("FULL_NAME", "Fred Flintstone")
    }

    @Test
    fun testInsertWithGeneratedKey() {
        val record = GeneratedAlwaysRecord(firstName = "Fred", lastName = "Flintstone")

        val insertStatement = insert(record).into(GeneratedAlways) {
            map(GeneratedAlways.firstName).toProperty("firstName")
            map(GeneratedAlways.lastName).toProperty("lastName")
        }

        val keyHolder = GeneratedKeyHolder()

        val rows = template.insert(insertStatement, keyHolder)
        assertThat(rows).isEqualTo(1)
        assertThat(keyHolder.keys).containsEntry("ID", 22)
        assertThat(keyHolder.keys).containsEntry("FULL_NAME", "Fred Flintstone")
    }

    @Test
    fun testMultiRowInsertWithGeneratedKey() {
        val record1 = GeneratedAlwaysRecord(firstName = "Fred", lastName = "Flintstone")
        val record2 = GeneratedAlwaysRecord(firstName = "Barney", lastName = "Rubble")

        val insertStatement = insertMultiple(record1, record2)
            .into(GeneratedAlways) {
            map(GeneratedAlways.firstName).toProperty("firstName")
            map(GeneratedAlways.lastName).toProperty("lastName")
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
    fun testRawSelect() {
        val selectStatement = select(
            id.`as`("A_ID"), firstName, lastName, birthDate, employed, occupation,
            addressId
        ).from(Person) {
            where(id, isLessThan(4)) {
                and(occupation, isNotNull())
            }
            and(occupation, isNotNull())
            orderBy(id)
            limit(3)
        }

        val rows = template.selectList(selectStatement, personRowMapper)

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
    fun testRawSelectWithMissingRecord() {
        val selectStatement = select(
            id.`as`("A_ID"), firstName, lastName, birthDate, employed, occupation,
            addressId
        ).from(Person) {
            where(id, isEqualTo(300))
        }

        val record = template.selectOne(selectStatement, personRowMapper)

        assertThat(record).isNull()
    }

    @Test
    fun testRawSelectByPrimaryKey() {
        val selectStatement = select(
            id.`as`("A_ID"), firstName, lastName, birthDate, employed, occupation,
            addressId
        ).from(Person) {
            where(id, isEqualTo(1))
        }

        val record = template.selectOne(selectStatement, personRowMapper)

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
    fun testRawSelectWithUnion() {
        val selectStatement = select(
            id.`as`("A_ID"), firstName, lastName, birthDate, employed, occupation,
            addressId
        ).from(Person) {
            where(id, isEqualTo(1))
            union {
                select(
                    id.`as`("A_ID"), firstName, lastName, birthDate, employed, occupation,
                    addressId
                ).from(Person) {
                    where(id, isEqualTo(2))
                }
            }
            union {
                select(
                    id.`as`("A_ID"), firstName, lastName, birthDate, employed, occupation,
                    addressId
                ).from(Person) {
                    where(id, isEqualTo(3))
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
            assertThat(birthDate).isNotNull()
            assertThat(employed).isTrue()
            assertThat(occupation).isEqualTo("Brontosaurus Operator")
            assertThat(addressId).isEqualTo(1)
        }

        with(records[2]) {
            assertThat(id).isEqualTo(3)
            assertThat(firstName).isEqualTo("Pebbles")
            assertThat(lastName!!.name).isEqualTo("Flintstone")
            assertThat(birthDate).isNotNull()
            assertThat(employed).isFalse()
            assertThat(occupation).isNull()
            assertThat(addressId).isEqualTo(1)
        }
    }

    @Test
    fun testRawSelectWithUnionAndAlias() {
        val selectStatement = select(
            id.`as`("A_ID"), firstName, lastName, birthDate, employed, occupation,
            addressId
        ).from(Person) {
            where(id, isEqualTo(1))
            union {
                select(
                    id.`as`("A_ID"), firstName, lastName, birthDate, employed, occupation,
                    addressId
                ).from(Person) {
                    where(id, isEqualTo(2))
                }
            }
            union {
                select(
                    id.`as`("A_ID"), firstName, lastName, birthDate, employed, occupation,
                    addressId
                ).from(Person, "p") {
                    where(id, isEqualTo(3))
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
                "select p.id as A_ID, p.first_name, p.last_name, p.birth_date, p.employed, p.occupation, p.address_id " +
                "from Person p " +
                "where p.id = :p3"

        assertThat(selectStatement.selectStatement).isEqualTo(expected)

        val records = template.selectList(selectStatement, personRowMapper)

        assertThat(records).hasSize(3)
        with(records[0]) {
            assertThat(id).isEqualTo(1)
            assertThat(firstName).isEqualTo("Fred")
            assertThat(lastName!!.name).isEqualTo("Flintstone")
            assertThat(birthDate).isNotNull()
            assertThat(employed).isTrue()
            assertThat(occupation).isEqualTo("Brontosaurus Operator")
            assertThat(addressId).isEqualTo(1)
        }

        with(records[2]) {
            assertThat(id).isEqualTo(3)
            assertThat(firstName).isEqualTo("Pebbles")
            assertThat(lastName!!.name).isEqualTo("Flintstone")
            assertThat(birthDate).isNotNull()
            assertThat(employed).isFalse()
            assertThat(occupation).isNull()
            assertThat(addressId).isEqualTo(1)
        }
    }

    @Test
    fun testRawSelectWithUnionAndDistinct() {
        val selectStatement = select(
            id.`as`("A_ID"), firstName, lastName, birthDate, employed, occupation,
            addressId
        ).from(Person) {
            where(id, isEqualTo(1))
            union {
                select(
                    id.`as`("A_ID"), firstName, lastName, birthDate, employed, occupation,
                    addressId
                ).from(Person) {
                    where(id, isEqualTo(2))
                }
            }
            union {
                selectDistinct(
                    id.`as`("A_ID"), firstName, lastName, birthDate, employed, occupation,
                    addressId
                ).from(Person, "p") {
                    where(id, isEqualTo(3))
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
                "select distinct p.id as A_ID, p.first_name, p.last_name, p.birth_date, p.employed, p.occupation, p.address_id " +
                "from Person p " +
                "where p.id = :p3"

        assertThat(selectStatement.selectStatement).isEqualTo(expected)

        val records = template.selectList(selectStatement, personRowMapper)

        assertThat(records).hasSize(3)
        with(records[0]) {
            assertThat(id).isEqualTo(1)
            assertThat(firstName).isEqualTo("Fred")
            assertThat(lastName!!.name).isEqualTo("Flintstone")
            assertThat(birthDate).isNotNull()
            assertThat(employed).isTrue()
            assertThat(occupation).isEqualTo("Brontosaurus Operator")
            assertThat(addressId).isEqualTo(1)
        }

        with(records[2]) {
            assertThat(id).isEqualTo(3)
            assertThat(firstName).isEqualTo("Pebbles")
            assertThat(lastName!!.name).isEqualTo("Flintstone")
            assertThat(birthDate).isNotNull()
            assertThat(employed).isFalse()
            assertThat(occupation).isNull()
            assertThat(addressId).isEqualTo(1)
        }
    }

    @Test
    fun testRawSelectWithUnionAllAndDistinct() {
        val selectStatement = select(
            id.`as`("A_ID"), firstName, lastName, birthDate, employed, occupation,
            addressId
        ).from(Person) {
            where(id, isEqualTo(1))
            union {
                select(
                    id.`as`("A_ID"), firstName, lastName, birthDate, employed, occupation,
                    addressId
                ).from(Person) {
                    where(id, isEqualTo(2))
                }
            }
            unionAll {
                selectDistinct(
                    id.`as`("A_ID"), firstName, lastName, birthDate, employed, occupation,
                    addressId
                ).from(Person, "p") {
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
                "select distinct p.id as A_ID, p.first_name, p.last_name, p.birth_date, p.employed, p.occupation, p.address_id " +
                "from Person p " +
                "order by A_ID"

        assertThat(selectStatement.selectStatement).isEqualTo(expected)

        val records = template.selectList(selectStatement, personRowMapper)

        assertThat(records).hasSize(8)
        with(records[0]) {
            assertThat(id).isEqualTo(1)
            assertThat(firstName).isEqualTo("Fred")
            assertThat(lastName!!.name).isEqualTo("Flintstone")
            assertThat(birthDate).isNotNull()
            assertThat(employed).isTrue()
            assertThat(occupation).isEqualTo("Brontosaurus Operator")
            assertThat(addressId).isEqualTo(1)
        }

        with(records[2]) {
            assertThat(id).isEqualTo(2)
            assertThat(firstName).isEqualTo("Wilma")
            assertThat(lastName!!.name).isEqualTo("Flintstone")
            assertThat(birthDate).isNotNull()
            assertThat(employed).isTrue()
            assertThat(occupation).isEqualTo("Accountant")
            assertThat(addressId).isEqualTo(1)
        }
    }

    @Test
    fun testRawSelectWithJoin() {
        val selectStatement = select(
            id.`as`("A_ID"), firstName, lastName, birthDate, employed, occupation,
            Address.id, Address.streetAddress, Address.city, Address.state
        )
            .from(Person, "p") {
                join(Address, "a") {
                    on(addressId, equalTo(Address.id))
                }
                where(id, isLessThan(4))
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
    fun testRawSelectWithComplexWhere1() {
        val selectStatement = select(
            id.`as`("A_ID"), firstName, lastName, birthDate, employed, occupation,
            addressId
        ).from(Person) {
            where(id, isLessThan(5))
            and(id, isLessThan(4)) {
                and(id, isLessThan(3)) {
                    and(id, isLessThan(2))
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
            assertThat(birthDate).isNotNull()
            assertThat(employed).isTrue()
            assertThat(occupation).isEqualTo("Brontosaurus Operator")
            assertThat(addressId).isEqualTo(1)
        }
    }

    @Test
    fun testRawSelectWithComplexWhere2() {
        val selectStatement = select(
            id.`as`("A_ID"), firstName, lastName, birthDate, employed, occupation,
            addressId
        ).from(Person) {
            where(id, isEqualTo(5))
            or(id, isEqualTo(4)) {
                or(id, isEqualTo(3)) {
                    or(id, isEqualTo(2))
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
            assertThat(birthDate).isNotNull()
            assertThat(employed).isTrue()
            assertThat(occupation).isEqualTo("Brontosaurus Operator")
            assertThat(addressId).isEqualTo(2)
        }
    }

    @Test
    fun testRawUpdate1() {
        val updateStatement = update(Person) {
            set(lastName).equalTo(LastName("Smith"))
            where(firstName, isEqualTo("Fred"))
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
        val updateStatement = update(Person) {
            set(firstName).equalTo("Sam")
            where(firstName, isEqualTo("Fred")) {
                or(id, isGreaterThan(3))
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
        val updateStatement = update(Person) {
            set(firstName).equalTo("Sam")
            where(firstName, isEqualTo("Fred"))
            or(id, isEqualTo(5)) {
                or(id, isEqualTo(6))
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
        val updateStatement = update(Person) {
            set(firstName).equalTo("Sam")
            where(firstName, isEqualTo("Fred"))
            and(id, isEqualTo(1)) {
                or(id, isEqualTo(6))
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
        val updateStatement = update(Person) {
            set(firstName).equalTo("Sam")
            where(firstName, isEqualTo("Fred"))
            or(id, isEqualTo(3))
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
    fun testUpdateWithTypeConverterAndNullValue() {
        val record = PersonRecord(id = 3, firstName = "Sam")

        val updateStatement = update(Person) {
            set(firstName).equalTo(record::firstName)
            set(lastName).equalTo(record::lastName)
            where(id, isEqualTo(record::id))
        }

        assertThat(updateStatement.updateStatement).isEqualTo(
            "update Person" +
                    " set first_name = :p1," +
                    " last_name = :p2" +
                    " where id = :p3"
        )

        assertThat(updateStatement.parameters).containsEntry("p1", "Sam")
        assertThat(updateStatement.parameters).containsEntry("p2", null)
        assertThat(updateStatement.parameters).containsEntry("p3", 3)

        val rows = template.update(updateStatement)

        assertThat(rows).isEqualTo(1)

        val selectStatement = select(
            id, firstName, lastName, birthDate, employed, occupation, addressId
        ).from(Person) {
            where(id, isEqualTo(record::id))
        }

        val returnedRecord = template.selectOne(selectStatement, personRowMapper)
        assertThat(returnedRecord).isNotNull()
        assertThat(returnedRecord!!.lastName).isNull()
    }

    @Test
    fun testUpdateWithTypeConverterAndNonNullValue() {
        val record = PersonRecord(id = 3, firstName = "Sam", lastName = LastName("Smith"))

        val updateStatement = update(Person) {
            set(firstName).equalTo(record::firstName)
            set(lastName).equalTo(record::lastName)
            where(id, isEqualTo(record::id))
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

        val selectStatement = select(
            id, firstName, lastName, birthDate, employed, occupation, addressId
        ).from(Person) {
            where(id, isEqualTo(record::id))
        }

        val returnedRecord = template.selectOne(selectStatement, personRowMapper)
        assertThat(returnedRecord).isNotNull()
        assertThat(returnedRecord!!.lastName?.name).isEqualTo("Smith")
    }
}
