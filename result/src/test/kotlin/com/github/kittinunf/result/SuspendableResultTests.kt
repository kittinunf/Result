package com.github.kittinunf.result

import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert
import org.junit.Assert.assertThat
import org.junit.Test
import java.io.File
import java.io.FileNotFoundException

/**
 * Created by tudorgk on 08/12/2017.
 * Copyright (C) 2017 Visma e-conomic A/S
 */

class SuspendableResultTests {

    @Test
    fun testCreateValue() {
        val v = runBlocking { SuspendableResult.of(1) }

        Assert.assertThat("SuspendableResult is created successfully", v, CoreMatchers.notNullValue())
        Assert.assertThat("v is SuspendableResult.Success type", v is SuspendableResult.Success, CoreMatchers.`is`(true))
    }

    @Test
    fun testCreateError() {
        val e = runBlocking { SuspendableResult.error(RuntimeException()) }

        Assert.assertThat("SuspendableResult is created successfully", e, CoreMatchers.notNullValue())
        Assert.assertThat("e is SuspendableResult.Failure type", e is SuspendableResult.Failure, CoreMatchers.`is`(true))
    }

    @Test
    fun testCreateOptionalValue() {
        val value1: String? = null
        val value2: String? = "1"

        val result1 = runBlocking { SuspendableResult.of(value1) { UnsupportedOperationException("value is null") } }
        val result2 = runBlocking { SuspendableResult.of(value2) { IllegalStateException("value is null") } }

        Assert.assertThat("result1 is SuspendableResult.Failure type", result1 is SuspendableResult.Failure, CoreMatchers.`is`(true))
        Assert.assertThat("result2 is SuspendableResult.Success type", result2 is SuspendableResult.Success, CoreMatchers.`is`(true))
    }

    @Test
    fun testCreateFromLambda() {
        runBlocking {

            val result1 = SuspendableResult.of { fooString() }
            val result2 = SuspendableResult.of { invalidArrayAccessor() }
            val result3 = SuspendableResult.of (invalidNullAssignmentToFinalProperty())

            assertThat("result1 is Result.Success type", result1 is SuspendableResult.Success, `is`(true))
            assertThat("result2 is Result.Failure type", result2 is SuspendableResult.Failure, `is`(true))
            assertThat("result3 is Result.Failure type", result3 is SuspendableResult.Failure, `is`(true))
        }
    }

    private suspend fun fooString() = "foo"
    private suspend fun invalidArrayAccessor() {
        val v = arrayListOf<Int>()
        v[1]
    }
    private suspend fun invalidNullAssignmentToFinalProperty(): String? {
        val s: String?
        s = null
        return s
    }

    @Test
    fun testOr() {
        val one = runBlocking { SuspendableResult.of<Int>(null) or 1 }

        Assert.assertThat("one is SuspendableResult.Success type", one is SuspendableResult.Success, CoreMatchers.`is`(true))
        Assert.assertThat("value one is 1", one.component1()!!, CoreMatchers.`is`(1))
    }

    @Test
    fun testOrElse() {
        val one = runBlocking { SuspendableResult.of<Int>(null) getOrElse 1 }

        Assert.assertThat("one is 1", one, CoreMatchers.`is`(1))
    }

    @Test
    fun testSuccess() {
        val result = runBlocking { SuspendableResult.of { true } }

        var beingCalled = false
            runBlocking { result.success {
                beingCalled = true
            }
        }

        var notBeingCalled = true
            runBlocking { result.failure {
                notBeingCalled = false
            }
        }

        Assert.assertThat(beingCalled, CoreMatchers.`is`(true))
        Assert.assertThat(notBeingCalled, CoreMatchers.`is`(true))
    }

    @Test
    fun testFailure() {
        runBlocking {
            val result = SuspendableResult.of { File("not_found_file").readText() }

            var beingCalled = false
            var notBeingCalled = true

            result.failure {
                beingCalled = true
            }

            result.success {
                notBeingCalled = false
            }

            Assert.assertThat(beingCalled, CoreMatchers.`is`(true))
            Assert.assertThat(notBeingCalled, CoreMatchers.`is`(true))
        }
    }

