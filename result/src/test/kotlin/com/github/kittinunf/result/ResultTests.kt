package com.github.kittinunf.result

import org.junit.Test
import java.io.File
import java.io.FileNotFoundException
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Created by Kittinun Vantasin on 10/27/15.
 */

class ResultTests {

    @Test
    fun testCreateValue() {
        val v = Result.create(1)

        assertNotNull(v, "Result is created successfully")
        assertTrue(v is Result.Success, "v is Result.Success type")
    }

    @Test
    fun testCreateError() {
        val e = Result.create(RuntimeException())

        assertNotNull(e, "Result is created successfully")
        assertTrue(e is Result.Failure, "v is Result.Success type")
    }

    @Test
    fun testCreateOptionalValue() {
        val value1: String? = null
        val value2: String? = "1"

        val result1 = Result.create(value1) { UnsupportedOperationException("value is null") }
        val result2 = Result.create(value2) { IllegalStateException("value is null") }

        assertTrue(result1 is Result.Failure, "result1 is Result.Failure type")
        assertTrue(result2 is Result.Success, "result2 is Result.Success type")
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

        val result1 = Result.create(f1)
        val result2 = Result.create(f2)
        val result3 = Result.create(f3())

        assertTrue(result1 is Result.Success, "result1 is Result.Success type")
        assertTrue(result2 is Result.Failure, "result2 is Result.Failure type")
        assertTrue(result3 is Result.Failure, "result2 is Result.Failure type")
    }

    @Test
    fun testDematerialize() {
        val f1 = { true }
        val f2 = { File("not_found_file").readText() }

        val result1 = Result.create(f1)
        val result2 = Result.create(f2)

        assertTrue(result1.dematerialize(), "result1 is true")
        assertTrue("result2 expecting to throw FileNotFoundException") {
            var result = false
            try {
                result2.dematerialize()
            } catch(e: FileNotFoundException) {
                result = true
            }
            result
        }
    }

    @Test
    fun testGetValue() {
        val result1 = Result.create(22)
        val result2 = Result.create(KotlinNullPointerException())

        val v1: Int = result1.get() ?: 0
        val (v2, err) = result2

        assertTrue { v1 == 22 }
        assertTrue { err is KotlinNullPointerException }
    }

    @Test
    fun testFold() {
        val success = Result.create("success")
        val failure = Result.create(RuntimeException("failure"))

        val v1 = success.fold({ 1 }, { 0 })
        val v2 = failure.fold({ 1 }, { 0 })

        assertTrue { v1 == 1 }
        assertTrue { v2 == 0 }
    }

    //helper
    fun Nothing.count() = 0
    fun Nothing.getMessage() = ""

    @Test
    fun testMap() {
        val success = Result.create("success")
        val failure = Result.create(RuntimeException("failure"))

        val v1 = success.map { it.count() }
        val v2 = failure.map { it.count() }

        assertTrue { v1.get<Int>() == 7 }
        assertTrue { v2.get<Int>() ?: 0 == 0 }
    }

    @Test
    fun testFlatMap() {
        val success = Result.create("success")
        val failure = Result.create(RuntimeException("failure"))

        val v1 = success.flatMap { Result.create(it.last()) }
        val v2 = failure.flatMap { Result.create(it.count()) }

        assertTrue { v1.get<Char>() == 's' }
        assertTrue { v2.get<Char>() ?: "" == "" }
    }

    @Test
    fun testMapError() {
        val success = Result.create("success")
        val failure = Result.create(Exception("failure"))

        val v1 = success.mapError { InstantiationException(it.getMessage()) }
        val v2 = failure.mapError { InstantiationException(it.getMessage()) }

        assertTrue { v1.value == "success" && v1.error == null }
        assertTrue {
            val (value, error) = v2
            error is InstantiationException && error.getMessage() == "failure"
        }
    }

    @Test
    fun testFlatMapError() {
        val success = Result.create("success")
        val failure = Result.create(Exception("failure"))

        val v1 = success.flatMapError { Result.create(IllegalArgumentException()) }
        val v2 = failure.flatMapError { Result.create(IllegalArgumentException()) }

        assertTrue { v1.get<String>() == "success" }
        assertTrue { v2.error is IllegalArgumentException }
    }

    @Test
    fun testComposableFunctions1() {
        val foo = { readFromAssetFileName("foo.txt") }
        val bar = { readFromAssetFileName("bar.txt") }

        val notFound = { readFromAssetFileName("fooo.txt") }

        val (value1, error1) = Result.create(foo).map { it.count() }.mapError { IllegalStateException() }
        val (value2, error2) = Result.create(notFound).map { bar }.mapError { IllegalStateException() }

        assertTrue { value1 == 574 && error1 == null }
        assertTrue { value2 == null && error2 is IllegalStateException }
    }

    @Test
    fun testComposableFunctions2() {
        val r1 = Result.create(functionThatCanReturnNull(false)).flatMap { resultReadFromAssetFileName("bar.txt") }.mapError { Exception("this should not happen") }
        val r2 = Result.create(functionThatCanReturnNull(true)).map { it.rangeTo(Int.MAX_VALUE) }.mapError { KotlinNullPointerException() }

        assertTrue { r1 is Result.Success }
        assertTrue { r2 is Result.Failure }
    }

    @Test
    fun testNoException() {
        val r = concat("1", "2")
        assertTrue { r is Result.Failure }
    }

    // helper
    fun readFromAssetFileName(name: String): String {
        val dir = System.getProperty("user.dir")
        val assetsDir = File(dir, "src/test/assets/")
        return File(assetsDir, name).readText()
    }

    fun resultReadFromAssetFileName(name: String): Result<String, Exception> {
        val operation = { readFromAssetFileName(name) }
        return Result.create(operation)
    }

    fun functionThatCanReturnNull(nullEnabled: Boolean): Int? = if (nullEnabled) null else Int.MIN_VALUE

    fun concat(a: String, b: String): Result<String, NoException> = Result.Success(a + b)

}
