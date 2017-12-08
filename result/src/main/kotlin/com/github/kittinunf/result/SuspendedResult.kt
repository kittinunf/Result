package com.github.kittinunf.result


inline fun <reified X> SuspendedResult<*, *>.getAs() = when (this) {
    is SuspendedResult.Success -> value as? X
    is SuspendedResult.Failure -> error as? X
}

suspend fun <V : Any> SuspendedResult<V, *>.success(f: suspend (V) -> Unit) = fold(f, {})

suspend fun <E : Exception> SuspendedResult<*, E>.failure(f: suspend (E) -> Unit) = fold({}, f)

infix fun <V : Any, E : Exception> SuspendedResult<V, E>.or(fallback: V) = when (this) {
    is SuspendedResult.Success -> this
    else -> SuspendedResult.Success(fallback)
}

infix fun <V : Any, E : Exception> SuspendedResult<V, E>.getOrElse(fallback: V) = when (this) {
    is SuspendedResult.Success -> value
    else -> fallback
}

suspend fun <V : Any, U : Any, E : Exception> SuspendedResult<V, E>.map(transform: suspend (V) -> U): SuspendedResult<U, E> = when (this) {
    is SuspendedResult.Success -> SuspendedResult.Success(transform(value))
    is SuspendedResult.Failure -> SuspendedResult.Failure(error)
}

suspend fun <V : Any, U : Any, E : Exception> SuspendedResult<V, E>.flatMap(transform: suspend (V) -> SuspendedResult<U, E>): SuspendedResult<U, E> = when (this) {
    is SuspendedResult.Success -> transform(value)
    is SuspendedResult.Failure -> SuspendedResult.Failure(error)
}

suspend fun <V : Any, E : Exception, E2 : Exception> SuspendedResult<V, E>.mapError(transform: suspend (E) -> E2) = when (this) {
    is SuspendedResult.Success -> SuspendedResult.Success<V, E2>(value)
    is SuspendedResult.Failure -> SuspendedResult.Failure<V, E2>(transform(error))
}

suspend fun <V : Any, E : Exception, E2 : Exception> SuspendedResult<V, E>.flatMapError(transform: suspend (E) -> SuspendedResult<V, E2>) = when (this) {
    is SuspendedResult.Success -> SuspendedResult.Success(value)
    is SuspendedResult.Failure -> transform(error)
}

suspend fun <V : Any> SuspendedResult<V, *>.any(predicate: suspend (V) -> Boolean): Boolean = when (this) {
    is SuspendedResult.Success -> predicate(value)
    is SuspendedResult.Failure -> false
}

suspend fun <V : Any, U: Any> SuspendedResult<V, *>.fanout(other: suspend () -> SuspendedResult<U, *>): SuspendedResult<Pair<V, U>, *> =
    flatMap { outer -> other().map { outer to it } }

sealed class SuspendedResult<out V : Any, out E : Exception> {

    abstract operator fun component1(): V?
    abstract operator fun component2(): E?

    suspend fun <X> fold(success: suspend (V) -> X, failure: suspend (E) -> X): X {
      return when (this) {
        is Success -> success(this.value)
        is Failure -> failure(this.error)
      }
    }

    abstract fun get(): V

    class Success<out V : Any, out E : Exception>(val value: V) : SuspendedResult<V, E>() {
        override fun component1(): V? = value
        override fun component2(): E? = null

        override fun get(): V = value

        override fun toString() = "[Success: $value]"

        override fun hashCode(): Int = value.hashCode()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            return other is Success<*, *> && value == other.value
        }
    }

    class Failure<out V : Any, out E : Exception>(val error: E) : SuspendedResult<V, E>() {
        override fun component1(): V? = null
        override fun component2(): E? = error

        override fun get(): V = throw error

        fun getException(): E = error

        override fun toString() = "[Failure: $error]"

        override fun hashCode(): Int = error.hashCode()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            return other is Failure<*, *> && error == other.error
        }
    }

    companion object {
        // Factory methods
        fun <E : Exception> error(ex: E) = Failure<Nothing, E>(ex)

        suspend fun <V : Any> of(value: V?, fail: (() -> Exception) = { Exception() }): SuspendedResult<V, Exception> {
            return value?.let { Success<V, Nothing>(it) } ?: error(fail())
        }

        suspend fun <V : Any> of(f: () -> V): SuspendedResult<V, Exception> = try {
            Success(f())
        } catch(ex: Exception) {
            Failure(ex)
        }
    }

}
