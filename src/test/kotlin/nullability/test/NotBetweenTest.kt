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
        val matchCount = compilerErrorReports.matchCount(ExpectedErrorLocation(9,33))
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
        val matchCount = compilerErrorReports.matchCount(ExpectedErrorLocation(9,39))
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
            ExpectedErrorLocation(9,33),
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
        val matchCount = compilerErrorReports.matchCount(ExpectedErrorLocation(10,39))
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
        val matchCount = compilerErrorReports.matchCount(ExpectedErrorLocation(10,41))
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
            ExpectedErrorLocation(10,39),
            ExpectedErrorLocation(10,49)
        )
        assertThat(matchCount).isEqualTo(2)
    }
}
