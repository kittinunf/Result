package com.github.kittinunf.result

expect inline fun <V> runCatching(block: () -> V?): Result<V?, Throwable>

inline infix fun <T, V> T.runCatching(block: T.() -> V?): Result<V?, Throwable> = doTry(
    function = {
        Result.success(block())
    }
) {
    Result.failure(it)
}

expect inline fun <T, R> T.doTry(function: () -> R, errFunction: (Throwable) -> R): R