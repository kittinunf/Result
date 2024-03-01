package com.github.kittinunf.result

inline infix fun <T, V> T.runCatching(block: T.() -> V?): Result<V?, Throwable> {
    return doTry(work = {
        Result.success(block())
    }, errorHandler = {
        Result.failure(it)
    })
}

inline fun <V> runCatching(block: () -> V?): Result<V?, Throwable> {
    return doTry(work = {
        Result.success(block())
    }, errorHandler = {
        Result.failure(it)
    })
}

inline fun <R> doTry(
    work: () -> R,
    errorHandler: (Throwable) -> R,
): R {
    return try {
        work()
    } catch (t: Throwable) {
        errorHandler(t)
    }
}
