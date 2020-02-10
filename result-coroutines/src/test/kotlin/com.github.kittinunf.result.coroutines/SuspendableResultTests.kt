package com.github.kittinunf.result.coroutines

import com.github.kittinunf.result.NoException
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.getOrElse
import com.github.kittinunf.result.getOrNull
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Test
import java.io.File
import java.io.FileNotFoundException

class SuspendableResultTests {

    @Test
    fun testCreateValue() {
        val v = runBlocking { SuspendableResult.of(1) }

        assertThat("SuspendableResult is created successfully", v, notNullValue())
        assertThat("v is SuspendableResult.Success type", v is SuspendableResult.Success, equalTo(true))
    }

    @Test
    fun testCreateError() {
        val e = runBlocking { SuspendableResult.error(RuntimeException()) }

        assertThat("SuspendableResult is created successfully", e, notNullValue())
        assertThat("e is SuspendableResult.Failure type", e is SuspendableResult.Failure, equalTo(true))
    }

    @Test
    fun testCreateOptionalValue() {
        val value1: String? = null
        val value2: String? = "1"

        val result1 = runBlocking { SuspendableResult.of(value1) { UnsupportedOperationException("value is null") } }
        val result2 = runBlocking { SuspendableResult.of(value2) { IllegalStateException("value is null") } }

        assertThat("result1 is SuspendableResult.Failure type", result1 is SuspendableResult.Failure, equalTo(true))
        assertThat("result2 is SuspendableResult.Success type", result2 is SuspendableResult.Success, equalTo(true))
    }

