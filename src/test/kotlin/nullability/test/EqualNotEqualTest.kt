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

class EqualNotEqualTest {
    @Test
    fun `Test That Null Equal Causes Compile Error`() {
        val sourceLines = """
            package temp.kotlin.test
            
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.firstName
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom

            fun testFunction() {
                countFrom(person) {
                    where { firstName isEqualTo null }
                }
            }
        """.trimIndent().lines()

        val compilerMessageCollector = compile(sourceLines)
        assertThat(compilerMessageCollector.errorLocations()).isEqualTo(listOf(ExpectedErrorLocation(9, 37)))
    }

    @Test
    fun `Test That Null Equal When Present is OK`() {
        val sourceLines = """
            package temp.kotlin.test
            
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.firstName
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom

            fun testFunction() {
                countFrom(person) {
                    where { firstName isEqualToWhenPresent null }
                }
            }
        """.trimIndent().lines()

        val compilerMessageCollector = compile(sourceLines)
        assertThat(compilerMessageCollector.errors).isEmpty()
    }

    @Test
    fun `Test That Null Not Equal Causes Compile Error`() {
        val sourceLines = """
            package temp.kotlin.test
            
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.firstName
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom

            fun testFunction() {
                val ids = listOf(4, null)
                countFrom(person) {
                    where { firstName isNotEqualTo null }
                }
            }
        """.trimIndent().lines()

        val compilerMessageCollector = compile(sourceLines)
        assertThat(compilerMessageCollector.errorLocations()).isEqualTo(listOf(ExpectedErrorLocation(10, 40)))
    }

    @Test
    fun `Test That Null Not Equal When Present is OK`() {
        val sourceLines = """
            package temp.kotlin.test
            
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.firstName
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom

            fun testFunction() {
                countFrom(person) {
                    where { firstName isNotEqualToWhenPresent null }
                }
            }
        """.trimIndent().lines()

        val compilerMessageCollector = compile(sourceLines)
        assertThat(compilerMessageCollector.errors).isEmpty()
    }

    @Test
    fun `Test That Null Elements Equal Causes Compile Error`() {
        val sourceLines = """
            package temp.kotlin.test
            
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.firstName
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom
            import org.mybatis.dynamic.sql.util.kotlin.elements.isEqualTo

            fun testFunction() {
                countFrom(person) {
                    where { firstName (isEqualTo(null)) }
                }
            }
        """.trimIndent().lines()

        val compilerMessageCollector = compile(sourceLines)
        assertThat(compilerMessageCollector.errorLocations()).isEqualTo(listOf(ExpectedErrorLocation(10, 38)))
    }

    @Test
    fun `Test That Null Elements Equal When Present is OK`() {
        val sourceLines = """
            package temp.kotlin.test
            
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.firstName
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom
            import org.mybatis.dynamic.sql.util.kotlin.elements.isEqualToWhenPresent

            fun testFunction() {
                countFrom(person) {
                    where { firstName (isEqualToWhenPresent(null)) }
                }
            }
        """.trimIndent().lines()

        val compilerMessageCollector = compile(sourceLines)
        assertThat(compilerMessageCollector.errors).isEmpty()
    }

    @Test
    fun `Test That Null Elements Not Equal Causes Compile Error`() {
        val sourceLines = """
            package temp.kotlin.test
            
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.firstName
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom
            import org.mybatis.dynamic.sql.util.kotlin.elements.isNotEqualTo

            fun testFunction() {
                val ids = listOf(4, null)
                countFrom(person) {
                    where { firstName (isNotEqualTo(null)) }
                }
            }
        """.trimIndent().lines()

        val compilerMessageCollector = compile(sourceLines)
        assertThat(compilerMessageCollector.errorLocations()).isEqualTo(listOf(ExpectedErrorLocation(11, 41)))
    }

    @Test
    fun `Test That Null Elements Not Equal When Present is OK`() {
        val sourceLines = """
            package temp.kotlin.test
            
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.firstName
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom
            import org.mybatis.dynamic.sql.util.kotlin.elements.isNotEqualToWhenPresent

            fun testFunction() {
                countFrom(person) {
                    where { firstName (isNotEqualToWhenPresent(null)) }
                }
            }
        """.trimIndent().lines()

        val compilerMessageCollector = compile(sourceLines)
        assertThat(compilerMessageCollector.errors).isEmpty()
    }
}
