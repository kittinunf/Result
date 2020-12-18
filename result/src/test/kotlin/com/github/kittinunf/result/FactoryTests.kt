package com.github.kittinunf.result

import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Test
import org.hamcrest.MatcherAssert.assertThat

class FactoryTests {
    @Test
    fun testRunCatching() {
        val r1 = runCatching {
            val v = arrayListOf<Int>()
            v[1]
        }
        val r2 = runCatching { "foo" }
        assertThat("r1 is IndexOutOfBoundsException instance", r1.getExceptionOrNull(), instanceOf(IndexOutOfBoundsException::class.java))
        assertThat("r2 is foo", r2.get(), equalTo("foo"))
    }
}