package com.github.kittinunf.result

actual inline fun <V> runCatching(block: () -> V?): Result<V?, Throwable> = try {
    Result.success(block())
} catch (e: Exception) {
    Result.failure(e)
}

actual inline infix fun <T, V> T.runCatching(block: T.() -> V?): Result<V?, Throwable> = try {
    Result.success(block())
} catch (e: Exception) {
    Result.failure(e)
}