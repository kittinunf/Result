package com.github.kittinunf.result

import org.hamcrest.CoreMatchers.*
import org.junit.Test
import java.io.File
import java.io.FileNotFoundException
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.hamcrest.CoreMatchers.`is` as isEqualTo

/**
 * Created by Kittinun Vantasin on 10/27/15.
 */

class ResultTests {

    @Test
    fun testCreateValue() {
        val v = Result.of(1)

        assertThat("Result is created successfully", v, notNullValue())
        assertThat("v is Result.Success type", v is Result.Success, isEqualTo(true))
    }

    @Test
    fun testCreateError() {
        val e = Result.error(RuntimeException())

        assertThat("Result is created successfully", e, notNullValue())
        assertThat("e is Result.Failure type", e is Result.Failure, isEqualTo(true))
    }

    @Test
    fun testCreateOptionalValue() {
        val value1: String? = null
        val value2: String? = "1"

        val result1 = Result.of(value1) { UnsupportedOperationException("value is null") }
        val result2 = Result.of(value2) { IllegalStateException("value is null") }

        assertThat("result1 is Result.Failure type", result1 is Result.Failure, isEqualTo(true))
        assertThat("result2 is Result.Success type", result2 is Result.Success, isEqualTo(true))
    }

    @Test
    fun testCreateFromLambda() {
        val f1 = { "foo" }
        val f2 = {
            val v = arrayListOf<Int>()
            v[1]
        }

        val f3 = {
            val s: String?
            s = null
            s
        }

        val result1 = Result.of(f1)
        val result2 = Result.of(f2)
        val result3 = Result.of(f3())

        assertThat("result1 is Result.Success type", result1 is Result.Success, isEqualTo(true))
        assertThat("result2 is Result.Failure type", result2 is Result.Failure, isEqualTo(true))
        assertThat("result3 is Result.Failure type", result3 is Result.Failure, isEqualTo(true))
    }

    @Test
    fun testOr() {
        val one = Result.of<Int>(null) or 1

        assertThat("one is Result.Success type", one is Result.Success, isEqualTo(true))
        assertThat("value one is 1", one.component1()!!, isEqualTo(1))
    }

    @Test
    fun testOrElse() {
        val one = Result.of<Int>(null) getOrElse 1

        assertThat("one is 1", one, isEqualTo(1))
    }

    @Test
    fun testSuccess() {
        val result = Result.of { true }

        var beingCalled = false
        result.success {
            beingCalled = true
        }

        var notBeingCalled = true
        result.failure {
            notBeingCalled = false
        }

        assertThat(beingCalled, isEqualTo(true))
        assertThat(notBeingCalled, isEqualTo(true))
    }

    @Test
    fun testFailure() {
        val result = Result.of { File("not_found_file").readText() }

        var beingCalled = false
        result.failure {
            beingCalled = true
        }

        var notBeingCalled = true
        result.success {
            notBeingCalled = false
        }

        assertThat(beingCalled, isEqualTo(true))
        assertThat(notBeingCalled, isEqualTo(true))
    }

    @Test
    fun testGet() {
        val f1 = { true }
        val f2 = { File("not_found_file").readText() }

        val result1 = Result.of(f1)
        val result2 = Result.of(f2)

        assertThat("result1 is true", result1.get(), isEqualTo(true))

        var result = false
        try {
            result2.get()
        } catch(e: FileNotFoundException) {
            result = true
        }

        assertThat("result2 expecting to throw FileNotFoundException", result, isEqualTo(true))
    }

    @Suppress("UNUSED_VARIABLE")
    @Test
    fun testGetAsValue() {
        val result1 = Result.of(22)
        val result2 = Result.error(KotlinNullPointerException())

        val v1: Int = result1.getAs()!!
        val (v2, err) = result2

        assertThat("v1 is equal 22", v1, isEqualTo(22))
        assertThat("err is KotlinNullPointerException type", err is KotlinNullPointerException, isEqualTo(true))
    }

    @Test
    fun testFold() {
        val success = Result.of("success")
        val failure = Result.error(RuntimeException("failure"))

        val v1 = success.fold({ 1 }, { 0 })
        val v2 = failure.fold({ 1 }, { 0 })

        assertThat("v1 is equal 1", v1, isEqualTo(1))
        assertThat("v2 is equal 1", v2, isEqualTo(0))
    }

    //helper
    fun Nothing.count() = 0

    fun Nothing.getMessage() = ""

