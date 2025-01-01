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
package nullability.test

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ComparisonTest {
    @Test
    fun `Test That Null In EqualTo Causes Compile Error`() {
        val source = """
            package temp.kotlin.test

            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.id
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom

            fun testFunction() {
                countFrom(person) {
                    where { id isEqualTo null }
                }
            }
        """

        val compilerMessageCollector = compile(source)
        assertThat(compilerMessageCollector.errorLocations())
            .hasSize(1)
            .contains(ErrorLocation(9, 20))
    }

    @Test
    fun `Test That Null In EqualToWhenPresent is OK`() {
        val source = """
            package temp.kotlin.test

            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.id
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom

            fun testFunction() {
                countFrom(person) {
                    where { id isEqualToWhenPresent null }
                }
            }
        """

        val compilerMessageCollector = compile(source)
        assertThat(compilerMessageCollector.hasErrors()).isFalse
    }

    @Test
    fun `Test That Null In EqualTo Elements Causes Compile Error`() {
        val source = """
            package temp.kotlin.test

            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.id
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom
            import org.mybatis.dynamic.sql.util.kotlin.elements.isEqualTo

            fun testFunction() {
                countFrom(person) {
                    where { id (isEqualTo(null)) }
                }
            }
        """

        val compilerMessageCollector = compile(source)
        assertThat(compilerMessageCollector.errorLocations())
            .hasSize(1)
            .contains(ErrorLocation(10, 21))
    }

    @Test
    fun `Test That Null In EqualToWhenPresent Elements is OK`() {
        val source = """
            package temp.kotlin.test

            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.id
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom
            import org.mybatis.dynamic.sql.util.kotlin.elements.isEqualToWhenPresent

            fun testFunction() {
                countFrom(person) {
                    where { id (isEqualToWhenPresent(null)) }
                }
            }
        """

        val compilerMessageCollector = compile(source)
        assertThat(compilerMessageCollector.hasErrors()).isFalse
    }

    @Test
    fun `Test That Null In NotEqualTo Causes Compile Error`() {
        val source = """
            package temp.kotlin.test

            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.id
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom

            fun testFunction() {
                countFrom(person) {
                    where { id isNotEqualTo null }
                }
            }
        """

        val compilerMessageCollector = compile(source)
        assertThat(compilerMessageCollector.errorLocations())
            .hasSize(1)
            .contains(ErrorLocation(9, 20))
    }

    @Test
    fun `Test That Null In NotEqualToWhenPresent is OK`() {
        val source = """
            package temp.kotlin.test

            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.id
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom

            fun testFunction() {
                countFrom(person) {
                    where { id isNotEqualToWhenPresent null }
                }
            }
        """

        val compilerMessageCollector = compile(source)
        assertThat(compilerMessageCollector.hasErrors()).isFalse
    }

    @Test
    fun `Test That Null In NotEqualTo Elements Causes Compile Error`() {
        val source = """
            package temp.kotlin.test

            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.id
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom
            import org.mybatis.dynamic.sql.util.kotlin.elements.isNotEqualTo

            fun testFunction() {
                countFrom(person) {
                    where { id (isNotEqualTo(null)) }
                }
            }
        """

        val compilerMessageCollector = compile(source)
        assertThat(compilerMessageCollector.errorLocations())
            .hasSize(1)
            .contains(ErrorLocation(10, 21))
    }

    @Test
    fun `Test That Null In NotEqualToWhenPresent Elements is OK`() {
        val source = """
            package temp.kotlin.test

            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.id
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom
            import org.mybatis.dynamic.sql.util.kotlin.elements.isNotEqualToWhenPresent

            fun testFunction() {
                countFrom(person) {
                    where { id (isNotEqualToWhenPresent(null)) }
                }
            }
        """

        val compilerMessageCollector = compile(source)
        assertThat(compilerMessageCollector.hasErrors()).isFalse
    }

    @Test
    fun `Test That Null In GreaterThan Causes Compile Error`() {
        val source = """
            package temp.kotlin.test

            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.id
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom

            fun testFunction() {
                countFrom(person) {
                    where { id isGreaterThan null }
                }
            }
        """

        val compilerMessageCollector = compile(source)
        assertThat(compilerMessageCollector.errorLocations())
            .hasSize(1)
            .contains(ErrorLocation(9, 20))
    }

    @Test
    fun `Test That Null In GreaterThanWhenPresent is OK`() {
        val source = """
            package temp.kotlin.test

            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.id
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom

            fun testFunction() {
                countFrom(person) {
                    where { id isGreaterThanWhenPresent null }
                }
            }
        """

        val compilerMessageCollector = compile(source)
        assertThat(compilerMessageCollector.hasErrors()).isFalse
    }

    @Test
    fun `Test That Null In GreaterThan Elements Causes Compile Error`() {
        val source = """
            package temp.kotlin.test

            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.id
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom
            import org.mybatis.dynamic.sql.util.kotlin.elements.isGreaterThan

            fun testFunction() {
                countFrom(person) {
                    where { id (isGreaterThan(null)) }
                }
            }
        """

        val compilerMessageCollector = compile(source)
        assertThat(compilerMessageCollector.errorLocations())
            .hasSize(1)
            .contains(ErrorLocation(10, 21))
    }

    @Test
    fun `Test That Null In GreaterThanWhenPresent Elements is OK`() {
        val source = """
            package temp.kotlin.test

            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.id
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom
            import org.mybatis.dynamic.sql.util.kotlin.elements.isGreaterThanWhenPresent

            fun testFunction() {
                countFrom(person) {
                    where { id (isGreaterThanWhenPresent(null)) }
                }
            }
        """

        val compilerMessageCollector = compile(source)
        assertThat(compilerMessageCollector.hasErrors()).isFalse
    }

    @Test
    fun `Test That Null In GreaterThanOrEqualTo Causes Compile Error`() {
        val source = """
            package temp.kotlin.test

            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.id
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom

            fun testFunction() {
                countFrom(person) {
                    where { id isGreaterThanOrEqualTo null }
                }
            }
        """

        val compilerMessageCollector = compile(source)
        assertThat(compilerMessageCollector.errorLocations())
            .hasSize(1)
            .contains(ErrorLocation(9, 20))
    }

    @Test
    fun `Test That Null In GreaterThanOrEqualToWhenPresent is OK`() {
        val source = """
            package temp.kotlin.test

            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.id
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom

            fun testFunction() {
                countFrom(person) {
                    where { id isGreaterThanOrEqualToWhenPresent null }
                }
            }
        """

        val compilerMessageCollector = compile(source)
        assertThat(compilerMessageCollector.hasErrors()).isFalse
    }

    @Test
    fun `Test That Null In GreaterThanOrEqualTo Elements Causes Compile Error`() {
        val source = """
            package temp.kotlin.test

            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.id
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom
            import org.mybatis.dynamic.sql.util.kotlin.elements.isGreaterThanOrEqualTo

            fun testFunction() {
                countFrom(person) {
                    where { id (isGreaterThanOrEqualTo(null)) }
                }
            }
        """

        val compilerMessageCollector = compile(source)
        assertThat(compilerMessageCollector.errorLocations())
            .hasSize(1)
            .contains(ErrorLocation(10, 21))
    }

    @Test
    fun `Test That Null In GreaterThanOrEqualToWhenPresent Elements is OK`() {
        val source = """
            package temp.kotlin.test

            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.id
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom
            import org.mybatis.dynamic.sql.util.kotlin.elements.isGreaterThanOrEqualToWhenPresent

            fun testFunction() {
                countFrom(person) {
                    where { id (isGreaterThanOrEqualToWhenPresent(null)) }
                }
            }
        """

        val compilerMessageCollector = compile(source)
        assertThat(compilerMessageCollector.hasErrors()).isFalse
    }

    @Test
    fun `Test That Null In LessThan Causes Compile Error`() {
        val source = """
            package temp.kotlin.test

            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.id
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom

            fun testFunction() {
                countFrom(person) {
                    where { id isLessThan null }
                }
            }
        """

        val compilerMessageCollector = compile(source)
        assertThat(compilerMessageCollector.errorLocations())
            .hasSize(1)
            .contains(ErrorLocation(9, 20))
    }

    @Test
    fun `Test That Null In LessThanWhenPresent is OK`() {
        val source = """
            package temp.kotlin.test

            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.id
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom

            fun testFunction() {
                countFrom(person) {
                    where { id isLessThanWhenPresent null }
                }
            }
        """

        val compilerMessageCollector = compile(source)
        assertThat(compilerMessageCollector.hasErrors()).isFalse
    }

    @Test
    fun `Test That Null In LessThan Elements Causes Compile Error`() {
        val source = """
            package temp.kotlin.test

            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.id
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom
            import org.mybatis.dynamic.sql.util.kotlin.elements.isLessThan

            fun testFunction() {
                countFrom(person) {
                    where { id (isLessThan(null)) }
                }
            }
        """

        val compilerMessageCollector = compile(source)
        assertThat(compilerMessageCollector.errorLocations())
            .hasSize(1)
            .contains(ErrorLocation(10, 21))
    }

    @Test
    fun `Test That Null In LessThanWhenPresent Elements is OK`() {
        val source = """
            package temp.kotlin.test

            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.id
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom
            import org.mybatis.dynamic.sql.util.kotlin.elements.isLessThanWhenPresent

            fun testFunction() {
                countFrom(person) {
                    where { id (isLessThanWhenPresent(null)) }
                }
            }
        """

        val compilerMessageCollector = compile(source)
        assertThat(compilerMessageCollector.hasErrors()).isFalse
    }

    @Test
    fun `Test That Null In LessThanOrEqualTo Causes Compile Error`() {
        val source = """
            package temp.kotlin.test

            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.id
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom

            fun testFunction() {
                countFrom(person) {
                    where { id isLessThanOrEqualTo null }
                }
            }
        """

        val compilerMessageCollector = compile(source)
        assertThat(compilerMessageCollector.errorLocations())
            .hasSize(1)
            .contains(ErrorLocation(9, 20))
    }

    @Test
    fun `Test That Null In LessThanOrEqualToWhenPresent is OK`() {
        val source = """
            package temp.kotlin.test

            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.id
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom

            fun testFunction() {
                countFrom(person) {
                    where { id isLessThanOrEqualToWhenPresent null }
                }
            }
        """

        val compilerMessageCollector = compile(source)
        assertThat(compilerMessageCollector.hasErrors()).isFalse
    }

    @Test
    fun `Test That Null In LessThanOrEqualTo Elements Causes Compile Error`() {
        val source = """
            package temp.kotlin.test

            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.id
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom
            import org.mybatis.dynamic.sql.util.kotlin.elements.isLessThanOrEqualTo

            fun testFunction() {
                countFrom(person) {
                    where { id (isLessThanOrEqualTo(null)) }
                }
            }
        """

        val compilerMessageCollector = compile(source)
        assertThat(compilerMessageCollector.errorLocations())
            .hasSize(1)
            .contains(ErrorLocation(10, 21))
    }

    @Test
    fun `Test That Null In LessThanOrEqualToWhenPresent Elements is OK`() {
        val source = """
            package temp.kotlin.test

            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.id
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom
            import org.mybatis.dynamic.sql.util.kotlin.elements.isLessThanOrEqualToWhenPresent

            fun testFunction() {
                countFrom(person) {
                    where { id (isLessThanOrEqualToWhenPresent(null)) }
                }
            }
        """

        val compilerMessageCollector = compile(source)
        assertThat(compilerMessageCollector.hasErrors()).isFalse
    }
}
