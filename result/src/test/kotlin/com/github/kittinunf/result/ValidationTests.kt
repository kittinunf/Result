package com.github.kittinunf.result

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class ValidationTests {

    @Test
    fun testValidation() {
        val r1: Result<Int, Exception> = Result.of(1)
        val r2: Result<Int, Exception> = Result.of(2)
        val r3: Result<Int, Exception> = Result.of(3)

        val validation = Validation(r1, r2, r3)
        assertThat("validation.hasFailures", validation.hasFailure, equalTo(false))
        assertThat("validation.failures", validation.failures, equalTo(listOf()))
    }

    @Test
    fun testValidationWithError() {

        val r1: Result<Int, Exception> = Result.of(1)
        val r2: Result<Int, Exception> = Result.of { throw Exception("Not a number") }
        val r3: Result<Int, Exception> = Result.of(3)
        val r4: Result<Int, Exception> = Result.of { throw Exception("Division by zero") }

        val validation = Validation(r1, r2, r3, r4)
        assertThat("validation.hasFailures", validation.hasFailure, equalTo(true))
        assertThat("validation.failures", validation.failures.map { it.message }, equalTo(listOf<String?>("Not a number", "Division by zero")))
    }

}
