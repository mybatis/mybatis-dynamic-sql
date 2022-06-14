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

class NotInTest {
    @Test
    fun `Test That Null In VarAgs Causes Compile Error`() {
        val sourceLines = """
            package temp.kotlin.test
            
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.id
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom

            fun testFunction() {
                countFrom(person) {
                    where { id.isNotIn(4, null) }
                }
            }
        """.trimIndent().lines()

        val compilerMessageCollector = compile(sourceLines)
        assertThat(compilerMessageCollector.errorLocations()).isEqualTo(listOf(ExpectedErrorLocation(9, 31)))
    }

    @Test
    fun `Test That Null in List Causes Compile Error`() {
        val sourceLines = """
            package temp.kotlin.test
            
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.id
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom

            fun testFunction() {
                val ids = listOf(4, null)
                countFrom(person) {
                    where { id isNotIn ids }
                }
            }
        """.trimIndent().lines()

        val compilerMessageCollector = compile(sourceLines)
        assertThat(compilerMessageCollector.errorLocations()).isEqualTo(listOf(ExpectedErrorLocation(10, 28)))
    }

    @Test
    fun `Test That Null In VarArgs Elements Method Causes Compile Error`() {
        val sourceLines = """
            package temp.kotlin.test
            
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.id
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom
            import org.mybatis.dynamic.sql.util.kotlin.elements.isNotIn

            fun testFunction() {
                countFrom(person) {
                    where { id (isNotIn(4, null)) }
                }
            }
        """.trimIndent().lines()

        val compilerMessageCollector = compile(sourceLines)
        assertThat(compilerMessageCollector.errorLocations()).isEqualTo(listOf(ExpectedErrorLocation(10, 32)))
    }

    @Test
    fun `Test That Null In List Elements Method Causes Compile Error`() {
        val sourceLines = """
            package temp.kotlin.test
            
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.id
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom
            import org.mybatis.dynamic.sql.util.kotlin.elements.isNotIn

            fun testFunction() {
                val ids = listOf(4, null)
                countFrom(person) {
                    where { id (isNotIn(ids)) }
                }
            }
        """.trimIndent().lines()

        val compilerMessageCollector = compile(sourceLines)
        assertThat(compilerMessageCollector.errorLocations()).isEqualTo(listOf(ExpectedErrorLocation(11, 29)))
    }
}
