package com.github.kittinunf.result

import kotlin.js.JsName
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.test.fail

class ResultTest {
    @Test
    @JsName("should_create_value_with_success_from_construction_block")
    fun `should create value with success from construction block`() {
        val v = Result.of<Int, Throwable> { 1 }

        assertNotNull(v.get())
        assertIs<Result.Success<Int>>(v)
        assertEquals(Kind.Success, v.kind)
    }

    @Test
    @JsName("should_create_value_with_failure_from_construction_block")
    fun `should create value with failure from construction block`() {
        val v = Result.of<Int, Throwable> { throw RuntimeException() }

        assertNull(v.component1())
        assertNotNull(v.component2())
        assertIs<Result.Failure<RuntimeException>>(v)
        assertEquals(Kind.Failure, v.kind)
    }

    @Test
    @JsName("should_get_return_value_and_get_failure_return_null_for_result_with_success")
    fun `should get return value and get failure return null for result with success`() {
        val s = Result.of<Int, IllegalStateException> { 42 }

        assertNotNull(s.component1())
        assertNull(s.component2())
        assertNull(s.getFailureOrNull())
        assertNotNull(s.getOrNull())
        assertEquals(42, s.getOrNull())
    }

    @Test
    @JsName("should_get_return_null_and_get_failure_return_error_for_result_with_failure")
    fun `should get return null and get failure return error for result with failure`() {
        val f = Result.of<String, IllegalStateException> { throw IllegalStateException("foo") }

        assertNull(f.getOrNull())
        assertNotNull(f.getFailureOrNull())
        assertIs<IllegalStateException>(f.getFailureOrNull())
    }

    @Test
    @JsName("should_create_value_with_success")
    fun `should create value with success`() {
        val s = Result.success(42)
        val ss = Result.success(null)
        val (value, _) = s

        assertNotNull(s.get())
        assertNull(ss.get())
        assertEquals(42, value)
    }

    @Test
    @JsName("should_create_value_with_failure")
    fun `should create value with failure`() {
        val e = Result.failure(RuntimeException())
        val (value, err) = e

        assertNull(value)
        assertNotNull(e.failure())
        assertNotNull(err)
    }

    @Test
    @JsName("should_get_the_alternative_value_if_failure_otherwise_get_the_value")
    fun `should get the alternative value if failure otherwise get the value`() {
        val s = Result.success(42)
        val f = Result.failure(RuntimeException())

        val alternative1 = s.getOrElse { 100 }
        val alternative2 = f.getOrElse { 100 }

        assertEquals(42, alternative1)
        assertEquals(100, alternative2)
    }

    @Test
    @JsName("should_create_optional_value_with_either_success")
    fun `should create optional value with either success`() {
        val nullableStr: String? = null
        val str = "42"

        val s = Result.of<String, Throwable> { str }
        val ns = Result.of<String, Throwable> { nullableStr }

        assertIs<Result.Success<String>>(s)
        assertIs<Result.Success<String>>(ns)
    }

    @Test
    @JsName("should_respond_to_isSuccess_and_isFailure_according_to_the_state")
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
    @JsName("should_response_to_isSuccess_and_isFailure_and_also_implies_that_contract_is_working_correctly")
    fun `should response to isSuccess and isFailure and also implies that contract is working correctly`() {
        val str = "42"

        val s = Result.of<String, Throwable> { str }
        val ns = Result.of<String, Throwable> { throw IllegalStateException("42") }

        if (s.isSuccess()) {
            // contract should imply that s is Result.Success
            s.value
            assertEquals(42, s.value.toInt())
        } else {
            fail("This should not be called")
        }

        if (ns.isFailure()) {
            // contract should imply that ns is Result.Failure
            ns.error
            assertIs<IllegalStateException>(ns.error)
        } else {
            fail("This should not be called")
        }
    }

    @Test
    @JsName("should_response_to_onSuccess_that_implies_contract_is_working_for_first_value")
    fun `should response to onSuccess that implies contact is working for first value`() {
        val s = Result.of<Int, Throwable> { 0 }

        val i: Int
        s.onSuccess {
            i = 42
        }
        assertEquals(42, i)
    }

    @Test
    @JsName("should_response_to_onFailure_that_implies_contract_is_working_for_first_value")
    fun `should response to onFailure that implies contact is working for first value`() {
        val s = Result.of<Int, Throwable> { throw IllegalStateException("40") }

        val i: Int
        s.onFailure {
            i = 42
        }
        assertEquals(42, i)
    }

