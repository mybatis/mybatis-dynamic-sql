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
package examples.spring;

import static examples.spring.AddressDynamicSqlSupport.address;
import static examples.spring.PersonDynamicSqlSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.delete.DeleteModel;
import org.mybatis.dynamic.sql.insert.BatchInsertModel;
import org.mybatis.dynamic.sql.insert.GeneralInsertModel;
import org.mybatis.dynamic.sql.insert.InsertModel;
import org.mybatis.dynamic.sql.insert.MultiRowInsertModel;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.update.UpdateModel;
import org.mybatis.dynamic.sql.util.Buildable;
import org.mybatis.dynamic.sql.util.spring.NamedParameterJdbcTemplateExtensions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
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
        assertThat(rows.get(0).getId()).isEqualTo(1);
        assertThat(rows.get(5).getId()).isEqualTo(6);

    }

    @Test
    void testSelectAllOrdered() {
        Buildable<SelectModel> selectStatement = select(id, firstName, lastName, birthDate, employed, occupation, addressId)
                .from(person)
                .orderBy(lastName.descending(), firstName.descending());

        List<PersonRecord> rows = template.selectList(selectStatement, personRowMapper);

        assertThat(rows).hasSize(6);
        assertThat(rows.get(0).getId()).isEqualTo(5);
        assertThat(rows.get(5).getId()).isEqualTo(1);

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
        assertThat(rows.get(0).getId()).isEqualTo(3);
        assertThat(rows.get(1).getId()).isEqualTo(6);
    }

    @Test
    void testSelectBetweenWithTypeHandler() {
        Buildable<SelectModel> selectStatement = select(id, firstName, lastName, birthDate, employed, occupation, addressId)
                .from(person)
                .where(lastName, isBetween(LastName.of("Adams")).and(LastName.of("Jones")))
                .orderBy(id);

        List<PersonRecord> rows = template.selectList(selectStatement, personRowMapper);

        assertThat(rows).hasSize(3);
        assertThat(rows.get(0).getId()).isEqualTo(1);
        assertThat(rows.get(1).getId()).isEqualTo(2);
    }

    @Test
    void testSelectListWithTypeHandler() {
        Buildable<SelectModel> selectStatement = select(id, firstName, lastName, birthDate, employed, occupation, addressId)
                .from(person)
                .where(lastName, isIn(LastName.of("Flintstone"), LastName.of("Rubble")))
                .orderBy(id);

        List<PersonRecord> rows = template.selectList(selectStatement, personRowMapper);

        assertThat(rows).hasSize(6);
        assertThat(rows.get(0).getId()).isEqualTo(1);
        assertThat(rows.get(1).getId()).isEqualTo(2);
    }

    @Test
    void testSelectByPrimaryKeyWithMissingRecord() {
        Buildable<SelectModel> selectStatement = select(id, firstName, lastName, birthDate, employed, occupation, addressId)
                .from(person)
                .where(id, isEqualTo(300));

        Optional<PersonRecord> record = template.selectOne(selectStatement, personRowMapper);

        assertThat(record).isNotPresent();
    }

    @Test
    void testFirstNameIn() {
        Buildable<SelectModel> selectStatement = select(id, firstName, lastName, birthDate, employed, occupation, addressId)
                .from(person)
                .where(firstName, isIn("Fred", "Barney"));

        List<PersonRecord> rows = template.selectList(selectStatement, personRowMapper);

        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).getLastName().getName()).isEqualTo("Flintstone");
        assertThat(rows.get(1).getLastName().getName()).isEqualTo("Rubble");
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
        PersonRecord record = new PersonRecord();
        record.setId(100);
        record.setFirstName("Joe");
        record.setLastName(LastName.of("Jones"));
        record.setBirthDate(new Date());
        record.setEmployed(true);
        record.setOccupation("Developer");
        record.setAddressId(1);

        Buildable<InsertModel<PersonRecord>> insertStatement = insert(record).into(person)
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
                .set(lastName).toValue(LastName.of("Jones"))
                .set(birthDate).toValue(new Date())
                .set(employed).toValue(true)
                .set(occupation).toValue("Developer")
                .set(addressId).toValue(1);

        int rows = template.generalInsert(insertStatement);

        assertThat(rows).isEqualTo(1);
    }

    @Test
    void testInsertMultiple() {

        List<PersonRecord> records = new ArrayList<>();

        PersonRecord record = new PersonRecord();
        record.setId(100);
        record.setFirstName("Joe");
        record.setLastName(LastName.of("Jones"));
        record.setBirthDate(new Date());
        record.setEmployed(true);
        record.setOccupation("Developer");
        record.setAddressId(1);
        records.add(record);

        record = new PersonRecord();
        record.setId(101);
        record.setFirstName("Sarah");
        record.setLastName(LastName.of("Smith"));
        record.setBirthDate(new Date());
        record.setEmployed(true);
        record.setOccupation("Architect");
        record.setAddressId(2);
        records.add(record);

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

        List<PersonRecord> records = new ArrayList<>();

        PersonRecord record = new PersonRecord();
        record.setId(100);
        record.setFirstName("Joe");
        record.setLastName(LastName.of("Jones"));
        record.setBirthDate(new Date());
        record.setEmployed(true);
        record.setOccupation("Developer");
        record.setAddressId(1);
        records.add(record);

        record = new PersonRecord();
        record.setId(101);
        record.setFirstName("Sarah");
        record.setLastName(LastName.of("Smith"));
        record.setBirthDate(new Date());
        record.setEmployed(true);
        record.setOccupation("Architect");
        record.setAddressId(2);
        records.add(record);

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
        PersonRecord record = new PersonRecord();
        record.setId(100);
        record.setFirstName("Joe");
        record.setLastName(LastName.of("Jones"));
        record.setBirthDate(new Date());
        record.setEmployed(false);
        record.setAddressId(1);

        Buildable<InsertModel<PersonRecord>> insertStatement = insert(record).into(person)
                .map(id).toPropertyWhenPresent("id", record::getId)
                .map(firstName).toPropertyWhenPresent("firstName", record::getFirstName)
                .map(lastName).toPropertyWhenPresent("lastNameAsString", record::getLastNameAsString)
                .map(birthDate).toPropertyWhenPresent("birthDate", record::getBirthDate)
                .map(employed).toPropertyWhenPresent("employedAsString", record::getEmployedAsString)
                .map(occupation).toPropertyWhenPresent("occupation", record::getOccupation)
                .map(addressId).toPropertyWhenPresent("addressId", record::getAddressId);

        int rows = template.insert(insertStatement);

        assertThat(rows).isEqualTo(1);
    }

    @Test
    void testUpdateByPrimaryKey() {

        Buildable<GeneralInsertModel> insertStatement = insertInto(person)
                .set(id).toValue(100)
                .set(firstName).toValue("Joe")
                .set(lastName).toValue(LastName.of("Jones"))
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
        assertThat(newRecord).isPresent();
        assertThat(newRecord.get().getOccupation()).isEqualTo("Programmer");
    }

    @Test
    void testUpdateByPrimaryKeyWithTypeHandler() {

        Buildable<GeneralInsertModel> insertStatement = insertInto(person)
                .set(id).toValue(100)
                .set(firstName).toValue("Joe")
                .set(lastName).toValue(LastName.of("Jones"))
                .set(birthDate).toValue(new Date())
                .set(employed).toValue(true)
                .set(occupation).toValue("Developer")
                .set(addressId).toValue(1);

        int rows = template.generalInsert(insertStatement);
        assertThat(rows).isEqualTo(1);

        Buildable<UpdateModel> updateStatement = update(person)
                .set(lastName).equalTo(LastName.of("Smith"))
                .where(id, isEqualTo(100));

        rows = template.update(updateStatement);
        assertThat(rows).isEqualTo(1);

        Buildable<SelectModel> selectStatement = select(id, firstName, lastName, birthDate, employed, occupation, addressId)
                .from(person)
                .where(id, isEqualTo(100));
        Optional<PersonRecord> newRecord = template.selectOne(selectStatement, personRowMapper);
        assertThat(newRecord).isPresent();
        assertThat(newRecord.get().getLastName().getName()).isEqualTo("Smith");
    }

    @Test
    void testUpdateByPrimaryKeySelective() {
        Buildable<GeneralInsertModel> insertStatement = insertInto(person)
                .set(id).toValue(100)
                .set(firstName).toValue("Joe")
                .set(lastName).toValue(LastName.of("Jones"))
                .set(birthDate).toValue(new Date())
                .set(employed).toValue(true)
                .set(occupation).toValue("Developer")
                .set(addressId).toValue(1);

        int rows = template.generalInsert(insertStatement);
        assertThat(rows).isEqualTo(1);

        PersonRecord updateRecord = new PersonRecord();
        updateRecord.setId(100);
        updateRecord.setOccupation("Programmer");

        Buildable<UpdateModel> updateStatement = update(person)
                .set(firstName).equalToWhenPresent(updateRecord::getFirstName)
                .set(lastName).equalToWhenPresent(updateRecord::getLastName)
                .set(birthDate).equalToWhenPresent(updateRecord::getBirthDate)
                .set(employed).equalToWhenPresent(updateRecord::getEmployed)
                .set(occupation).equalToWhenPresent(updateRecord::getOccupation)
                .set(addressId).equalToWhenPresent(updateRecord::getAddressId)
                .where(id, isEqualTo(updateRecord::getId));

        rows = template.update(updateStatement);
        assertThat(rows).isEqualTo(1);

        Buildable<SelectModel> selectStatement = select(id, firstName, lastName, birthDate, employed, occupation, addressId)
                .from(person)
                .where(id, isEqualTo(100));
        Optional<PersonRecord> newRecord = template.selectOne(selectStatement, personRowMapper);
        assertThat(newRecord).isPresent();
        assertThat(newRecord.get().getOccupation()).isEqualTo("Programmer");
        assertThat(newRecord.get().getFirstName()).isEqualTo("Joe");
    }

    @Test
    void testUpdate() {
        PersonRecord record = new PersonRecord();
        record.setId(100);
        record.setFirstName("Joe");
        record.setLastName(LastName.of("Jones"));
        record.setBirthDate(new Date());
        record.setEmployed(true);
        record.setOccupation("Developer");
        record.setAddressId(1);

        Buildable<InsertModel<PersonRecord>> insertStatement = insert(record).into(person)
                .map(id).toProperty("id")
                .map(firstName).toProperty("firstName")
                .map(lastName).toProperty("lastNameAsString")
                .map(birthDate).toProperty("birthDate")
                .map(employed).toProperty("employedAsString")
                .map(occupation).toProperty("occupation")
                .map(addressId).toProperty("addressId");

        int rows = template.insert(insertStatement);
        assertThat(rows).isEqualTo(1);

        record.setOccupation("Programmer");

        Buildable<UpdateModel> updateStatement = update(person)
                .set(firstName).equalTo(record::getFirstName)
                .set(lastName).equalTo(record::getLastName)
                .set(birthDate).equalTo(record::getBirthDate)
                .set(employed).equalTo(record::getEmployed)
                .set(occupation).equalTo(record::getOccupation)
                .set(addressId).equalTo(record::getAddressId)
                .where(id, isEqualTo(record::getId));

        rows = template.update(updateStatement);
        assertThat(rows).isEqualTo(1);

        Buildable<SelectModel> selectStatement = select(id, firstName, lastName, birthDate, employed, occupation, addressId)
                .from(person)
                .where(id, isEqualTo(100));
        Optional<PersonRecord> newRecord = template.selectOne(selectStatement, personRowMapper);
        assertThat(newRecord).isPresent();
        assertThat(newRecord.get().getOccupation()).isEqualTo("Programmer");
        assertThat(newRecord.get().getFirstName()).isEqualTo("Joe");
    }

    @Test
    void testUpdateOneField() {
        Buildable<GeneralInsertModel> insertStatement = insertInto(person)
                .set(id).toValue(100)
                .set(firstName).toValue("Joe")
                .set(lastName).toValue(LastName.of("Jones"))
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
        assertThat(newRecord).isPresent();
        assertThat(newRecord.get().getOccupation()).isEqualTo("Programmer");
    }

    @Test
    void testUpdateAll() {
        Buildable<GeneralInsertModel> insertStatement = insertInto(person)
                .set(id).toValue(100)
                .set(firstName).toValue("Joe")
                .set(lastName).toValue(LastName.of("Jones"))
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
        assertThat(newRecord).isPresent();
        assertThat(newRecord.get().getOccupation()).isEqualTo("Programmer");
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
                .where(lastName, isLike(LastName.of("Fl%")))
                .orderBy(id);

        List<PersonRecord> rows = template.selectList(selectStatement, personRowMapper);
        assertThat(rows).hasSize(3);
        assertThat(rows.get(0).getFirstName()).isEqualTo("Fred");
    }

    @Test
    void testTypeHandledNotLike() {
        Buildable<SelectModel> selectStatement = select(id, firstName, lastName, birthDate, employed, occupation, addressId)
                .from(person)
                .where(lastName, isNotLike(LastName.of("Fl%")))
                .orderBy(id);

        List<PersonRecord> rows = template.selectList(selectStatement, personRowMapper);

        assertThat(rows).hasSize(3);
        assertThat(rows.get(0).getFirstName()).isEqualTo("Barney");
    }

    @Test
    void testAutoMapping() {
        Buildable<SelectModel> selectStatement = select(address.id.as("id"), address.streetAddress,
                address.city, address.state)
                .from(address)
                .orderBy(address.id);

        List<AddressRecord> records = template.selectList(selectStatement,
                BeanPropertyRowMapper.newInstance(AddressRecord.class));

        assertThat(records).hasSize(2);
        assertThat(records.get(0).getId()).isEqualTo(1);
        assertThat(records.get(0).getStreetAddress()).isEqualTo("123 Main Street");
        assertThat(records.get(0).getCity()).isEqualTo("Bedrock");
        assertThat(records.get(0).getState()).isEqualTo("IN");
    }

    @Test
    void testJoinAllRows() {
        Buildable<SelectModel> selectStatement = select(id, firstName, lastName, birthDate, employed, occupation,
                address.id, address.streetAddress, address.city, address.state)
                .from(person)
                .join(address, on(person.addressId, equalTo(address.id)))
                .orderBy(id);

        List<PersonWithAddress> records = template.selectList(selectStatement, personWithAddressRowMapper);

        assertThat(records).hasSize(6);
        assertThat(records.get(0).getId()).isEqualTo(1);
        assertThat(records.get(0).getEmployed()).isTrue();
        assertThat(records.get(0).getFirstName()).isEqualTo("Fred");
        assertThat(records.get(0).getLastName()).isEqualTo(LastName.of("Flintstone"));
        assertThat(records.get(0).getOccupation()).isEqualTo("Brontosaurus Operator");
        assertThat(records.get(0).getBirthDate()).isNotNull();
        assertThat(records.get(0).getAddress().getId()).isEqualTo(1);
        assertThat(records.get(0).getAddress().getStreetAddress()).isEqualTo("123 Main Street");
        assertThat(records.get(0).getAddress().getCity()).isEqualTo("Bedrock");
        assertThat(records.get(0).getAddress().getState()).isEqualTo("IN");
    }

    @Test
    void testJoinOneRow() {
        Buildable<SelectModel> selectStatement = select(id, firstName, lastName, birthDate, employed, occupation,
                address.id, address.streetAddress, address.city, address.state)
                .from(person)
                .join(address, on(person.addressId, equalTo(address.id)))
                .where(id, isEqualTo(1));

        List<PersonWithAddress> records = template.selectList(selectStatement, personWithAddressRowMapper);

        assertThat(records).hasSize(1);
        assertThat(records.get(0).getId()).isEqualTo(1);
        assertThat(records.get(0).getEmployed()).isTrue();
        assertThat(records.get(0).getFirstName()).isEqualTo("Fred");
        assertThat(records.get(0).getLastName()).isEqualTo(LastName.of("Flintstone"));
        assertThat(records.get(0).getOccupation()).isEqualTo("Brontosaurus Operator");
        assertThat(records.get(0).getBirthDate()).isNotNull();
        assertThat(records.get(0).getAddress().getId()).isEqualTo(1);
        assertThat(records.get(0).getAddress().getStreetAddress()).isEqualTo("123 Main Street");
        assertThat(records.get(0).getAddress().getCity()).isEqualTo("Bedrock");
        assertThat(records.get(0).getAddress().getState()).isEqualTo("IN");
    }

    @Test
    void testJoinPrimaryKey() {
        Buildable<SelectModel> selectStatement = select(id, firstName, lastName, birthDate, employed, occupation,
                address.id, address.streetAddress, address.city, address.state)
                .from(person)
                .join(address, on(person.addressId, equalTo(address.id)))
                .where(id, isEqualTo(1));

        Optional<PersonWithAddress> record = template.selectOne(selectStatement, personWithAddressRowMapper);

        assertThat(record).hasValueSatisfying(r -> {
            assertThat(r.getId()).isEqualTo(1);
            assertThat(r.getEmployed()).isTrue();
            assertThat(r.getFirstName()).isEqualTo("Fred");
            assertThat(r.getLastName()).isEqualTo(LastName.of("Flintstone"));
            assertThat(r.getOccupation()).isEqualTo("Brontosaurus Operator");
            assertThat(r.getBirthDate()).isNotNull();
            assertThat(r.getAddress().getId()).isEqualTo(1);
            assertThat(r.getAddress().getStreetAddress()).isEqualTo("123 Main Street");
            assertThat(r.getAddress().getCity()).isEqualTo("Bedrock");
            assertThat(r.getAddress().getState()).isEqualTo("IN");
        });
    }

    @Test
    void testJoinPrimaryKeyInvalidRecord() {
        Buildable<SelectModel> selectStatement = select(id, firstName, lastName, birthDate, employed, occupation,
                address.id, address.streetAddress, address.city, address.state)
                .from(person)
                .join(address, on(person.addressId, equalTo(address.id)))
                .where(id, isEqualTo(55));

        Optional<PersonWithAddress> record = template.selectOne(selectStatement, personWithAddressRowMapper);
        assertThat(record).isEmpty();
    }

    @Test
    void testJoinCount() {
        Buildable<SelectModel> countStatement = countFrom(person)
                .join(address, on(person.addressId, equalTo(address.id)))
                .where(id, isEqualTo(55));

        long count = template.count(countStatement);
        assertThat(count).isZero();
    }

    @Test
    void testJoinCountWithSubCriteria() {
        Buildable<SelectModel> countStatement = countFrom(person)
                .join(address, on(person.addressId, equalTo(address.id)))
                .where(person.id, isEqualTo(55), or(person.id, isEqualTo(1)));

        long count = template.count(countStatement);
        assertThat(count).isEqualTo(1);
    }

    private final RowMapper<PersonWithAddress> personWithAddressRowMapper =
            (rs, i) -> {
                PersonWithAddress record = new PersonWithAddress();
                record.setId(rs.getInt(1));
                record.setFirstName(rs.getString(2));
                record.setLastName(LastName.of(rs.getString(3)));
                record.setBirthDate(rs.getTimestamp(4));
                record.setEmployed("Yes".equals(rs.getString(5)));
                record.setOccupation(rs.getString(6));

                AddressRecord address = new AddressRecord();
                record.setAddress(address);
                address.setId(rs.getInt(7));
                address.setStreetAddress(rs.getString(8));
                address.setCity(rs.getString(9));
                address.setState(rs.getString(10));

                return record;
            };


    static RowMapper<PersonRecord> personRowMapper =
            (rs, i) -> {
                PersonRecord record = new PersonRecord();
                record.setId(rs.getInt(1));
                record.setFirstName(rs.getString(2));
                record.setLastName(LastName.of(rs.getString(3)));
                record.setBirthDate(rs.getTimestamp(4));
                record.setEmployed("Yes".equals(rs.getString(5)));
                record.setOccupation(rs.getString(6));
                record.setAddressId(rs.getInt(7));
                return record;
            };
}
