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
package examples.spring;

import static examples.spring.AddressDynamicSqlSupport.address;
import static examples.spring.PersonDynamicSqlSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.delete.DeleteModel;
import org.mybatis.dynamic.sql.insert.BatchInsertModel;
import org.mybatis.dynamic.sql.insert.GeneralInsertModel;
import org.mybatis.dynamic.sql.insert.InsertModel;
import org.mybatis.dynamic.sql.insert.MultiRowInsertModel;
import org.mybatis.dynamic.sql.insert.render.GeneralInsertStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.update.UpdateModel;
import org.mybatis.dynamic.sql.util.Buildable;
import org.mybatis.dynamic.sql.util.spring.NamedParameterJdbcTemplateExtensions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

@SpringJUnitConfig(classes = SpringConfiguration.class)
@Transactional
class PersonTemplateTest {

    @Autowired
    private NamedParameterJdbcTemplateExtensions template;

    @Test
    void testSelect() {
        Buildable<SelectModel> selectStatement = select(id, firstName, lastName, birthDate, employed, occupation, addressId)
                .from(person)
                .where(id, isEqualTo(1))
                .or(occupation, isNull());

        List<PersonRecord> rows = template.selectList(selectStatement, personRowMapper);

        assertThat(rows).hasSize(3);
    }

    @Test
    void testSelectAll() {
        Buildable<SelectModel> selectStatement = select(id, firstName, lastName, birthDate, employed, occupation, addressId)
                .from(person);

        List<PersonRecord> rows = template.selectList(selectStatement, personRowMapper);

        assertThat(rows).hasSize(6);
        assertThat(rows.get(0).id()).isEqualTo(1);
        assertThat(rows.get(5).id()).isEqualTo(6);

    }

    @Test
    void testSelectAllOrdered() {
        Buildable<SelectModel> selectStatement = select(id, firstName, lastName, birthDate, employed, occupation, addressId)
                .from(person)
                .orderBy(lastName.descending(), firstName.descending());

        List<PersonRecord> rows = template.selectList(selectStatement, personRowMapper);

        assertThat(rows).hasSize(6);
        assertThat(rows.get(0).id()).isEqualTo(5);
        assertThat(rows.get(5).id()).isEqualTo(1);

    }

    @Test
    void testSelectDistinct() {
        Buildable<SelectModel> selectStatement = selectDistinct(id, firstName, lastName, birthDate, employed, occupation, addressId)
                .from(person)
                .where(id, isGreaterThan(1))
                .or(occupation, isNull());

        List<PersonRecord> rows = template.selectList(selectStatement, personRowMapper);

        assertThat(rows).hasSize(5);
    }

    @Test
    void testSelectWithUnion() {
        Buildable<SelectModel> selectStatement = select(id, firstName, lastName, birthDate, employed, occupation, addressId)
                .from(person)
                .where(id, isEqualTo(1))
                .union()
                .select(id, firstName, lastName, birthDate, employed, occupation, addressId)
                .from(person)
                .where(id, isEqualTo(2))
                .union()
                .select(id, firstName, lastName, birthDate, employed, occupation, addressId)
                .from(person)
                .where(id, isEqualTo(2));


        List<PersonRecord> rows = template.selectList(selectStatement, personRowMapper);

        assertThat(rows).hasSize(2);
    }

    @Test
    void testSelectWithUnionAll() {
        Buildable<SelectModel> selectStatement = select(id, firstName, lastName, birthDate, employed, occupation, addressId)
                .from(person)
                .where(id, isEqualTo(1))
                .union()
                .select(id, firstName, lastName, birthDate, employed, occupation, addressId)
                .from(person)
                .where(id, isEqualTo(2))
                .unionAll()
                .select(id, firstName, lastName, birthDate, employed, occupation, addressId)
                .from(person)
                .where(id, isEqualTo(2));


        List<PersonRecord> rows = template.selectList(selectStatement, personRowMapper);

        assertThat(rows).hasSize(3);
    }