    @Test
    @JsName("should_response_to_the_success_and_failure_block_according_to_the_state")
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
    @JsName("should_be_able_to_use_fold_to_check_for_value_for_both_success_and_or_failure")
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

    private fun count() = 0

    @Test
    @JsName("should_map_to_another_value_of_the_result_type")
    fun `should map to another value of the result type`() {
        val success = Result.of<String, Throwable> { "success" }
        val failure = Result.failure(RuntimeException("failure"))

        val v1 = success.map { it.count() }
        val v2 = failure.map { count() }

        assertIs<Result.Success<Int>>(v1)
        assertEquals(v1.value, 7)

        assertIs<Result.Failure<Throwable>>(v2)
        assertEquals(v2.getOrNull<Int, RuntimeException>(), null)
    }

    @Test
    @JsName("should_flatMap_with_another_result_type_and_flatten")
    fun `should flatMap with another result type and flatten`() {
        val success = Result.of<String, Throwable> { "success" }
        val failure = Result.failure(RuntimeException("failure"))

        val v1 = success.flatMap { Result.of { it.last() } }
        val v2 = failure.flatMap { Result.of { count() } }

        assertIs<Result.Success<Char>>(v1)
        assertEquals(v1.value, 's')

        assertIs<Result.Failure<Throwable>>(v2)
        assertEquals(v2.getOrNull<Int, RuntimeException>(), null)
    }

