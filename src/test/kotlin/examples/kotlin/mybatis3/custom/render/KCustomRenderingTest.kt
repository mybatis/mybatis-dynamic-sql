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
package examples.kotlin.mybatis3.custom.render

import config.TestContainersConfiguration
import examples.kotlin.mybatis3.custom.render.KJsonTestDynamicSqlSupport.description
import examples.kotlin.mybatis3.custom.render.KJsonTestDynamicSqlSupport.id
import examples.kotlin.mybatis3.custom.render.KJsonTestDynamicSqlSupport.info
import examples.kotlin.mybatis3.custom.render.KJsonTestDynamicSqlSupport.jsonTest
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource
import org.apache.ibatis.mapping.Environment
import org.apache.ibatis.session.Configuration
import org.apache.ibatis.session.SqlSession
import org.apache.ibatis.session.SqlSessionFactory
import org.apache.ibatis.session.SqlSessionFactoryBuilder
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mybatis.dynamic.sql.SqlColumn
import org.mybatis.dynamic.sql.util.kotlin.elements.`as`
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.insert
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.insertInto
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.insertMultiple
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.select
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.update
import org.mybatis.dynamic.sql.util.mybatis3.CommonSelectMapper
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.sql.JDBCType

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KCustomRenderingTest {

    private lateinit var sqlSessionFactory: SqlSessionFactory

    @BeforeAll
    fun setUp() {
        val dataSource = UnpooledDataSource(
            postgres.driverClassName,
            postgres.jdbcUrl,
            postgres.username,
            postgres.password
        )
        val environment = Environment("test", JdbcTransactionFactory(), dataSource)
        with(Configuration(environment)) {
            addMapper(KJsonTestMapper::class.java)
            addMapper(CommonSelectMapper::class.java)
            sqlSessionFactory = SqlSessionFactoryBuilder().build(this)
        }
    }

    private fun newSession(): SqlSession {
        return sqlSessionFactory.openSession()
    }

    @Test
    fun testInsertRecord() {
        newSession().use {
            val mapper = it.getMapper(KJsonTestMapper::class.java)
            var record = KJsonTestRecord(
                id = 1,
                description = "Fred",
                info = "{\"firstName\": \"Fred\", \"lastName\": \"Flintstone\", \"age\": 30}"
            )

            var insertStatement = insert(record) {
                into(jsonTest)
                map(id) toProperty "id"
                map(description) toProperty "description"
                map(info) toProperty "info"
            }
            val expected = "insert into JsonTest (id, description, info) " +
                "values (#{row.id,jdbcType=INTEGER}, #{row.description,jdbcType=VARCHAR}, " +
                "#{row.info,jdbcType=VARCHAR}::json)"
            assertThat(insertStatement.insertStatement).isEqualTo(expected)
            var rows = mapper.insert(insertStatement)
            assertThat(rows).isEqualTo(1)
            record = KJsonTestRecord(
                id = 2,
                description = "Wilma",
                info = "{\"firstName\": \"Wilma\", \"lastName\": \"Flintstone\", \"age\": 25}"
            )

            insertStatement = insert(record) {
                into(jsonTest)
                map(id) toProperty "id"
                map(description) toProperty "description"
                map(info) toProperty "info"
            }
            rows = mapper.insert(insertStatement)
            assertThat(rows).isEqualTo(1)
            val selectStatement = select(id, description, info) {
                from(jsonTest)
                orderBy(id)
            }
            val records = mapper.selectMany(selectStatement)
            assertThat(records).hasSize(2)
            assertThat(records[0].info)
                .isEqualTo("{\"firstName\": \"Fred\", \"lastName\": \"Flintstone\", \"age\": 30}")
            assertThat(records[1].info)
                .isEqualTo("{\"firstName\": \"Wilma\", \"lastName\": \"Flintstone\", \"age\": 25}")
        }
    }

    @Test
    fun testGeneralInsert() {
        newSession().use {
            val mapper = it.getMapper(KJsonTestMapper::class.java)
            var insertStatement = insertInto(jsonTest) {
                set(id) toValue 1
                set(description) toValue "Fred"
                set(info) toValue "{\"firstName\": \"Fred\", \"lastName\": \"Flintstone\", \"age\": 30}"
            }
            val expected = "insert into JsonTest (id, description, info) " +
                "values (#{parameters.p1,jdbcType=INTEGER}, #{parameters.p2,jdbcType=VARCHAR}, " +
                "#{parameters.p3,jdbcType=VARCHAR}::json)"
            assertThat(insertStatement.insertStatement).isEqualTo(expected)
            var rows = mapper!!.generalInsert(insertStatement)
            assertThat(rows).isEqualTo(1)
            insertStatement = insertInto(jsonTest) {
                set(id) toValue 2
                set(description) toValue "Wilma"
                set(info) toValue "{\"firstName\": \"Wilma\", \"lastName\": \"Flintstone\", \"age\": 25}"
            }
            rows = mapper.generalInsert(insertStatement)
            assertThat(rows).isEqualTo(1)
            val selectStatement = select(id, description, info) {
                from(jsonTest)
                orderBy(id)
            }
            val records = mapper.selectMany(selectStatement)
            assertThat(records).hasSize(2)
            assertThat(records[0].info)
                .isEqualTo("{\"firstName\": \"Fred\", \"lastName\": \"Flintstone\", \"age\": 30}")
            assertThat(records[1].info)
                .isEqualTo("{\"firstName\": \"Wilma\", \"lastName\": \"Flintstone\", \"age\": 25}")
        }
    }

    @Test
    fun testInsertMultiple() {
        newSession().use {
            val mapper = it.getMapper(KJsonTestMapper::class.java)
            val record1 = KJsonTestRecord(
                id = 1,
                description = "Fred",
                info = "{\"firstName\": \"Fred\", \"lastName\": \"Flintstone\", \"age\": 30}"
            )
            val record2 = KJsonTestRecord(
                id = 2,
                description = "Wilma",
                info = "{\"firstName\": \"Wilma\", \"lastName\": \"Flintstone\", \"age\": 25}"
            )
            val insertStatement = insertMultiple(listOf(record1, record2)) {
                into(jsonTest)
                map(id) toProperty "id"
                map(description) toProperty "description"
                map(info) toProperty "info"
            }
            val expected = "insert into JsonTest (id, description, info) " +
                "values (#{records[0].id,jdbcType=INTEGER}, #{records[0].description,jdbcType=VARCHAR}, " +
                "#{records[0].info,jdbcType=VARCHAR}::json), " +
                "(#{records[1].id,jdbcType=INTEGER}, #{records[1].description,jdbcType=VARCHAR}, " +
                "#{records[1].info,jdbcType=VARCHAR}::json)"
            assertThat(insertStatement.insertStatement).isEqualTo(expected)
            val rows = mapper.insertMultiple(insertStatement)
            assertThat(rows).isEqualTo(2)
            val selectStatement = select(id, description, info) {
                from(jsonTest)
                orderBy(id)
            }
            val records = mapper.selectMany(selectStatement)
            assertThat(records).hasSize(2)
            assertThat(records[0].info)
                .isEqualTo("{\"firstName\": \"Fred\", \"lastName\": \"Flintstone\", \"age\": 30}")
            assertThat(records[1].info)
                .isEqualTo("{\"firstName\": \"Wilma\", \"lastName\": \"Flintstone\", \"age\": 25}")
        }
    }

    @Test
    fun testUpdate() {
        newSession().use {
            val mapper = it.getMapper(KJsonTestMapper::class.java)
            val record1 = KJsonTestRecord(
                id = 1,
                description = "Fred",
                info = "{\"firstName\": \"Fred\", \"lastName\": \"Flintstone\", \"age\": 30}"
            )
            val record2 = KJsonTestRecord(
                id = 2,
                description = "Wilma",
                info = "{\"firstName\": \"Wilma\", \"lastName\": \"Flintstone\", \"age\": 25}"
            )
            val insertStatement = insertMultiple(listOf(record1, record2)) {
                into(jsonTest)
                map(id) toProperty "id"
                map(description) toProperty "description"
                map(info) toProperty "info"
            }
            var rows = mapper.insertMultiple(insertStatement)
            assertThat(rows).isEqualTo(2)
            val updateStatement = update(jsonTest) {
                set(info) equalTo "{\"firstName\": \"Wilma\", \"lastName\": \"Flintstone\", \"age\": 26}"
                where { id isEqualTo 2 }
            }
            val expected = "update JsonTest " +
                "set info = #{parameters.p1,jdbcType=VARCHAR}::json " +
                "where id = #{parameters.p2,jdbcType=INTEGER}"
            assertThat(updateStatement.updateStatement).isEqualTo(expected)
            rows = mapper.update(updateStatement)
            assertThat(rows).isEqualTo(1)
            val selectStatement = select(id, description, info) {
                from(jsonTest)
                orderBy(id)
            }
            val records = mapper.selectMany(selectStatement)
            assertThat(records).hasSize(2)
            assertThat(records[0].info)
                .isEqualTo("{\"firstName\": \"Fred\", \"lastName\": \"Flintstone\", \"age\": 30}")
            assertThat(records[1].info)
                .isEqualTo("{\"firstName\": \"Wilma\", \"lastName\": \"Flintstone\", \"age\": 26}")
        }
    }

    @Test
    fun testDeReference() {
        newSession().use {
            val mapper = it.getMapper(KJsonTestMapper::class.java)
            val record1 = KJsonTestRecord(
                id = 1,
                description = "Fred",
                info = "{\"firstName\": \"Fred\", \"lastName\": \"Flintstone\", \"age\": 30}"
            )
            val record2 = KJsonTestRecord(
                id = 2,
                description = "Wilma",
                info = "{\"firstName\": \"Wilma\", \"lastName\": \"Flintstone\", \"age\": 25}"
            )
            val insertStatement = insertMultiple(listOf(record1, record2)) {
                into(jsonTest)
                map(id) toProperty "id"
                map(description) toProperty "description"
                map(info) toProperty "info"
            }
            val rows = mapper.insertMultiple(insertStatement)
            assertThat(rows).isEqualTo(2)
            val selectStatement = select(id, description, info) {
                from(jsonTest)
                where { dereference(info, "age") isEqualTo "25" }
            }
            val expected = "select id, description, info " +
                "from JsonTest " +
                "where info->>'age' = #{parameters.p1,jdbcType=VARCHAR}"
            assertThat(selectStatement.selectStatement).isEqualTo(expected)
            val record = mapper.selectOne(selectStatement)
            assertThat(record).isNotNull
            assertThat(record!!.info).isEqualTo(
                "{\"firstName\": \"Wilma\", \"lastName\": \"Flintstone\", \"age\": 25}"
            )
        }
    }

    @Test
    fun testDereference2() {
        newSession().use {
            val mapper = it.getMapper(KJsonTestMapper::class.java)
            val record1 = KJsonTestRecord(
                id = 1,
                description = "Fred",
                info = "{\"firstName\": \"Fred\", \"lastName\": \"Flintstone\", \"age\": 30}"
            )
            val record2 = KJsonTestRecord(
                id = 2,
                description = "Wilma",
                info = "{\"firstName\": \"Wilma\", \"lastName\": \"Flintstone\", \"age\": 25}"
            )
            val insertStatement = insertMultiple(listOf(record1, record2)) {
                into(jsonTest)
                map(id) toProperty "id"
                map(description) toProperty "description"
                map(info) toProperty "info"
            }
            val rows = mapper.insertMultiple(insertStatement)
            assertThat(rows).isEqualTo(2)
            val selectStatement = select(dereference(info, "firstName") `as` "firstname") {
                from(jsonTest)
                where { dereference(info, "age") isEqualTo "25" }
            }
            val expected = "select info->>'firstName' as firstname " +
                "from JsonTest " +
                "where info->>'age' = #{parameters.p1,jdbcType=VARCHAR}"
            assertThat(selectStatement.selectStatement).isEqualTo(expected)
            val records = mapper.selectManyMappedRows(selectStatement)
            assertThat(records).hasSize(1)
            assertThat(records[0]).containsEntry("firstname", "Wilma")
        }
    }

    private fun <T> dereference(column: SqlColumn<T>, attribute: String) =
        SqlColumn.of<String>(column.name() + "->>'" + attribute + "'", column.table(), JDBCType.VARCHAR)

    companion object {
        @Container
        private val postgres = PostgreSQLContainer(TestContainersConfiguration.POSTGRES_LATEST)
            .withInitScript("examples/custom_render/dbInit.sql")
    }
}