    @Test
    fun testCreateFromLambda() {
        runBlocking {
            val result1 = SuspendableResult.of<String, Exception> { fooString() }
            val result2 = SuspendableResult.of<Unit, Exception> { invalidArrayAccessor() }
            val result3 = SuspendableResult.of(invalidNullAssignmentToFinalProperty())

            assertThat("result1 is Result.Success type", result1 is SuspendableResult.Success, equalTo(true))
            assertThat("result2 is Result.Failure type", result2 is SuspendableResult.Failure, equalTo(true))
            assertThat("result3 is Result.Failure type", result3 is SuspendableResult.Failure, equalTo(true))
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

        assertThat("one is SuspendableResult.Success type", one is SuspendableResult.Success, equalTo(true))
        assertThat("value one is 1", one.component1()!!, equalTo(1))
    }

    @Test
    fun testOrNull() {
        val one = runBlocking { SuspendableResult.of<Int, Exception> { throw Exception("Some error") } .getOrNull() }
        val two = SuspendableResult.of(1) .getOrNull()

        val result: Int? = null
        Assert.assertThat("one is null", one, equalTo(result))
        Assert.assertThat("two is one", two, equalTo(1))
    }

    @Test
    fun testOrElse() {
        val one = SuspendableResult.of<Int>(null) getOrElse { 1 }
        val two = SuspendableResult.of(2).getOrElse { 1 }
        val three = runBlocking { SuspendableResult.of<String, Exception>{ throw Exception("Message") }.getOrElse { it.message!! } }

        Assert.assertThat("one is 1", one, equalTo(1))
        Assert.assertThat("two is 2", two, equalTo(2))
        Assert.assertThat("three is exception message", three, equalTo("Message"))
    }

    @Test
    fun testSuccess() {
        val result = runBlocking { SuspendableResult.of<Boolean, NoException> { true } }

        var beingCalled = false
        runBlocking {
            result.success {
                beingCalled = true
            }
        }

        var notBeingCalled = true
        runBlocking {
            result.failure {
                notBeingCalled = false
            }
        }

        assertThat(beingCalled, equalTo(true))
        assertThat(notBeingCalled, equalTo(true))
    }

    @Test
    fun testFailure() {
        runBlocking {
            val result = SuspendableResult.of<String, Exception> { File("not_found_file").readText() }

            var beingCalled = false
            var notBeingCalled = true

            result.failure {
                beingCalled = true
            }

            result.success {
                notBeingCalled = false
            }

            assertThat(beingCalled, equalTo(true))
            assertThat(notBeingCalled, equalTo(true))
        }
    }

    @Test
    fun testGet() {
        val result1 = runBlocking { SuspendableResult.of(true) }
        val result2 = runBlocking { SuspendableResult.of<String, Exception> { runBlocking { File("not_found_file").readText() } } }
        val result3 = runBlocking { SuspendableResult.of<String?, Exception> { null } }

        assertThat("result1 is true", result1.get(), equalTo(true))

        var result = false
        try {
            result2.get()
        } catch (e: FileNotFoundException) {
            result = true
        }

        assertThat("result2 expecting to throw FileNotFoundException", result, equalTo(true))

        assertThat("result3 is default", result3.get() ?: "default", equalTo("default"))
    }

    @Suppress("UNUSED_VARIABLE")
    @Test
    fun testGetAsValue() {
        val result1 = runBlocking { SuspendableResult.of(22) }
        val result2 = runBlocking { SuspendableResult.error(KotlinNullPointerException()) }

        val v1: Int = result1.getAs()!!
        val (v2, err) = result2

        assertThat("v1 is equal 22", v1, equalTo(22))
        assertThat("err is KotlinNullPointerException type", err is KotlinNullPointerException, equalTo(true))
    }

    @Test
    fun testFold() {
        runBlocking {
            val success = SuspendableResult.of("success")
            val failure = SuspendableResult.error(RuntimeException("failure"))

            val v1 = success.fold({ 1 }, { 0 })
            val v2 = failure.fold({ 1 }, { 0 })

            assertThat("v1 is equal 1", v1, equalTo(1))
            assertThat("v2 is equal 1", v2, equalTo(0))
        }
    }

    //helper
    private fun Nothing.count() = 0

    fun Nothing.getMessage() = ""

    @Test
    fun testMap() {
        runBlocking {
            val success = SuspendableResult.of("success")
            val failure = SuspendableResult.error(RuntimeException("failure"))

            val v1 = success.map { it.count() }
            val v2 = failure.map { it.count() }

            assertThat("v1 getAsInt equals 7", v1.getAs(), equalTo(7))
            assertThat("v2 getAsInt null", v2.getAs<Int>(), nullValue())
        }
    }

    @Test
    fun testFlatMap() {
        runBlocking {
            val success = SuspendableResult.of("success")
            val failure = SuspendableResult.error(RuntimeException("failure"))

            val v1 = success.flatMap { SuspendableResult.of(it.last()) }
            val v2 = failure.flatMap { SuspendableResult.of(it.count()) }

            assertThat("v1 getAsChar equals s", v1.getAs(), equalTo('s'))
            assertThat("v2 getAsInt null", v2.getAs<Int>(), nullValue())
        }
    }

    @Test
    fun testMapError() {
        runBlocking {
            val success = SuspendableResult.of("success")
            val failure = SuspendableResult.error(Exception("failure"))

            val v1 = success.mapError { InstantiationException(it.message) }
            val v2 = failure.mapError { InstantiationException(it.message) }

            assertThat("v1 is success", v1 is SuspendableResult.Success, equalTo(true))
            assertThat("v1 is success", v1.component1(), equalTo("success"))
            assertThat("v2 is failure", v2 is SuspendableResult.Failure, equalTo(true))
            assertThat("v2 is failure", v2.component2()!!.message, equalTo("failure"))
        }
    }

    @Test
    fun testFlatMapError() {
        runBlocking {
            val success = SuspendableResult.of("success")
            val failure = SuspendableResult.error(Exception("failure"))

            val v1 = success.flatMapError { SuspendableResult.error(IllegalArgumentException()) }
            val v2 = failure.flatMapError { SuspendableResult.error(IllegalArgumentException()) }

            assertThat("v1 is success", v1 is SuspendableResult.Success, equalTo(true))
            assertThat("v1 is success", v1.getAs(), equalTo("success"))
            assertThat("v2 is failure", v2 is SuspendableResult.Failure, equalTo(true))
            assertThat("v2 is failure", v2.component2() is IllegalArgumentException, equalTo(true))
        }
    }

    @Test
    fun testAny() {
        runBlocking {
            val foo = SuspendableResult.of<String, Exception> { readFromAssetFileName("foo.txt") }
            val fooo = SuspendableResult.of<String, Exception> { readFromAssetFileName("fooo.txt") }

            val v1 = foo.any { "Lorem" in it }
            val v2 = fooo.any { "Lorem" in it }
            val v3 = foo.any { "LOREM" in it }

            assertThat(v1, equalTo(true))
            assertThat(v2, equalTo(false))
            assertThat(v3, equalTo(false))
        }
    }

    @Test
    fun testAnyWithThrow() {
        runBlocking {
            val foo = SuspendableResult.of<String, Exception> { readFromAssetFileName("foo.txt") }

            val v1 = foo.any { "Lorem" in it }
            val v2 = foo.any { readFromAssetFileName("fooo.txt"); true }

            assertThat(v1, equalTo(true))
            assertThat(v2, equalTo(false))
        }
    }

    @Test
    fun testComposableFunctions1() {
        runBlocking {
            val (value1, error1) = SuspendableResult.of<String, Exception> { readFromAssetFileName("foo.txt") }.map { it.count() }.mapError { IllegalStateException() }
            val (value2, error2) = SuspendableResult.of<String, Exception> { readFromAssetFileName("fooo.txt") }.map { readFromAssetFileName("bar.txt") }.mapError { IllegalStateException() }

            assertThat("value1 is 574", value1, equalTo(574))
            assertThat("error1 is null", error1, nullValue())
            assertThat("value2 is null", value2, nullValue())
            assertThat("error2 is Exception", error2 is IllegalStateException, equalTo(true))
        }
    }

    @Test
    fun testComposableFunctions2() {
        runBlocking {
            val r1 = SuspendableResult.of(functionThatCanReturnNull(false)).flatMap { resultReadFromAssetFileName("bar.txt") }.mapError { Exception("this should not happen") }
            val r2 = SuspendableResult.of(functionThatCanReturnNull(true)).map { it.rangeTo(Int.MAX_VALUE) }.mapError { KotlinNullPointerException() }

            assertThat("r1 is SuspendableResult.Success type", r1 is SuspendableResult.Success, equalTo(true))
            assertThat("r2 is SuspendableResult.Failure type", r2 is SuspendableResult.Failure, equalTo(true))
        }
    }

    @Test
    fun testNoException() {
        val r = concat("1", "2")
        assertThat("r is SuspendableResult.Success type", r is SuspendableResult.Success, equalTo(true))
    }

    @Test
    fun testFanoutSuccesses() {
        runBlocking {
            val readFooResult = resultReadFromAssetFileName("foo.txt")
            val readBarResult = resultReadFromAssetFileName("bar.txt")

            val finalResult = readFooResult.fanout { readBarResult }
            val (v, e) = finalResult

            assertThat("finalResult is success", finalResult is SuspendableResult.Success, equalTo(true))
            assertThat("finalResult has a pair type when both are successes", v is Pair<String, String>, equalTo(true))
            assertThat("value of finalResult has text from foo as left and text from bar as right",
                    v!!.first.startsWith("Lorem Ipsum is simply dummy text") && v.second.startsWith("Contrary to popular belief"), equalTo(true))
        }
    }

    @Test
    fun liftListToResultOfListSuccess() {
        runBlocking {
            val rs = listOf("bar", "foo").map { "$it.txt" }.map { resultReadFromAssetFileName(it) }.lift()

            assertThat(rs, instanceOf(SuspendableResult::class.java))
            assertThat(rs, instanceOf(SuspendableResult.Success::class.java))
            assertThat(rs.get()[0], equalTo(readFromAssetFileName("bar.txt")))
        }
    }

    @Test
    fun liftListToResultOfListFailure() {
        runBlocking {
            val rs = listOf("bar", "not_found").map { "$it.txt" }.map { resultReadFromAssetFileName(it) }.lift()

            assertThat(rs, instanceOf(SuspendableResult::class.java))
            assertThat(rs, instanceOf(SuspendableResult.Failure::class.java))
            val (_, error) = rs
            assertThat(error, instanceOf(FileNotFoundException::class.java))
        }
    }

    // helper
    private fun readFromAssetFileName(name: String): String {
        val dir = System.getProperty("user.dir")
        val assetsDir = File(dir, "src/test/assets/")
        Thread.sleep(1000)
        return File(assetsDir, name).readText()
    }

    private suspend fun resultReadFromAssetFileName(name: String): SuspendableResult<String, Exception> {
        return SuspendableResult.of { readFromAssetFileName(name) }
    }

    private fun functionThatCanReturnNull(nullEnabled: Boolean): Int? = if (nullEnabled) null else Int.MIN_VALUE

    private fun concat(a: String, b: String): SuspendableResult<String, NoException> = SuspendableResult.Success(a + b)

}
