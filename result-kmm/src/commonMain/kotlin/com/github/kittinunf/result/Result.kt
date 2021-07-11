package com.github.kittinunf.result

inline fun <V> Result<V, *>.success(f: (V) -> Unit) = fold(f, {})

inline fun <E : Throwable> Result<*, E>.failure(f: (E) -> Unit) = fold({}, f)

fun Result<*, *>.isSuccess() = this is Result.Success

fun Result<*, *>.isFailure() = this is Result.Failure

fun <V, E : Throwable> Result<V, E>.getOrNull(): V? = when (this) {
    is Result.Success -> value
    is Result.Failure -> null
}

inline fun <T, U, reified E : Throwable> Result<T, E>.map(transform: (T) -> U): Result<U, E> = try {
    when (this) {
        is Result.Success -> Result.success(transform(value))
        is Result.Failure -> Result.failure(error)
    }
} catch (ex: Exception) {
    when (ex) {
        is E -> Result.failure(ex)
        else -> throw ex
    }
}

inline fun <reified E : Throwable, reified EE : Throwable> Result<*, E>.mapError(transform: (E) -> EE): Result<*, EE> = try {
    when (this) {
        is Result.Success -> Result.success(value)
        is Result.Failure -> Result.failure(transform(error))
    }
} catch (ex: Exception) {
    when (ex) {
        is EE -> Result.failure(ex)
        else -> throw ex
    }
}

inline fun <V, U, reified E : Throwable> Result<V, E>.flatMap(transform: (V) -> Result<U, E>): Result<U, E> = try {
    when (this) {
        is Result.Success -> transform(value)
        is Result.Failure -> Result.failure(error)
    }
} catch (ex: Exception) {
    when (ex) {
        is E -> Result.failure(ex)
        else -> throw ex
    }
}

inline fun <reified E : Throwable, reified EE : Throwable> Result<*, E>.flatMapError(transform: (E) -> Result<*, EE>): Result<*, EE> = try {
    when (this) {
        is Result.Success -> Result.success(value)
        is Result.Failure -> transform(error)
    }
} catch (ex: Exception) {
    when (ex) {
        is EE -> Result.failure(ex)
        else -> throw ex
    }
}

inline fun <V, E : Throwable> Result<V, E>.onFailure(f: (E) -> Unit): Result<V, E> {
    when (this) {
        is Result.Success -> {
        }
        is Result.Failure -> {
            f(error)
        }
    }
    return this
}

inline fun <V, E : Throwable> Result<V, E>.onSuccess(f: (V) -> Unit): Result<V, E> {
    when (this) {
        is Result.Success -> {
            f(value)
        }
        is Result.Failure -> {
        }
    }
    return this
}

inline fun <V, reified E : Throwable> List<Result<V, E>>.lift(): Result<List<V>, E> {
    return fold(Result.success(mutableListOf<V>()) as Result<MutableList<V>, E>) { acc, result ->
        acc.flatMap { combine ->
            result.map {
                combine.apply { add(it) }
            }
        }
    }
}

sealed class Result<out V, out E : Throwable> {

    open operator fun component1(): V? = null
    open operator fun component2(): E? = null

    inline fun <X> fold(success: (V) -> X, failure: (E) -> X): X = when (this) {
        is Success -> success(value)
        is Failure -> failure(error)
    }

    abstract fun get(): V?

    class Success<out V> internal constructor(val value: V) : Result<V, Nothing>() {

        override fun component1(): V = value

        override fun get(): V = value

        override fun toString() = "[Success: $value]"

        override fun hashCode(): Int = value.hashCode()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            return other is Success<*> && value == other.value
        }
    }

    class Failure<out E : Throwable> internal constructor(val error: E) : Result<Nothing, E>() {

        override fun component2(): E = error

        override fun get() = throw error

        override fun toString() = "[Failure: $error]"

        override fun hashCode(): Int = error.hashCode()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            return other is Failure<*> && error == other.error
        }
    }

    companion object {
        // Factory methods
        fun <E : Throwable> failure(ex: E) = Failure(ex)
        fun <V> success(v: V) = Success(v)

        @Suppress("UNCHECKED_CAST")
        inline fun <V, reified E : Throwable> of(noinline f: () -> V?): Result<V, E> = try {
            success(f()) as Result<V, E>
        } catch (ex: Exception) {
            when (ex) {
                is E -> failure(ex)
                else -> throw ex
            }
        }
    }
}

