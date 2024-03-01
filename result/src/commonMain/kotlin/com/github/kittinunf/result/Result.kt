package com.github.kittinunf.result

import com.github.kittinunf.result.Kind.Failure
import com.github.kittinunf.result.Kind.Success
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

fun <V : Any?> V.success(): Result.Success<V> = Result.success(this)

fun <E : Throwable> E.failure(): Result.Failure<E> = Result.failure(this)

inline fun <V> Result<V, *>.success(f: (V) -> Unit) = fold(f, {})

inline fun <E : Throwable> Result<*, E>.failure(f: (E) -> Unit) = fold({}, f)

@OptIn(ExperimentalContracts::class)
fun <V, E : Throwable> Result<V, E>.isSuccess(): Boolean {
    contract {
        returns(true) implies (this@isSuccess is Result.Success)
    }
    return this is Result.Success
}

@OptIn(ExperimentalContracts::class)
fun <V, E : Throwable> Result<V, E>.isFailure(): Boolean {
    contract {
        returns(true) implies (this@isFailure is Result.Failure)
    }
    return this is Result.Failure
}

fun <V, E : Throwable> Result<V, E>.getOrNull(): V? =
    when (this) {
        is Result.Success -> value
        is Result.Failure -> null
    }

fun <V, E : Throwable> Result<V, E>.getFailureOrNull(): E? =
    when (this) {
        is Result.Success -> null
        is Result.Failure -> error
    }

inline infix fun <V, E : Throwable> Result<V, E>.getOrElse(fallback: (E) -> V): V =
    when (this) {
        is Result.Success -> value
        is Result.Failure -> fallback(error)
    }

@OptIn(ExperimentalContracts::class)
inline fun <V, U, reified E : Throwable> Result<V, E>.map(transform: (V) -> U): Result<U, E> {
    contract {
        callsInPlace(transform, InvocationKind.EXACTLY_ONCE)
    }
    return doTry(work = {
        when (this) {
            is Result.Success -> Result.success(transform(value))
            is Result.Failure -> Result.failure(error)
        }
    }, errorHandler = {
        when (it) {
            is E -> Result.failure(it)
            else -> throw it
        }
    })
}

@OptIn(ExperimentalContracts::class)
inline fun <V, reified E : Throwable, reified EE : Throwable> Result<V, E>.mapError(transform: (E) -> EE): Result<V, EE> {
    contract {
        callsInPlace(transform, InvocationKind.EXACTLY_ONCE)
    }
    return doTry(work = {
        when (this) {
            is Result.Success -> Result.success(value)
            is Result.Failure -> Result.failure(transform(error))
        }
    }, errorHandler = {
        when (it) {
            is EE -> Result.failure(it)
            else -> throw it
        }
    })
}

inline fun <V, U, reified E : Throwable, reified EE : Throwable> Result<V, E>.mapBoth(
    transformSuccess: (V) -> U,
    transformFailure: (E) -> EE,
): Result<U, EE> =
    doTry(work = {
        when (this) {
            is Result.Success -> Result.success(transformSuccess(value))
            is Result.Failure -> Result.failure(transformFailure(error))
        }
    }, errorHandler = {
        when (it) {
            is EE -> Result.failure(it)
            else -> throw it
        }
    })

@OptIn(ExperimentalContracts::class)
inline fun <V, U, reified E : Throwable> Result<V, E>.flatMap(transform: (V) -> Result<U, E>): Result<U, E> {
    contract {
        callsInPlace(transform, InvocationKind.EXACTLY_ONCE)
    }
    return doTry(work = {
        when (this) {
            is Result.Success -> transform(value)
            is Result.Failure -> Result.failure(error)
        }
    }, errorHandler = {
        when (it) {
            is E -> Result.failure(it)
            else -> throw it
        }
    })
}

@OptIn(ExperimentalContracts::class)
inline fun <V, reified E : Throwable, reified EE : Throwable> Result<V, E>.flatMapError(transform: (E) -> Result<V, EE>): Result<V, EE> {
    contract {
        callsInPlace(transform, InvocationKind.EXACTLY_ONCE)
    }
    return doTry(work = {
        when (this) {
            is Result.Success -> Result.success(value)
            is Result.Failure -> transform(error)
        }
    }, errorHandler = {
        when (it) {
            is EE -> Result.failure(it)
            else -> throw it
        }
    })
}

