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
        public fun <E : Exception> create(error: E) = Failure(error)

        public fun <V : Any> create(value: V?, fail: (() -> Exception)? = null) =
                value?.let { Success(it) } ?: Failure(fail?.invoke() ?: Exception())

        public fun <V: Any> create(f: Function0<V>): Result<V, Exception> {
            return try {
                Result.create(f())
            } catch(ex: Exception) {
                Result.create(ex)
            }
        }

    }

    public fun <X> fold(success: (V) -> X, failure: (E) -> X): X {
        return when (this) {
            is Success -> success(this.value!!)
            is Failure -> failure(this.error!!)
        }
    }

    public fun <X : Any> get(): X? {
        @Suppress("unchecked_cast")
        return when (this) {
            is Success -> this.value as X
            is Failure -> null
        }
    }

    public fun dematerialize(): V {
        when (this) {
            is Success -> return this.value as V
            is Failure -> throw this.error as E
        }
    }

    public fun <U : Any> map(transform: (V) -> U) = fold({ Result.Success(transform(it)) }, { Result.Failure(it) })

    public fun <U : Any, E : Exception> flatMap(transform: (V) -> Result<U, E>) = fold({ transform(it) }, { Result.Failure(it) })

    public fun <E2 : Exception> mapError(transform: (E) -> E2) = fold({ Result.Success(it) }, { Result.Failure(transform(it)) })

    public fun <V : Any, E2 : Exception> flatMapError(transform: (E) -> Result<V, E2>) = fold({ Result.Success(it) }, { transform(it) })

}