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
package examples.kotlin.mybatis3.canonical

import examples.kotlin.mybatis3.TestUtils
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.addressId
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.birthDate
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.id
import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.occupation
import org.apache.ibatis.session.SqlSessionFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mybatis.dynamic.sql.util.kotlin.WhereApplier
import org.mybatis.dynamic.sql.util.kotlin.andThen
import org.mybatis.dynamic.sql.util.kotlin.elements.where
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.select

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReusableWhereTest {
    private lateinit var sqlSessionFactory: SqlSessionFactory

    @BeforeAll
    fun setup() {
        sqlSessionFactory = TestUtils.buildSqlSessionFactory {
            withInitializationScript("/examples/kotlin/mybatis3/CreateSimpleDB.sql")
            withTypeHandler(YesNoTypeHandler::class)
            withMapper(PersonMapper::class)
        }
    }

    @Test
    fun testCount() {
        sqlSessionFactory.openSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val rows = mapper.count {
                applyWhere(commonWhere)
            }

            assertThat(rows).isEqualTo(3)
        }
    }

    @Test
    fun testDelete() {
        sqlSessionFactory.openSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val rows = mapper.delete {
                applyWhere(commonWhere)
            }

            assertThat(rows).isEqualTo(3)
        }
    }

    @Test
    fun testSelect() {
        sqlSessionFactory.openSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val rows = mapper.select {
                applyWhere(commonWhere)
                orderBy(id)
            }

            assertThat(rows).hasSize(3)
        }
    }

    @Test
    fun testUpdate() {
        sqlSessionFactory.openSession().use { session ->
            val mapper = session.getMapper(PersonMapper::class.java)

            val rows = mapper.update {
                set(occupation) equalToStringConstant "worker"
                applyWhere(commonWhere)
            }

            assertThat(rows).isEqualTo(3)
        }
    }

    @Test
    fun testDeprecatedComposition() {
        val composedWhereClause = commonWhere.andThen {
            and { birthDate.isNotNull() }
        }.andThen {
            or { addressId isLessThan 3 }
        }

        val selectStatement = select(person.allColumns()) {
            from(person)
            applyWhere(composedWhereClause)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select * from Person " +
                "where id = #{parameters.p1,jdbcType=INTEGER} or occupation is null " +
                "and birth_date is not null " +
                "or address_id < #{parameters.p2,jdbcType=INTEGER}"
        )
    }

    @Test
    fun testComposition() {
        val composedWhereClause = commonWhereClause.andThen {
            and { birthDate.isNotNull() }
        }.andThen {
            or { addressId isLessThan 3 }
        }

        val selectStatement = select(person.allColumns()) {
            from(person)
            where(composedWhereClause)
        }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select * from Person " +
                    "where id = #{parameters.p1,jdbcType=INTEGER} or occupation is null " +
                    "and birth_date is not null " +
                    "or address_id < #{parameters.p2,jdbcType=INTEGER}"
        )
    }

    private val commonWhere: WhereApplier = {
        where { id isEqualTo 1 }
        or { occupation.isNull() }
    }

    private val commonWhereClause = where {
        id isEqualTo 1
        or { occupation.isNull() }
    }
}
