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

        val result1 = Result.create(f1)
        val result2 = Result.create(f2)

        assertTrue(result1 is Result.Success, "result1 is Result.Success type")
        assertTrue(result2 is Result.Failure, "result2 is Result.Failure type")
    }

    @Test
    fun testDematerialize() {
        val f1 = { true }
        val f2 = { File("not_found_file").readText() }

        val result1 = Result.create(f1)
        val result2 = Result.create(f2)

        assertTrue(result1.dematerialize(), "result1 is true")
        assertTrue("result2 expecting to throw FileNotFoundException") {
            try {
                result2.dematerialize()
                false
            } catch(e: FileNotFoundException) {
                true
            }
        }
    }

    @Test
    fun testGetValue() {
        val result1 = Result.create(22)
        val result2 = Result.create(KotlinNullPointerException())

        val v1: Int = result1.get()
        val v2 = result2.get<Exception>()

        assertTrue { v1 == 22 }
        assertTrue { v2 is KotlinNullPointerException }
    }

}
