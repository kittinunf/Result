package com.github.kittinunf.result

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.io.FileNotFoundException

class ResultTests {

    @Test
    fun createValue() {
        val v = Result.of(1)

        assertThat("Result is created successfully", v, notNullValue())
        assertThat("v is Result.Success type", v, instanceOf(Result.Success::class.java))
    }

    @Test
    fun createError() {
        val e = Result.error(RuntimeException())

        assertThat("Result is created successfully", e, notNullValue())
        assertThat("e is Result.Failure type", e, instanceOf(Result.Failure::class.java))
    }

    @Test
    fun createOptionalValue() {
        val value1: String? = null
        val value2: String? = "1"

        val result1 = Result.of(value1) { UnsupportedOperationException("value is null") }
        val result2 = Result.of(value2) { IllegalStateException("value is null") }

        assertThat("result1 is Result.Failure type", result1, instanceOf(Result.Failure::class.java))
        assertThat("result2 is Result.Success type", result2, instanceOf(Result.Success::class.java))
    }

    @Test
    fun createFromLambda() {
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

        assertThat("result1 is Result.Success type", result1, instanceOf(Result.Success::class.java))
        assertThat("result2 is Result.Failure type", result2, instanceOf(Result.Failure::class.java))
        assertThat("result3 is Result.Failure type", result3, instanceOf(Result.Failure::class.java))
    }

    @Test
    fun or() {
        val one = Result.of<Int>(null) or 1

        assertThat("one is Result.Success type", one, instanceOf(Result.Success::class.java))
        assertThat("value one is 1", one.component1()!!, equalTo(1))
    }

    @Test
    fun orElse() {
        val one = Result.of<Int>(null) getOrElse 1

        assertThat("one is 1", one, equalTo(1))
    }

    @Test
    fun success() {
        val result = Result.of { true }

        var beingCalled = false
        result.success {
            beingCalled = true
        }

        var notBeingCalled = true
        result.failure {
            notBeingCalled = false
        }

        assertThat(beingCalled, equalTo(true))
        assertThat(notBeingCalled, equalTo(true))
    }

    @Test
    fun failure() {
        val result = Result.of { File("not_found_file").readText() }

        var beingCalled = false
        result.failure {
            beingCalled = true
        }

        var notBeingCalled = true
        result.success {
            notBeingCalled = false
        }

        assertThat(beingCalled, equalTo(true))
        assertThat(notBeingCalled, equalTo(true))
    }

    @Test
    fun get() {
        val f1 = { true }
        val f2 = { File("not_found_file").readText() }

        val result1 = Result.of(f1)
        val result2 = Result.of(f2)

        assertThat("result1 is true", result1.get(), equalTo(true))

        var result = false
        try {
            result2.get()
        } catch (e: FileNotFoundException) {
            result = true
        }

        assertThat("result2 expecting to throw FileNotFoundException", result, equalTo(true))
    }

    @Suppress("UNUSED_VARIABLE")
    @Test
    fun getAsValue() {
        val result1 = Result.of(22)
        val result2 = Result.error(KotlinNullPointerException())

        val v1: Int = result1.getAs()!!
        val (v2, err) = result2

        assertThat("v1 is equal 22", v1, equalTo(22))
        assertThat("err is KotlinNullPointerException type", err is KotlinNullPointerException, equalTo(true))
    }

    @Test
    fun fold() {
        val success = Result.of("success")
        val failure = Result.error(RuntimeException("failure"))

        val v1 = success.fold({ 1 }, { 0 })
        val v2 = failure.fold({ 1 }, { 0 })

        assertThat("v1 is equal 1", v1, equalTo(1))
        assertThat("v2 is equal 1", v2, equalTo(0))
    }

    //helper
    private fun Nothing.count() = 0

    @Test
    fun map() {
        val success = Result.of("success")
        val failure = Result.error(RuntimeException("failure"))

        val v1 = success.map { it.count() }
        val v2 = failure.map { it.count() }

        assertThat("v1 getAsInt equals 7", v1.getAs(), equalTo(7))
        assertThat("v2 getAsInt null", v2.getAs<Int>(), nullValue())
    }

    @Test
    fun flatMap() {
        val success = Result.of("success")
        val failure = Result.error(RuntimeException("failure"))

        val v1 = success.flatMap { Result.of(it.last()) }
        val v2 = failure.flatMap { Result.of(it.count()) }

        assertThat("v1 getAsChar equals s", v1.getAs(), equalTo('s'))
        assertThat("v2 getAsInt null", v2.getAs<Int>(), nullValue())
    }

    @Test
    fun mapError() {
        val success = Result.of("success")
        val failure = Result.error(Exception("failure"))

        val v1 = success.mapError { InstantiationException(it.message) }
        val v2 = failure.mapError { InstantiationException(it.message) }

        assertThat("v1 is success", v1, instanceOf(Result.Success::class.java))
        assertThat("v1 is success", v1.component1(), equalTo("success"))
        assertThat("v2 is failure", v2, instanceOf(Result.Failure::class.java))
        assertThat("v2 is failure", v2.component2()!!.message, equalTo("failure"))
    }

