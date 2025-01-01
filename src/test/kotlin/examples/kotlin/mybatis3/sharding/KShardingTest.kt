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
package examples.kotlin.mybatis3.sharding

import examples.kotlin.mybatis3.TestUtils
import examples.kotlin.mybatis3.sharding.KTableCodesTableDynamicSQLSupport.tableCodes
import org.apache.ibatis.session.SqlSessionFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.insertInto
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.select

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KShardingTest {
    private lateinit var sqlSessionFactory: SqlSessionFactory
    private val shards = mutableMapOf<String, KTableCodesTableDynamicSQLSupport.TableCodes>()

    @BeforeAll
    fun setup() {
        sqlSessionFactory = TestUtils.buildSqlSessionFactory {
            withInitializationScript("/examples/sharding/ShardingDB.sql")
            withMapper(KShardedMapper::class)
        }
    }

    @Test
    fun testShardedSelect() {
        sqlSessionFactory.openSession().use { session ->
            val mapper = session.getMapper(KShardedMapper::class.java)
            val table = calculateTable(1)

            val selectStatement = select(table.description) {
                from(table)
                where {
                    table.id isEqualTo 1
                }
            }

            assertThat(selectStatement.selectStatement).isEqualTo(
                "select description from tableCodes_odd where id = #{parameters.p1,jdbcType=INTEGER}"
            )

            val description = mapper.selectOneString(selectStatement)

            assertThat(description).isNull()
        }
    }

    @Test
    fun testShardedInserts() {
        sqlSessionFactory.openSession().use { session ->
            val mapper = session.getMapper(KShardedMapper::class.java)

            mapper.insert(1, "Description 1")
            mapper.insert(2, "Description 2")
            mapper.insert(3, "Description 3")
            mapper.insert(4, "Description 4")
            mapper.insert(5, "Description 5")
            mapper.insert(6, "Description 6")
            mapper.insert(7, "Description 7")

            val oddTable = calculateTable(1)
            val oddCountStatement = countFrom(oddTable) {
                allRows()
            }

            assertThat(oddCountStatement.selectStatement).isEqualTo("select count(*) from tableCodes_odd")
            val oddRows = mapper.count(oddCountStatement)
            assertThat(oddRows).isEqualTo(4L)

            val evenTable = calculateTable(2)
            val evenCountStatement = countFrom(evenTable) {
                allRows()
            }

            assertThat(evenCountStatement.selectStatement).isEqualTo("select count(*) from tableCodes_even")
            val evenRows = mapper.count(evenCountStatement)
            assertThat(evenRows).isEqualTo(3L)
        }
    }

    @Test
    fun testShardedSelects() {
        sqlSessionFactory.openSession().use { session ->
            val mapper = session.getMapper(KShardedMapper::class.java)

            mapper.insert(1, "Description 1")
            mapper.insert(2, "Description 2")

            assertThat(mapper.select(1)).isEqualTo("Description 1")
            assertThat(mapper.select(2)).isEqualTo("Description 2")
            assertThat(mapper.select(3)).isNull()
            assertThat(mapper.select(4)).isNull()
        }
    }

    fun KShardedMapper.insert(id: Int, description: String): Int {
        val table = calculateTable(id)
        val insertStatement = insertInto(table) {
            set(table.id) toValue id
            set(table.description) toValue description
        }

        return generalInsert(insertStatement)
    }

    fun KShardedMapper.select(id: Int): String? {
        val table = calculateTable(id)
        val selectStatement = select(table.description) {
            from(table)
            where {
                table.id isEqualTo id
            }
        }

        return selectOneString(selectStatement)
    }

    private fun calculateTable(id: Int) =
        if (id % 2 == 0) {
            shards.computeIfAbsent("even") { tableCodes }  // tableCodes_even is default
        } else {
            shards.computeIfAbsent("odd") { tableCodes.withName("tableCodes_odd") }
        }
}