    @Test
    @JsName("should_mapError_to_another_value_of_the_result_type")
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
    @JsName("should_flatMapError_to_another_value_of_the_result_type_and_flatten")
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
    @JsName("should_be_able_to_observe_error_with_onError")
    fun `should be able to observe error with onError`() {
        val success = Result.success("success")
        val failure = Result.failure(Exception("failure"))

        var hasSuccessChanged: Boolean
        var hasFailureChanged: Boolean

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
    @JsName("should_be_able_to_observe_success_with_onSuccess")
    fun `should be able to observe success with onSuccess`() {
        val success = Result.success("success")
        val failure = Result.failure(Exception("failure"))

        var hasSuccessChanged: Boolean
        var hasFailureChanged: Boolean

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
    @JsName("should_be_able_to_transform_both_value_from_2_sides")
    fun `should be able to transform both value from 2 sides`() {
        val s = Result.of<String, Throwable> { "hello world" }

        val transformedSuccess = s.mapBoth({ it.length }, { IllegalStateException("changed!") })

        assertIs<String>(s.get())
        assertIs<Int>(transformedSuccess.get())
        assertEquals(11, transformedSuccess.get())
        assertEquals(null, transformedSuccess.getFailureOrNull())

        val f = Result.of<Int, Throwable> { emptyList<Int>()[0] } // this should throw outofbounds

        val transformedFailure = f.mapBoth({ it > 1 }, { RuntimeException("changed!") })

        assertIs<IndexOutOfBoundsException>(f.getFailureOrNull())
        assertIs<RuntimeException>(transformedFailure.getFailureOrNull())
        assertEquals(null, transformedFailure.getOrNull())
        assertEquals("changed!", transformedFailure.getFailureOrNull()!!.message)
    }

    @Test
    @JsName("should_be_able_to_fanout_for_success")
    fun `should be able to fanout for success`() {
        val s1 = Result.of<String, Throwable> { Resource("lorem_short.txt").read() }
        val s2 = Result.of<Int, Throwable> { 42 }

        val (value, error) = s1.fanout { s2 }

        assertNull(error)
        assertContains(value!!.first, "Lorem ipsum dolor sit amet")
        assertEquals(42, value.second)
    }

    @Test
    @JsName("should_result_in_error_for_left_or_right_is_a_failure")
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
    @JsName("should_lift_the_result_to_success_if_all_of_the_items_are_success")
    fun `should lift the result to success if all of the items are success`() {
        val rs =
            listOf("lorem_short", "lorem_long").map {
                Result.of<String, Exception> {
                    Resource("$it.txt").read()
                }
            }.lift()

        assertIs<Result.Success<List<String>>>(rs)
        assertEquals(rs.get()[0], Resource("lorem_short.txt").read())
    }

    @Test
    @JsName("should_lift_the_result_to_failure_if_any_of_the_item_is_failure")
    fun `should lift the result to failure if any of the item is failure`() {
        val rs =
            listOf("lorem_short", "lorem_long", "not_found").map {
                Result.of<String, Throwable> {
                    Resource("$it.txt").read()
                }
            }.lift()

        assertIs<Result.Failure<*>>(rs)
        val msg = rs.error.message
        assertNotNull(msg)
        assertContains(msg, "No such file or directory", ignoreCase = true)
    }

    @Test
    @JsName("should_lift_success_and_failures_to_callback_returning_success")
    fun `should lift success and failures to callback returning success`() {
        val rs =
            listOf("lorem_short", "lorem_long", "not_found").map {
                Result.of<String, Throwable> {
                    Resource("$it.txt").read()
                }
            }.lift { successes, errors ->
                assertEquals(2, successes.size)
                assertEquals(1, errors.size)
                Result.success(successes)
            }

        assertIs<Result.Success<List<String>>>(rs)
    }

    @Test
    @JsName("should_lift_success_and_failures_to_callback_returning_error")
    fun `should lift success and failures to callback returning error`() {
        val rs =
            listOf("lorem_short", "lorem_long", "not_found").map {
                Result.of<String, Throwable> {
                    Resource("$it.txt").read()
                }
            }.lift { successes, errors ->
                assertEquals(2, successes.size)
                assertEquals(1, errors.size)
                Result.failure(errors.first())
            }

        assertIs<Result.Failure<Throwable>>(rs)
    }

    @Test
    @JsName("should_return_true_if_predicate_in_any_matches")
    fun `should return true if predicate in any matches`() {
        val rs = Result.of<String, Throwable> { Resource("lorem_short.txt").read() }

        assertFalse(rs.any { it.isEmpty() })
        assertTrue(rs.any { it.count() <= 446 })

        val rf = Result.of<Int, Throwable> { throw IllegalStateException() }

        assertFalse(rf.any { it == 0 })
    }

    @Test
    @JsName("should_result_in_Failure_just_the_same_as_companion_objects_function")
    fun `should result in Failure just the same as companion object's function`() {
        data class TestException(val errorCode: String, val errorMessage: String) : Exception(errorMessage)

        val originalFailure = Result.failure(TestException("error_code", "error message"))
        val extensionFailure = TestException("error_code", "error message").failure()

        assertEquals(originalFailure, extensionFailure)
    }

    @Test
    @JsName("should_result_in_Success_just_the_same_as_companion_objects_function")
    fun `should result in Success just the same as companion object's function`() {
        val originalSuccess = Result.success("success")
        val extensionSuccess = "success".success()

        assertEquals(originalSuccess, extensionSuccess)
    }

    @Test
    @JsName("should_return_a_merged_list_given_two_Results_Success_containing_a_list")
    fun `should return a merged list given two Results Success containing a list`() {
        val result = Result.success(listOf("A", "B"))
        val otherResult = Result.success(listOf("C", "D"))

        val mergedList = result + otherResult
        assertIs<Result.Success<List<String>>>(mergedList)
        assertEquals(mergedList.value, listOf("A", "B", "C", "D"))
    }

    @Test
    @JsName("should_return_an_Error_given_a_Result_containing_a_Success_list_and_another_Result_Error")
    fun `should return an Error given a Result containing a Success list and another Result Error`() {
        val error = RuntimeException()

        val result = Result.success(listOf("A", "B"))
        val otherResult = Result.failure(error)

        val mergedList = result + otherResult
        assertIs<Result.Failure<RuntimeException>>(mergedList)
        assertEquals(mergedList.error, error)
    }

    @Test
    @JsName("should_return_an_Error_given_a_Result_Error_and_another_one_containing_a_Result_Success_list")
    fun `should return an Error given a Result Error and another one containing a Result Success list`() {
        val error = RuntimeException()

        val result = Result.failure(error)
        val otherResult = Result.success(listOf("B", "C"))

        val mergedResult = result + otherResult
        assertIs<Result.Failure<RuntimeException>>(mergedResult)
        assertEquals(mergedResult.error, error)
    }

    @Test
    @JsName("should_return_the_Error_from_the_first_Result_given_two_Result_Errors")
    fun `should return the Error from the first Result given two Result Errors`() {
        val firstError = RuntimeException()
        val secondError = RuntimeException()

        val result = Result.of<List<String>, Throwable> { throw firstError }
        val otherResult = Result.of<List<String>, Throwable> { throw secondError }

        val mergedResult = result + otherResult
        assertIs<Result.Failure<RuntimeException>>(mergedResult)
        assertEquals(mergedResult.error, firstError)
    }
}