@OptIn(ExperimentalContracts::class)
inline fun <V, E : Throwable> Result<V, E>.onSuccess(f: (V) -> Unit): Result<V, E> {
    contract {
        callsInPlace(f, InvocationKind.EXACTLY_ONCE)
    }
    return fold({
        f(it)
        this
    }, { this })
}

@OptIn(ExperimentalContracts::class)
inline fun <V, E : Throwable> Result<V, E>.onFailure(f: (E) -> Unit): Result<V, E> {
    contract {
        callsInPlace(f, InvocationKind.EXACTLY_ONCE)
    }
    return fold({ this }, {
        f(it)
        this
    })
}

inline fun <V, U, reified E : Throwable> Result<V, E>.fanout(other: () -> Result<U, E>): Result<Pair<V, U>, E> =
    flatMap { outer ->
        other().map { outer to it }
    }

inline operator fun <V, reified E : Throwable> Result<List<V>, E>.plus(result: Result<List<V>, E>) =
    when (this) {
        is Result.Success ->
            when (result) {
                is Result.Success -> Result.success(this.value + result.value)
                is Result.Failure -> Result.failure(result.error)
            }

        is Result.Failure -> Result.failure(this.error)
    }

inline fun <V, reified E : Throwable> List<Result<V, E>>.lift(): Result<List<V>, E> =
    lift { successes, errors ->
        when (errors.isEmpty()) {
            true -> Result.success(successes)
            else -> Result.failure(errors.first())
        }
    }

inline fun <V, reified E : Throwable> List<Result<V, E>>.lift(fn: (v: List<V>, e: List<E>) -> Result<List<V>, E>): Result<List<V>, E> =
    fold(Pair<MutableList<V>, MutableList<E>>(mutableListOf(), mutableListOf())) { acc, result ->
        result.success { acc.first.add(it) }
        result.failure { acc.second.add(it) }
        acc
    }.let { fn(it.first, it.second) }

inline fun <V, E : Throwable> Result<V, E>.any(predicate: (V) -> Boolean): Boolean =
    doTry(work = {
        when (this) {
            is Result.Success -> predicate(value)
            is Result.Failure -> false
        }
    }, errorHandler = {
        false
    })

enum class Kind {
    Success,
    Failure,
}

sealed class Result<out V, out E : Throwable> {
    open operator fun component1(): V? = null

    open operator fun component2(): E? = null

    inline fun <X> fold(
        success: (V) -> X,
        failure: (E) -> X,
    ): X =
        when (this) {
            is Success -> success(value)
            is Failure -> failure(error)
        }

    abstract fun failure(): E

    abstract fun get(): V

    abstract val kind: Kind

    class Success<out V : Any?> internal constructor(val value: V) : Result<V, Nothing>() {
        override val kind: Kind = Success

        override fun component1(): V = value

        override fun get(): V = value

        override fun failure() = throw IllegalStateException("Result is in Success state: ${toString()}")

        override fun toString() = "[Success: $value]"

        override fun hashCode(): Int = value.hashCode()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            return other is Success<*> && value == other.value
        }
    }

    class Failure<out E : Throwable> internal constructor(val error: E) : Result<Nothing, E>() {
        override val kind: Kind = Failure

        override fun component2(): E = error

        override fun get() = throw error

        override fun failure(): E = error

        override fun toString() = "[Failure: $error]"

        override fun hashCode(): Int = error.hashCode()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            return other is Failure<*> && error == other.error
        }
    }

    companion object {
        // Factory methods
        fun <E : Throwable> failure(throwable: E) = Failure(throwable)

        fun <V> success(value: V) = Success(value)

        @Suppress("UNCHECKED_CAST")
        inline fun <V, reified E : Throwable> of(f: () -> V?): Result<V, E> =
            doTry(work = {
                success(f()) as Result<V, E>
            }, errorHandler = {
                when (it) {
                    is E -> failure(it)
                    else -> throw it
                }
            })
    }
}
