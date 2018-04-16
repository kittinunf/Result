package com.github.kittinunf.result.coroutines

class SuspendedValidation<out E : Exception>(vararg resultSequence: SuspendableResult<*, E>) {

    val failures: List<E> = resultSequence.filterIsInstance<SuspendableResult.Failure<*, E>>().map { it.getException() }

    val hasFailure = failures.isNotEmpty()

}