    @Test
    fun testGet() {
        val result1 = runBlocking { SuspendableResult.of(true) }
        val result2 = runBlocking { SuspendableResult.of{ runBlocking { File("not_found_file").readText()} } }

        Assert.assertThat("result1 is true", result1.get(), CoreMatchers.`is`(true))

        var result = false
        try {
            result2.get()
        } catch(e: FileNotFoundException) {
            result = true
        }

        Assert.assertThat("result2 expecting to throw FileNotFoundException", result, CoreMatchers.`is`(true))
    }

    @Suppress("UNUSED_VARIABLE")
    @Test
    fun testGetAsValue() {
        val result1 = runBlocking { SuspendableResult.of(22) }
        val result2 = runBlocking { SuspendableResult.error(KotlinNullPointerException()) }

        val v1: Int = result1.getAs()!!
        val (v2, err) = result2

        Assert.assertThat("v1 is equal 22", v1, CoreMatchers.`is`(22))
        Assert.assertThat("err is KotlinNullPointerException type", err is KotlinNullPointerException, CoreMatchers.`is`(true))
    }

    @Test
    fun testFold() {
        runBlocking {
            val success = SuspendableResult.of("success")
            val failure = SuspendableResult.error(RuntimeException("failure"))

            val v1 = success.fold({ 1 }, { 0 })
            val v2 = failure.fold({ 1 }, { 0 })

            Assert.assertThat("v1 is equal 1", v1, CoreMatchers.`is`(1))
            Assert.assertThat("v2 is equal 1", v2, CoreMatchers.`is`(0))
        }
    }

    //helper
    fun Nothing.count() = 0

    fun Nothing.getMessage() = ""

    @Test
    fun testMap() {
        runBlocking {
            val success = SuspendableResult.of("success")
            val failure = SuspendableResult.error(RuntimeException("failure"))

            val v1 = success.map { it.count() }
            val v2 = failure.map { it.count() }

            Assert.assertThat("v1 getAsInt equals 7", v1.getAs(), CoreMatchers.`is`(7))
            Assert.assertThat("v2 getAsInt null", v2.getAs<Int>(), CoreMatchers.nullValue())
        }
    }

    @Test
    fun testFlatMap() {
        runBlocking {
            val success = SuspendableResult.of("success")
            val failure = SuspendableResult.error(RuntimeException("failure"))

            val v1 = success.flatMap { SuspendableResult.of(it.last()) }
            val v2 = failure.flatMap { SuspendableResult.of(it.count()) }

            Assert.assertThat("v1 getAsChar equals s", v1.getAs(), CoreMatchers.`is`('s'))
            Assert.assertThat("v2 getAsInt null", v2.getAs<Int>(), CoreMatchers.nullValue())
        }
    }

    @Test
    fun testMapError() {
        runBlocking {
            val success = SuspendableResult.of("success")
            val failure = SuspendableResult.error(Exception("failure"))

            val v1 = success.mapError { InstantiationException(it.message) }
            val v2 = failure.mapError { InstantiationException(it.message) }

            Assert.assertThat("v1 is success", v1 is SuspendableResult.Success, CoreMatchers.`is`(true))
            Assert.assertThat("v1 is success", v1.component1(), CoreMatchers.`is`("success"))
            Assert.assertThat("v2 is failure", v2 is SuspendableResult.Failure, CoreMatchers.`is`(true))
            Assert.assertThat("v2 is failure", v2.component2()!!.message, CoreMatchers.`is`("failure"))
        }
    }

    @Test
    fun testFlatMapError() {
        runBlocking {
            val success = SuspendableResult.of("success")
            val failure = SuspendableResult.error(Exception("failure"))

            val v1 = success.flatMapError { SuspendableResult.error(IllegalArgumentException()) }
            val v2 = failure.flatMapError { SuspendableResult.error(IllegalArgumentException()) }


            Assert.assertThat("v1 is success", v1 is SuspendableResult.Success, CoreMatchers.`is`(true))
            Assert.assertThat("v1 is success", v1.getAs(), CoreMatchers.`is`("success"))
            Assert.assertThat("v2 is failure", v2 is SuspendableResult.Failure, CoreMatchers.`is`(true))
            Assert.assertThat("v2 is failure", v2.component2() is IllegalArgumentException, CoreMatchers.`is`(true))
        }
    }

