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

import examples.kotlin.mybatis3.joins.ItemMasterDynamicSQLSupport.itemMaster
import examples.kotlin.mybatis3.joins.OrderLineDynamicSQLSupport.orderLine
import examples.kotlin.spring.canonical.PersonDynamicSqlSupport.person
import examples.kotlin.spring.canonical.PersonDynamicSqlSupport.firstName
import examples.kotlin.spring.canonical.PersonDynamicSqlSupport.id
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mybatis.dynamic.sql.DerivedColumn
import org.mybatis.dynamic.sql.util.kotlin.elements.`as`
import org.mybatis.dynamic.sql.util.kotlin.elements.invoke
import org.mybatis.dynamic.sql.util.kotlin.spring.deleteFrom
import org.mybatis.dynamic.sql.util.kotlin.spring.select
import org.mybatis.dynamic.sql.util.kotlin.spring.selectList
import org.mybatis.dynamic.sql.util.kotlin.spring.update
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.transaction.annotation.Transactional

@SpringJUnitConfig(classes = [SpringConfiguration::class])
@Transactional
open class SpringKotlinSubQueryTest {
    @Autowired
    private lateinit var template: NamedParameterJdbcTemplate

    @Test
    fun testBasicSubQuery() {
        val rowNum = DerivedColumn.of<Int>("rownum()")

        val selectStatement =
            select(firstName, rowNum) {
                from {
                    select(id, firstName) {
                        from(person)
                        where { id isLessThan 22 }
                        orderBy(firstName.descending())
                    }
                }
                where { rowNum isLessThan 5 }
                and { firstName isLike "%a%" }
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
                    from(person)
                    where { id isLessThan 22 }
                    orderBy(firstName.descending())
                }
            }
            where { rowNum isLessThan 5 }
            and { firstName isLike "%a%" }
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
        val rowNum = DerivedColumn.of<Int>("rownum()") `as` "myRows"
        val outerFirstName = "b"(firstName)
        val personId = DerivedColumn.of<Int>("personId", "b")

        val selectStatement =
            select(outerFirstName.asCamelCase(), personId, rowNum) {
                from {
                    select(id `as` "personId", firstName) {
                        from(person, "a")
                        where { id isLessThan 22 }
                        orderBy(firstName.descending())
                    }
                    + "b"
                }
                where { rowNum isLessThan 5 }
                and { outerFirstName isLike "%a%" }
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

    @Test
    fun testDeleteWithSoftAliasRendersProperlyWithSpring() {
        val deleteStatement = deleteFrom(itemMaster, "im") {
            where {
                not {
                    exists {
                        select(orderLine.allColumns()) {
                            from(orderLine, "ol")
                            where { orderLine.itemId isEqualTo itemMaster.itemId }
                        }
                    }
                }
            }
        }

        val expectedStatement = "delete from ItemMaster im where not exists " +
                "(select ol.* from OrderLine ol where ol.item_id = im.item_id)"

        assertThat(deleteStatement.deleteStatement).isEqualTo(expectedStatement)
    }

    @Test
    fun testUpdateWithSoftAliasRendersProperlyWithSpring() {
        val updateStatement = update(itemMaster, "im") {
            set(itemMaster.description) equalTo "No Orders"
            where {
                not {
                    exists {
                        select(orderLine.allColumns()) {
                            from(orderLine, "ol")
                            where { orderLine.itemId isEqualTo itemMaster.itemId }
                        }
                    }
                }
            }
        }

        val expectedStatement = "update ItemMaster im " +
                "set im.description = :p1 " +
                "where not exists (select ol.* from OrderLine ol where ol.item_id = im.item_id)"

        assertThat(updateStatement.updateStatement).isEqualTo(expectedStatement)
    }
}
