package nullability.test

import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.config.Services
import java.io.File

/**
 * Compiles the source lines and looks for errors in the expected locations.
 * Returns the number of errors that match the expected locations.
 */
fun compileIt(sourceLines: List<String>, expectedErrorLocations: List<ExpectedErrorLocation>): Int {
    val file: File = File.createTempFile("KotlinTest", ".kt")
    file.deleteOnExit()

    file.writeText(sourceLines.joinToString(separator = System.lineSeparator()))

    val compilerArgs = K2JVMCompilerArguments().apply {
        freeArgs += file.path
        classpath = System.getProperty("java.class.path")
//            noStdlib = true
//            noReflect = true
    }
    val errorCollector = CompilerErrorMessageCollector(expectedErrorLocations)
    K2JVMCompiler().exec(errorCollector, Services.EMPTY, compilerArgs)
    return errorCollector.matchingErrors()
}

class CompilerErrorMessageCollector(private val expectedLocations: List<ExpectedErrorLocation>): MessageCollector {
    val errors = mutableListOf<Report>()
    override fun clear() {
        errors.clear()
    }

    override fun hasErrors(): Boolean {
        return errors.size > 0
    }

    fun matchingErrors(): Int = errors.filter { it.matchesExpected }.size

    override fun report(
        severity: CompilerMessageSeverity,
        message: String,
        location: CompilerMessageSourceLocation?
    ) {
        if (severity.isError) {
            errors.add(Report(severity, message, location, matchesExpected(location)))
        }
    }

    private fun matchesExpected(location: CompilerMessageSourceLocation?) : Boolean {
        expectedLocations.forEach {
            if (location?.line == it.line && location.column == it.column){
                return true
            }
        }
        return false
    }
}

data class Report(
    val severity: CompilerMessageSeverity,
    val message: String,
    val location: CompilerMessageSourceLocation?,
    val matchesExpected: Boolean
)
data class ExpectedErrorLocation(val line: Int, val column: Int)
