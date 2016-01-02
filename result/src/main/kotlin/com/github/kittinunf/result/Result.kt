package com.github.kittinunf.result

/**
 * Created by Kittinun Vantasin on 10/26/15.
 */

public inline fun <reified X> Result<*, *>.getAs() = when (this) {
    is Result.Success -> value as? X
    is Result.Failure -> error as? X
}

public infix fun <V : Any, E : Exception> Result<V, E>.or(fallback: V) = when (this) {
    is Result.Success -> this
    else -> Result.Success<V, E>(fallback)
}

public fun <V : Any, U : Any, E : Exception> Result<V, E>.map(transform: (V) -> U): Result<U, E> = when (this) {
    is Result.Success -> Result.Success<U, E>(transform(value))
    is Result.Failure -> Result.Failure<U, E>(error)
}

public fun <V : Any, U : Any, E : Exception> Result<V, E>.flatMap(transform: (V) -> Result<U, E>): Result<U, E> = when (this) {
    is Result.Success -> transform(value)
    is Result.Failure -> Result.Failure<U, E>(error)
}

public fun <V : Any, E : Exception, E2 : Exception> Result<V, E>.mapError(transform: (E) -> E2) = when (this) {
    is Result.Success -> Result.Success<V, E2>(value)
    is Result.Failure -> Result.Failure<V, E2>(transform(error))
}

public fun <V : Any, E : Exception, E2 : Exception> Result<V, E>.flatMapError(transform: (E) -> Result<V, E2>) = when (this) {
    is Result.Success -> Result.Success<V, E2>(value)
    is Result.Failure -> transform(error)
}


sealed public class Result<out V : Any, out E : Exception> {

    public abstract operator fun component1(): V?
    public abstract operator fun component2(): E?

    public abstract fun <X> fold(success: (V) -> X, failure: (E) -> X): X

    public abstract fun get(): V

    public class Success<out V : Any, out E : Exception>(val value: V) : Result<V, E>() {
        override fun component1(): V? = value
        override fun component2(): E? = null

        override fun <X> fold(success: (V) -> X, failure: (E) -> X): X = success(value)

        override fun get(): V = value

        override fun toString() = "[Success: $value]"
    }


    public class Failure<out V : Any, out E : Exception>(val error: E) : Result<V, E>() {
        override fun component1(): V? = null
        override fun component2(): E? = error

        override fun <X> fold(success: (V) -> X, failure: (E) -> X): X = failure(error)

        override fun get(): V = throw error

        override fun toString() = "[Failure: $error]"
    }

    companion object {
        // Factory methods
        public fun <E : Exception> error(ex: E) = Failure<Nothing, E>(ex)

        public fun <V : Any> of(value: V?, fail: (() -> Exception) = { Exception() }): Result<V, Exception> {
            return value?.let { Success<V, Nothing>(it) } ?: error(fail())
        }

        public fun <V : Any> of(f: () -> V): Result<V, Exception> = try {
            Success(f())
        } catch(ex: Exception) {
            Failure(ex)
        }
    }
}