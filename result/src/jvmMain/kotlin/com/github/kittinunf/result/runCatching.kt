package com.github.kittinunf.result

actual inline fun <V> runCatching(block: () -> V?): Result<V?, Throwable> {
    return try {
        Result.success(block())
    } catch (e: Exception) {
        Result.failure(e)
    }
}

actual inline fun <T, R> T.doTry(function: () -> R, errFunction: (Throwable) -> R): R {
    return try {
        function()
    } catch (t: Throwable) {
        errFunction(t)
    }
}