    @Test
    void testSelectWithTypeHandler() {
        Buildable<SelectModel> selectStatement = select(id, firstName, lastName, birthDate, employed, occupation, addressId)
                .from(person)
                .where(employed, isEqualTo(false))
                .orderBy(id);

        List<PersonRecord> rows = template.selectList(selectStatement, personRowMapper);

        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).id()).isEqualTo(3);
        assertThat(rows.get(1).id()).isEqualTo(6);
    }

    @Test
    void testSelectBetweenWithTypeHandler() {
        Buildable<SelectModel> selectStatement = select(id, firstName, lastName, birthDate, employed, occupation, addressId)
                .from(person)
                .where(lastName, isBetween(new LastName("Adams")).and(new LastName("Jones")))
                .orderBy(id);

        List<PersonRecord> rows = template.selectList(selectStatement, personRowMapper);

        assertThat(rows).hasSize(3);
        assertThat(rows.get(0).id()).isEqualTo(1);
        assertThat(rows.get(1).id()).isEqualTo(2);
    }

    @Test
    void testSelectListWithTypeHandler() {
        Buildable<SelectModel> selectStatement = select(id, firstName, lastName, birthDate, employed, occupation, addressId)
                .from(person)
                .where(lastName, isIn(new LastName("Flintstone"), new LastName("Rubble")))
                .orderBy(id);

        List<PersonRecord> rows = template.selectList(selectStatement, personRowMapper);

        assertThat(rows).hasSize(6);
        assertThat(rows.get(0).id()).isEqualTo(1);
        assertThat(rows.get(1).id()).isEqualTo(2);
    }

    @Test
    void testSelectByPrimaryKeyWithMissingRecord() {
        Buildable<SelectModel> selectStatement = select(id, firstName, lastName, birthDate, employed, occupation, addressId)
                .from(person)
                .where(id, isEqualTo(300));

        Optional<PersonRecord> row = template.selectOne(selectStatement, personRowMapper);

        assertThat(row).isNotPresent();
    }

    @Test
    void testFirstNameIn() {
        Buildable<SelectModel> selectStatement = select(id, firstName, lastName, birthDate, employed, occupation, addressId)
                .from(person)
                .where(firstName, isIn("Fred", "Barney"));

        List<PersonRecord> rows = template.selectList(selectStatement, personRowMapper);

        assertThat(rows).hasSize(2);

        assertThat(rows).satisfiesExactly(
                person1 -> assertThat(person1).isNotNull()
                        .extracting("lastName").isNotNull()
                        .extracting("name").isEqualTo("Flintstone"),
                person2 -> assertThat(person2).isNotNull()
                        .extracting("lastName").isNotNull()
                        .extracting("name").isEqualTo("Rubble")
        );
    }

    @Test
    void testDelete() {
        Buildable<DeleteModel> deleteStatement = deleteFrom(person)
                .where(occupation, isNull());

        int rows = template.delete(deleteStatement);

        assertThat(rows).isEqualTo(2);
    }

    @Test
    void testDeleteAll() {
        Buildable<DeleteModel> deleteStatement = deleteFrom(person);

        int rows = template.delete(deleteStatement);

        assertThat(rows).isEqualTo(6);
    }

    @Test
    void testDeleteByPrimaryKey() {
        Buildable<DeleteModel> deleteStatement = deleteFrom(person)
                .where(id,  isEqualTo(2));

        int rows = template.delete(deleteStatement);

        assertThat(rows).isEqualTo(1);
    }

    @Test
    void testInsert() {
        PersonRecord row = new PersonRecord(100, "Joe", new LastName("Jones"), new Date(), true, "Developer", 1);

        Buildable<InsertModel<PersonRecord>> insertStatement = insert(row).into(person)
                .map(id).toProperty("id")
                .map(firstName).toProperty("firstName")
                .map(lastName).toProperty("lastNameAsString")
                .map(birthDate).toProperty("birthDate")
                .map(employed).toProperty("employedAsString")
                .map(occupation).toProperty("occupation")
                .map(addressId).toProperty("addressId");

        int rows = template.insert(insertStatement);

        assertThat(rows).isEqualTo(1);
    }

    @Test
    void testGeneralInsert() {
        Buildable<GeneralInsertModel> insertStatement = insertInto(person)
                .set(id).toValue(100)
                .set(firstName).toValue("Joe")
                .set(lastName).toValue(new LastName("Jones"))
                .set(birthDate).toValue(new Date())
                .set(employed).toValue(true)
                .set(occupation).toValue("Developer")
                .set(addressId).toValue(1);

        int rows = template.generalInsert(insertStatement);

        assertThat(rows).isEqualTo(1);
    }

    @Test
    void testInsertMultiple() {

        List<PersonRecord> records = List.of(
                new PersonRecord(100, "Joe", new LastName("Jones"), new Date(), true, "Developer", 1),
                new PersonRecord(101, "Sarah", new LastName("Smith"), new Date(), true, "Architect", 2));

        Buildable<MultiRowInsertModel<PersonRecord>> insertStatement = insertMultiple(records).into(person)
                .map(id).toProperty("id")
                .map(firstName).toProperty("firstName")
                .map(lastName).toProperty("lastNameAsString")
                .map(birthDate).toProperty("birthDate")
                .map(employed).toProperty("employedAsString")
                .map(occupation).toProperty("occupation")
                .map(addressId).toProperty("addressId");

        int rows = template.insertMultiple(insertStatement);

        assertThat(rows).isEqualTo(2);
    }

    @Test
    void testInsertBatch() {

        List<PersonRecord> records = List.of(
                new PersonRecord(100, "Joe", new LastName("Jones"), new Date(), true, "Developer", 1),
                new PersonRecord(101, "Sarah", new LastName("Smith"), new Date(), true, "Architect", 2));

        Buildable<BatchInsertModel<PersonRecord>> insertStatement = insertBatch(records).into(person)
                .map(id).toProperty("id")
                .map(firstName).toProperty("firstName")
                .map(lastName).toProperty("lastNameAsString")
                .map(birthDate).toProperty("birthDate")
                .map(employed).toProperty("employedAsString")
                .map(occupation).toProperty("occupation")
                .map(addressId).toProperty("addressId");

        int[] rows = template.insertBatch(insertStatement);

        assertThat(rows).hasSize(2);
        assertThat(rows[0]).isEqualTo(1);
        assertThat(rows[1]).isEqualTo(1);
    }

    @Test
    void testInsertSelective() {
        PersonRecord row = new PersonRecord(100, "Joe", new LastName("Jones"), new Date(), false, null, 1);

        Buildable<InsertModel<PersonRecord>> insertStatement = insert(row).into(person)
                .map(id).toPropertyWhenPresent("id", row::id)
                .map(firstName).toPropertyWhenPresent("firstName", row::firstName)
                .map(lastName).toPropertyWhenPresent("lastNameAsString", row::getLastNameAsString)
                .map(birthDate).toPropertyWhenPresent("birthDate", row::birthDate)
                .map(employed).toPropertyWhenPresent("employedAsString", row::getEmployedAsString)
                .map(occupation).toPropertyWhenPresent("occupation", row::occupation)
                .map(addressId).toPropertyWhenPresent("addressId", row::addressId);

        int rows = template.insert(insertStatement);

        assertThat(rows).isEqualTo(1);
    }

    @Test
    void testGeneralInsertWhenTypeConverterReturnsNull() {

        GeneralInsertStatementProvider insertStatement = insertInto(person)
                .set(id).toValue(100)
                .set(firstName).toValue("Joe")
                .set(lastName).toValueWhenPresent(new LastName("Slate"))
                .set(birthDate).toValue(new Date())
                .set(employed).toValue(true)
                .set(occupation).toValue("Quarry Owner")
                .set(addressId).toValue(1)
                .build()
                .render(RenderingStrategies.SPRING_NAMED_PARAMETER);

        assertThat(insertStatement.getInsertStatement())
                .isEqualTo("insert into Person (id, first_name, birth_date, employed, occupation, address_id) values (:p1, :p2, :p3, :p4, :p5, :p6)");
        int rows = template.generalInsert(insertStatement);
        assertThat(rows).isEqualTo(1);

        Buildable<SelectModel> selectStatement = select(id, firstName, lastName, birthDate, employed, occupation, addressId)
                .from(person)
                .where(id, isEqualTo(100));
        Optional<PersonRecord> newRecord = template.selectOne(selectStatement, personRowMapper);
        assertThat(newRecord).hasValueSatisfying(
                r -> assertThat(r).isNotNull().extracting("lastName").isNotNull().extracting("name").isNull()
        );
    }

    @Test
    void testUpdateByPrimaryKey() {

        Buildable<GeneralInsertModel> insertStatement = insertInto(person)
                .set(id).toValue(100)
                .set(firstName).toValue("Joe")
                .set(lastName).toValue(new LastName("Jones"))
                .set(birthDate).toValue(new Date())
                .set(employed).toValue(true)
                .set(occupation).toValue("Developer")
                .set(addressId).toValue(1);

        int rows = template.generalInsert(insertStatement);
        assertThat(rows).isEqualTo(1);

        Buildable<UpdateModel> updateStatement = update(person)
                .set(occupation).equalTo("Programmer")
                .where(id, isEqualTo(100));

        rows = template.update(updateStatement);
        assertThat(rows).isEqualTo(1);

        Buildable<SelectModel> selectStatement = select(id, firstName, lastName, birthDate, employed, occupation, addressId)
                .from(person)
                .where(id, isEqualTo(100));
        Optional<PersonRecord> newRecord = template.selectOne(selectStatement, personRowMapper);
        assertThat(newRecord).hasValueSatisfying(r -> assertThat(r.occupation()).isEqualTo("Programmer"));
    }

    @Test
    void testUpdateByPrimaryKeyWithTypeHandler() {

        Buildable<GeneralInsertModel> insertStatement = insertInto(person)
                .set(id).toValue(100)
                .set(firstName).toValue("Joe")
                .set(lastName).toValue(new LastName("Jones"))
                .set(birthDate).toValue(new Date())
                .set(employed).toValue(true)
                .set(occupation).toValue("Developer")
                .set(addressId).toValue(1);

        int rows = template.generalInsert(insertStatement);
        assertThat(rows).isEqualTo(1);

        Buildable<UpdateModel> updateStatement = update(person)
                .set(lastName).equalTo(new LastName("Smith"))
                .where(id, isEqualTo(100));

        rows = template.update(updateStatement);
        assertThat(rows).isEqualTo(1);

        Buildable<SelectModel> selectStatement = select(id, firstName, lastName, birthDate, employed, occupation, addressId)
                .from(person)
                .where(id, isEqualTo(100));
        Optional<PersonRecord> newRecord = template.selectOne(selectStatement, personRowMapper);
        assertThat(newRecord).hasValueSatisfying(r -> assertThat(r).isNotNull()
                .extracting("lastName").isNotNull()
                .extracting("name").isEqualTo("Smith")
        );
    }

    @Test
    void testUpdateByPrimaryKeySelective() {
        Buildable<GeneralInsertModel> insertStatement = insertInto(person)
                .set(id).toValue(100)
                .set(firstName).toValue("Joe")
                .set(lastName).toValue(new LastName("Jones"))
                .set(birthDate).toValue(new Date())
                .set(employed).toValue(true)
                .set(occupation).toValue("Developer")
                .set(addressId).toValue(1);

        int rows = template.generalInsert(insertStatement);
        assertThat(rows).isEqualTo(1);

        PersonRecord updateRecord = new PersonRecord(100, null, null, null, null, "Programmer", null);

        Buildable<UpdateModel> updateStatement = update(person)
                .set(firstName).equalToWhenPresent(updateRecord::firstName)
                .set(lastName).equalToWhenPresent(updateRecord::lastName)
                .set(birthDate).equalToWhenPresent(updateRecord::birthDate)
                .set(employed).equalToWhenPresent(updateRecord::employed)
                .set(occupation).equalToWhenPresent(updateRecord::occupation)
                .set(addressId).equalToWhenPresent(updateRecord::addressId)
                .where(id, isEqualTo(updateRecord::id));

        rows = template.update(updateStatement);
        assertThat(rows).isEqualTo(1);

        Buildable<SelectModel> selectStatement = select(id, firstName, lastName, birthDate, employed, occupation, addressId)
                .from(person)
                .where(id, isEqualTo(100));
        Optional<PersonRecord> newRecord = template.selectOne(selectStatement, personRowMapper);
        assertThat(newRecord).hasValueSatisfying(r -> {
            assertThat(r.occupation()).isEqualTo("Programmer");
            assertThat(r.firstName()).isEqualTo("Joe");
        });
    }

    @Test
    void testUpdate() {
        PersonRecord row = new PersonRecord(100, "Joe", new LastName("Jones"), new Date(), true, "Developer", 1);

        Buildable<InsertModel<PersonRecord>> insertStatement = insert(row).into(person)
                .map(id).toProperty("id")
                .map(firstName).toProperty("firstName")
                .map(lastName).toProperty("lastNameAsString")
                .map(birthDate).toProperty("birthDate")
                .map(employed).toProperty("employedAsString")
                .map(occupation).toProperty("occupation")
                .map(addressId).toProperty("addressId");

        int rows = template.insert(insertStatement);
        assertThat(rows).isEqualTo(1);

        row = row.withOccupation("Programmer");

        Buildable<UpdateModel> updateStatement = update(person)
                .set(firstName).equalToWhenPresent(row::firstName)
                .set(lastName).equalToWhenPresent(row::lastName)
                .set(birthDate).equalToWhenPresent(row::birthDate)
                .set(employed).equalToWhenPresent(row::employed)
                .set(occupation).equalToWhenPresent(row::occupation)
                .set(addressId).equalToWhenPresent(row::addressId)
                .where(id, isEqualTo(row::id));

        rows = template.update(updateStatement);
        assertThat(rows).isEqualTo(1);

        Buildable<SelectModel> selectStatement = select(id, firstName, lastName, birthDate, employed, occupation, addressId)
                .from(person)
                .where(id, isEqualTo(100));
        Optional<PersonRecord> newRecord = template.selectOne(selectStatement, personRowMapper);
        assertThat(newRecord).hasValueSatisfying(r -> {
            assertThat(r.occupation()).isEqualTo("Programmer");
            assertThat(r.firstName()).isEqualTo("Joe");
        });
    }

    @Test
    void testUpdateAll() {
        Buildable<GeneralInsertModel> insertStatement = insertInto(person)
                .set(id).toValue(100)
                .set(firstName).toValue("Joe")
                .set(lastName).toValue(new LastName("Jones"))
                .set(birthDate).toValue(new Date())
                .set(employed).toValue(true)
                .set(occupation).toValue("Developer")
                .set(addressId).toValue(1);

        int rows = template.generalInsert(insertStatement);
        assertThat(rows).isEqualTo(1);

        Buildable<UpdateModel> updateStatement = update(person)
                .set(occupation).equalTo("Programmer");

        rows = template.update(updateStatement);
        assertThat(rows).isEqualTo(7);

        Buildable<SelectModel> selectStatement = select(id, firstName, lastName, birthDate, employed, occupation, addressId)
                .from(person)
                .where(id, isEqualTo(100));

        Optional<PersonRecord> newRecord = template.selectOne(selectStatement, personRowMapper);
        assertThat(newRecord).hasValueSatisfying(r -> assertThat(r.occupation()).isEqualTo("Programmer"));
    }

    @Test
    void testCount() {
        Buildable<SelectModel> countStatement = countFrom(person)
                .where(occupation, isNull());

        long rows = template.count(countStatement);
        assertThat(rows).isEqualTo(2L);
    }

    @Test
    void testCountAll() {
        Buildable<SelectModel> countStatement = countFrom(person);

        long rows = template.count(countStatement);
        assertThat(rows).isEqualTo(6L);
    }

    @Test
    void testCountLastName() {
        Buildable<SelectModel> countStatement = countColumn(lastName).from(person);

        long rows = template.count(countStatement);
        assertThat(rows).isEqualTo(6L);
    }

    @Test
    void testCountDistinctLastName() {
        Buildable<SelectModel> countStatement = countDistinctColumn(lastName).from(person);

        long rows = template.count(countStatement);
        assertThat(rows).isEqualTo(2L);
    }

    @Test
    void testTypeHandledLike() {
        Buildable<SelectModel> selectStatement = select(id, firstName, lastName, birthDate, employed, occupation, addressId)
                .from(person)
                .where(lastName, isLike(new LastName("Fl%")))
                .orderBy(id);

        List<PersonRecord> rows = template.selectList(selectStatement, personRowMapper);
        assertThat(rows).hasSize(3);
        assertThat(rows.get(0).firstName()).isEqualTo("Fred");
    }

    @Test
    void testTypeHandledNotLike() {
        Buildable<SelectModel> selectStatement = select(id, firstName, lastName, birthDate, employed, occupation, addressId)
                .from(person)
                .where(lastName, isNotLike(new LastName("Fl%")))
                .orderBy(id);

        List<PersonRecord> rows = template.selectList(selectStatement, personRowMapper);

        assertThat(rows).hasSize(3);
        assertThat(rows.get(0).firstName()).isEqualTo("Barney");
    }

    @Test
    void testAutoMapping() {
        Buildable<SelectModel> selectStatement = select(address.id.as("id"), address.streetAddress,
                address.city, address.state)
                .from(address)
                .orderBy(address.id);


        List<AddressRecord> records = template.selectList(selectStatement,
                DataClassRowMapper.newInstance(AddressRecord.class));

        assertThat(records).hasSize(2);
        assertThat(records.get(0).id()).isEqualTo(1);
        assertThat(records.get(0).streetAddress()).isEqualTo("123 Main Street");
        assertThat(records.get(0).city()).isEqualTo("Bedrock");
        assertThat(records.get(0).state()).isEqualTo("IN");
    }

    @Test
    void testJoinAllRows() {
        Buildable<SelectModel> selectStatement = select(id, firstName, lastName, birthDate, employed, occupation,
                address.id, address.streetAddress, address.city, address.state)
                .from(person)
                .join(address, on(person.addressId, isEqualTo(address.id)))
                .orderBy(id);

        List<PersonWithAddress> records = template.selectList(selectStatement, personWithAddressRowMapper);

        assertThat(records).hasSize(6);
        assertThat(records.get(0).id()).isEqualTo(1);
        assertThat(records.get(0).employed()).isTrue();
        assertThat(records.get(0).firstName()).isEqualTo("Fred");
        assertThat(records.get(0).lastName()).isEqualTo(new LastName("Flintstone"));
        assertThat(records.get(0).occupation()).isEqualTo("Brontosaurus Operator");
        assertThat(records.get(0).birthDate()).isNotNull();
        assertThat(records.get(0).address().id()).isEqualTo(1);
        assertThat(records.get(0).address().streetAddress()).isEqualTo("123 Main Street");
        assertThat(records.get(0).address().city()).isEqualTo("Bedrock");
        assertThat(records.get(0).address().state()).isEqualTo("IN");
    }

    @Test
    void testJoinOneRow() {
        Buildable<SelectModel> selectStatement = select(id, firstName, lastName, birthDate, employed, occupation,
                address.id, address.streetAddress, address.city, address.state)
                .from(person)
                .join(address, on(person.addressId, isEqualTo(address.id)))
                .where(id, isEqualTo(1));

        List<PersonWithAddress> records = template.selectList(selectStatement, personWithAddressRowMapper);

        assertThat(records).hasSize(1);
        assertThat(records.get(0).id()).isEqualTo(1);
        assertThat(records.get(0).employed()).isTrue();
        assertThat(records.get(0).firstName()).isEqualTo("Fred");
        assertThat(records.get(0).lastName()).isEqualTo(new LastName("Flintstone"));
        assertThat(records.get(0).occupation()).isEqualTo("Brontosaurus Operator");
        assertThat(records.get(0).birthDate()).isNotNull();
        assertThat(records.get(0).address().id()).isEqualTo(1);
        assertThat(records.get(0).address().streetAddress()).isEqualTo("123 Main Street");
        assertThat(records.get(0).address().city()).isEqualTo("Bedrock");
        assertThat(records.get(0).address().state()).isEqualTo("IN");
    }

    @Test
    void testJoinPrimaryKey() {
        Buildable<SelectModel> selectStatement = select(id, firstName, lastName, birthDate, employed, occupation,
                address.id, address.streetAddress, address.city, address.state)
                .from(person)
                .join(address, on(person.addressId, isEqualTo(address.id)))
                .where(id, isEqualTo(1));

        Optional<PersonWithAddress> row = template.selectOne(selectStatement, personWithAddressRowMapper);

        assertThat(row).hasValueSatisfying(r -> {
            assertThat(r.id()).isEqualTo(1);
            assertThat(r.employed()).isTrue();
            assertThat(r.firstName()).isEqualTo("Fred");
            assertThat(r.lastName()).isEqualTo(new LastName("Flintstone"));
            assertThat(r.occupation()).isEqualTo("Brontosaurus Operator");
            assertThat(r.birthDate()).isNotNull();
            assertThat(r.address().id()).isEqualTo(1);
            assertThat(r.address().streetAddress()).isEqualTo("123 Main Street");
            assertThat(r.address().city()).isEqualTo("Bedrock");
            assertThat(r.address().state()).isEqualTo("IN");
        });
    }

    @Test
    void testJoinPrimaryKeyInvalidRecord() {
        Buildable<SelectModel> selectStatement = select(id, firstName, lastName, birthDate, employed, occupation,
                address.id, address.streetAddress, address.city, address.state)
                .from(person)
                .join(address, on(person.addressId, isEqualTo(address.id)))
                .where(id, isEqualTo(55));

        Optional<PersonWithAddress> row = template.selectOne(selectStatement, personWithAddressRowMapper);
        assertThat(row).isEmpty();
    }

    @Test
    void testJoinCount() {
        Buildable<SelectModel> countStatement = countFrom(person)
                .join(address, on(person.addressId, isEqualTo(address.id)))
                .where(id, isEqualTo(55));

        long count = template.count(countStatement);
        assertThat(count).isZero();
    }

    @Test
    void testJoinCountWithSubCriteria() {
        Buildable<SelectModel> countStatement = countFrom(person)
                .join(address, on(person.addressId, isEqualTo(address.id)))
                .where(person.id, isEqualTo(55), or(person.id, isEqualTo(1)));

        long count = template.count(countStatement);
        assertThat(count).isEqualTo(1);
    }

    private final RowMapper<PersonWithAddress> personWithAddressRowMapper =
            (rs, i) -> new PersonWithAddress(rs.getInt(1),
                    rs.getString(2),
                    new LastName(rs.getString(3)),
                    rs.getTimestamp(4),
                    "Yes".equals(rs.getString(5)),
                    rs.getString(6),
                    new AddressRecord(rs.getInt(7),
                            rs.getString(8),
                            rs.getString(9),
                            rs.getString(10)));


    static final RowMapper<PersonRecord> personRowMapper =
            (rs, i) -> new PersonRecord(rs.getInt(1),
                    rs.getString(2),
                    new LastName(rs.getString(3)),
                    rs.getTimestamp(4),
                    "Yes".equals(rs.getString(5)),
                    rs.getString(6),
                    rs.getInt(7));
}