    @Test
    fun flatMapError() {
        val success = Result.of("success")
        val failure = Result.error(Exception("failure"))

        val v1 = success.flatMapError { Result.error(IllegalArgumentException()) }
        val v2 = failure.flatMapError { Result.error(IllegalArgumentException()) }


        assertThat("v1 is success", v1, instanceOf(Result.Success::class.java))
        assertThat("v1 is success", v1.getAs(), equalTo("success"))
        assertThat("v2 is failure", v2, instanceOf(Result.Failure::class.java))
        assertThat("v2 is failure", v2.component2() is IllegalArgumentException, equalTo(true))
    }

    @Test
    fun any() {
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
    fun composableFunctions1() {
        val foo = { readFromAssetFileName("foo.txt") }
        val bar = { readFromAssetFileName("bar.txt") }

        val notFound = { readFromAssetFileName("fooo.txt") }

        val (value1, error1) = Result.of(foo).map { it.count() }.mapError { IllegalStateException() }
        val (value2, error2) = Result.of(notFound).map { bar }.mapError { IllegalStateException() }

        assertThat("value1 is 574", value1, equalTo(574))
        assertThat("error1 is null", error1, nullValue())
        assertThat("value2 is null", value2, nullValue())
        assertThat("error2 is Exception", error2 is IllegalStateException, equalTo(true))
    }

    @Test
    fun composableFunctions2() {
        val r1 = Result.of(functionThatCanReturnNull(false)).flatMap { resultReadFromAssetFileName("bar.txt") }.mapError { Exception("this should not happen") }
        val r2 = Result.of(functionThatCanReturnNull(true)).map { it.rangeTo(Int.MAX_VALUE) }.mapError { KotlinNullPointerException() }

        assertThat("r1 is Result.Success type", r1, instanceOf(Result.Success::class.java))
        assertThat("r2 is Result.Failure type", r2, instanceOf(Result.Failure::class.java))
    }

    @Test
    fun noException() {
        val r = concat("1", "2")
        assertThat("r is Result.Success type", r, instanceOf(Result.Success::class.java))
    }

    @Test
    fun fanoutSuccesses() {
        val readFooResult = resultReadFromAssetFileName("foo.txt")
        val readBarResult = resultReadFromAssetFileName("bar.txt")

        val finalResult = readFooResult.fanout { readBarResult }
        val (v, e) = finalResult

        assertThat("finalResult is success", finalResult, instanceOf(Result.Success::class.java))
        assertThat("finalResult has a pair type when both are successes", v is Pair<String, String>, equalTo(true))
        assertThat("value of finalResult has text from foo as left and text from bar as right",
                v!!.first.startsWith("Lorem Ipsum is simply dummy text") && v.second.startsWith("Contrary to popular belief"), equalTo(true))
        assertThat("value of the second component is null", e, nullValue())
    }

    @Test
    fun fanoutFailureOnLeft() {
        val readFoooResult = resultReadFromAssetFileName("fooo.txt")
        val readBarResult = resultReadFromAssetFileName("bar.txt")

        val finalResult = readFoooResult.fanout { readBarResult }
        val (v, e) = finalResult

        assertThat("value of the first component is null", v, nullValue())
        assertThat("finalResult is failure", finalResult, instanceOf(Result.Failure::class.java))
        assertThat("error is a file not found exception", e, instanceOf(FileNotFoundException::class.java))
    }

    @Test
    fun fanoutFailureOnRight() {
        val readFoooResult = resultReadFromAssetFileName("foo.txt")
        val readBarResult = resultReadFromAssetFileName("barr.txt")

        val finalResult = readFoooResult.fanout { readBarResult }
        val (v, e) = finalResult

        assertThat("value of the first component is null", v, nullValue())
        assertThat("finalResult is failure", finalResult, instanceOf(Result.Failure::class.java))
        assertThat("error is a file not found exception", e, instanceOf(FileNotFoundException::class.java))
    }

    @Test
    fun mapThatThrows() {
        val result = Result.of(1)

        var isCalled = false

        val newResult = result.map { throws() }
        newResult.failure { isCalled = true }

        assertThat("newResult is transformed into failure", newResult, instanceOf(Result.Failure::class.java))
        assertThat("isCalled is being set as true", isCalled, equalTo(true))
    }

    @Test
    fun flatMapThatThrows() {
        val result = Result.of("hello")

        var isCalled = false

        val newResult = result.flatMap { throwsForFlatmap() }
        newResult.failure { isCalled = true }

        assertThat("newResult is transformed into failure", newResult, instanceOf(Result.Failure::class.java))
        assertThat("isCalled is being set as true", isCalled, equalTo(true))
    }

    // helper
    private fun readFromAssetFileName(name: String): String {
        val dir = System.getProperty("user.dir")
        val assetsDir = File(dir, "src/test/assets/")
        return File(assetsDir, name).readText()
    }

    private fun resultReadFromAssetFileName(name: String): Result<String, Exception> {
        val operation = { readFromAssetFileName(name) }
        return Result.of(operation)
    }

    private fun functionThatCanReturnNull(nullEnabled: Boolean): Int? = if (nullEnabled) null else Int.MIN_VALUE

    private fun concat(a: String, b: String): Result<String, NoException> = Result.Success(a + b)

    private fun throws() {
        throw Exception()
    }

    private fun throwsForFlatmap(): Result<Int, Exception> {
        throws()
        return Result.of(1)
    }
}
