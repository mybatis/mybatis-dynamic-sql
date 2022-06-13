package nullability.test

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class NullabilityTest {
    @Test
    fun `Test That Null In Varargs In Method Causes Compile Error`() {
        val sourceLines = """
            package temp.kotlin.test
            
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.id
            import examples.kotlin.mybatis3.canonical.PersonDynamicSqlSupport.person
            import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom
            import org.mybatis.dynamic.sql.util.kotlin.elements.isIn

            fun testFunction() {
                countFrom(person) {
                    where { id (isIn(4, 5, null)) }
                }
            }
        """.trimIndent().lines()

        val compilerErrorReports = compileIt(sourceLines)
        assertThat(compilerErrorReports).hasSize(1)
        val matchCount = compilerErrorReports.matchCount(ExpectedErrorLocation(10,32))
        assertThat(matchCount).isEqualTo(1)
   }

    @Test
    fun `Test That First Null In Between Method Causes Compile Error`() {
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

    @Disabled
    @Test
    fun `Test That Second Null In Between Method Causes Compile Error`() {
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

    @Disabled
    @Test
    fun `Test That Both Null In Between Method Causes Compile Error`() {
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
}
