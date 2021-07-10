package com.github.kittinunf.result

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ResultTest {

    @Test
    fun `should create value with success from construction block`() {
        val v = Result.of<Int, Throwable> { 1 }

        assertNotNull(v.get())
        assertIs<Result.Success<Int>>(v)
    }

    @Test
    fun `should create value with failure from construction block`() {
        val v = Result.of<Int, Throwable> { throw RuntimeException() }

        assertNull(v.component1())
        assertIs<Result.Failure<RuntimeException>>(v)
    }

    @Test
    fun `should create value with success`() {
        val s = Result.success(42)
        val ss = Result.success(null)

        assertNotNull(s.get())
        assertNull(ss.get())
    }

    @Test
    fun `should create value with failure`() {
        val e = Result.failure(RuntimeException())

        assertNull(e.getOrNull())
        assertNull(e.component1())
    }

    @Test
    fun `should create optional value with either success`() {
        val nullableStr: String? = null
        val str = "42"

        val s = Result.of<String, Throwable> { str }
        val ns = Result.of<String, Throwable> { nullableStr }

        assertIs<Result.Success<String>>(s)
        assertIs<Result.Success<String>>(ns)
    }

    @Test
    fun `should respond to isSuccess and isFailure according to the state`() {
        val str = "42"

        val s = Result.of<String, Throwable> { str }
        val ns = Result.of<String, Throwable> { throw IllegalStateException("42") }

        assertTrue(s.isSuccess())
        assertTrue(ns.isFailure())

        val nullableStr: String? = null
        val nns = Result.of<String, Throwable> { nullableStr }
        assertTrue(nns.isSuccess())
        assertNull(nns.getOrNull())
    }

    @Test
    fun `should response to the success and failure block according to the state`() {
        val str = "42"

        val s = Result.of<String, Throwable> { str }
        val ns = Result.of<String, Throwable> { throw IllegalStateException("42") }

        var isSuccessCalled = false
        s.success {
            isSuccessCalled = true
        }

        var isFailureNotCalled = false
        s.failure {
            isFailureNotCalled = true
        }

        var isFailureCalled = false
        ns.failure {
            isFailureCalled = true
        }

        var isSuccessNotCalled = false
        ns.success {
            isSuccessNotCalled = true
        }

        assertTrue(isSuccessCalled)
        assertFalse(isSuccessNotCalled)
        assertTrue(isFailureCalled)
        assertFalse(isFailureNotCalled)
    }

    private fun Nothing.count() = 0

    @Test
    @Suppress("UNREACHABLE_CODE")
    fun `should map to another value of the result type`() {

        val success = Result.of<String, Throwable> { "success" }
        val failure = Result.failure(RuntimeException("failure"))

        val v1 = success.map { it.count() }
        val v2 = failure.map { it.count() }

        assertIs<Result.Success<Int>>(v1)
        assertEquals(v1.value, 7)

        assertIs<Result.Failure<Throwable>>(v2)
        assertEquals(v2.getOrNull(), null)
    }

    @Test
    @Suppress("UNREACHABLE_CODE")
    fun `should flatMap with another result type and flatten`() {
        val success = Result.of<String, Throwable> { "success" }
        val failure = Result.failure(RuntimeException("failure"))

        val v1 = success.flatMap { Result.of { it.last() } }
        val v2 = failure.flatMap { Result.of { it.count() } }

        assertIs<Result.Success<Char>>(v1)
        assertEquals(v1.value, 's')

        assertIs<Result.Failure<Throwable>>(v2)
        assertEquals(v2.getOrNull(), null)
    }

    @Test
    fun `should mapError to another value of the result type`() {
        val success = Result.of<String, Throwable> { "success" }
        val failure = Result.failure(Exception("failure"))

        val v1 = success.mapError { IllegalStateException(it.message) }
        val v2 = failure.mapError { IllegalStateException(it.message) }

        assertIs<Result.Success<String>>(v1)
        assertEquals(v1.value, "success")

        assertIs<Result.Failure<IllegalStateException>>(v2)
        assertEquals(v2.error.message, "failure")
    }

    @Test
    fun `should flatMapError to another value of the result type and flatten`() {
        val success = Result.of<String, Throwable> { "success" }
        val failure = Result.failure(RuntimeException("failure"))

        val v1 = success.flatMapError { Result.of { throw IllegalArgumentException(it.message) } }
        val v2 = failure.flatMapError { Result.of { throw IllegalArgumentException(it.message) } }

        assertIs<Result.Success<String>>(v1)
        assertEquals(v1.value, "success")

        assertIs<Result.Failure<IllegalArgumentException>>(v2)
        assertEquals(v2.error.message, "failure")
    }

    @Test
    fun `should be able to observe error with onError`() {
        val success = Result.success("success")
        val failure = Result.failure(Exception("failure"))

        var hasSuccessChanged = false
        var hasFailureChanged = false

        val v1 = success.onError { hasSuccessChanged = true }
        val v2 = failure.onError { hasFailureChanged = true }

        assertIs<Result.Success<*>>(v1)
        assertSame(v1, success)
        assertIs<Result.Failure<*>>(v2)
        assertSame(v2, failure)

        assertFalse(hasSuccessChanged)
        assertTrue(hasFailureChanged)
    }

    @Test
    fun `should be able to observe success with onSuccess`() {
        val success = Result.success("success")
        val failure = Result.failure(Exception("failure"))

        var hasSuccessChanged = false
        var hasFailureChanged = false

        val v1 = success.onSuccess { hasSuccessChanged = true }
        val v2 = failure.onSuccess { hasFailureChanged = true }

        assertIs<Result.Success<*>>(v1)
        assertEquals(v1, success)
        assertIs<Result.Failure<*>>(v2)
        assertSame(v2, failure)

        assertTrue(hasSuccessChanged)
        assertFalse(hasFailureChanged)
    }

    @Test
    fun `should lift the result to success if all of the item is success`() {
        val rs = listOf("lorem_short", "lorem_long").map {
            Result.of<String, Exception> {
                readFile(
                    directory = "src/commonTest/resources/",
                    fileName = "$it.txt"
                )
            }
        }.lift()

        assertIs<Result.Success<List<String>>>(rs)
        assertEquals(rs.get()[0], readFile("src/commonTest/resources", "lorem_short.txt"))
    }

    @Test
    fun `should lift the result to failure if any of the item is failure`() {
        val rs = listOf("lorem_short", "lorem_long", "not_found").map {
            Result.of<String, Exception> {
                readFile(
                    directory = "src/commonTest/resources/",
                    fileName = "$it.txt"
                )
            }
        }.lift()

        assertIs<Result.Failure<*>>(rs)
    }
}
