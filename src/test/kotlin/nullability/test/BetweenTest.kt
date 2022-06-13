package nullability.test

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BetweenTest {
    @Test
    fun `Test That First Null Causes Compile Error`() {
        val sourceLines = """
            package temp.kotlin.test
            
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.id
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom

            fun testFunction() {
                countFrom(person) {
                    where { id isBetween null and 4 }
                }
            }
        """.trimIndent().lines()

        val compilerErrorReports = compileIt(sourceLines)
        assertThat(compilerErrorReports).hasSize(1)
        val matchCount = compilerErrorReports.matchCount(ExpectedErrorLocation(9,30))
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
                    where { id isBetween 4 and null }
                }
            }
        """.trimIndent().lines()

        val compilerErrorReports = compileIt(sourceLines)
        assertThat(compilerErrorReports).hasSize(1)
        val matchCount = compilerErrorReports.matchCount(ExpectedErrorLocation(9,36))
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
                    where { id isBetween null and null }
                }
            }
        """.trimIndent().lines()

        val compilerErrorReports = compileIt(sourceLines)
        assertThat(compilerErrorReports).hasSize(2)
        val matchCount = compilerErrorReports.matchCount(
            ExpectedErrorLocation(9,30),
            ExpectedErrorLocation(9, 39)
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
            import org.mybatis.dynamic.sql.util.kotlin.elements.isBetween

            fun testFunction() {
                countFrom(person) {
                    where { id (isBetween<Int>(null).and(4)) }
                }
            }
        """.trimIndent().lines()

        val compilerErrorReports = compileIt(sourceLines)
        assertThat(compilerErrorReports).hasSize(1)
        val matchCount = compilerErrorReports.matchCount(ExpectedErrorLocation(10,36))
        assertThat(matchCount).isEqualTo(1)
    }

    @Test
    fun `Test That Second Null In Elements Method Causes Compile Error`() {
        val sourceLines = """
            package temp.kotlin.test
            
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.id
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom
            import org.mybatis.dynamic.sql.util.kotlin.elements.isBetween

            fun testFunction() {
                countFrom(person) {
                    where { id (isBetween(4).and(null)) }
                }
            }
        """.trimIndent().lines()

        val compilerErrorReports = compileIt(sourceLines)
        assertThat(compilerErrorReports).hasSize(1)
        val matchCount = compilerErrorReports.matchCount(ExpectedErrorLocation(10,38))
        assertThat(matchCount).isEqualTo(1)
    }

    @Test
    fun `Test That Both Null In Elements Method Causes Compile Error`() {
        val sourceLines = """
            package temp.kotlin.test
            
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.id
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom
            import org.mybatis.dynamic.sql.util.kotlin.elements.isBetween

            fun testFunction() {
                countFrom(person) {
                    where { id (isBetween<Int>(null).and(null)) }
                }
            }
        """.trimIndent().lines()

        val compilerErrorReports = compileIt(sourceLines)
        assertThat(compilerErrorReports).hasSize(2)
        val matchCount = compilerErrorReports.matchCount(
            ExpectedErrorLocation(10,36),
            ExpectedErrorLocation(10,46)
        )
        assertThat(matchCount).isEqualTo(2)
    }
}
