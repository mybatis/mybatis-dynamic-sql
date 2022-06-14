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

class LikeNotLikeTest {
    @Test
    fun `Test That Null Like Causes Compile Error`() {
        val sourceLines = """
            package temp.kotlin.test
            
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.firstName
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom

            fun testFunction() {
                countFrom(person) {
                    where { firstName isLike null }
                }
            }
        """.trimIndent().lines()

        val compilerErrorReports = compile(sourceLines)
        assertThat(compilerErrorReports).hasSize(1)
        val matchCount = compilerErrorReports.matchCount(ExpectedErrorLocation(9, 34))
        assertThat(matchCount).isEqualTo(1)
    }

    @Test
    fun `Test That Null Like When Present is OK`() {
        val sourceLines = """
            package temp.kotlin.test
            
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.firstName
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom

            fun testFunction() {
                countFrom(person) {
                    where { firstName isLikeWhenPresent null }
                }
            }
        """.trimIndent().lines()

        val compilerErrorReports = compile(sourceLines)
        assertThat(compilerErrorReports).isEmpty()
    }

    @Test
    fun `Test That Null Not Like Causes Compile Error`() {
        val sourceLines = """
            package temp.kotlin.test
            
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.firstName
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom

            fun testFunction() {
                val ids = listOf(4, null)
                countFrom(person) {
                    where { firstName isNotLike null }
                }
            }
        """.trimIndent().lines()

        val compilerErrorReports = compile(sourceLines)
        assertThat(compilerErrorReports).hasSize(1)
        val matchCount = compilerErrorReports.matchCount(ExpectedErrorLocation(10, 37))
        assertThat(matchCount).isEqualTo(1)
    }

    @Test
    fun `Test That Null Not Like When Present is OK`() {
        val sourceLines = """
            package temp.kotlin.test
            
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.firstName
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom

            fun testFunction() {
                countFrom(person) {
                    where { firstName isNotLikeWhenPresent null }
                }
            }
        """.trimIndent().lines()

        val compilerErrorReports = compile(sourceLines)
        assertThat(compilerErrorReports).isEmpty()
    }

    @Test
    fun `Test That Null Elements Like Causes Compile Error`() {
        val sourceLines = """
            package temp.kotlin.test
            
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.firstName
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom
            import org.mybatis.dynamic.sql.util.kotlin.elements.isLike

            fun testFunction() {
                countFrom(person) {
                    where { firstName (isLike(null)) }
                }
            }
        """.trimIndent().lines()

        val compilerErrorReports = compile(sourceLines)
        assertThat(compilerErrorReports).hasSize(1)
        val matchCount = compilerErrorReports.matchCount(ExpectedErrorLocation(10, 35))
        assertThat(matchCount).isEqualTo(1)
    }

    @Test
    fun `Test That Null Elements Like When Present is OK`() {
        val sourceLines = """
            package temp.kotlin.test
            
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.firstName
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom
            import org.mybatis.dynamic.sql.util.kotlin.elements.isLikeWhenPresent

            fun testFunction() {
                countFrom(person) {
                    where { firstName (isLikeWhenPresent(null)) }
                }
            }
        """.trimIndent().lines()

        val compilerErrorReports = compile(sourceLines)
        assertThat(compilerErrorReports).isEmpty()
    }

    @Test
    fun `Test That Null Elements Not Like Causes Compile Error`() {
        val sourceLines = """
            package temp.kotlin.test
            
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.firstName
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom
            import org.mybatis.dynamic.sql.util.kotlin.elements.isNotLike

            fun testFunction() {
                val ids = listOf(4, null)
                countFrom(person) {
                    where { firstName (isNotLike(null)) }
                }
            }
        """.trimIndent().lines()

        val compilerErrorReports = compile(sourceLines)
        assertThat(compilerErrorReports).hasSize(1)
        val matchCount = compilerErrorReports.matchCount(ExpectedErrorLocation(11, 38))
        assertThat(matchCount).isEqualTo(1)
    }

    @Test
    fun `Test That Null Elements Not Like When Present is OK`() {
        val sourceLines = """
            package temp.kotlin.test
            
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.firstName
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom
            import org.mybatis.dynamic.sql.util.kotlin.elements.isNotLikeWhenPresent

            fun testFunction() {
                countFrom(person) {
                    where { firstName (isNotLikeWhenPresent(null)) }
                }
            }
        """.trimIndent().lines()

        val compilerErrorReports = compile(sourceLines)
        assertThat(compilerErrorReports).isEmpty()
    }
}
