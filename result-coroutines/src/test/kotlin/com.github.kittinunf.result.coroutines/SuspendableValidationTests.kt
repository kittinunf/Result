package com.github.kittinunf.result.coroutines

import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import java.io.File

class SuspendableValidationTests {

    @Test
    fun testSuspendableValidation() {
        runBlocking {
            val readFooResult = async { resultReadFromAssetFileName("foo.txt") }
            val readBarResult = async { resultReadFromAssetFileName("bar.txt") }

            val validation = SuspendedValidation(readFooResult.await(), readBarResult.await())
            assertThat("validation.hasFailures", validation.hasFailure, equalTo(false))
            assertThat("validation.failures", validation.failures, equalTo(listOf<Exception>()))
        }
    }

    @Test
    fun testSuspendableValidationWithError() {
        runBlocking {
            val r1 = async { SuspendableResult.of { readFromAssetFileName("foo.txt") } }
            val r2 = async { SuspendableResult.of { throw Exception("Exception r2") } }
            val r3 = async { SuspendableResult.of { readFromAssetFileName("bar.txt") } }
            val r4 = async { SuspendableResult.of { throw Exception("Exception r4") } }

            val validation = SuspendedValidation(r1.await(), r2.await(), r3.await(), r4.await())
            assertThat("validation.hasFailures", validation.hasFailure, equalTo(true))
            assertThat("validation.failures", validation.failures.map { it.message }, equalTo(listOf<String?>("Exception r2", "Exception r4")))
        }
    }

    // helper
    private fun readFromAssetFileName(name: String): String {
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
