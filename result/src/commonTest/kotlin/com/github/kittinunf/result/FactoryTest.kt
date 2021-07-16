package com.github.kittinunf.result

import kotlin.test.Test
import kotlin.test.assertIs

class FactoryTest {

    @Test
    fun `should properly catch with runCatching as success`() {
        val r1 = com.github.kittinunf.result.runCatching {
            42
        }

        val r2 = runCatching {
            3 * 100
        }

        val r3 = runCatching<String?> { null }

        assertIs<Result.Success<Int>>(r1)
        assertIs<Result.Success<Int>>(r2)
        assertIs<Result.Success<String?>>(r3)
    }

    @Test
    fun `should properly catch with runCatching as failure`() {
        val r1 = com.github.kittinunf.result.runCatching {
            throw IllegalStateException("failure 1")
        }

        val r2 = runCatching {
            throw IllegalStateException("failure 2")
        }

        assertIs<Result.Failure<IllegalStateException>>(r1)
        assertIs<Result.Failure<IllegalStateException>>(r2)
    }

    @Test
    fun `should properly catch with runCatching with block parameters`() {
        val sf = SimpleFile()
        val s = runCatching(sf::found)
        val f = runCatching(sf::notFound)

        assertIs<Result.Success<String>>(s)
        assertIs<Result.Failure<Throwable>>(f)
    }

    @Test
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
        fun found() = readFile(directory = "src/commonTest/resources/", fileName = "lorem_short.txt")
        fun notFound() = readFile(directory = "src/commonTest/resources/", fileName = "not_found.txt")
    }
}
