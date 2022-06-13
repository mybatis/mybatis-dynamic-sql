/*
 *    Copyright 2016-2022 the original author or authors.
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
package nullability.test

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NotBetweenTest {
    @Test
    fun `Test That First Null Causes Compile Error`() {
        val sourceLines = """
            package temp.kotlin.test
            
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.id
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom

            fun testFunction() {
                countFrom(person) {
                    where { id isNotBetween null and 4 }
                }
            }
        """.trimIndent().lines()

        val compilerErrorReports = compileIt(sourceLines)
        assertThat(compilerErrorReports).hasSize(1)
        val matchCount = compilerErrorReports.matchCount(ExpectedErrorLocation(9, 33))
        assertThat(matchCount).isEqualTo(1)
    }

    @Test
    fun `Test That Second Null Causes Compile Error`() {
        val sourceLines = """
            package temp.kotlin.test
            
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.id
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom

            fun testFunction() {
                countFrom(person) {
                    where { id isNotBetween 4 and null }
                }
            }
        """.trimIndent().lines()

        val compilerErrorReports = compileIt(sourceLines)
        assertThat(compilerErrorReports).hasSize(1)
        val matchCount = compilerErrorReports.matchCount(ExpectedErrorLocation(9, 39))
        assertThat(matchCount).isEqualTo(1)
    }

    @Test
    fun `Test That Both Null Causes Compile Errors`() {
        val sourceLines = """
            package temp.kotlin.test
            
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.id
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom

            fun testFunction() {
                countFrom(person) {
                    where { id isNotBetween null and null }
                }
            }
        """.trimIndent().lines()

        val compilerErrorReports = compileIt(sourceLines)
        assertThat(compilerErrorReports).hasSize(2)
        val matchCount = compilerErrorReports.matchCount(
            ExpectedErrorLocation(9, 33),
            ExpectedErrorLocation(9, 42)
        )
        assertThat(matchCount).isEqualTo(2)
    }

    @Test
    fun `Test That First Null In Elements Method Causes Compile Error`() {
        val sourceLines = """
            package temp.kotlin.test
            
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.id
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom
            import org.mybatis.dynamic.sql.util.kotlin.elements.isNotBetween

            fun testFunction() {
                countFrom(person) {
                    where { id (isNotBetween<Int>(null).and(4)) }
                }
            }
        """.trimIndent().lines()

        val compilerErrorReports = compileIt(sourceLines)
        assertThat(compilerErrorReports).hasSize(1)
        val matchCount = compilerErrorReports.matchCount(ExpectedErrorLocation(10, 39))
        assertThat(matchCount).isEqualTo(1)
    }

    @Test
    fun `Test That Second Null In Elements Method Causes Compile Error`() {
        val sourceLines = """
            package temp.kotlin.test
            
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.id
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom
            import org.mybatis.dynamic.sql.util.kotlin.elements.isNotBetween

            fun testFunction() {
                countFrom(person) {
                    where { id (isNotBetween(4).and(null)) }
                }
            }
        """.trimIndent().lines()

        val compilerErrorReports = compileIt(sourceLines)
        assertThat(compilerErrorReports).hasSize(1)
        val matchCount = compilerErrorReports.matchCount(ExpectedErrorLocation(10, 41))
        assertThat(matchCount).isEqualTo(1)
    }

    @Test
    fun `Test That Both Null In Elements Method Causes Compile Error`() {
        val sourceLines = """
            package temp.kotlin.test
            
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.id
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom
            import org.mybatis.dynamic.sql.util.kotlin.elements.isNotBetween

            fun testFunction() {
                countFrom(person) {
                    where { id (isNotBetween<Int>(null).and(null)) }
                }
            }
        """.trimIndent().lines()

        val compilerErrorReports = compileIt(sourceLines)
        assertThat(compilerErrorReports).hasSize(2)
        val matchCount = compilerErrorReports.matchCount(
            ExpectedErrorLocation(10, 39),
            ExpectedErrorLocation(10, 49)
        )
        assertThat(matchCount).isEqualTo(2)
    }
}
