package com.github.kittinunf.result

/**
 * Created by Kittinun Vantasin on 10/26/15.
 */

public interface ResultType<out V : Any, out E : Exception> {

    val value: V?
    val error: E?

    public operator fun component1(): V? = this.value
    public operator fun component2(): E? = this.error

    public fun <X> fold(success: (V) -> X, failure: (E) -> X): X

}

public inline fun <reified X> Result<*, *>.getAs() = fold({ this.value as? X }, { this.error as? X })
public inline fun <reified V : Any, reified E : Exception> Result<V, E>.get() = fold({ this.value as V }, { throw this.error as E })

public infix fun <V : Any, E : Exception> ResultType<V, E>.or(fallback: V) = fold({ Result.Success(it) }, { Result.Success(fallback) })

public fun <V : Any, U : Any, E : Exception> ResultType<V, E>.map(transform: (V) -> U) = fold({ Result.Success(transform(it)) }, { Result.Failure(it) })
public fun <V : Any, U : Any, E : Exception> ResultType<V, E>.flatMap(transform: (V) -> Result<U, E>) = fold({ transform(it) }, { Result.Failure(it) })

public fun <V : Any, E : Exception, E2 : Exception> ResultType<V, E>.mapError(transform: (E) -> E2) = fold({ Result.Success(it) }, { Result.Failure(transform(it)) })
public fun <V : Any, E : Exception, E2 : Exception> ResultType<V, E>.flatMapError(transform: (E) -> Result<V, E2>) = fold({ Result.Success(it) }, { transform(it) })


sealed public class Result<out V : Any, out E : Exception> private constructor(override val value: V?, override val error: E?) : ResultType<V, E> {

    public class Success<V : Any>(value: V) : Result<V, Nothing>(value, null)
    public class Failure<E : Exception>(error: E) : Result<Nothing, E>(null, error)

    companion object {
        // Factory methods
        public fun <E : Exception> of(error: E) = Failure(error)

        public fun <V : Any> of(value: V?, fail: (() -> Exception)? = null) =
                value?.let { Success(it) } ?: Failure(fail?.invoke() ?: Exception())

        public fun <V : Any> of(f: Function0<V>): Result<V, Exception> {
            return try {
                Result.of(f())
            } catch(ex: Exception) {
                Result.of(ex)
            }
        }

    }

    override fun <X> fold(success: (V) -> X, failure: (E) -> X): X {
        return when (this) {
            is Success -> success(this.value!!)
            is Failure -> failure(this.error!!)
        }
    }

    override fun toString() = fold({ "[Success: $it]" }, { "[Failure: $it]" })

}

