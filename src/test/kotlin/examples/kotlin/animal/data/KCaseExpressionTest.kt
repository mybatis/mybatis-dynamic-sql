/*
 *    Copyright 2016-2024 the original author or authors.
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
package examples.kotlin.animal.data

import examples.kotlin.animal.data.AnimalDataDynamicSqlSupport.animalData
import examples.kotlin.animal.data.AnimalDataDynamicSqlSupport.animalName
import examples.kotlin.animal.data.AnimalDataDynamicSqlSupport.id
import examples.kotlin.mybatis3.TestUtils
import org.apache.ibatis.session.ExecutorType
import org.apache.ibatis.session.SqlSession
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.assertj.core.api.Assertions.entry
import org.junit.jupiter.api.Test
import org.mybatis.dynamic.sql.exception.InvalidSqlException
import org.mybatis.dynamic.sql.util.Messages
import org.mybatis.dynamic.sql.util.kotlin.KInvalidSQLException
import org.mybatis.dynamic.sql.util.kotlin.elements.`as`
import org.mybatis.dynamic.sql.util.kotlin.elements.case
import org.mybatis.dynamic.sql.util.kotlin.elements.cast
import org.mybatis.dynamic.sql.util.kotlin.elements.isEqualTo
import org.mybatis.dynamic.sql.util.kotlin.elements.value
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.select
import org.mybatis.dynamic.sql.util.mybatis3.CommonSelectMapper
import java.math.BigDecimal

class KCaseExpressionTest {
    private fun newSession(executorType: ExecutorType = ExecutorType.REUSE): SqlSession {
        // this method re-initializes the database with every test - needed because of autoincrement fields
        return TestUtils.buildSqlSessionFactory {
            withInitializationScript("/examples/animal/data/CreateAnimalData.sql")
            withMapper(CommonSelectMapper::class)
        }.openSession(executorType)
    }

    @Test
    fun testSearchedCaseWithStrings() {
        newSession().use { session ->
            val mapper = session.getMapper(CommonSelectMapper::class.java)

            val selectStatement = select(animalName,
                case {
                    `when` {
                        animalName isEqualTo "Artic fox"
                        or { animalName isEqualTo "Red fox" }
                        then("Fox")
                    }
                    `when` {
                        animalName isEqualTo "Little brown bat"
                        or { animalName isEqualTo "Big brown bat" }
                        then("Bat")
                    }
                    `else`("Not a Fox or a bat")
                } `as` "AnimalType"
            ) {
                from(animalData, "a")
                where { id.isIn(2, 3, 31, 32, 38, 39) }
                orderBy(id)
            }

            val expected = "select a.animal_name, case " +
                    "when a.animal_name = #{parameters.p1,jdbcType=VARCHAR} or a.animal_name = #{parameters.p2,jdbcType=VARCHAR} then 'Fox' " +
                    "when a.animal_name = #{parameters.p3,jdbcType=VARCHAR} or a.animal_name = #{parameters.p4,jdbcType=VARCHAR} then 'Bat' " +
                    "else 'Not a Fox or a bat' end as AnimalType " +
                    "from AnimalData a where a.id in (" +
                    "#{parameters.p5,jdbcType=INTEGER},#{parameters.p6,jdbcType=INTEGER}," +
                    "#{parameters.p7,jdbcType=INTEGER},#{parameters.p8,jdbcType=INTEGER},#{parameters.p9,jdbcType=INTEGER}," +
                    "#{parameters.p10,jdbcType=INTEGER}) " +
                    "order by id"
            assertThat(selectStatement.selectStatement).isEqualTo(expected)
            assertThat(selectStatement.parameters).containsOnly(
                    entry("p1", "Artic fox"),
                    entry("p2", "Red fox"),
                    entry("p3", "Little brown bat"),
                    entry("p4", "Big brown bat"),
                    entry("p5", 2),
                    entry("p6", 3),
                    entry("p7", 31),
                    entry("p8", 32),
                    entry("p9", 38),
                    entry("p10", 39)
                )

            val records = mapper.selectManyMappedRows(selectStatement)
            assertThat(records).hasSize(6)
            assertThat(records[0]).containsOnly(
                entry("ANIMAL_NAME", "Little brown bat"),
                entry("ANIMALTYPE", "Bat               ")
            )
            assertThat(records[1]).containsOnly(
                entry("ANIMAL_NAME", "Big brown bat"),
                entry("ANIMALTYPE", "Bat               ")
            )
            assertThat(records[2]).containsOnly(
                entry("ANIMAL_NAME", "Cat"),
                entry("ANIMALTYPE", "Not a Fox or a bat")
            )
            assertThat(records[3]).containsOnly(
                entry("ANIMAL_NAME", "Artic fox"),
                entry("ANIMALTYPE", "Fox               ")
            )
            assertThat(records[4]).containsOnly(
                entry("ANIMAL_NAME", "Red fox"),
                entry("ANIMALTYPE", "Fox               ")
            )
            assertThat(records[5]).containsOnly(
                entry("ANIMAL_NAME", "Raccoon"),
                entry("ANIMALTYPE", "Not a Fox or a bat")
            )
        }
    }

    @Test
    fun testSearchedCaseWithIntegers() {
        newSession().use { session ->
            val mapper = session.getMapper(CommonSelectMapper::class.java)

            val selectStatement = select(animalName,
                case {
                    `when` {
                        animalName isEqualTo "Artic fox"
                        or { animalName isEqualTo "Red fox" }
                        then(1)
                    }
                    `when` {
                        animalName isEqualTo "Little brown bat"
                        or { animalName isEqualTo "Big brown bat" }
                        then(2)
                    }
                    `else`(3)
                } `as` "AnimalType"
            ) {
                from(animalData, "a")
                where { id.isIn(2, 3, 31, 32, 38, 39) }
                orderBy(id)
            }

            val expected = "select a.animal_name, case " +
                    "when a.animal_name = #{parameters.p1,jdbcType=VARCHAR} or a.animal_name = #{parameters.p2,jdbcType=VARCHAR} then 1 " +
                    "when a.animal_name = #{parameters.p3,jdbcType=VARCHAR} or a.animal_name = #{parameters.p4,jdbcType=VARCHAR} then 2 " +
                    "else 3 end as AnimalType " +
                    "from AnimalData a where a.id in (" +
                    "#{parameters.p5,jdbcType=INTEGER},#{parameters.p6,jdbcType=INTEGER}," +
                    "#{parameters.p7,jdbcType=INTEGER},#{parameters.p8,jdbcType=INTEGER},#{parameters.p9,jdbcType=INTEGER}," +
                    "#{parameters.p10,jdbcType=INTEGER}) " +
                    "order by id"
            assertThat(selectStatement.selectStatement).isEqualTo(expected)
            assertThat(selectStatement.parameters).containsOnly(
                entry("p1", "Artic fox"),
                entry("p2", "Red fox"),
                entry("p3", "Little brown bat"),
                entry("p4", "Big brown bat"),
                entry("p5", 2),
                entry("p6", 3),
                entry("p7", 31),
                entry("p8", 32),
                entry("p9", 38),
                entry("p10", 39)
            )

            val records = mapper.selectManyMappedRows(selectStatement)
            assertThat(records).hasSize(6)
            assertThat(records[0]).containsOnly(
                entry("ANIMAL_NAME", "Little brown bat"),
                entry("ANIMALTYPE", 2)
            )
            assertThat(records[1]).containsOnly(
                entry("ANIMAL_NAME", "Big brown bat"),
                entry("ANIMALTYPE", 2)
            )
            assertThat(records[2]).containsOnly(
                entry("ANIMAL_NAME", "Cat"),
                entry("ANIMALTYPE", 3)
            )
            assertThat(records[3]).containsOnly(
                entry("ANIMAL_NAME", "Artic fox"),
                entry("ANIMALTYPE", 1)
            )
            assertThat(records[4]).containsOnly(
                entry("ANIMAL_NAME", "Red fox"),
                entry("ANIMALTYPE",  1)
            )
            assertThat(records[5]).containsOnly(
                entry("ANIMAL_NAME", "Raccoon"),
                entry("ANIMALTYPE", 3)
            )
        }
    }

    @Test
    fun testSearchedCaseWithBoundValues() {
        newSession().use { session ->
            val mapper = session.getMapper(CommonSelectMapper::class.java)

            val selectStatement = select(animalName,
                case {
                    `when` {
                        animalName isEqualTo "Artic fox"
                        or { animalName isEqualTo "Red fox" }
                        then(value("Fox"))
                    }
                    `when` {
                        animalName isEqualTo "Little brown bat"
                        or { animalName isEqualTo "Big brown bat" }
                        then(value("Bat"))
                    }
                    `else`(cast { value("Not a Fox or a bat") `as` "VARCHAR(30)" })
                } `as` "AnimalType"
            ) {
                from(animalData, "a")
                where { id.isIn(2, 3, 31, 32, 38, 39) }
                orderBy(id)
            }

            val expected = "select a.animal_name, case " +
                    "when a.animal_name = #{parameters.p1,jdbcType=VARCHAR} or a.animal_name = #{parameters.p2,jdbcType=VARCHAR} then #{parameters.p3} " +
                    "when a.animal_name = #{parameters.p4,jdbcType=VARCHAR} or a.animal_name = #{parameters.p5,jdbcType=VARCHAR} then #{parameters.p6} " +
                    "else cast(#{parameters.p7} as VARCHAR(30)) end as AnimalType " +
                    "from AnimalData a where a.id in (" +
                    "#{parameters.p8,jdbcType=INTEGER},#{parameters.p9,jdbcType=INTEGER}," +
                    "#{parameters.p10,jdbcType=INTEGER},#{parameters.p11,jdbcType=INTEGER},#{parameters.p12,jdbcType=INTEGER}," +
                    "#{parameters.p13,jdbcType=INTEGER}) " +
                    "order by id"
            assertThat(selectStatement.selectStatement).isEqualTo(expected)
            assertThat(selectStatement.parameters).containsOnly(
                entry("p1", "Artic fox"),
                entry("p2", "Red fox"),
                entry("p3", "Fox"),
                entry("p4", "Little brown bat"),
                entry("p5", "Big brown bat"),
                entry("p6", "Bat"),
                entry("p7", "Not a Fox or a bat"),
                entry("p8", 2),
                entry("p9", 3),
                entry("p10", 31),
                entry("p11", 32),
                entry("p12", 38),
                entry("p13", 39)
            )

            val records = mapper.selectManyMappedRows(selectStatement)
            assertThat(records).hasSize(6)
            assertThat(records[0]).containsOnly(
                entry("ANIMAL_NAME", "Little brown bat"),
                entry("ANIMALTYPE", "Bat")
            )
            assertThat(records[1]).containsOnly(
                entry("ANIMAL_NAME", "Big brown bat"),
                entry("ANIMALTYPE", "Bat")
            )
            assertThat(records[2]).containsOnly(
                entry("ANIMAL_NAME", "Cat"),
                entry("ANIMALTYPE", "Not a Fox or a bat")
            )
            assertThat(records[3]).containsOnly(
                entry("ANIMAL_NAME", "Artic fox"),
                entry("ANIMALTYPE", "Fox")
            )
            assertThat(records[4]).containsOnly(
                entry("ANIMAL_NAME", "Red fox"),
                entry("ANIMALTYPE", "Fox")
            )
            assertThat(records[5]).containsOnly(
                entry("ANIMAL_NAME", "Raccoon"),
                entry("ANIMALTYPE", "Not a Fox or a bat")
            )
        }
    }

    @Test
    fun testSearchedCaseNoElse() {
        newSession().use { session ->
            val mapper = session.getMapper(CommonSelectMapper::class.java)

            val selectStatement = select(
                animalName,
                case {
                    `when` {
                        animalName isEqualTo "Artic fox"
                        or { animalName isEqualTo "Red fox" }
                        then("Fox")
                    }
                    `when` {
                        animalName isEqualTo "Little brown bat"
                        or { animalName isEqualTo "Big brown bat" }
                        then("Bat")
                    }
                } `as` "AnimalType"
            ) {
                from(animalData, "a")
                where { id.isIn(2, 3, 31, 32, 38, 39) }
                orderBy(id)
            }

            val expected = "select a.animal_name, case " +
                    "when a.animal_name = #{parameters.p1,jdbcType=VARCHAR} or a.animal_name = #{parameters.p2,jdbcType=VARCHAR} then 'Fox' " +
                    "when a.animal_name = #{parameters.p3,jdbcType=VARCHAR} or a.animal_name = #{parameters.p4,jdbcType=VARCHAR} then 'Bat' " +
                    "end as AnimalType " +
                    "from AnimalData a where a.id in (" +
                    "#{parameters.p5,jdbcType=INTEGER},#{parameters.p6,jdbcType=INTEGER}," +
                    "#{parameters.p7,jdbcType=INTEGER},#{parameters.p8,jdbcType=INTEGER},#{parameters.p9,jdbcType=INTEGER}," +
                    "#{parameters.p10,jdbcType=INTEGER}) " +
                    "order by id"
            assertThat(selectStatement.selectStatement).isEqualTo(expected)
            assertThat(selectStatement.parameters)
                .containsOnly(
                    entry("p1", "Artic fox"),
                    entry("p2", "Red fox"),
                    entry("p3", "Little brown bat"),
                    entry("p4", "Big brown bat"),
                    entry("p5", 2),
                    entry("p6", 3),
                    entry("p7", 31),
                    entry("p8", 32),
                    entry("p9", 38),
                    entry("p10", 39)
                )

            val records =
                mapper.selectManyMappedRows(selectStatement)
            assertThat(records).hasSize(6)
            assertThat(records[0]).containsOnly(
                entry("ANIMAL_NAME", "Little brown bat"),
                entry("ANIMALTYPE", "Bat")
            )
            assertThat(records[1]).containsOnly(
                entry("ANIMAL_NAME", "Big brown bat"),
                entry("ANIMALTYPE", "Bat")
            )
            assertThat(records[2]).containsOnly(entry("ANIMAL_NAME", "Cat"))
            assertThat(records[3]).containsOnly(
                entry("ANIMAL_NAME", "Artic fox"),
                entry("ANIMALTYPE", "Fox")
            )
            assertThat(records[4]).containsOnly(
                entry("ANIMAL_NAME", "Red fox"),
                entry("ANIMALTYPE", "Fox")
            )
            assertThat(records[5]).containsOnly(entry("ANIMAL_NAME", "Raccoon"))
        }
    }

    @Test
    fun testSearchedCaseWithGroup() {
        newSession().use { session ->
            val mapper = session.getMapper(CommonSelectMapper::class.java)

            val selectStatement = select(
                animalName,
                case {
                    `when` {
                        animalName isEqualTo "Artic fox"
                        or { animalName isEqualTo "Red fox" }
                        then("Fox")
                    }
                    `when` {
                        animalName isEqualTo "Little brown bat"
                        or { animalName isEqualTo "Big brown bat" }
                        then("Bat")
                    }
                    `when` {
                        group {
                            animalName isEqualTo "Cat"
                            and { id isEqualTo 31 }
                        }
                        or { id isEqualTo 39 }
                        then("Fred")
                    }
                    `else`("Not a Fox or a bat")
                } `as` "AnimalType"
            ) {
                from(animalData, "a")
                where { id.isIn(2, 3, 4, 31, 32, 38, 39) }
                orderBy(id)
            }

            val expected = "select a.animal_name, case " +
                    "when a.animal_name = #{parameters.p1,jdbcType=VARCHAR} or a.animal_name = #{parameters.p2,jdbcType=VARCHAR} then 'Fox' " +
                    "when a.animal_name = #{parameters.p3,jdbcType=VARCHAR} or a.animal_name = #{parameters.p4,jdbcType=VARCHAR} then 'Bat' " +
                    "when (a.animal_name = #{parameters.p5,jdbcType=VARCHAR} and a.id = #{parameters.p6,jdbcType=INTEGER}) or a.id = #{parameters.p7,jdbcType=INTEGER} then 'Fred' " +
                    "else 'Not a Fox or a bat' end as AnimalType " +
                    "from AnimalData a where a.id in (" +
                    "#{parameters.p8,jdbcType=INTEGER},#{parameters.p9,jdbcType=INTEGER}," +
                    "#{parameters.p10,jdbcType=INTEGER},#{parameters.p11,jdbcType=INTEGER},#{parameters.p12,jdbcType=INTEGER}," +
                    "#{parameters.p13,jdbcType=INTEGER},#{parameters.p14,jdbcType=INTEGER}) " +
                    "order by id"
            assertThat(selectStatement.selectStatement).isEqualTo(expected)
            assertThat(selectStatement.parameters)
                .containsOnly(
                    entry("p1", "Artic fox"),
                    entry("p2", "Red fox"),
                    entry("p3", "Little brown bat"),
                    entry("p4", "Big brown bat"),
                    entry("p5", "Cat"),
                    entry("p6", 31),
                    entry("p7", 39),
                    entry("p8", 2),
                    entry("p9", 3),
                    entry("p10", 4),
                    entry("p11", 31),
                    entry("p12", 32),
                    entry("p13", 38),
                    entry("p14", 39)
                )

            val records = mapper.selectManyMappedRows(selectStatement)
            assertThat(records).hasSize(7)
            assertThat(records[0]).containsOnly(
                entry("ANIMAL_NAME", "Little brown bat"),
                entry("ANIMALTYPE", "Bat               ")
            )
            assertThat(records[1]).containsOnly(
                entry("ANIMAL_NAME", "Big brown bat"),
                entry("ANIMALTYPE", "Bat               ")
            )
            assertThat(records[2]).containsOnly(
                entry("ANIMAL_NAME", "Mouse"),
                entry("ANIMALTYPE", "Not a Fox or a bat")
            )
            assertThat(records[3]).containsOnly(
                entry("ANIMAL_NAME", "Cat"),
                entry("ANIMALTYPE", "Fred              ")
            )
            assertThat(records[4]).containsOnly(
                entry("ANIMAL_NAME", "Artic fox"),
                entry("ANIMALTYPE", "Fox               ")
            )
            assertThat(records[5]).containsOnly(
                entry("ANIMAL_NAME", "Red fox"),
                entry("ANIMALTYPE", "Fox               ")
            )
            assertThat(records[6]).containsOnly(
                entry("ANIMAL_NAME", "Raccoon"),
                entry("ANIMALTYPE", "Fred              ")
            )
        }
    }

    @Test
    fun testSimpleCaseWithStrings() {
        newSession().use { session ->
            val mapper = session.getMapper(CommonSelectMapper::class.java)

            val selectStatement = select(
                animalName,
                case(animalName) {
                    `when` (isEqualTo("Artic fox"), isEqualTo("Red fox")) { then("yes") }
                    `else`("no")
                } `as` "IsAFox"
            ) {
                from(animalData)
                where { id.isIn(31, 32, 38, 39) }
                orderBy(id)
            }

            val expected = "select animal_name, " +
                    "case animal_name when = #{parameters.p1,jdbcType=VARCHAR}, = #{parameters.p2,jdbcType=VARCHAR} then 'yes' else 'no' end " +
                    "as IsAFox from AnimalData where id in " +
                    "(#{parameters.p3,jdbcType=INTEGER},#{parameters.p4,jdbcType=INTEGER},#{parameters.p5,jdbcType=INTEGER},#{parameters.p6,jdbcType=INTEGER}) " +
                    "order by id"
            assertThat(selectStatement.selectStatement).isEqualTo(expected)
            assertThat(selectStatement.parameters)
                .containsOnly(
                    entry("p1", "Artic fox"),
                    entry("p2", "Red fox"),
                    entry("p3", 31),
                    entry("p4", 32),
                    entry("p5", 38),
                    entry("p6", 39)
                )

            val records = mapper.selectManyMappedRows(selectStatement)
            assertThat(records).hasSize(4)
            assertThat(records[0]).containsOnly(
                entry("ANIMAL_NAME", "Cat"),
                entry("ISAFOX", "no ")
            )
            assertThat(records[1]).containsOnly(
                entry("ANIMAL_NAME", "Artic fox"),
                entry("ISAFOX", "yes")
            )
            assertThat(records[2]).containsOnly(
                entry("ANIMAL_NAME", "Red fox"),
                entry("ISAFOX", "yes")
            )
            assertThat(records[3]).containsOnly(
                entry("ANIMAL_NAME", "Raccoon"),
                entry("ISAFOX", "no ")
            )
        }
    }

    @Test
    fun testSimpleCaseBasicWithStrings() {
        newSession().use { session ->
            val mapper = session.getMapper(CommonSelectMapper::class.java)

            val selectStatement = select(
                animalName,
                case(animalName) {
                    `when` ("Artic fox", "Red fox") { then("yes") }
                    `else`("no")
                } `as` "IsAFox"
            ) {
                from(animalData)
                where { id.isIn(31, 32, 38, 39) }
                orderBy(id)
            }

            val expected = "select animal_name, " +
                    "case animal_name when #{parameters.p1,jdbcType=VARCHAR}, #{parameters.p2,jdbcType=VARCHAR} then 'yes' else 'no' end " +
                    "as IsAFox from AnimalData where id in " +
                    "(#{parameters.p3,jdbcType=INTEGER},#{parameters.p4,jdbcType=INTEGER},#{parameters.p5,jdbcType=INTEGER},#{parameters.p6,jdbcType=INTEGER}) " +
                    "order by id"
            assertThat(selectStatement.selectStatement).isEqualTo(expected)
            assertThat(selectStatement.parameters)
                .containsOnly(
                    entry("p1", "Artic fox"),
                    entry("p2", "Red fox"),
                    entry("p3", 31),
                    entry("p4", 32),
                    entry("p5", 38),
                    entry("p6", 39)
                )

            val records = mapper.selectManyMappedRows(selectStatement)
            assertThat(records).hasSize(4)
            assertThat(records[0]).containsOnly(
                entry("ANIMAL_NAME", "Cat"),
                entry("ISAFOX", "no ")
            )
            assertThat(records[1]).containsOnly(
                entry("ANIMAL_NAME", "Artic fox"),
                entry("ISAFOX", "yes")
            )
            assertThat(records[2]).containsOnly(
                entry("ANIMAL_NAME", "Red fox"),
                entry("ISAFOX", "yes")
            )
            assertThat(records[3]).containsOnly(
                entry("ANIMAL_NAME", "Raccoon"),
                entry("ISAFOX", "no ")
            )
        }
    }

    @Test
    fun testSimpleCaseWithBooleans() {
        newSession().use { session ->
            val mapper = session.getMapper(CommonSelectMapper::class.java)

            val selectStatement = select(
                animalName,
                case(animalName) {
                    `when` (isEqualTo("Artic fox"), isEqualTo("Red fox")) { then(true) }
                    `else`(false)
                } `as` "IsAFox"
            ) {
                from(animalData)
                where { id.isIn(31, 32, 38, 39) }
                orderBy(id)
            }

            val expected = "select animal_name, " +
                    "case animal_name when = #{parameters.p1,jdbcType=VARCHAR}, = #{parameters.p2,jdbcType=VARCHAR} then true else false end " +
                    "as IsAFox from AnimalData where id in " +
                    "(#{parameters.p3,jdbcType=INTEGER},#{parameters.p4,jdbcType=INTEGER},#{parameters.p5,jdbcType=INTEGER},#{parameters.p6,jdbcType=INTEGER}) " +
                    "order by id"
            assertThat(selectStatement.selectStatement).isEqualTo(expected)
            assertThat(selectStatement.parameters)
                .containsOnly(
                    entry("p1", "Artic fox"),
                    entry("p2", "Red fox"),
                    entry("p3", 31),
                    entry("p4", 32),
                    entry("p5", 38),
                    entry("p6", 39)
                )

            val records = mapper.selectManyMappedRows(selectStatement)
            assertThat(records).hasSize(4)
            assertThat(records[0]).containsOnly(
                entry("ANIMAL_NAME", "Cat"),
                entry("ISAFOX", false)
            )
            assertThat(records[1]).containsOnly(
                entry("ANIMAL_NAME", "Artic fox"),
                entry("ISAFOX", true)
            )
            assertThat(records[2]).containsOnly(
                entry("ANIMAL_NAME", "Red fox"),
                entry("ISAFOX", true)
            )
            assertThat(records[3]).containsOnly(
                entry("ANIMAL_NAME", "Raccoon"),
                entry("ISAFOX", false)
            )
        }
    }

    @Test
    fun testSimpleCaseBasicWithBooleans() {
        newSession().use { session ->
            val mapper = session.getMapper(CommonSelectMapper::class.java)

            val selectStatement = select(
                animalName,
                case(animalName) {
                    `when` ("Artic fox", "Red fox") { then(true) }
                    `else`(false)
                } `as` "IsAFox"
            ) {
                from(animalData)
                where { id.isIn(31, 32, 38, 39) }
                orderBy(id)
            }

            val expected = "select animal_name, " +
                    "case animal_name when #{parameters.p1,jdbcType=VARCHAR}, #{parameters.p2,jdbcType=VARCHAR} then true else false end " +
                    "as IsAFox from AnimalData where id in " +
                    "(#{parameters.p3,jdbcType=INTEGER},#{parameters.p4,jdbcType=INTEGER},#{parameters.p5,jdbcType=INTEGER},#{parameters.p6,jdbcType=INTEGER}) " +
                    "order by id"
            assertThat(selectStatement.selectStatement).isEqualTo(expected)
            assertThat(selectStatement.parameters)
                .containsOnly(
                    entry("p1", "Artic fox"),
                    entry("p2", "Red fox"),
                    entry("p3", 31),
                    entry("p4", 32),
                    entry("p5", 38),
                    entry("p6", 39)
                )

            val records = mapper.selectManyMappedRows(selectStatement)
            assertThat(records).hasSize(4)
            assertThat(records[0]).containsOnly(
                entry("ANIMAL_NAME", "Cat"),
                entry("ISAFOX", false)
            )
            assertThat(records[1]).containsOnly(
                entry("ANIMAL_NAME", "Artic fox"),
                entry("ISAFOX", true)
            )
            assertThat(records[2]).containsOnly(
                entry("ANIMAL_NAME", "Red fox"),
                entry("ISAFOX", true)
            )
            assertThat(records[3]).containsOnly(
                entry("ANIMAL_NAME", "Raccoon"),
                entry("ISAFOX", false)
            )
        }
    }

    @Test
    fun testSimpleCaseNoElse() {
        newSession().use { session ->
            val mapper = session.getMapper(CommonSelectMapper::class.java)

            val selectStatement = select(
                animalName,
                case(animalName) {
                    `when`(isEqualTo("Artic fox"), isEqualTo("Red fox")) { then("yes") }
                } `as` "IsAFox"
            ) {
                from(animalData)
                where { id.isIn(31, 32, 38, 39) }
                orderBy(id)
            }

            val expected = "select animal_name, " +
                    "case animal_name when = #{parameters.p1,jdbcType=VARCHAR}, = #{parameters.p2,jdbcType=VARCHAR} then 'yes' end " +
                    "as IsAFox from AnimalData where id in " +
                    "(#{parameters.p3,jdbcType=INTEGER},#{parameters.p4,jdbcType=INTEGER},#{parameters.p5,jdbcType=INTEGER},#{parameters.p6,jdbcType=INTEGER}) " +
                    "order by id"
            assertThat(selectStatement.selectStatement).isEqualTo(expected)
            assertThat(selectStatement.parameters)
                .containsOnly(
                    entry("p1", "Artic fox"),
                    entry("p2", "Red fox"),
                    entry("p3", 31),
                    entry("p4", 32),
                    entry("p5", 38),
                    entry("p6", 39)
                )

            val records = mapper.selectManyMappedRows(selectStatement)
            assertThat(records).hasSize(4)
            assertThat(records[0]).containsOnly(entry("ANIMAL_NAME", "Cat"))
            assertThat(records[1]).containsOnly(
                entry("ANIMAL_NAME", "Artic fox"),
                entry("ISAFOX", "yes")
            )
            assertThat(records[2]).containsOnly(
                entry("ANIMAL_NAME", "Red fox"),
                entry("ISAFOX", "yes")
            )
            assertThat(records[3]).containsOnly(entry("ANIMAL_NAME", "Raccoon"))
        }
    }

    @Test
    fun testCastString() {
        newSession().use { session ->
            val mapper = session.getMapper(CommonSelectMapper::class.java)

            val selectStatement = select(
                animalName,
                case(animalName) {
                    `when`(isEqualTo("Artic fox"), isEqualTo("Red fox")) { then(cast { "It's a fox" `as` "VARCHAR(30)" })}
                    `else`("It's not a fox")
                } `as` "IsAFox"
            ) {
                from(animalData)
                where { id.isIn(31, 32, 38, 39) }
                orderBy(id)
            }

            val expected = "select animal_name, " +
                    "case animal_name when = #{parameters.p1,jdbcType=VARCHAR}, = #{parameters.p2,jdbcType=VARCHAR} then cast('It''s a fox' as VARCHAR(30)) " +
                    "else 'It''s not a fox' end " +
                    "as IsAFox from AnimalData where id in " +
                    "(#{parameters.p3,jdbcType=INTEGER},#{parameters.p4,jdbcType=INTEGER},#{parameters.p5,jdbcType=INTEGER},#{parameters.p6,jdbcType=INTEGER}) " +
                    "order by id"
            assertThat(selectStatement.selectStatement).isEqualTo(expected)
            assertThat(selectStatement.parameters)
                .containsOnly(
                    entry("p1", "Artic fox"),
                    entry("p2", "Red fox"),
                    entry("p3", 31),
                    entry("p4", 32),
                    entry("p5", 38),
                    entry("p6", 39)
                )

            val records = mapper.selectManyMappedRows(selectStatement)
            assertThat(records).hasSize(4)
            assertThat(records[0]).containsOnly(entry("ANIMAL_NAME", "Cat"), entry("ISAFOX", "It's not a fox"))
            assertThat(records[1]).containsOnly(entry("ANIMAL_NAME", "Artic fox"), entry("ISAFOX", "It's a fox"))
            assertThat(records[2]).containsOnly(entry("ANIMAL_NAME", "Red fox"), entry("ISAFOX", "It's a fox"))
            assertThat(records[3]).containsOnly(entry("ANIMAL_NAME", "Raccoon"), entry("ISAFOX", "It's not a fox"))
        }
    }

    @Test
    fun testCaseLongs() {
        newSession().use { session ->
            val mapper = session.getMapper(CommonSelectMapper::class.java)

            val selectStatement = select(
                animalName,
                case(animalName) {
                    `when`(isEqualTo("Artic fox"), isEqualTo("Red fox")) { then( 1L) }
                    `else`(2L)
                } `as` "IsAFox"
            ) {
                from(animalData)
                where { id.isIn(31, 32, 38, 39) }
                orderBy(id)
            }

            val expected = "select animal_name, " +
                    "case animal_name when = #{parameters.p1,jdbcType=VARCHAR}, = #{parameters.p2,jdbcType=VARCHAR} then 1 " +
                    "else 2 end " +
                    "as IsAFox from AnimalData where id in " +
                    "(#{parameters.p3,jdbcType=INTEGER},#{parameters.p4,jdbcType=INTEGER},#{parameters.p5,jdbcType=INTEGER},#{parameters.p6,jdbcType=INTEGER}) " +
                    "order by id"
            assertThat(selectStatement.selectStatement).isEqualTo(expected)
            assertThat(selectStatement.parameters)
                .containsOnly(
                    entry("p1", "Artic fox"),
                    entry("p2", "Red fox"),
                    entry("p3", 31),
                    entry("p4", 32),
                    entry("p5", 38),
                    entry("p6", 39)
                )

            val records = mapper.selectManyMappedRows(selectStatement)
            assertThat(records).hasSize(4)
            assertThat(records[0]).containsOnly(entry("ANIMAL_NAME", "Cat"), entry("ISAFOX", 2))
            assertThat(records[1]).containsOnly(entry("ANIMAL_NAME", "Artic fox"), entry("ISAFOX", 1))
            assertThat(records[2]).containsOnly(entry("ANIMAL_NAME", "Red fox"), entry("ISAFOX", 1))
            assertThat(records[3]).containsOnly(entry("ANIMAL_NAME", "Raccoon"), entry("ISAFOX", 2))
        }
    }

    @Test
    fun testCaseDoubles() {
        newSession().use { session ->
            val mapper = session.getMapper(CommonSelectMapper::class.java)

            val selectStatement = select(
                animalName,
                case(animalName) {
                    `when`(isEqualTo("Artic fox"), isEqualTo("Red fox")) { then( 1.1) }
                    `else`(2.2)
                } `as` "IsAFox"
            ) {
                from(animalData)
                where { id.isIn(31, 32, 38, 39) }
                orderBy(id)
            }

            val expected = "select animal_name, " +
                    "case animal_name when = #{parameters.p1,jdbcType=VARCHAR}, = #{parameters.p2,jdbcType=VARCHAR} then 1.1 " +
                    "else 2.2 end " +
                    "as IsAFox from AnimalData where id in " +
                    "(#{parameters.p3,jdbcType=INTEGER},#{parameters.p4,jdbcType=INTEGER},#{parameters.p5,jdbcType=INTEGER},#{parameters.p6,jdbcType=INTEGER}) " +
                    "order by id"
            assertThat(selectStatement.selectStatement).isEqualTo(expected)
            assertThat(selectStatement.parameters)
                .containsOnly(
                    entry("p1", "Artic fox"),
                    entry("p2", "Red fox"),
                    entry("p3", 31),
                    entry("p4", 32),
                    entry("p5", 38),
                    entry("p6", 39)
                )

            val records = mapper.selectManyMappedRows(selectStatement)
            assertThat(records).hasSize(4)
            assertThat(records[0]).containsOnly(entry("ANIMAL_NAME", "Cat"), entry("ISAFOX", BigDecimal("2.2")))
            assertThat(records[1]).containsOnly(entry("ANIMAL_NAME", "Artic fox"), entry("ISAFOX", BigDecimal("1.1")))
            assertThat(records[2]).containsOnly(entry("ANIMAL_NAME", "Red fox"), entry("ISAFOX", BigDecimal("1.1")))
            assertThat(records[3]).containsOnly(entry("ANIMAL_NAME", "Raccoon"), entry("ISAFOX", BigDecimal("2.2")))
        }
    }

    @Test
    fun testCaseCastDoubles() {
        newSession().use { session ->
            val mapper = session.getMapper(CommonSelectMapper::class.java)

            val selectStatement = select(
                animalName,
                case(animalName) {
                    `when`(isEqualTo("Artic fox"), isEqualTo("Red fox")) { then( 1.1) }
                    `else`(cast { 2.2 `as` "DOUBLE" })
                } `as` "IsAFox"
            ) {
                from(animalData)
                where { id.isIn(31, 32, 38, 39) }
                orderBy(id)
            }

            val expected = "select animal_name, " +
                    "case animal_name when = #{parameters.p1,jdbcType=VARCHAR}, = #{parameters.p2,jdbcType=VARCHAR} then 1.1 " +
                    "else cast(2.2 as DOUBLE) end " +
                    "as IsAFox from AnimalData where id in " +
                    "(#{parameters.p3,jdbcType=INTEGER},#{parameters.p4,jdbcType=INTEGER},#{parameters.p5,jdbcType=INTEGER},#{parameters.p6,jdbcType=INTEGER}) " +
                    "order by id"
            assertThat(selectStatement.selectStatement).isEqualTo(expected)
            assertThat(selectStatement.parameters)
                .containsOnly(
                    entry("p1", "Artic fox"),
                    entry("p2", "Red fox"),
                    entry("p3", 31),
                    entry("p4", 32),
                    entry("p5", 38),
                    entry("p6", 39)
                )

            val records = mapper.selectManyMappedRows(selectStatement)
            assertThat(records).hasSize(4)
            assertThat(records[0]).containsOnly(entry("ANIMAL_NAME", "Cat"), entry("ISAFOX", 2.2))
            assertThat(records[1]).containsOnly(entry("ANIMAL_NAME", "Artic fox"), entry("ISAFOX", 1.1))
            assertThat(records[2]).containsOnly(entry("ANIMAL_NAME", "Red fox"), entry("ISAFOX", 1.1))
            assertThat(records[3]).containsOnly(entry("ANIMAL_NAME", "Raccoon"), entry("ISAFOX", 2.2))
        }
    }

    @Test
    fun testInvalidDoubleElseSimple() {
        assertThatExceptionOfType(KInvalidSQLException::class.java).isThrownBy {
            case(animalName) {
                `when`(isEqualTo("Artic fox"), isEqualTo("Red fox")) { then("'yes'") }
                `else`("Fred")
                `else`("Wilma")
            }
        }.withMessage(Messages.getString("ERROR.42"))
    }

    @Test
    fun testInvalidDoubleThenSimple() {
        assertThatExceptionOfType(KInvalidSQLException::class.java).isThrownBy {
            case(animalName) {
                `when`(isEqualTo("Artic fox"), isEqualTo("Red fox")) {
                    then("'yes'")
                    then("no")
                }
                `else`("Fred")
            }
        }.withMessage(Messages.getString("ERROR.41"))
    }

    @Test
    fun testInvalidDoubleElseSearched() {
        assertThatExceptionOfType(KInvalidSQLException::class.java).isThrownBy {
            case {
                `when` {
                    id isEqualTo 22
                    then("'yes'")
                }
                `else`("Fred")
                `else`("Wilma")
            }
        }.withMessage(Messages.getString("ERROR.42"))
    }

    @Test
    fun testInvalidDoubleThenSearched() {
        assertThatExceptionOfType(KInvalidSQLException::class.java).isThrownBy {
            case {
                `when` {
                    id isEqualTo 22
                    then("'yes'")
                    then("'no'")
                }
            }
        }.withMessage(Messages.getString("ERROR.41"))
    }

    @Test
    fun testInvalidSearchedMissingWhen() {
        assertThatExceptionOfType(InvalidSqlException::class.java).isThrownBy {
            select(case { `else`("Fred") }) { from(animalData) }
        }.withMessage(Messages.getString("ERROR.40"))
    }

    @Test
    fun testInvalidSimpleMissingWhen() {
        assertThatExceptionOfType(InvalidSqlException::class.java).isThrownBy {
            select(case (id) { `else`("Fred") }) { from (animalData) }
        }.withMessage(Messages.getString("ERROR.40"))
    }

    @Test
    fun testInvalidCastMissingAs() {
        assertThatExceptionOfType(KInvalidSQLException::class.java).isThrownBy {
            cast {}
        }.withMessage(Messages.getString("ERROR.43"))
    }

    @Test
    fun testInvalidCastDoubleAs() {
        assertThatExceptionOfType(KInvalidSQLException::class.java).isThrownBy {
            cast {
                "Fred" `as` "VARCHAR"
                "Wilma" `as` "VARCHAR"
            }
        }.withMessage(Messages.getString("ERROR.43"))
    }
}
