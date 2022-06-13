package nullability.test

import org.assertj.core.api.Assertions.assertThat
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

        val matchingErrors = compileIt(sourceLines, listOf(ExpectedErrorLocation(10,32)))
        assertThat(matchingErrors).isEqualTo(1)
   }
}
