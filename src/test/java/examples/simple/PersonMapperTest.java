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
package examples.simple;

import static examples.simple.AddressDynamicSqlSupport.address;
import static examples.simple.PersonDynamicSqlSupport.addressId;
import static examples.simple.PersonDynamicSqlSupport.birthDate;
import static examples.simple.PersonDynamicSqlSupport.employed;
import static examples.simple.PersonDynamicSqlSupport.firstName;
import static examples.simple.PersonDynamicSqlSupport.id;
import static examples.simple.PersonDynamicSqlSupport.lastName;
import static examples.simple.PersonDynamicSqlSupport.occupation;
import static examples.simple.PersonDynamicSqlSupport.person;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.SortSpecification;
import org.mybatis.dynamic.sql.delete.DeleteDSLCompleter;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.insert.render.GeneralInsertStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.CountDSLCompleter;
import org.mybatis.dynamic.sql.select.SelectDSLCompleter;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;

class PersonMapperTest {

    private static final String JDBC_URL = "jdbc:hsqldb:mem:aname";
    private static final String JDBC_DRIVER = "org.hsqldb.jdbcDriver";

    private SqlSessionFactory sqlSessionFactory;

    @BeforeEach
    void setup() throws Exception {
        Class.forName(JDBC_DRIVER);
        InputStream is = getClass().getResourceAsStream("/examples/simple/CreateSimpleDB.sql");
        try (Connection connection = DriverManager.getConnection(JDBC_URL, "sa", "")) {
            ScriptRunner sr = new ScriptRunner(connection);
            sr.setLogWriter(null);
            sr.runScript(new InputStreamReader(is));
        }

        UnpooledDataSource ds = new UnpooledDataSource(JDBC_DRIVER, JDBC_URL, "sa", "");
        Environment environment = new Environment("test", new JdbcTransactionFactory(), ds);
        Configuration config = new Configuration(environment);
        config.addMapper(PersonMapper.class);
        config.addMapper(PersonWithAddressMapper.class);
        config.addMapper(AddressMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(config);
    }

    @Test
    void testSelect() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);

            List<PersonRecord> rows = mapper.select(c ->
                    c.where(id, isEqualTo(1))
                    .or(occupation, isNull()));

