package com.github.kittinunf.result

class Validation<out E : Exception>(vararg resultSequence: Result<*, E>) {

    val failures: List<E> = resultSequence.filterIsInstance<Result.Failure<*, E>>().map { it.getException() }

    val hasFailure = failures.isNotEmpty()

}

class SuspendedValidation<out E : Exception>(vararg resultSequence: SuspendableResult<*, E>) {

    val failures: List<E> = resultSequence.filterIsInstance<SuspendableResult.Failure<*, E>>().map { it.getException() }

    val hasFailure = failures.isNotEmpty()

}
