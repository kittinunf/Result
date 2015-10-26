package com.github.kittinunf.result

/**
 * Created by Kittinun Vantasin on 10/26/15.
 */

sealed public class Result<out V : Any, out E : Exception>(val value: V?, val error: E?) {

    operator public fun component1(): V? = this.value
    operator public fun component2(): E? = this.error

    companion object {
        // Constructors
        public fun <V : Any> create(value: V) = Success(value)

        public fun <E : Exception> create(error: E) = Failure(error)

        public fun <V : Any, E : Exception> create(value: V?, fail: () -> E) = value?.let { Success(it) } ?: Failure(fail())

        public fun <V : Any> create(f: () -> V): Result<V, Exception> {
            try {
                return Result.create(f())
            } catch(e: Exception) {
                return Result.create(e)
            }
        }
    }

    public fun <X> get(): X {
        @Suppress("unchecked_cast")
        when (this) {
            is Success -> return this.value as X
            is Failure -> return this.error as X
        }
    }

    public fun dematerialize(): V {
        when (this) {
            is Success -> return this.value as V
            is Failure -> throw this.error as E
        }
    }

    public class Success<V : Any>(value: V) : Result<V, Nothing>(value, null)

    public class Failure<E : Exception>(error: E) : Result<Nothing, E>(null, error)

}