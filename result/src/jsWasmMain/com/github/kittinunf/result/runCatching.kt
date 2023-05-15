package com.github.kittinunf.result

actual inline fun <R> doTry(work: () -> R, errorHandler: (Throwable) -> R): R {
    return try {
        work()
    } catch (t: Throwable) {
        errorHandler(t)
    } catch (d: dynamic) {
        errorHandler(Throwable(d.toString()))
    }
}
