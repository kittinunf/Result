package com.github.kittinunf.result

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

inline fun <reified X> Result<*, *>.getAs() = when (this) {
    is Result.Success -> value as? X
    is Result.Failure -> error as? X
}

inline fun <V : Any?> Result<V, *>.success(f: (V) -> Unit) = fold(f, {})

inline fun <E : Exception> Result<*, E>.failure(f: (E) -> Unit) = fold({}, f)

infix fun <V : Any?, E : Exception> Result<V, E>.or(fallback: V) = when (this) {
    is Result.Success -> this
    else -> Result.Success(fallback)
}

inline infix fun <V: Any?, E: Exception> Result<V, E>.getOrElse(fallback: (E) -> V): V {
    return when (this) {
        is Result.Success -> value
        is Result.Failure -> fallback(error)
    }
}

fun <V: Any?, E: Exception> Result<V, E>.getOrNull(): V? {
    return when (this) {
        is Result.Success -> value
        is Result.Failure -> null
    }
}

fun <V : Any?, E : Exception> Result<V, E>.getExceptionOrNull(): E? {
    return when (this) {
        is Result.Success -> null
        is Result.Failure -> error
    }
}

inline fun <V : Any?, E : Exception, U : Any?, F : Exception> Result<V, E>.mapEither(success: (V) -> U, failure: (E) -> F): Result<U, F> {
    return when (this) {
        is Result.Success -> Result.success(success(value))
        is Result.Failure -> Result.error(failure(error))
    }
}

inline fun <V : Any?, U : Any?, reified E : Exception> Result<V, E>.map(transform: (V) -> U): Result<U, E> = try {
    when (this) {
        is Result.Success -> Result.Success(transform(value))
        is Result.Failure -> Result.Failure(error)
    }
} catch (ex: Exception) {
    when (ex) {
        is E -> Result.error(ex)
        else -> throw ex
    }
}

inline fun <V : Any?, U : Any?, reified E : Exception> Result<V, E>.flatMap(transform: (V) -> Result<U, E>): Result<U, E> = try {
    when (this) {
        is Result.Success -> transform(value)
        is Result.Failure -> Result.Failure(error)
    }
} catch (ex: Exception) {
    when (ex) {
        is E -> Result.error(ex)
        else -> throw ex
    }
}

inline fun <V : Any?, E : Exception, E2 : Exception> Result<V, E>.mapError(transform: (E) -> E2) = when (this) {
    is Result.Success -> Result.Success(value)
    is Result.Failure -> Result.Failure(transform(error))
}

inline fun <V : Any?, E : Exception, E2 : Exception> Result<V, E>.flatMapError(transform: (E) -> Result<V, E2>) = when (this) {
    is Result.Success -> Result.Success(value)
    is Result.Failure -> transform(error)
}

inline fun <V : Any?, E : Exception> Result<V, E>.onError(f: (E) -> Unit) = when(this) {
    is Result.Success -> Result.Success(value)
    is Result.Failure -> {
        f(error)
        this
    }
}

inline fun <V : Any?, E : Exception> Result<V, E>.onSuccess(f: (V) -> Unit): Result<V, E> {
    return when (this) {
        is Result.Success -> {
            f(value)
            this
        }
        is Result.Failure -> this
    }
}

inline fun <V : Any?, E : Exception> Result<V, E>.any(predicate: (V) -> Boolean): Boolean = try {
    when (this) {
        is Result.Success -> predicate(value)
        is Result.Failure -> false
    }
} catch (ex: Exception) {
    false
}

inline fun <V : Any?, U : Any?> Result<V, *>.fanout(other: () -> Result<U, *>): Result<Pair<V, U>, *> =
        flatMap { outer -> other().map { outer to it } }

inline fun <V : Any?, reified E : Exception> List<Result<V, E>>.lift(): Result<List<V>, E> = fold(Result.success(mutableListOf<V>()) as Result<MutableList<V>, E>) { acc, result ->
    acc.flatMap { combine ->
        result.map { combine.apply { add(it) } }
    }
}

inline fun <V, E : Exception> Result<V, E>.unwrap(failure: (E) -> Nothing): V =
    apply { component2()?.let(failure) }.component1()!!

inline fun <V, E : Exception> Result<V, E>.unwrapError(success: (V) -> Nothing): E =
    apply { component1()?.let(success) }.component2()!!


sealed class Result<out V : Any?, out E : Exception>: ReadOnlyProperty<Any?, V> {

    open operator fun component1(): V? = null
    open operator fun component2(): E? = null

    inline fun <X> fold(success: (V) -> X, failure: (E) -> X): X = when (this) {
        is Success -> success(this.value)
        is Failure -> failure(this.error)
    }

    abstract fun get(): V

    class Success<out V : Any?>(val value: V) : Result<V, Nothing>() {
        override fun component1(): V? = value

        override fun get(): V = value

        override fun toString() = "[Success: $value]"

        override fun hashCode(): Int = value.hashCode()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            return other is Success<*> && value == other.value
        }

        override fun getValue(thisRef: Any?, property: KProperty<*>): V  = get()
    }

    class Failure<out E : Exception>(val error: E) : Result<Nothing, E>() {
        override fun component2(): E? = error

        override fun get() = throw error

        fun getException(): E = error

        override fun toString() = "[Failure: $error]"

        override fun hashCode(): Int = error.hashCode()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            return other is Failure<*> && error == other.error
        }

        override fun getValue(thisRef: Any?, property: KProperty<*>): Nothing = get()
    }

    companion object {
        // Factory methods
        fun <E : Exception> error(ex: E) = Failure(ex)

        fun <V : Any?> success(v: V) = Success(v)

        inline fun <V : Any?> of(value: V?, fail: (() -> Exception) = { Exception() }): Result<V, Exception> =
                value?.let { success(it) } ?: error(fail())

        inline fun <V : Any?, reified E: Exception> of(noinline f: () -> V): Result<V, E> = try {
            success(f())
        } catch (ex: Exception) {
            when (ex) {
                is E -> error(ex)
                else -> throw ex
            }
        }
    }

}