    @Test
    fun testMap() {
        val success = Result.of("success")
        val failure = Result.error(RuntimeException("failure"))

        val v1 = success.map { it.count() }
        val v2 = failure.map { it.count() }

        assertThat("v1 getAsInt equals 7", v1.getAs(), isEqualTo(7))
        assertThat("v2 getAsInt null", v2.getAs<Int>(), nullValue())
    }

    @Test
    fun testFlatMap() {
        val success = Result.of("success")
        val failure = Result.error(RuntimeException("failure"))

        val v1 = success.flatMap { Result.of(it.last()) }
        val v2 = failure.flatMap { Result.of(it.count()) }

        assertThat("v1 getAsChar equals s", v1.getAs(), isEqualTo('s'))
        assertThat("v2 getAsInt null", v2.getAs<Int>(), nullValue())
    }

    @Test
    fun testMapError() {
        val success = Result.of("success")
        val failure = Result.error(Exception("failure"))

        val v1 = success.mapError { InstantiationException(it.message) }
        val v2 = failure.mapError { InstantiationException(it.message) }

        assertThat("v1 is success", v1 is Result.Success, isEqualTo(true))
        assertThat("v1 is success", v1.component1(), isEqualTo("success"))
        assertThat("v2 is failure", v2 is Result.Failure, isEqualTo(true))
        assertThat("v2 is failure", v2.component2()!!.message, isEqualTo("failure"))
    }

    @Test
    fun testFlatMapError() {
        val success = Result.of("success")
        val failure = Result.error(Exception("failure"))

        val v1 = success.flatMapError { Result.error(IllegalArgumentException()) }
        val v2 = failure.flatMapError { Result.error(IllegalArgumentException()) }


        assertThat("v1 is success", v1 is Result.Success, isEqualTo(true))
        assertThat("v1 is success", v1.getAs(), isEqualTo("success"))
        assertThat("v2 is failure", v2 is Result.Failure, isEqualTo(true))
        assertThat("v2 is failure", v2.component2() is IllegalArgumentException, isEqualTo(true))
    }

    @Test
    fun testAny() {
        val foo = Result.of { readFromAssetFileName("foo.txt") }
        val fooo = Result.of { readFromAssetFileName("fooo.txt") }

        val v1 = foo.any { "Lorem" in it }
        val v2 = fooo.any { "Lorem" in it }
        val v3 = foo.any { "LOREM" in it }

        assertTrue("v1 is true", v1)
        assertFalse("v2 is false", v2)
        assertFalse("v3 is false", v3)
    }

    @Test
    fun testComposableFunctions1() {
        val foo = { readFromAssetFileName("foo.txt") }
        val bar = { readFromAssetFileName("bar.txt") }

        val notFound = { readFromAssetFileName("fooo.txt") }

        val (value1, error1) = Result.of(foo).map { it.count() }.mapError { IllegalStateException() }
        val (value2, error2) = Result.of(notFound).map { bar }.mapError { IllegalStateException() }

        assertThat("value1 is 574", value1, isEqualTo(574))
        assertThat("error1 is null", error1, nullValue())
        assertThat("value2 is null", value2, nullValue())
        assertThat("error2 is Exception", error2 is IllegalStateException, isEqualTo(true))
    }

    @Test
    fun testComposableFunctions2() {
        val r1 = Result.of(functionThatCanReturnNull(false)).flatMap { resultReadFromAssetFileName("bar.txt") }.mapError { Exception("this should not happen") }
        val r2 = Result.of(functionThatCanReturnNull(true)).map { it.rangeTo(Int.MAX_VALUE) }.mapError { KotlinNullPointerException() }

        assertThat("r1 is Result.Success type", r1 is Result.Success, isEqualTo(true))
        assertThat("r2 is Result.Failure type", r2 is Result.Failure, isEqualTo(true))
    }

    @Test
    fun testNoException() {
        val r = concat("1", "2")
        assertThat("r is Result.Success type", r is Result.Success, isEqualTo(true))
    }

    // helper
    fun readFromAssetFileName(name: String): String {
        val dir = System.getProperty("user.dir")
        val assetsDir = File(dir, "src/test/assets/")
        return File(assetsDir, name).readText()
    }

    fun resultReadFromAssetFileName(name: String): Result<String, Exception> {
        val operation = { readFromAssetFileName(name) }
        return Result.of(operation)
    }

    fun functionThatCanReturnNull(nullEnabled: Boolean): Int? = if (nullEnabled) null else Int.MIN_VALUE

    fun concat(a: String, b: String): Result<String, NoException> = Result.Success(a + b)

}
