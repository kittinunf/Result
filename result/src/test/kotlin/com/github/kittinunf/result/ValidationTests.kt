package com.github.kittinunf.result

import org.junit.Assert.assertThat
import org.junit.Test
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class ValidationTests {

    @Test
    fun testValidation() {
        val r1: Result<Int, Exception> = Result.of(1)
        val r2: Result<Int, Exception> = Result.of(2)
        val r3: Result<Int, Exception> = Result.of(3)

        val validation = Validation(r1, r2, r3)
        assertThat("validation.hasFailures", validation.hasFailure, isEqualTo(false))
        assertThat("validation.failures", validation.failures, isEqualTo(listOf()))
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

}