            assertThat(rows).hasSize(3);
        }
    }

    @Test
    void testSelectEmployed() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);

            List<PersonRecord> rows = mapper.select(c ->
                    c.where(employed, isTrue())
                    .orderBy(id));

            assertThat(rows).hasSize(4);
            assertThat(rows.get(0).getId()).isEqualTo(1);
        }
    }

    @Test
    void testSelectUnemployed() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);

            List<PersonRecord> rows = mapper.select(c ->
                    c.where(employed, isFalse())
                            .orderBy(id));

            assertThat(rows).hasSize(2);
            assertThat(rows.get(0).getId()).isEqualTo(3);
        }
    }

    @Test
    void testSelectWithTypeConversion() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);

            List<PersonRecord> rows = mapper.select(c ->
                    c.where(id, isEqualTo("1").map(Integer::parseInt))
                            .or(occupation, isNull()));

            assertThat(rows).hasSize(3);
        }
    }

    @Test
    void testSelectWithTypeConversionAndFilterAndNull() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);

            List<PersonRecord> rows = mapper.select(c ->
                    c.where(id, isEqualTo((String) null).filter(Objects::nonNull).map(Integer::parseInt))
                            .or(occupation, isNull()));

            assertThat(rows).hasSize(2);
        }
    }

    // this example is in the quick start documentation...
    @Test
    void testGeneralSelect() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);

            SelectStatementProvider selectStatement = select(id.as("A_ID"), firstName, lastName, birthDate, employed,
                        occupation, addressId)
                    .from(person)
                    .where(id, isEqualTo(1))
                    .or(occupation, isNull())
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            List<PersonRecord> rows = mapper.selectMany(selectStatement);
            assertThat(rows).hasSize(3);
        }
    }

    @Test
    void testSelectAll() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);

            List<PersonRecord> rows = mapper.select(SelectDSLCompleter.allRows());

            assertThat(rows).hasSize(6);
            assertThat(rows.get(0).getId()).isEqualTo(1);
            assertThat(rows.get(5).getId()).isEqualTo(6);
        }
    }

    @Test
    void testSelectAllOrdered() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);

            List<PersonRecord> rows = mapper
                    .select(SelectDSLCompleter.allRowsOrderedBy(lastName.descending(), firstName.descending()));

            assertThat(rows).hasSize(6);
            assertThat(rows.get(0).getId()).isEqualTo(5);
            assertThat(rows.get(5).getId()).isEqualTo(1);
        }
    }

    @Test
    void testSelectDistinct() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);

            List<PersonRecord> rows = mapper.selectDistinct(c ->
                    c.where(id, isGreaterThan(1))
                    .or(occupation, isNull()));

            assertThat(rows).hasSize(5);
        }
    }

    @Test
    void testSelectWithTypeHandler() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);

            List<PersonRecord> rows = mapper.select(c ->
                    c.where(employed, isEqualTo(false))
                    .orderBy(id));

            assertAll(
                    () -> assertThat(rows).hasSize(2),
                    () -> assertThat(rows.get(0).getId()).isEqualTo(3),
                    () -> assertThat(rows.get(1).getId()).isEqualTo(6)
            );
        }
    }

    @Test
    void testSelectByPrimaryKeyWithMissingRecord() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);

            Optional<PersonRecord> record = mapper.selectByPrimaryKey(300);
            assertThat(record).isNotPresent();
        }
    }

    @Test
    void testFirstNameIn() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);

            List<PersonRecord> rows = mapper.select(c ->
                    c.where(firstName, isIn("Fred", "Barney")));

            assertAll(
                    () -> assertThat(rows).hasSize(2),
                    () -> assertThat(rows.get(0).getLastName().getName()).isEqualTo("Flintstone"),
                    () -> assertThat(rows.get(1).getLastName().getName()).isEqualTo("Rubble")
            );
        }
    }

    @Test
    void testOrderByCollection() {
        Collection<SortSpecification> orderByColumns = new ArrayList<>();
        orderByColumns.add(firstName);

        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);

            List<PersonRecord> rows = mapper.select(c -> c
                    .where(firstName, isIn("Fred", "Barney"))
                    .orderBy(orderByColumns)
            );

            assertThat(rows).hasSize(2);
            assertThat(rows.get(0).getLastName().getName()).isEqualTo("Rubble");
            assertThat(rows.get(1).getLastName().getName()).isEqualTo("Flintstone");
        }
    }

    @Test
    void testDelete() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);
            int rows = mapper.delete(c ->
                    c.where(occupation, isNull()));
            assertThat(rows).isEqualTo(2);
        }
    }

    // this test is in the quick start documentation
    @Test
    void testGeneralDelete() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);

            DeleteStatementProvider deleteStatement = deleteFrom(person)
                    .where(occupation, isNull())
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            int rows = mapper.delete(deleteStatement);
            assertThat(rows).isEqualTo(2);
        }
    }

    @Test
    void testDeleteAll() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);
            int rows = mapper.delete(DeleteDSLCompleter.allRows());

            assertThat(rows).isEqualTo(6);
        }
    }

    @Test
    void testDeleteByPrimaryKey() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);
            int rows = mapper.deleteByPrimaryKey(2);

            assertThat(rows).isEqualTo(1);
        }
    }

    @Test
    void testInsert() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);
            PersonRecord record = new PersonRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName(LastName.of("Jones"));
            record.setBirthDate(new Date());
            record.setEmployed(true);
            record.setOccupation("Developer");
            record.setAddressId(1);

            int rows = mapper.insert(record);
            assertThat(rows).isEqualTo(1);
        }
    }

    @Test
    void testGeneralInsert() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);
            int rows = mapper.generalInsert(c ->
                c.set(id).toValue(100)
                .set(firstName).toValue("Joe")
                .set(lastName).toValue(LastName.of("Jones"))
                .set(birthDate).toValue(new Date())
                .set(employed).toValue(true)
                .set(occupation).toValue("Developer")
                .set(addressId).toValue(1)
            );

            assertThat(rows).isEqualTo(1);
        }
    }

    @Test
    void testInsertMultiple() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);

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

            int rows = mapper.insertMultiple(records);
            assertThat(rows).isEqualTo(2);
        }
    }

    @Test
    void testInsertSelective() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);
            PersonRecord record = new PersonRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName(LastName.of("Jones"));
            record.setBirthDate(new Date());
            record.setEmployed(false);
            record.setAddressId(1);

            int rows = mapper.insertSelective(record);
            assertThat(rows).isEqualTo(1);
        }
    }

    @Test
    void testUpdateByPrimaryKey() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);
            PersonRecord record = new PersonRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName(LastName.of("Jones"));
            record.setBirthDate(new Date());
            record.setEmployed(true);
            record.setOccupation("Developer");
            record.setAddressId(1);

            int rows = mapper.insert(record);
            assertThat(rows).isEqualTo(1);

            record.setOccupation("Programmer");
            rows = mapper.updateByPrimaryKey(record);
            assertThat(rows).isEqualTo(1);

            Optional<PersonRecord> newRecord = mapper.selectByPrimaryKey(100);
            assertThat(newRecord).isPresent();
            assertThat(newRecord.get().getOccupation()).isEqualTo("Programmer");
        }
    }

    @Test
    void testUpdateByPrimaryKeySelective() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);
            PersonRecord record = new PersonRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName(LastName.of("Jones"));
            record.setBirthDate(new Date());
            record.setEmployed(true);
            record.setOccupation("Developer");
            record.setAddressId(1);

            int rows = mapper.insert(record);
            assertThat(rows).isEqualTo(1);

            PersonRecord updateRecord = new PersonRecord();
            updateRecord.setId(100);
            updateRecord.setOccupation("Programmer");
            rows = mapper.updateByPrimaryKeySelective(updateRecord);
            assertThat(rows).isEqualTo(1);

            Optional<PersonRecord> newRecord = mapper.selectByPrimaryKey(100);
            assertThat(newRecord).isPresent();
            assertThat(newRecord.get().getOccupation()).isEqualTo("Programmer");
            assertThat(newRecord.get().getFirstName()).isEqualTo("Joe");
        }
    }

    @Test
    void testUpdate() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);
            PersonRecord record = new PersonRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName(LastName.of("Jones"));
            record.setBirthDate(new Date());
            record.setEmployed(true);
            record.setOccupation("Developer");
            record.setAddressId(1);

            int rows = mapper.insert(record);
            assertThat(rows).isEqualTo(1);

            record.setOccupation("Programmer");

            rows = mapper.update(c ->
                PersonMapper.updateAllColumns(record, c)
                .where(id, isEqualTo(100))
                .and(firstName, isEqualTo("Joe")));

            assertThat(rows).isEqualTo(1);

            Optional<PersonRecord> newRecord = mapper.selectByPrimaryKey(100);
            assertThat(newRecord).isPresent();
            assertThat(newRecord.get().getOccupation()).isEqualTo("Programmer");
        }
    }

    @Test
    void testUpdateOneField() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);
            PersonRecord record = new PersonRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName(LastName.of("Jones"));
            record.setBirthDate(new Date());
            record.setEmployed(true);
            record.setOccupation("Developer");
            record.setAddressId(1);

            int rows = mapper.insert(record);
            assertThat(rows).isEqualTo(1);

            rows = mapper.update(c ->
                c.set(occupation).equalTo("Programmer")
                .where(id, isEqualTo(100)));

            assertThat(rows).isEqualTo(1);

            Optional<PersonRecord> newRecord = mapper.selectByPrimaryKey(100);
            assertThat(newRecord).isPresent();
            assertThat(newRecord.get().getOccupation()).isEqualTo("Programmer");
        }
    }

    @Test
    void testUpdateAll() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);
            PersonRecord record = new PersonRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName(LastName.of("Jones"));
            record.setBirthDate(new Date());
            record.setEmployed(true);
            record.setOccupation("Developer");
            record.setAddressId(1);

            int rows = mapper.insert(record);
            assertThat(rows).isEqualTo(1);

            PersonRecord updateRecord = new PersonRecord();
            updateRecord.setOccupation("Programmer");
            rows = mapper.update(c ->
                PersonMapper.updateSelectiveColumns(updateRecord, c));

            assertThat(rows).isEqualTo(7);

            Optional<PersonRecord> newRecord = mapper.selectByPrimaryKey(100);
            assertThat(newRecord).isPresent();
            assertThat(newRecord.get().getOccupation()).isEqualTo("Programmer");
        }
    }

    @Test
    void testUpdateSelective() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);
            PersonRecord record = new PersonRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName(LastName.of("Jones"));
            record.setBirthDate(new Date());
            record.setEmployed(true);
            record.setOccupation("Developer");
            record.setAddressId(1);

            int rows = mapper.insert(record);
            assertThat(rows).isEqualTo(1);

            PersonRecord updateRecord = new PersonRecord();
            updateRecord.setOccupation("Programmer");
            rows = mapper.update(c ->
                PersonMapper.updateSelectiveColumns(updateRecord, c)
                .where(id, isEqualTo(100)));

            assertThat(rows).isEqualTo(1);

            Optional<PersonRecord> newRecord = mapper.selectByPrimaryKey(100);
            assertThat(newRecord).isPresent();
            assertThat(newRecord.get().getOccupation()).isEqualTo("Programmer");
        }
    }

    @Test
    void testCount() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);
            long rows = mapper.count(c ->
                    c.where(occupation, isNull()));

            assertThat(rows).isEqualTo(2L);
        }
    }

    @Test
    void testCountAll() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);
            long rows = mapper.count(CountDSLCompleter.allRows());

            assertThat(rows).isEqualTo(6L);
        }
    }

    @Test
    void testCountLastName() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);
            long rows = mapper.count(lastName, CountDSLCompleter.allRows());

            assertThat(rows).isEqualTo(6L);
        }
    }

    @Test
    void testCountDistinctLastName() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);
            long rows = mapper.countDistinct(lastName, CountDSLCompleter.allRows());

            assertThat(rows).isEqualTo(2L);
        }
    }

    @Test
    void testTypeHandledLike() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);

            List<PersonRecord> rows = mapper.select(c ->
                    c.where(lastName, isLike(LastName.of("Fl%")))
                    .orderBy(id));

            assertThat(rows).hasSize(3);
            assertThat(rows.get(0).getFirstName()).isEqualTo("Fred");
        }
    }

    @Test
    void testTypeHandledNotLike() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapper mapper = session.getMapper(PersonMapper.class);

            List<PersonRecord> rows = mapper.select(c ->
                    c.where(lastName, isNotLike(LastName.of("Fl%")))
                    .orderBy(id));

            assertThat(rows).hasSize(3);
            assertThat(rows.get(0).getFirstName()).isEqualTo("Barney");
        }
    }

    @Test
    void testJoinAllRows() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonWithAddressMapper mapper = session.getMapper(PersonWithAddressMapper.class);
            List<PersonWithAddress> records = mapper.select(
                    SelectDSLCompleter.allRowsOrderedBy(id)
            );

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
            assertThat(records.get(0).getAddress().getAddressType()).isEqualTo(AddressRecord.AddressType.HOME);

            assertThat(records.get(4).getAddress().getAddressType()).isEqualTo(AddressRecord.AddressType.BUSINESS);
        }
    }

    @Test
    void testJoinOneRow() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonWithAddressMapper mapper = session.getMapper(PersonWithAddressMapper.class);
            List<PersonWithAddress> records = mapper.select(c -> c.where(id, isEqualTo(1)));

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
    }

    @Test
    void testJoinPrimaryKey() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonWithAddressMapper mapper = session.getMapper(PersonWithAddressMapper.class);
            Optional<PersonWithAddress> record = mapper.selectByPrimaryKey(1);

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
    }

    @Test
    void testJoinPrimaryKeyInvalidRecord() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonWithAddressMapper mapper = session.getMapper(PersonWithAddressMapper.class);
            Optional<PersonWithAddress> record = mapper.selectByPrimaryKey(55);

            assertThat(record).isEmpty();
        }
    }

    @Test
    void testJoinCount() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonWithAddressMapper mapper = session.getMapper(PersonWithAddressMapper.class);
            long count = mapper.count(c -> c.where(person.id, isEqualTo(55)));

            assertThat(count).isZero();
        }
    }

    @Test
    void testJoinCountWithSubcriteria() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonWithAddressMapper mapper = session.getMapper(PersonWithAddressMapper.class);
            long count = mapper.count(c -> c.where(person.id, isEqualTo(55), or(person.id, isEqualTo(1))));

            assertThat(count).isEqualTo(1);
        }
    }

    @Test
    void testWithEnumOrdinalTypeHandler() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            AddressMapper mapper = session.getMapper(AddressMapper.class);

            GeneralInsertStatementProvider insertStatement = insertInto(address)
                    .set(address.id).toValue(4)
                    .set(address.streetAddress).toValue("987 Elm Street")
                    .set(address.city).toValue("Mayberry")
                    .set(address.state).toValue("NC")
                    .set(address.addressType).toValue(AddressRecord.AddressType.HOME)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            int rows = mapper.generalInsert(insertStatement);
            assertThat(rows).isEqualTo(1);

            SelectStatementProvider selectStatement = select(address.addressType)
                    .from(address)
                    .where(address.id, isEqualTo(4))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            Optional<Integer> type = mapper.selectOptionalInteger(selectStatement);
            assertThat(type).hasValueSatisfying(i -> assertThat(i).isZero());
        }
    }
}
