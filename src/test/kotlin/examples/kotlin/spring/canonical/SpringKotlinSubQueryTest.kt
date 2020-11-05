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

import examples.kotlin.spring.canonical.PersonDynamicSqlSupport.Person
import examples.kotlin.spring.canonical.PersonDynamicSqlSupport.Person.firstName
import examples.kotlin.spring.canonical.PersonDynamicSqlSupport.Person.id
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mybatis.dynamic.sql.DerivedColumn
import org.mybatis.dynamic.sql.SqlBuilder.*
import org.mybatis.dynamic.sql.util.kotlin.spring.selectList
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType
import org.mybatis.dynamic.sql.util.kotlin.spring.select

class SpringKotlinSubQueryTest {
    private lateinit var template: NamedParameterJdbcTemplate

    @BeforeEach
    fun setup() {
        val db = with(EmbeddedDatabaseBuilder()) {
            setType(EmbeddedDatabaseType.HSQL)
            generateUniqueName(true)
            addScript("classpath:/examples/kotlin/spring/CreateSimpleDB.sql")
            build()
        }
        template = NamedParameterJdbcTemplate(db)
    }

    @Test
    fun testBasicSubQuery() {
        val rowNum = DerivedColumn.of<Int>("rownum()")

        val selectStatement =
            select(firstName, rowNum) {
                from {
                    select(id, firstName) {
                        from(Person)
                        where(id, isLessThan(22))
                        orderBy(firstName.descending())
                    }
                }
                where(rowNum, isLessThan(5))
                and(firstName, isLike("%a%"))
            }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select first_name, rownum() " +
                    "from (select id, first_name " +
                    "from Person where id < :p1 " +
                    "order by first_name DESC) " +
                    "where rownum() < :p2 and first_name like :p3"
        )

        assertThat(selectStatement.parameters).containsEntry("p1", 22)
        assertThat(selectStatement.parameters).containsEntry("p2", 5)
        assertThat(selectStatement.parameters).containsEntry("p3", "%a%")

        val rows = template.selectList(selectStatement) { rs, _ ->
            mapOf(
                Pair("FIRST_NAME", rs.getString(1)),
                Pair("ROWNUM", rs.getInt(2))
            )
        }

        assertThat(rows).hasSize(3)
        assertThat(rows[2]).containsEntry("FIRST_NAME", "Wilma")
        assertThat(rows[2]).containsEntry("ROWNUM", 3)
    }

    @Test
    fun testBasicSubQueryTemplateDirect() {
        val rowNum = DerivedColumn.of<Int>("rownum()")

        val rows = template.select(firstName, rowNum) {
                from {
                    select(id, firstName) {
                        from(Person)
                        where(id, isLessThan(22))
                        orderBy(firstName.descending())
                    }
                }
                where(rowNum, isLessThan(5))
                and(firstName, isLike("%a%"))
            }.withRowMapper { rs, _ ->
            mapOf(
                Pair("FIRST_NAME", rs.getString(1)),
                Pair("ROWNUM", rs.getInt(2))
            )
        }

        assertThat(rows).hasSize(3)
        assertThat(rows[2]).containsEntry("FIRST_NAME", "Wilma")
        assertThat(rows[2]).containsEntry("ROWNUM", 3)
    }

    @Test
    fun testBasicSubQueryWithAliases() {
        val rowNum = DerivedColumn.of<Int>("rownum()").`as`("myRows")
        val outerFirstName = firstName.qualifiedWith("b")
        val personId = DerivedColumn.of<Int>("personId", "b")

        val selectStatement =
            select(outerFirstName.asCamelCase(), personId, rowNum) {
                from ("b") {
                    select(id.`as`("personId"), firstName) {
                        from(Person, "a")
                        where(id, isLessThan(22))
                        orderBy(firstName.descending())
                    }
                }
                where(rowNum, isLessThan(5))
                and(outerFirstName, isLike("%a%"))
            }

        assertThat(selectStatement.selectStatement).isEqualTo(
            "select b.first_name as \"firstName\", b.personId, rownum() as myRows " +
                    "from (select a.id as personId, a.first_name " +
                    "from Person a where a.id < :p1 " +
                    "order by first_name DESC) b " +
                    "where rownum() < :p2 and b.first_name like :p3"
        )

        assertThat(selectStatement.parameters).containsEntry("p1", 22)
        assertThat(selectStatement.parameters).containsEntry("p2", 5)
        assertThat(selectStatement.parameters).containsEntry("p3", "%a%")

        val rows = template.selectList(selectStatement) { rs, _ ->
            mapOf(
                Pair("firstName", rs.getString("firstName")),
                Pair("PERSONID", rs.getInt("PERSONID")),
                Pair("ROWNUM", rs.getInt("MYROWS"))
            )
        }

        assertThat(rows).hasSize(3)
        assertThat(rows[2]).containsEntry("firstName", "Wilma")
        assertThat(rows[2]).containsEntry("PERSONID", 2)
        assertThat(rows[2]).containsEntry("ROWNUM", 3)
    }
}
