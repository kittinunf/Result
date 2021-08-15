package com.github.kittinunf.result

import kotlin.test.Test
import kotlin.test.assertContains
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
        assertEquals(Kind.Success, v.kind)
    }

    @Test
    fun `should create value with failure from construction block`() {
        val v = Result.of<Int, Throwable> { throw RuntimeException() }

        assertNull(v.component1())
        assertIs<Result.Failure<RuntimeException>>(v)
        assertEquals(Kind.Failure, v.kind)
    }

    @Test
    fun `should get return value and get failure return null for result with success`() {
        val s = Result.of<Int, IllegalStateException> { 42 }

        assertNull(s.getFailureOrNull())
        assertNotNull(s.getOrNull())
        assertEquals(42, s.getOrNull())
    }

    @Test
    fun `should get return null and get failure return error for result with failure`() {
        val f = Result.of<String, IllegalStateException> { throw IllegalStateException("foo") }

        assertNull(f.getOrNull())
        assertNotNull(f.getFailureOrNull())
        assertIs<IllegalStateException>(f.getFailureOrNull())
    }

    @Test
    fun `should create value with success`() {
        val s = Result.success(42)
        val ss = Result.success(null)
        val value = s.component1()

        assertNotNull(s.get())
        assertNull(ss.get())
        assertEquals(42, value)
    }

    @Test
    fun `should create value with failure`() {
        val e = Result.failure(RuntimeException())
        val err = e.component2()

        assertNull(e.component1())
        assertNotNull(err)
    }

    @Test
    fun `should get the alternative value if failure otherwise get the value`() {
        val s = Result.success(42)
        val f = Result.failure(RuntimeException())

        val alternative1 = s.getOrElse { 100 }
        val alternative2 = f.getOrElse { 100 }

        assertEquals(42, alternative1)
        assertEquals(100, alternative2)
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

    @Test
    fun `should be able to use fold to check for value for both success and or failure`() {
        val success = Result.of<String, Throwable> { "success" }
        val failure = Result.failure(RuntimeException("failure"))

        var hasFoldInSuccessCalled = false
        success.fold({
            hasFoldInSuccessCalled = true
        }, {
            hasFoldInSuccessCalled = false
        })

        var hasFoldInFailureCalled = false
        failure.fold({
            hasFoldInFailureCalled = false
        }, {
            hasFoldInFailureCalled = true
        })

        assertTrue(hasFoldInSuccessCalled)
        assertTrue(hasFoldInFailureCalled)
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

        val v1 = success.onFailure { hasSuccessChanged = true }
        val v2 = failure.onFailure { hasFailureChanged = true }

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
    fun `should be able to fanout for success`() {
        val s1 = Result.of<String, Throwable> { readFile(directory = "src/commonTest/resources/", fileName = "lorem_short.txt") }
        val s2 = Result.of<Int, Throwable> { 42 }

        val (value, error) = s1.fanout { s2 }

        assertNull(error)
        assertContains(value!!.first, "Lorem ipsum dolor sit amet")
        assertEquals(42, value.second)
    }

    @Test
    fun `should result in error for left or right is a failure`() {
        val err = Result.failure(IllegalStateException("foo foo"))
        val s = Result.success(42)

        val (value, error) = s.fanout { err }

        assertNotNull(error)
        assertNull(value)

        val (anotherValue, anotherError) = err.fanout { s }

        assertNotNull(anotherError)
        assertNull(anotherValue)
    }

    @Test
    fun `should lift the result to success if all of the items are success`() {
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
        val msg = rs.error.message
        assertNotNull(msg)
        assertContains(msg, "src/commonTest/resources/not_found.txt (No such file or directory)")
    }

    @Test
    fun `should return true if predicate in any matches`() {
        val rs = Result.of<String, Throwable> { readFile(directory = "src/commonTest/resources/", fileName = "lorem_short.txt") }

        assertFalse(rs.any { it.isEmpty() })
        assertTrue(rs.any { it.count() <= 446 })

        val rf = Result.of<Int, Throwable> { throw IllegalStateException() }

        assertFalse(rf.any { it == 0 })
    }

    @Test
    fun `should result in Failure just the same as companion object's function`() {
        data class TestException(val errorCode: String, val errorMessage: String): Exception(errorMessage)

        val originalFailure = Result.failure(TestException("error_code", "error message"))
        val extensionFailure = TestException("error_code", "error message").failure()

        assertEquals(originalFailure, extensionFailure)
    }

    @Test
    fun `should result in Success just the same as companion object's function`() {
        val originalSuccess = Result.success("success")
        val extensionSuccess = "success".success()

        assertEquals(originalSuccess, extensionSuccess)
    }
}
