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
package examples.kotlin.spring.canonical

import examples.kotlin.spring.canonical.CompoundKeyDynamicSqlSupport.compoundKey
import examples.kotlin.spring.canonical.CompoundKeyDynamicSqlSupport.id1
import examples.kotlin.spring.canonical.CompoundKeyDynamicSqlSupport.id2
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mybatis.dynamic.sql.util.kotlin.spring.insert
import org.mybatis.dynamic.sql.util.kotlin.spring.insertBatch
import org.mybatis.dynamic.sql.util.kotlin.spring.insertMultiple
import org.mybatis.dynamic.sql.util.kotlin.spring.select
import org.mybatis.dynamic.sql.util.kotlin.spring.selectList
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.transaction.annotation.Transactional

@SpringJUnitConfig(classes = [SpringConfiguration::class])
@Transactional
open class SpringKotlinMapToRowTest {
    @Autowired
    private lateinit var template: NamedParameterJdbcTemplate

    @Test
    fun testInsertOne() {
        val i = 1

        val insertStatement = insert(i) {
            into(compoundKey)
            map(id1).toConstant("22")
            map(id2).toRow()
        }

        val expected = "insert into CompoundKey (id1, id2) values (22, :row)"
        assertThat(insertStatement.insertStatement).isEqualTo(expected)

        val rows = template.insert(insertStatement)
        assertThat(rows).isEqualTo(1)

        val selectStatement = select(id1, id2) {
            from(compoundKey)
            orderBy(id1, id2)
        }

        val records = template.selectList(selectStatement, compoundKeyRowMapper)
        assertThat(records).hasSize(1)
    }

    @Test
    open fun testInsertMultiple() {
        val integers = listOf(1, 2, 3)

        val insertStatement = insertMultiple(integers) {
            into(compoundKey)
            map(id1).toConstant("22")
            map(id2).toRow()
        }

        val expected =
            "insert into CompoundKey (id1, id2) values (22, :records[0]), (22, :records[1]), (22, :records[2])"
        assertThat(insertStatement.insertStatement).isEqualTo(expected)

        val rows = template.insertMultiple(insertStatement)
        assertThat(rows).isEqualTo(3)

        val selectStatement = select(id1, id2) {
            from(compoundKey)
            orderBy(id1, id2)
        }

        val records = template.selectList(selectStatement, compoundKeyRowMapper)
        assertThat(records).hasSize(3)
    }

    @Test
    open fun testInsertBatch() {
        val integers = listOf(1, 2, 3)

        val insertStatement = insertBatch(integers) {
            into(compoundKey)
            map(id1).toConstant("22")
            map(id2).toRow()
        }

        val expected = "insert into CompoundKey (id1, id2) values (22, :row)"
        assertThat(insertStatement.insertStatementSQL).isEqualTo(expected)

        val rowCounts = template.insertBatch(insertStatement)
        assertThat(rowCounts).hasSize(3)
        assertThat(rowCounts.sum()).isEqualTo(3)

        val selectStatement = select(id1, id2) {
            from(compoundKey)
            orderBy(id1, id2)
        }

        val records = template.selectList(selectStatement, compoundKeyRowMapper)
        assertThat(records).hasSize(3)
    }
}