    @Test
    fun testAny() {
        runBlocking {
            val foo = SuspendableResult.of { readFromAssetFileName("foo.txt") }
            val fooo = SuspendableResult.of { readFromAssetFileName("fooo.txt") }

            val v1 = foo.any { "Lorem" in it }
            val v2 = fooo.any { "Lorem" in it }
            val v3 = foo.any { "LOREM" in it }

            Assert.assertTrue("v1 is true", v1)
            Assert.assertFalse("v2 is false", v2)
            Assert.assertFalse("v3 is false", v3)
        }
    }

    @Test
    fun testComposableFunctions1() {
        runBlocking {
            val foo = { runBlocking { readFromAssetFileName("foo.txt") }}
            val bar = { runBlocking { readFromAssetFileName("bar.txt") }}

            val notFound = { runBlocking { readFromAssetFileName("fooo.txt") }}

            val (value1, error1) = Result.of(foo).map { it.count() }.mapError { IllegalStateException() }
            val (value2, error2) = Result.of(notFound).map { bar }.mapError { IllegalStateException() }

            Assert.assertThat("value1 is 574", value1, CoreMatchers.`is`(574))
            Assert.assertThat("error1 is null", error1, CoreMatchers.nullValue())
            Assert.assertThat("value2 is null", value2, CoreMatchers.nullValue())
            Assert.assertThat("error2 is Exception", error2 is IllegalStateException, CoreMatchers.`is`(true))
        }
    }

    @Test
    fun testComposableFunctions2() {
        runBlocking {
            val r1 = SuspendableResult.of(functionThatCanReturnNull(false)).flatMap { resultReadFromAssetFileName("bar.txt") }.mapError { Exception("this should not happen") }
            val r2 = SuspendableResult.of(functionThatCanReturnNull(true)).map { it.rangeTo(Int.MAX_VALUE) }.mapError { KotlinNullPointerException() }

            Assert.assertThat("r1 is SuspendableResult.Success type", r1 is SuspendableResult.Success, CoreMatchers.`is`(true))
            Assert.assertThat("r2 is SuspendableResult.Failure type", r2 is SuspendableResult.Failure, CoreMatchers.`is`(true))
        }
    }

    @Test
    fun testNoException() {
        val r = concat("1", "2")
        Assert.assertThat("r is SuspendableResult.Success type", r is SuspendableResult.Success, CoreMatchers.`is`(true))
    }

    @Test
    fun testFanoutSuccesses() {
        runBlocking {
            val readFooResult = resultReadFromAssetFileName("foo.txt")
            val readBarResult = resultReadFromAssetFileName("bar.txt")

            val finalResult = readFooResult.fanout { readBarResult }
            val (v, e) = finalResult

            Assert.assertThat("finalResult is success", finalResult is SuspendableResult.Success, CoreMatchers.`is`(true))
            Assert.assertThat("finalResult has a pair type when both are successes", v is Pair<String, String>, CoreMatchers.`is`(true))
            Assert.assertThat("value of finalResult has text from foo as left and text from bar as right",
                    v!!.first.startsWith("Lorem Ipsum is simply dummy text") && v!!.second.startsWith("Contrary to popular belief"), CoreMatchers.`is`(true))
        }
    }

    // helper
    suspend fun readFromAssetFileName(name: String): String {
        val dir = System.getProperty("user.dir")
        val assetsDir = File(dir, "src/test/assets/")
        Thread.sleep(1000)
        return File(assetsDir, name).readText()
    }

    suspend fun resultReadFromAssetFileName(name: String): SuspendableResult<String, Exception> {
        val operation = readFromAssetFileName(name)
        return SuspendableResult.of(operation)
    }

    fun functionThatCanReturnNull(nullEnabled: Boolean): Int? = if (nullEnabled) null else Int.MIN_VALUE

    fun concat(a: String, b: String): SuspendableResult<String, NoException> = SuspendableResult.Success(a + b)

}
