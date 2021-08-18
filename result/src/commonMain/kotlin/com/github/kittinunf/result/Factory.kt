package com.github.kittinunf.result

expect inline fun <V> runCatching(block: () -> V?): Result<V?, Throwable>

expect inline infix fun <T, V> T.runCatching(block: T.() -> V?): Result<V?, Throwable>
