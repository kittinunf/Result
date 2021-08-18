package com.github.kittinunf.result

actual inline fun <V> runCatching(block: () -> V?): Result<V?, Throwable> {
    return try {
        Result.success(block())
    } catch (e: Exception) {
        Result.failure(e)
    } catch (e: dynamic) {
        Result.failure(Throwable(e.toString()))
    }
}

actual inline infix fun <T, V> T.runCatching(block: T.() -> V?): Result<V?, Throwable> {
    return try {
        Result.success(block())
    } catch (e: Exception) {
        Result.failure(e)
    } catch (e: dynamic) {
        Result.failure(Throwable(e.toString()))
    }
}

actual inline fun <U, reified E : Throwable> doTry(function: () -> Result<U, E>): Result<U, E> {
    return try {
        function()
    } catch (ex: Throwable) {
        when (ex) {
            is E -> Result.failure(ex)
            else -> throw ex
        }
    } catch (e: dynamic) {
        throw Throwable(e.toString())
    }
}
