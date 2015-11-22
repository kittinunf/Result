package com.github.kittinunf.result

/**
 * Created by Kittinun Vantasin on 10/26/15.
 */

sealed public class Result<out V : Any, out E : Exception> private constructor(val value: V?, val error: E?) {

    operator public fun component1(): V? = this.value
    operator public fun component2(): E? = this.error

    public class Success<V : Any>(value: V) : Result<V, Nothing>(value, null)

    public class Failure<E : Exception>(error: E) : Result<Nothing, E>(null, error)

    companion object {
        // Factory methods
        public fun <E : Exception> of(error: E) = Failure(error)

        public fun <V : Any> of(value: V?, fail: (() -> Exception)? = null) =
                value?.let { Success(it) } ?: Failure(fail?.invoke() ?: Exception())

        public fun <V: Any> of(f: Function0<V>): Result<V, Exception> {
            return try {
                Result.of(f())
            } catch(ex: Exception) {
                Result.of(ex)
            }
        }

    }

    /**
     * provide a fallback value in case of failure
     *
     * It is the equivalent of the elvis operator ?: for nullable types
     *
     * Example:
     *
     * val vecsize= Result.of(vec.size() or 0)
     *
     */
    public inline infix fun <reified V : Any> or(fallbackValue:V):Result<V,E> {
        return when(this) {
            is Success -> Success(this.value as V)
            is Failure -> Success(fallbackValue)
        }
    }

    public fun <X> fold(success: (V) -> X, failure: (E) -> X): X {
        return when (this) {
            is Success -> success(this.value!!)
            is Failure -> failure(this.error!!)
        }
    }

    public inline fun <reified X> getAs(): X? {
        @Suppress("unchecked_cast")
        return when (this) {
            is Success -> this.value as? X
            is Failure -> this.error as? X
        }
    }

    public fun get(): V {
        when (this) {
            is Success -> return this.value as V
            is Failure -> throw this.error as E
        }
    }

    public fun <U : Any> map(transform: (V) -> U) = fold({ Result.Success(transform(it)) }, { Result.Failure(it) })

    public fun <U : Any, E : Exception> flatMap(transform: (V) -> Result<U, E>) = fold({ transform(it) }, { Result.Failure(it) })

    public fun <E2 : Exception> mapError(transform: (E) -> E2) = fold({ Result.Success(it) }, { Result.Failure(transform(it)) })

    public fun <V : Any, E2 : Exception> flatMapError(transform: (E) -> Result<V, E2>) = fold({ Result.Success(it) }, { transform(it) })

    override fun toString() = fold({ "[Success: $it]" }, { "[Failure: $it]" })

}