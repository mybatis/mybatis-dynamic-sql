/*
 *    Copyright 2016-2026 the original author or authors.
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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
import org.mybatis.dynamic.sql.AndOrCriteriaGroup;
import org.mybatis.dynamic.sql.ColumnAndConditionCriterion;
import org.mybatis.dynamic.sql.NullCriterion;
import org.mybatis.dynamic.sql.SortSpecification;
import org.mybatis.dynamic.sql.SqlCriterion;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.dsl.CountDSL;
import org.mybatis.dynamic.sql.dsl.CountDSLCompleter;
import org.mybatis.dynamic.sql.dsl.DeleteDSL;
import org.mybatis.dynamic.sql.dsl.DeleteDSLCompleter;
import org.mybatis.dynamic.sql.dsl.SelectDSL;
import org.mybatis.dynamic.sql.dsl.SelectDSLCompleter;
import org.mybatis.dynamic.sql.dsl.UpdateDSL;
import org.mybatis.dynamic.sql.exception.NonRenderingWhereClauseException;
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.HavingApplier;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.mybatis3.CommonSelectMapper;
import org.mybatis.dynamic.sql.where.WhereApplier;

class PersonMapperV2Test {

    private static final String JDBC_URL = "jdbc:hsqldb:mem:aname";
    private static final String JDBC_DRIVER = "org.hsqldb.jdbcDriver";

    private SqlSessionFactory sqlSessionFactory;

    @BeforeEach
    void setup() throws Exception {
        Class.forName(JDBC_DRIVER);
        try (InputStream is = getClass().getResourceAsStream("/examples/simple/CreateSimpleDB.sql")) {
            assert is != null;
            try (Connection connection = DriverManager.getConnection(JDBC_URL, "sa", "")) {
                ScriptRunner sr = new ScriptRunner(connection);
                sr.setLogWriter(null);
                sr.runScript(new InputStreamReader(is));
            }
        }

        UnpooledDataSource ds = new UnpooledDataSource(JDBC_DRIVER, JDBC_URL, "sa", "");
        Environment environment = new Environment("test", new JdbcTransactionFactory(), ds);
        Configuration config = new Configuration(environment);
        config.addMapper(PersonMapperV2.class);
        config.addMapper(PersonWithAddressMapperV2.class);
        config.addMapper(CommonSelectMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(config);
    }

    @Test
    void testSelect() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapperV2 mapper = session.getMapper(PersonMapperV2.class);

            List<PersonRecord> rows = mapper.select(c ->
                    c.where(id, isEqualTo(1))
                            .or(occupation, isNull()));

            assertThat(rows).hasSize(3);
        }
    }

    @Test
    void testSelectEmployed() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapperV2 mapper = session.getMapper(PersonMapperV2.class);

            List<PersonRecord> rows = mapper.select(c ->
                    c.where(employed, isTrue())
                            .orderBy(id));

            assertThat(rows).hasSize(4);
            assertThat(rows.get(0).id()).isEqualTo(1);
        }
    }

    @Test
    void testSelectUnemployed() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapperV2 mapper = session.getMapper(PersonMapperV2.class);

            List<PersonRecord> rows = mapper.select(c ->
                    c.where(employed, isFalse())
                            .orderBy(id));

            assertThat(rows).hasSize(2);
            assertThat(rows.get(0).id()).isEqualTo(3);
        }
    }

    @Test
    void testSelectWithTypeConversion() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapperV2 mapper = session.getMapper(PersonMapperV2.class);

            List<PersonRecord> rows = mapper.select(c ->
                    c.where(id, isEqualTo("1").map(Integer::parseInt))
                            .or(occupation, isNull()));

            assertThat(rows).hasSize(3);
        }
    }

    @Test
    void testSelectWithTypeConversionAndFilterAndNull() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapperV2 mapper = session.getMapper(PersonMapperV2.class);

            List<PersonRecord> rows = mapper.select(c ->
                    c.where(id, isEqualToWhenPresent((String) null).map(Integer::parseInt))
                            .or(occupation, isNull()));

            assertThat(rows).hasSize(2);
        }
    }

    // this example is in the quick start documentation...
    @Test
    void testGeneralSelect() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapperV2 mapper = session.getMapper(PersonMapperV2.class);

            SelectStatementProvider selectStatement = SelectDSL.select(id.as("A_ID"), firstName, lastName, birthDate, employed,
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
            PersonMapperV2 mapper = session.getMapper(PersonMapperV2.class);

            List<PersonRecord> rows = mapper.select(SelectDSLCompleter.allRows());

            assertThat(rows).hasSize(6);
            assertThat(rows.get(0).id()).isEqualTo(1);
            assertThat(rows.get(5).id()).isEqualTo(6);
        }
    }

    @Test
    void testSelectAllOrdered() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapperV2 mapper = session.getMapper(PersonMapperV2.class);

            List<PersonRecord> rows = mapper
                    .select(SelectDSLCompleter.allRowsOrderedBy(lastName.descending(), firstName.descending()));

            assertThat(rows).hasSize(6);
            assertThat(rows.get(0).id()).isEqualTo(5);
            assertThat(rows.get(5).id()).isEqualTo(1);
        }
    }

    @Test
    void testSelectDistinct() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapperV2 mapper = session.getMapper(PersonMapperV2.class);

            List<PersonRecord> rows = mapper.selectDistinct(c ->
                    c.where(id, isGreaterThan(1))
                            .or(occupation, isNull()));

            assertThat(rows).hasSize(5);
        }
    }

    @Test
    void testSelectWithTypeHandler() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapperV2 mapper = session.getMapper(PersonMapperV2.class);

            List<PersonRecord> rows = mapper.select(c ->
                    c.where(employed, isEqualTo(false))
                            .orderBy(id));

            assertThat(rows).hasSize(2);
            assertThat(rows.get(0).id()).isEqualTo(3);
            assertThat(rows.get(1).id()).isEqualTo(6);
        }
    }

    @Test
    void testSelectByPrimaryKeyWithMissingRecord() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapperV2 mapper = session.getMapper(PersonMapperV2.class);

            Optional<PersonRecord> row = mapper.selectByPrimaryKey(300);
            assertThat(row).isNotPresent();
        }
    }

    @Test
    void testFirstNameIn() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapperV2 mapper = session.getMapper(PersonMapperV2.class);

            List<PersonRecord> rows = mapper.select(c ->
                    c.where(firstName, isIn("Fred", "Barney")));

            assertThat(rows).hasSize(2);
            assertThat(rows.get(0))
                    .isNotNull()
                    .extracting("lastName").isNotNull()
                    .extracting("name").isEqualTo("Flintstone");
            assertThat(rows.get(1))
                    .isNotNull()
                    .extracting("lastName").isNotNull()
                    .extracting("name").isEqualTo("Rubble");
        }
    }

    @Test
    void testOrderByCollection() {
        Collection<SortSpecification> orderByColumns = List.of(firstName);

        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapperV2 mapper = session.getMapper(PersonMapperV2.class);

            List<PersonRecord> rows = mapper.select(c -> c
                    .where(firstName, isIn("Fred", "Barney"))
                    .orderBy(orderByColumns)
            );

            assertThat(rows).hasSize(2);
            assertThat(rows.get(0))
                    .isNotNull()
                    .extracting("lastName").isNotNull()
                    .extracting("name").isEqualTo("Rubble");
            assertThat(rows.get(1))
                    .isNotNull()
                    .extracting("lastName").isNotNull()
                    .extracting("name").isEqualTo("Flintstone");
        }
    }

    @Test
    void testDelete() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapperV2 mapper = session.getMapper(PersonMapperV2.class);
            int rows = mapper.delete(c ->
                    c.where(occupation, isNull()));
            assertThat(rows).isEqualTo(2);
        }
    }

    // this test is in the quick start documentation
    @Test
    void testGeneralDelete() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapperV2 mapper = session.getMapper(PersonMapperV2.class);

            DeleteStatementProvider deleteStatement = DeleteDSL.deleteFrom(person)
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
            PersonMapperV2 mapper = session.getMapper(PersonMapperV2.class);
            int rows = mapper.delete(DeleteDSLCompleter.allRows());

            assertThat(rows).isEqualTo(6);
        }
    }

    @Test
    void testDeleteByPrimaryKey() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapperV2 mapper = session.getMapper(PersonMapperV2.class);
            int rows = mapper.deleteByPrimaryKey(2);

            assertThat(rows).isEqualTo(1);
        }
    }

    @Test
    void testInsert() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapperV2 mapper = session.getMapper(PersonMapperV2.class);
            PersonRecord row = new PersonRecord(100, "Joe", new LastName("Jones"), new Date(), true, "Developer", 1);

            int rows = mapper.insert(row);
            assertThat(rows).isEqualTo(1);
        }
    }

    @Test
    void testRawInsert() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapperV2 mapper = session.getMapper(PersonMapperV2.class);
            PersonRecord row = new PersonRecord(100, "Joe", new LastName("Jones"), new Date(), true, "Developer", 1);

            InsertStatementProvider<PersonRecord> insertStatement = insert(row).into(person)
                    .withMappedColumn(id)
                    .withMappedColumn(firstName)
                    .withMappedColumn(lastName)
                    .withMappedColumn(birthDate)
                    .withMappedColumn(employed)
                    .withMappedColumn(occupation)
                    .withMappedColumn(addressId)
                    .build().render(RenderingStrategies.MYBATIS3);

            int rows = mapper.insert(insertStatement);
            assertThat(rows).isEqualTo(1);
        }
    }

    @Test
    void testGeneralInsert() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapperV2 mapper = session.getMapper(PersonMapperV2.class);
            int rows = mapper.generalInsert(c ->
                    c.set(id).toValue(100)
                            .set(firstName).toValue("Joe")
                            .set(lastName).toValue(new LastName("Jones"))
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
            PersonMapperV2 mapper = session.getMapper(PersonMapperV2.class);

            List<PersonRecord> records = List.of(
                    new PersonRecord(100, "Joe", new LastName("Jones"), new Date(), true, "Developer", 1),
                    new PersonRecord(101, "Sarah", new LastName("Smith"), new Date(), true, "Architect", 2)
            );

            int rows = mapper.insertMultiple(records);
            assertThat(rows).isEqualTo(2);
        }
    }

    @Test
    void testInsertSelective() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapperV2 mapper = session.getMapper(PersonMapperV2.class);
            PersonRecord row = new PersonRecord(100, "Joe", new LastName("Jones"), new Date(), false, null, 1);

            int rows = mapper.insertSelective(row);
            assertThat(rows).isEqualTo(1);
        }
    }

    @Test
    void testUpdateByPrimaryKeyNullKeyShouldThrowException() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapperV2 mapper = session.getMapper(PersonMapperV2.class);
            PersonRecord row = new PersonRecord(null, "Joe", new LastName("Jones"), new Date(), true, "Developer", 1);

            assertThatExceptionOfType(NonRenderingWhereClauseException.class).isThrownBy(() -> mapper.updateByPrimaryKey(row));
        }
    }

    @Test
    void testUpdateByPrimaryKey() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapperV2 mapper = session.getMapper(PersonMapperV2.class);
            PersonRecord row = new PersonRecord(100, "Joe", new LastName("Jones"), new Date(), true, "Developer", 1);

            int rows = mapper.insert(row);
            assertThat(rows).isEqualTo(1);

            row = row.withOccupation("Programmer");
            rows = mapper.updateByPrimaryKey(row);
            assertThat(rows).isEqualTo(1);

            Optional<PersonRecord> newRecord = mapper.selectByPrimaryKey(100);
            assertThat(newRecord).hasValueSatisfying(r ->
                    assertThat(r.occupation()).isEqualTo("Programmer"));
        }
    }

    @Test
    void testUpdateByPrimaryKeyDelayedWhere() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapperV2 mapper = session.getMapper(PersonMapperV2.class);
            PersonRecord row = new PersonRecord(100, "Joe", new LastName("Jones"), new Date(), true, "Developer", 1);

            int rows = mapper.insert(row);
            assertThat(rows).isEqualTo(1);

            row = row.withOccupation("Programmer");

            var start = UpdateDSL.update(person)
                    .set(firstName).equalToOrNull(row::firstName)
                    .set(lastName).equalToOrNull(row::lastName)
                    .set(birthDate).equalToOrNull(row::birthDate)
                    .set(employed).equalToOrNull(row::employed)
                    .set(occupation).equalToOrNull(row::occupation)
                    .set(addressId).equalToOrNull(row::addressId)
                    .where();

            var statement = start.and(id, isEqualToWhenPresent(row::id))
                    .build().render(RenderingStrategies.MYBATIS3);


            rows = mapper.update(statement);
            assertThat(rows).isEqualTo(1);

            Optional<PersonRecord> newRecord = mapper.selectByPrimaryKey(100);
            assertThat(newRecord).hasValueSatisfying(r ->
                    assertThat(r.occupation()).isEqualTo("Programmer"));
        }
    }

    @Test
    void testUpdateByPrimaryKeySelective() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapperV2 mapper = session.getMapper(PersonMapperV2.class);
            PersonRecord row = new PersonRecord(100, "Joe", new LastName("Jones"), new Date(), true, "Developer", 1);

            int rows = mapper.insert(row);
            assertThat(rows).isEqualTo(1);

            PersonRecord updateRecord = new PersonRecord(100, null, null, null, null, "Programmer", null);
            rows = mapper.updateByPrimaryKeySelective(updateRecord);
            assertThat(rows).isEqualTo(1);

            Optional<PersonRecord> newRecord = mapper.selectByPrimaryKey(100);
            assertThat(newRecord).hasValueSatisfying(r -> {
                assertThat(r.occupation()).isEqualTo("Programmer");
                assertThat(r.firstName()).isEqualTo("Joe");
            });
        }
    }

    @Test
    void testUpdate() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapperV2 mapper = session.getMapper(PersonMapperV2.class);
            PersonRecord row = new PersonRecord(100, "Joe", new LastName("Jones"), new Date(), true, "Developer", 1);

            int rows = mapper.insert(row);
            assertThat(rows).isEqualTo(1);

            PersonRecord updateRow = row.withOccupation("Programmer");

            rows = mapper.update(c ->
                    PersonMapperV2.updateAllColumns(updateRow, c)
                            .where(id, isEqualTo(100))
                            .and(firstName, isEqualTo("Joe")));

            assertThat(rows).isEqualTo(1);

            Optional<PersonRecord> newRecord = mapper.selectByPrimaryKey(100);
            assertThat(newRecord).hasValueSatisfying(r ->
                    assertThat(r.occupation()).isEqualTo("Programmer"));
        }
    }

    @Test
    void testUpdateOneField() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapperV2 mapper = session.getMapper(PersonMapperV2.class);
            PersonRecord row = new PersonRecord(100, "Joe", new LastName("Jones"), new Date(), true, "Developer", 1);

            int rows = mapper.insert(row);
            assertThat(rows).isEqualTo(1);

            rows = mapper.update(c ->
                    c.set(occupation).equalTo("Programmer")
                            .where(id, isEqualTo(100)));

            assertThat(rows).isEqualTo(1);

            Optional<PersonRecord> newRecord = mapper.selectByPrimaryKey(100);
            assertThat(newRecord).hasValueSatisfying(r ->
                    assertThat(r.occupation()).isEqualTo("Programmer"));
        }
    }

    @Test
    void testUpdateAll() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapperV2 mapper = session.getMapper(PersonMapperV2.class);
            PersonRecord row = new PersonRecord(100, "Joe", new LastName("Jones"), new Date(), true, "Developer", 1);

            int rows = mapper.insert(row);
            assertThat(rows).isEqualTo(1);

            PersonRecord updateRecord = new PersonRecord(null, null, null, null, null, "Programmer", null);
            rows = mapper.update(c ->
                    PersonMapperV2.updateSelectiveColumns(updateRecord, c));

            assertThat(rows).isEqualTo(7);

            Optional<PersonRecord> newRecord = mapper.selectByPrimaryKey(100);
            assertThat(newRecord).hasValueSatisfying(r ->
                    assertThat(r.occupation()).isEqualTo("Programmer"));
        }
    }

    @Test
    void testUpdateSelective() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapperV2 mapper = session.getMapper(PersonMapperV2.class);
            PersonRecord row = new PersonRecord(100, "Joe", new LastName("Jones"), new Date(), true, "Developer", 1);

            int rows = mapper.insert(row);
            assertThat(rows).isEqualTo(1);

            PersonRecord updateRecord = new PersonRecord(null, null, null, null, null, "Programmer", null);
            rows = mapper.update(c ->
                    PersonMapperV2.updateSelectiveColumns(updateRecord, c)
                            .where(id, isEqualTo(100)));

            assertThat(rows).isEqualTo(1);

            Optional<PersonRecord> newRecord = mapper.selectByPrimaryKey(100);
            assertThat(newRecord).hasValueSatisfying(r ->
                    assertThat(r.occupation()).isEqualTo("Programmer"));
        }
    }

    @Test
    void testCount() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapperV2 mapper = session.getMapper(PersonMapperV2.class);
            long rows = mapper.count(c ->
                    c.where(occupation, isNull()));

            assertThat(rows).isEqualTo(2L);
        }
    }

    @Test
    void testCountWithDelayedWhere() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapperV2 mapper = session.getMapper(PersonMapperV2.class);
            var start = CountDSL.countFrom(person).where();
            var statement = start.and(occupation, isNull()).build().render(RenderingStrategies.MYBATIS3);
            long rows = mapper.count(statement);

            assertThat(rows).isEqualTo(2L);
        }
    }

    @Test
    void testCountWithAlias() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapperV2 mapper = session.getMapper(PersonMapperV2.class);
            var statement = CountDSL.countFrom(person, "p")
                    .where(occupation, isNull())
                    .build().render(RenderingStrategies.MYBATIS3);
            long rows = mapper.count(statement);

            assertThat(rows).isEqualTo(2L);
        }
    }

    @Test
    void testCountAll() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapperV2 mapper = session.getMapper(PersonMapperV2.class);
            long rows = mapper.count(CountDSLCompleter.allRows());

            assertThat(rows).isEqualTo(6L);
        }
    }

    @Test
    void testCountLastName() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapperV2 mapper = session.getMapper(PersonMapperV2.class);
            long rows = mapper.count(lastName, CountDSLCompleter.allRows());

            assertThat(rows).isEqualTo(6L);
        }
    }

    @Test
    void testCountDistinctLastName() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapperV2 mapper = session.getMapper(PersonMapperV2.class);
            long rows = mapper.countDistinct(lastName, CountDSLCompleter.allRows());

            assertThat(rows).isEqualTo(2L);
        }
    }

    @Test
    void testTypeHandledLike() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapperV2 mapper = session.getMapper(PersonMapperV2.class);

            List<PersonRecord> rows = mapper.select(c ->
                    c.where(lastName, isLike(new LastName("Fl%")))
                            .orderBy(id));

            assertThat(rows).hasSize(3);
            assertThat(rows.get(0).firstName()).isEqualTo("Fred");
        }
    }

    @Test
    void testTypeHandledNotLike() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonMapperV2 mapper = session.getMapper(PersonMapperV2.class);

            List<PersonRecord> rows = mapper.select(c ->
                    c.where(lastName, isNotLike(new LastName("Fl%")))
                            .orderBy(id));

            assertThat(rows).hasSize(3);
            assertThat(rows.get(0).firstName()).isEqualTo("Barney");
        }
    }

    @Test
    void testJoinAllRows() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonWithAddressMapperV2 mapper = session.getMapper(PersonWithAddressMapperV2.class);
            List<PersonWithAddress> records = mapper.select(
                    SelectDSLCompleter.allRowsOrderedBy(id)
            );

            assertThat(records).hasSize(6);
            assertThat(records.get(0).getId()).isEqualTo(1);
            assertThat(records.get(0).getEmployed()).isTrue();
            assertThat(records.get(0).getFirstName()).isEqualTo("Fred");
            assertThat(records.get(0).getLastName()).isEqualTo(new LastName("Flintstone"));
            assertThat(records.get(0).getOccupation()).isEqualTo("Brontosaurus Operator");
            assertThat(records.get(0).getBirthDate()).isNotNull();
            assertThat(records.get(0).getAddress()).isNotNull()
                    .extracting("id", "streetAddress", "city", "state", "addressType")
                    .containsExactly(1, "123 Main Street", "Bedrock", "IN", AddressRecord.AddressType.HOME);

            assertThat(records.get(4).getAddress()).isNotNull()
                    .extracting("addressType")
                    .isEqualTo(AddressRecord.AddressType.BUSINESS);
        }
    }

    @Test
    void testJoinOneRow() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonWithAddressMapperV2 mapper = session.getMapper(PersonWithAddressMapperV2.class);
            List<PersonWithAddress> records = mapper.select(c -> c.where(id, isEqualTo(1)));

            assertThat(records).hasSize(1);
            assertThat(records.get(0).getId()).isEqualTo(1);
            assertThat(records.get(0).getEmployed()).isTrue();
            assertThat(records.get(0).getFirstName()).isEqualTo("Fred");
            assertThat(records.get(0).getLastName()).isEqualTo(new LastName("Flintstone"));
            assertThat(records.get(0).getOccupation()).isEqualTo("Brontosaurus Operator");
            assertThat(records.get(0).getBirthDate()).isNotNull();
            assertThat(records.get(0).getAddress()).isNotNull()
                    .extracting("id", "streetAddress", "city", "state", "addressType")
                    .containsExactly(1, "123 Main Street", "Bedrock", "IN", AddressRecord.AddressType.HOME);
        }
    }

    @Test
    void testJoinPrimaryKey() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonWithAddressMapperV2 mapper = session.getMapper(PersonWithAddressMapperV2.class);
            Optional<PersonWithAddress> row = mapper.selectByPrimaryKey(1);

            assertThat(row).hasValueSatisfying(r -> {
                assertThat(r.getId()).isEqualTo(1);
                assertThat(r.getEmployed()).isTrue();
                assertThat(r.getFirstName()).isEqualTo("Fred");
                assertThat(r.getLastName()).isEqualTo(new LastName("Flintstone"));
                assertThat(r.getOccupation()).isEqualTo("Brontosaurus Operator");
                assertThat(r.getBirthDate()).isNotNull();
                assertThat(r.getAddress()).isNotNull()
                        .extracting("id", "streetAddress", "city", "state", "addressType")
                        .containsExactly(1, "123 Main Street", "Bedrock", "IN", AddressRecord.AddressType.HOME);
            });
        }
    }

    @Test
    void testJoinPrimaryKeyInvalidRecord() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonWithAddressMapperV2 mapper = session.getMapper(PersonWithAddressMapperV2.class);
            Optional<PersonWithAddress> row = mapper.selectByPrimaryKey(55);

            assertThat(row).isEmpty();
        }
    }

    @Test
    void testJoinCount() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonWithAddressMapperV2 mapper = session.getMapper(PersonWithAddressMapperV2.class);
            long count = mapper.count(c -> c.where(person.id, isEqualTo(55)));

            assertThat(count).isZero();
        }
    }

    @Test
    void testJoinCountWithSubcriteria() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonWithAddressMapperV2 mapper = session.getMapper(PersonWithAddressMapperV2.class);
            long count = mapper.count(c -> c.where(person.id, isEqualTo(55), or(person.id, isEqualTo(1))));

            assertThat(count).isEqualTo(1);
        }
    }

    @Test
    void testJoinCountWithSubcriteriaInSingleStatement() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonWithAddressMapperV2 mapper = session.getMapper(PersonWithAddressMapperV2.class);

            SelectStatementProvider selectStatement = CountDSL.countFrom(person)
                    .join(address).on(person.id, isEqualTo(address.id))
                    .where(person.id, isEqualTo(55), or(person.id, isEqualTo(1)))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = """
                    select count(*)
                    from Person
                    join Address on Person.id = Address.address_id
                    where Person.id = #{parameters.p1,jdbcType=INTEGER} or Person.id = #{parameters.p2,jdbcType=INTEGER}
                    """;
            assertThat(selectStatement.getSelectStatement()).isEqualToNormalizingWhitespace(expected);

            long count = mapper.count(selectStatement);
            assertThat(count).isEqualTo(1);
        }
    }

    @Test
    void testJoinCountInSingleStatement() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonWithAddressMapperV2 mapper = session.getMapper(PersonWithAddressMapperV2.class);

            SelectStatementProvider selectStatement = CountDSL.countFrom(person, "p")
                    .join(address, "a").on(person.id, isEqualTo(address.id))
                    .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = """
                    select count(*)
                    from Person p
                    join Address a on p.id = a.address_id
                    """;
            assertThat(selectStatement.getSelectStatement()).isEqualToNormalizingWhitespace(expected);

            long count = mapper.count(selectStatement);
            assertThat(count).isEqualTo(2);
        }
    }

    @Test
    void testJoinCountWhereApplier() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonWithAddressMapperV2 mapper = session.getMapper(PersonWithAddressMapperV2.class);

            WhereApplier whereApplier = where(person.id, isLessThan(4)).toWhereApplier();

            SelectStatementProvider selectStatement = CountDSL.countFrom(person, "p")
                    .join(address, "a").on(person.id, isEqualTo(address.id))
                    .applyWhere(whereApplier)
                    .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = """
                    select count(*)
                    from Person p
                    join Address a on p.id = a.address_id
                    where p.id < #{parameters.p1,jdbcType=INTEGER}
                    """;
            assertThat(selectStatement.getSelectStatement()).isEqualToNormalizingWhitespace(expected);

            long count = mapper.count(selectStatement);
            assertThat(count).isEqualTo(2);
        }
    }

    @Test
    void testJoinCountDelayedWhere() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PersonWithAddressMapperV2 mapper = session.getMapper(PersonWithAddressMapperV2.class);

            var start = CountDSL.countFrom(person, "p")
                    .join(address, "a").on(person.id, isEqualTo(address.id))
                    .where();

            var selectStatement = start
                    .or(person.id, isLessThan(4))
                    .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = """
                    select count(*)
                    from Person p
                    join Address a on p.id = a.address_id
                    where p.id < #{parameters.p1,jdbcType=INTEGER}
                    """;
            assertThat(selectStatement.getSelectStatement()).isEqualToNormalizingWhitespace(expected);

            long count = mapper.count(selectStatement);
            assertThat(count).isEqualTo(2);
        }
    }

    @Test
    void testHaving() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);
            SelectStatementProvider selectStatement = SelectDSL.select(lastName, count())
                    .from(person)
                    .groupBy(lastName)
                    .having(count(), isGreaterThan(1L))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = """
                    select last_name, count(*)
                    from Person
                    group by last_name
                    having count(*) > #{parameters.p1}
                    """;
            assertThat(selectStatement.getSelectStatement()).isEqualToNormalizingWhitespace(expected);

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);
            assertThat(rows).hasSize(2);
        }
    }

    @Test
    void testHavingOrderBy() {
        SelectStatementProvider selectStatement = SelectDSL.select(lastName, count())
                .from(person)
                .groupBy(lastName)
                .having(count(), isGreaterThan(1L))
                .and(count(), isLessThan(3L))
                .orderBy(lastName)
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = """
                select last_name, count(*)
                from Person
                group by last_name
                having count(*) > #{parameters.p1}
                and count(*) < #{parameters.p2}
                order by last_name
                """;
        assertThat(selectStatement.getSelectStatement()).isEqualToNormalizingWhitespace(expected);
    }

    @Test
    void testHavingUnion() {
        SelectStatementProvider selectStatement = SelectDSL.select(lastName, count())
                .from(person)
                .groupBy(lastName)
                .having(count(), isGreaterThan(1L))
                .union()
                .select(lastName, count())
                .from(person)
                .groupBy(lastName)
                .having(count(), isLessThan(5L))
                .orderBy(lastName)
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = """
                select last_name, count(*)
                from Person
                group by last_name
                having count(*) > #{parameters.p1}
                union
                select last_name, count(*)
                from Person
                group by last_name
                having count(*) < #{parameters.p2}
                order by last_name
                """;
        assertThat(selectStatement.getSelectStatement()).isEqualToNormalizingWhitespace(expected);
    }

    @Test
    void testHavingUnionAll() {
        SelectStatementProvider selectStatement = SelectDSL.select(lastName, count())
                .from(person)
                .groupBy(lastName)
                .having(count(), isGreaterThan(1L))
                .unionAll()
                .select(lastName, count())
                .from(person)
                .groupBy(lastName)
                .having(count(), isLessThan(5L))
                .orderBy(lastName)
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = """
                select last_name, count(*)
                from Person
                group by last_name
                having count(*) > #{parameters.p1}
                union all
                select last_name, count(*)
                from Person
                group by last_name
                having count(*) < #{parameters.p2}
                order by last_name
                """;
        assertThat(selectStatement.getSelectStatement()).isEqualToNormalizingWhitespace(expected);
    }

    @Test
    void testHavingLimit() {
        SelectStatementProvider selectStatement = SelectDSL.select(lastName, count())
                .from(person)
                .groupBy(lastName)
                .having(count(), isGreaterThan(1L))
                .and(count(), isLessThan(3L))
                .limit(10)
                .orderBy(lastName)
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = """
                select last_name, count(*)
                from Person
                group by last_name
                having count(*) > #{parameters.p1}
                and count(*) < #{parameters.p2}
                order by last_name
                limit #{parameters.p3}
                """;
        assertThat(selectStatement.getSelectStatement()).isEqualToNormalizingWhitespace(expected);
    }

    @Test
    void testHavingOffset() {
        SelectStatementProvider selectStatement = SelectDSL.select(lastName, count())
                .from(person)
                .groupBy(lastName)
                .having(count(), isGreaterThan(1L))
                .and(count(), isLessThan(3L))
                .offset(10)
                .orderBy(lastName)
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = """
                select last_name, count(*)
                from Person
                group by last_name
                having count(*) > #{parameters.p1}
                and count(*) < #{parameters.p2}
                order by last_name
                offset #{parameters.p3} rows
                """;
        assertThat(selectStatement.getSelectStatement()).isEqualToNormalizingWhitespace(expected);
    }

    @Test
    void testHavingFetchFirst() {
        SelectStatementProvider selectStatement = SelectDSL.select(lastName, count())
                .from(person)
                .groupBy(lastName)
                .having(count(), isGreaterThan(1L))
                .and(count(), isLessThan(3L))
                .fetchFirst(10).rowsOnly()
                .orderBy(lastName)
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = """
                select last_name, count(*)
                from Person
                group by last_name
                having count(*) > #{parameters.p1}
                and count(*) < #{parameters.p2}
                order by last_name
                fetch first #{parameters.p3} rows only
                """;
        assertThat(selectStatement.getSelectStatement()).isEqualToNormalizingWhitespace(expected);
    }

    @Test
    void testHavingNoWait() {
        SelectStatementProvider selectStatement = SelectDSL.select(lastName, count())
                .from(person)
                .groupBy(lastName)
                .having(count(), isGreaterThan(1L))
                .and(count(), isLessThan(3L))
                .nowait()
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = """
                select last_name, count(*)
                from Person
                group by last_name
                having count(*) > #{parameters.p1}
                and count(*) < #{parameters.p2}
                nowait
                """;
        assertThat(selectStatement.getSelectStatement()).isEqualToNormalizingWhitespace(expected);
    }

    @Test
    void testHavingForUpdate() {
        SelectStatementProvider selectStatement = SelectDSL.select(lastName, count())
                .from(person)
                .groupBy(lastName)
                .having(count(), isGreaterThan(1L))
                .and(count(), isLessThan(3L))
                .forUpdate()
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = """
                select last_name, count(*)
                from Person
                group by last_name
                having count(*) > #{parameters.p1}
                and count(*) < #{parameters.p2}
                for update
                """;
        assertThat(selectStatement.getSelectStatement()).isEqualToNormalizingWhitespace(expected);
    }

    @Test
    void testComplexHaving() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);
            SelectStatementProvider selectStatement = SelectDSL.select(id, lastName, count())
                    .from(person)
                    .groupBy(id, lastName)
                    .having(
                            not(id, isGreaterThan(25)),
                            and(count(), isGreaterThanOrEqualTo(0L))
                    )
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = """
                    select id, last_name, count(*)
                    from Person
                    group by id, last_name
                    having not id > #{parameters.p1,jdbcType=INTEGER} and count(*) >= #{parameters.p2}
                    """;
            assertThat(selectStatement.getSelectStatement()).isEqualToNormalizingWhitespace(expected);

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);
            assertThat(rows).hasSize(6);
        }
    }

    @Test
    void testHavingApplier() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);

            HavingApplier havingApplier = having(count(), isGreaterThan(1L)).toHavingApplier();

            HavingApplier composedHaving = havingApplier
                    .andThen(c -> c.and(lastName, isEqualTo(new LastName("Flintstone"))));

            SelectStatementProvider selectStatement = SelectDSL.select(lastName, count())
                    .from(person)
                    .groupBy(lastName)
                    .applyHaving(composedHaving)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = """
                    select last_name, count(*)
                    from Person
                    group by last_name
                    having count(*) > #{parameters.p1}
                    and last_name = #{parameters.p2,jdbcType=VARCHAR,typeHandler=examples.simple.LastNameTypeHandler}
                    """;
            assertThat(selectStatement.getSelectStatement()).isEqualToNormalizingWhitespace(expected);

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);
            assertThat(rows).hasSize(1);
        }
    }

    @Test
    void testWherePaging1() {
        SelectStatementProvider selectStatement = SelectDSL.select(id, lastName)
                .from(person)
                .where(id, isGreaterThan(25))
                .limit(10)
                .offset(10)
                .nowait()
                .forUpdate()
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = """
                select id, last_name
                from Person
                where id > #{parameters.p1,jdbcType=INTEGER}
                limit #{parameters.p2}
                offset #{parameters.p3}
                for update
                nowait
                """;
        assertThat(selectStatement.getSelectStatement()).isEqualToNormalizingWhitespace(expected);
    }

    @Test
    void testWherePaging2() {
        SelectStatementProvider selectStatement = SelectDSL.select(id, lastName)
                .from(person)
                .where(id, isGreaterThan(25))
                .fetchFirst(10)
                .rowsOnly()
                .nowait()
                .forUpdate()
                .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = """
                select id, last_name
                from Person
                where id > #{parameters.p1,jdbcType=INTEGER}
                fetch first #{parameters.p2} rows only
                for update
                nowait
                """;
        assertThat(selectStatement.getSelectStatement()).isEqualToNormalizingWhitespace(expected);
    }

    @Test
    void testWherePaging3() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);
            SelectStatementProvider selectStatement = SelectDSL.select(id, lastName)
                    .from(person)
                    .where(id, isGreaterThan(25))
                    .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
                    .offset(10)
                    .fetchFirst(10)
                    .rowsOnly()
                    .forUpdate()
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = """
                    select id, last_name
                    from Person
                    where id > #{parameters.p1,jdbcType=INTEGER}
                    offset #{parameters.p2} rows
                    fetch first #{parameters.p3} rows only
                    for update
                    """;
            assertThat(selectStatement.getSelectStatement()).isEqualToNormalizingWhitespace(expected);

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);
            assertThat(rows).isEmpty();
        }
    }

    @Test
    void testWhereUnion() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);
            SelectStatementProvider selectStatement = SelectDSL.select(id, lastName)
                    .from(person)
                    .where(id, isGreaterThan(25))
                    .union()
                    .select(id, lastName)
                    .from(person)
                    .where(id, isLessThan(10))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = """
                    select id, last_name
                    from Person
                    where id > #{parameters.p1,jdbcType=INTEGER}
                    union
                    select id, last_name
                    from Person
                    where id < #{parameters.p2,jdbcType=INTEGER}
                    """;
            assertThat(selectStatement.getSelectStatement()).isEqualToNormalizingWhitespace(expected);

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);
            assertThat(rows).hasSize(6);
        }
    }

    @Test
    void testWhereUnionAll() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            CommonSelectMapper mapper = session.getMapper(CommonSelectMapper.class);
            SelectStatementProvider selectStatement = SelectDSL.select(id, lastName)
                    .from(person)
                    .where(id, isGreaterThan(25))
                    .unionAll()
                    .selectDistinct(id, lastName)
                    .from(person)
                    .where(id, isLessThan(10))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);

            String expected = """
                    select id, last_name
                    from Person
                    where id > #{parameters.p1,jdbcType=INTEGER}
                    union all
                    select distinct id, last_name
                    from Person
                    where id < #{parameters.p2,jdbcType=INTEGER}
                    """;
            assertThat(selectStatement.getSelectStatement()).isEqualToNormalizingWhitespace(expected);

            List<Map<String, Object>> rows = mapper.selectManyMappedRows(selectStatement);
            assertThat(rows).hasSize(6);
        }
    }

    @Test
    void testWhereApplier() {
        var whereApplier = where(id, isGreaterThan(25)).toWhereApplier();
        var composedWhere = whereApplier.andThen(c -> c.and(lastName, isEqualTo(new LastName("Flintstone"))));

        var selectStatement = SelectDSL.select(id, lastName)
                .from(person)
                .applyWhere(composedWhere)
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = """
                select id, last_name
                from Person
                where id > #{parameters.p1,jdbcType=INTEGER}
                and last_name = #{parameters.p2,jdbcType=VARCHAR,typeHandler=examples.simple.LastNameTypeHandler}
                """;
        assertThat(selectStatement.getSelectStatement()).isEqualToNormalizingWhitespace(expected);
    }

    @Test
    void testWhereGroupBy() {
        var selectStatement = SelectDSL.select(id, lastName, count())
                .from(person)
                .where(id, isGreaterThan(25))
                .groupBy(id, lastName)
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = """
                select id, last_name, count(*)
                from Person
                where id > #{parameters.p1,jdbcType=INTEGER}
                group by id, last_name
                """;
        assertThat(selectStatement.getSelectStatement()).isEqualToNormalizingWhitespace(expected);
    }

    @Test
    void testWhereNoWait() {
        var selectStatement = SelectDSL.select(id, lastName)
                .from(person)
                .where(id, isGreaterThan(25))
                .nowait()
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = """
                select id, last_name
                from Person
                where id > #{parameters.p1,jdbcType=INTEGER}
                nowait
                """;
        assertThat(selectStatement.getSelectStatement()).isEqualToNormalizingWhitespace(expected);
    }

    @Test
    void testWhereLimitNoWait() {
        var selectStatement = SelectDSL.select(id, lastName)
                .from(person)
                .where(id, isGreaterThan(25))
                .limit(10)
                .nowait()
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = """
                select id, last_name
                from Person
                where id > #{parameters.p1,jdbcType=INTEGER}
                limit #{parameters.p2}
                nowait
                """;
        assertThat(selectStatement.getSelectStatement()).isEqualToNormalizingWhitespace(expected);
    }

    @Test
    void testWhereOffsetNoWait() {
        var selectStatement = SelectDSL.select(id, lastName)
                .from(person)
                .where(id, isGreaterThan(25))
                .offset(10)
                .nowait()
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = """
                select id, last_name
                from Person
                where id > #{parameters.p1,jdbcType=INTEGER}
                offset #{parameters.p2} rows
                nowait
                """;
        assertThat(selectStatement.getSelectStatement()).isEqualToNormalizingWhitespace(expected);
    }

    @Test
    void testWhereNoForUpdate() {
        var selectStatement = SelectDSL.select(id, lastName)
                .from(person)
                .where(id, isGreaterThan(25))
                .forUpdate()
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = """
                select id, last_name
                from Person
                where id > #{parameters.p1,jdbcType=INTEGER}
                for update
                """;
        assertThat(selectStatement.getSelectStatement()).isEqualToNormalizingWhitespace(expected);
    }

    @Test
    void testWhereMultipleCriteria() {
        SqlCriterion initialCriterion = new NullCriterion();
        List<AndOrCriteriaGroup> groups = List.of(
                new AndOrCriteriaGroup.Builder()
                        .withConnector("and")
                        .withInitialCriterion(new ColumnAndConditionCriterion.Builder<Integer>()
                                .withColumn(id)
                                .withCondition(isGreaterThan(25))
                                .build()
                        ).build()
        );

        var selectStatement = SelectDSL.select(id, lastName)
                .from(person)
                .where(initialCriterion, groups)
                .forUpdate()
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = """
                select id, last_name
                from Person
                where id > #{parameters.p1,jdbcType=INTEGER}
                for update
                """;
        assertThat(selectStatement.getSelectStatement()).isEqualToNormalizingWhitespace(expected);
    }

    @Test
    void testSelectJoinConfigure() {
        SelectStatementProvider selectStatement = SelectDSL.select(person.id, address.city)
                .from(person, "p")
                .join(address, "a").on(person.id, isEqualTo(address.id))
                .configureStatement(c -> c.setNonRenderingWhereClauseAllowed(true))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = """
                select p.id, a.city
                from Person p
                join Address a on p.id = a.address_id
                """;
        assertThat(selectStatement.getSelectStatement()).isEqualToNormalizingWhitespace(expected);
    }

    @Test
    void testSelectJoinWhere() {
        SelectStatementProvider selectStatement = SelectDSL.select(person.id, address.city)
                .from(person, "p")
                .join(address, "a").on(person.id, isEqualTo(address.id))
                .where(person.id, isGreaterThan(25))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = """
                select p.id, a.city
                from Person p
                join Address a on p.id = a.address_id
                where p.id > #{parameters.p1,jdbcType=INTEGER}
                """;
        assertThat(selectStatement.getSelectStatement()).isEqualToNormalizingWhitespace(expected);
    }

    @Test
    void testSelectJoinDelayedWhere() {
        var start = SelectDSL.select(person.id, address.city)
                .from(person, "p")
                .join(address, "a").on(person.id, isEqualTo(address.id))
                .where();

        var selectStatement = start
                .and(person.id, isGreaterThan(25))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = """
                select p.id, a.city
                from Person p
                join Address a on p.id = a.address_id
                where p.id > #{parameters.p1,jdbcType=INTEGER}
                """;
        assertThat(selectStatement.getSelectStatement()).isEqualToNormalizingWhitespace(expected);
    }

    @Test
    void testSelectJoinWhereApplier() {
        var whereApplier = where(person.id, isGreaterThan(25)).toWhereApplier();

        var selectStatement = SelectDSL.select(person.id, address.city)
                .from(person, "p")
                .join(address, "a").on(person.id, isEqualTo(address.id))
                .applyWhere(whereApplier)
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = """
                select p.id, a.city
                from Person p
                join Address a on p.id = a.address_id
                where p.id > #{parameters.p1,jdbcType=INTEGER}
                """;
        assertThat(selectStatement.getSelectStatement()).isEqualToNormalizingWhitespace(expected);
    }

    @Test
    void testSelectJoinGroupBy() {
        var selectStatement = SelectDSL.select(address.city, count())
                .from(person, "p")
                .join(address, "a").on(person.id, isEqualTo(address.id))
                .groupBy(address.city)
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = """
                select a.city, count(*)
                from Person p
                join Address a on p.id = a.address_id
                group by a.city
                """;
        assertThat(selectStatement.getSelectStatement()).isEqualToNormalizingWhitespace(expected);
    }

    @Test
    void testSelectJoinOrderBy() {
        SelectStatementProvider selectStatement = SelectDSL.select(person.id, address.city)
                .from(person, "p")
                .join(address, "a").on(person.id, isEqualTo(address.id))
                .orderBy(person.id, address.city)
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = """
                select p.id, a.city
                from Person p
                join Address a on p.id = a.address_id
                order by id, city
                """;
        assertThat(selectStatement.getSelectStatement()).isEqualToNormalizingWhitespace(expected);
    }

    @Test
    void testSelectJoinLimit() {
        SelectStatementProvider selectStatement = SelectDSL.select(person.id, address.city)
                .from(person, "p")
                .join(address, "a").on(person.id, isEqualTo(address.id))
                .limit(10)
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = """
                select p.id, a.city
                from Person p
                join Address a on p.id = a.address_id
                limit #{parameters.p1}
                """;
        assertThat(selectStatement.getSelectStatement()).isEqualToNormalizingWhitespace(expected);
    }

    @Test
    void testSelectJoinOffset() {
        SelectStatementProvider selectStatement = SelectDSL.select(person.id, address.city)
                .from(person, "p")
                .join(address, "a").on(person.id, isEqualTo(address.id))
                .offset(10)
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = """
                select p.id, a.city
                from Person p
                join Address a on p.id = a.address_id
                offset #{parameters.p1} rows
                """;
        assertThat(selectStatement.getSelectStatement()).isEqualToNormalizingWhitespace(expected);
    }

    @Test
    void testSelectJoinFetchFirst() {
        SelectStatementProvider selectStatement = SelectDSL.select(person.id, address.city)
                .from(person, "p")
                .join(address, "a").on(person.id, isEqualTo(address.id))
                .fetchFirst(10).rowsOnly()
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = """
                select p.id, a.city
                from Person p
                join Address a on p.id = a.address_id
                fetch first #{parameters.p1} rows only
                """;
        assertThat(selectStatement.getSelectStatement()).isEqualToNormalizingWhitespace(expected);
    }

    @Test
    void testSelectJoinNoWait() {
        SelectStatementProvider selectStatement = SelectDSL.select(person.id, address.city)
                .from(person, "p")
                .join(address, "a").on(person.id, isEqualTo(address.id))
                .nowait()
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = """
                select p.id, a.city
                from Person p
                join Address a on p.id = a.address_id
                nowait
                """;
        assertThat(selectStatement.getSelectStatement()).isEqualToNormalizingWhitespace(expected);
    }

    @Test
    void testSelectJoinForUpdate() {
        SelectStatementProvider selectStatement = SelectDSL.select(person.id, address.city)
                .from(person, "p")
                .join(address, "a").on(person.id, isEqualTo(address.id))
                .forUpdate()
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = """
                select p.id, a.city
                from Person p
                join Address a on p.id = a.address_id
                for update
                """;
        assertThat(selectStatement.getSelectStatement()).isEqualToNormalizingWhitespace(expected);
    }

    @Test
    void testSelectJoinUnion() {
        SelectStatementProvider selectStatement = SelectDSL.select(person.id, address.city)
                .from(person, "p")
                .join(address, "a").on(person.id, isEqualTo(address.id))
                .union()
                .select(person.id, address.city)
                .from(person, "p")
                .join(address, "a").on(person.id, isEqualTo(address.id))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = """
                select p.id, a.city
                from Person p
                join Address a on p.id = a.address_id
                union
                select p.id, a.city
                from Person p
                join Address a on p.id = a.address_id
                """;
        assertThat(selectStatement.getSelectStatement()).isEqualToNormalizingWhitespace(expected);
    }

    @Test
    void testSelectJoinUnionAll() {
        SelectStatementProvider selectStatement = SelectDSL.select(person.id, address.city)
                .from(person, "p")
                .join(address, "a").on(person.id, isEqualTo(address.id))
                .unionAll()
                .select(person.id, address.city)
                .from(person, "p")
                .join(address, "a").on(person.id, isEqualTo(address.id))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        String expected = """
                select p.id, a.city
                from Person p
                join Address a on p.id = a.address_id
                union all
                select p.id, a.city
                from Person p
                join Address a on p.id = a.address_id
                """;
        assertThat(selectStatement.getSelectStatement()).isEqualToNormalizingWhitespace(expected);
    }
}
