package com.github.kittinunf.result

inline fun <V> runCatching(block: () -> V?): Result<V?, Throwable> {
    return try {
        Result.success(block())
    } catch (e: Exception) {
        Result.failure(e)
    }
}

inline infix fun <T, V> T.runCatching(block: T.() -> V?): Result<V?, Throwable> {
    return try {
        Result.success(block())
    } catch (e: Exception) {
        Result.failure(e)
    }
}
