package com.github.kittinunf.result

import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.assertThat
import org.junit.Test
import java.io.File
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class ValidationTests {

    @Test
    fun testValidation() {
        val r1: Result<Int, Exception> = Result.of(1)
        val r2: Result<Int, Exception> = Result.of(2)
        val r3: Result<Int, Exception> = Result.of(3)

        val validation = Validation(r1, r2, r3)
        assertThat("validation.hasFailures", validation.hasFailure, isEqualTo(false))
        assertThat("validation.failures", validation.failures, isEqualTo(listOf<Exception>()))
    }

    @Test
    fun testValidationWithError() {

        val r1: Result<Int, Exception> = Result.of(1)
        val r2: Result<Int, Exception> = Result.of { throw Exception("Not a number") }
        val r3: Result<Int, Exception> = Result.of(3)
        val r4: Result<Int, Exception> = Result.of { throw Exception("Division by zero") }

        val validation = Validation(r1, r2, r3, r4)
        assertThat("validation.hasFailures", validation.hasFailure, isEqualTo(true))
        assertThat("validation.failures", validation.failures.map { it.message }, isEqualTo(listOf<String?>("Not a number", "Division by zero")))
    }

    @Test
    fun testSuspendableValidation() {
        runBlocking {
            val readFooResult = async { resultReadFromAssetFileName("foo.txt") }
            val readBarResult = async { resultReadFromAssetFileName("bar.txt") }

            val validation = SuspendedValidation(readFooResult.await(), readBarResult.await())
            assertThat("validation.hasFailures", validation.hasFailure, isEqualTo(false))
            assertThat("validation.failures", validation.failures, isEqualTo(listOf<Exception>()))
        }
    }

    @Test
    fun testSuspendableValidationWithError() {
        runBlocking {
            val r1 = async { SuspendableResult.of { readFromAssetFileName("foo.txt")} }
            val r2 = async { SuspendableResult.of { throw Exception("Exception r2") } }
            val r3 = async { SuspendableResult.of { readFromAssetFileName("bar.txt")} }
            val r4 = async { SuspendableResult.of { throw Exception("Exception r4") } }

            val validation = SuspendedValidation(r1.await(), r2.await(), r3.await(), r4.await())
            assertThat("validation.hasFailures", validation.hasFailure, isEqualTo(true))
            assertThat("validation.failures", validation.failures.map { it.message }, isEqualTo(listOf<String?>("Exception r2", "Exception r4")))
        }
    }

    // helper
    fun readFromAssetFileName(name: String): String {
        val dir = System.getProperty("user.dir")
        val assetsDir = File(dir, "src/test/assets/")
        Thread.sleep(2000)
        return File(assetsDir, name).readText()
    }

    suspend fun resultReadFromAssetFileName(name: String): SuspendableResult<String, Exception> {
        val operation = async { readFromAssetFileName(name) }.await()
        return SuspendableResult.of(operation)
    }

}
