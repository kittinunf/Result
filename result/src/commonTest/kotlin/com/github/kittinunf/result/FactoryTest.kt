package com.github.kittinunf.result

import kotlin.js.JsName
import kotlin.test.Test
import kotlin.test.assertIs

class FactoryTest {
    @Test
    @JsName("should_properly_catch_with_runCatching_as_success")
    fun `should properly catch with runCatching as success`() {
        val r1 =
            com.github.kittinunf.result.runCatching {
                42
            }

        val r2 =
            runCatching {
                3 * 100
            }

        val r3 = runCatching<String?> { null }

        assertIs<Result.Success<Int>>(r1)
        assertIs<Result.Success<Int>>(r2)
        assertIs<Result.Success<String?>>(r3)
    }

    @Test
    @JsName("should_properly_catch_with_runCatching_as_failure")
    fun `should properly catch with runCatching as failure`() {
        val r1 =
            com.github.kittinunf.result.runCatching {
                throw IllegalStateException("failure 1")
            }

        val r2 =
            runCatching {
                throw IllegalStateException("failure 2")
            }

        assertIs<Result.Failure<IllegalStateException>>(r1)
        assertIs<Result.Failure<IllegalStateException>>(r2)
    }

    @Test
    @JsName("should_properly_catch_with_runCatching_with_block_parameters")
    fun `should properly catch with runCatching with block parameters`() {
        val sf = SimpleFile()
        val s = runCatching(sf::found)
        val f = runCatching(sf::notFound)

        assertIs<Result.Success<String>>(s)
        assertIs<Result.Failure<Throwable>>(f)
    }

    @Test
    @JsName("should_properly_catch_with_runCatching_with_extension_parameters")
    fun `should properly catch with runCatching with extension parameters`() {
        val sf = SimpleFile()
        val s = sf runCatching { found() }
        val sn = sf runCatching { nullable() }
        val f = sf runCatching { notFound() }

        assertIs<Result.Success<String>>(s)
        assertIs<Result.Success<*>>(sn)
        assertIs<Result.Failure<Throwable>>(f)
    }

    class SimpleFile {
        fun nullable() = null

        fun found() = Resource("lorem_short.txt").read()

        fun notFound() = Resource("not_found.txt").read()
    }
}
