# Result

[![Kotlin](https://img.shields.io/badge/Kotlin-0.14.449-blue.svg)](http://kotlinlang.org)

This is a tiny framework for modelling success/failure of operations in [Kotlin](http://kotlinlang.org). In short, it is a model in type of `Result<V, E : Exception>`.

## Ideology

`Result<V, E: Exception>` is to provide higher abstraction of operation that can be ended with result either success or failure. The is somewhat similar to Kotlin's `nullable types` (`T?`) (https://kotlinlang.org/docs/reference/null-safety.html).

`Result.Success` represents `value` in case of success, and `Result.Failure` represents `error` in case of failure which is upper bounded with `Exception` type. 

## Why

One can use `Result` whenever there is a need to represent an operation that has the possibility of failure. Error handling can be cumbersome to work with. 
`Result` helps process the operations in a nice, functional way, while maintaining readability to your code.

Let's consider a need to read data from `foo`, and to perform some further validation 

``` Kotlin
fun process(): String {
    try {
        val foo = File("/path/to/file/foo.txt").readText()
        val isSuccessful = processData(foo)
        if (!isSuccessful) {
            return "Data is corrupted and cannot be processed"
        }
    } catch (e: Exception) {
        //do something if error 
        Logger.log(ERROR, e.message())
    }
}
```

However, things start getting ugly when we have chain of operations being run sequentially, such as

``` Kotlin
fun process(): String {
    try {
        val foo = File("/path/to/file/foo.txt").readText()
        val isSuccessful = normalizedData(foo)
        if (!isSuccessful) {
            return "Data cannot be processable"
        }
        val request = createRequestFromData(foo)
        try {
            val result = database.updateFromRequest(request)
            if (!result) {
                return "Record in DB is not found"
            }
        } catch (dbEx: DBException) {
            return "DB error, cannot update"
        }
    } catch (e: Exception) {
        //do something if error 
        Logger.log(ERROR, e.message())
    }
}
```

Ouch, it looks pretty bleak.

Let's see how `Result` can help us.

First, we break things down into a small set of model in `Result`.

``` Kotlin
val fooOperation = { File("/path/to/file/foo.txt").readText() }
Result.create(fooOperation)  // Result<String, FileException>
```

``` Kotlin
fun normalizedData(foo): Result<Boolean, NormalizedException> {
}
```

``` Kotlin
fun createRequestFromData(foo): Request {
}
```

``` Kotlin
fun database.updateFromRequest(request): Result<Boolean, DBException> {
}
```

``` Kotlin
Result.create(fooOperation)
        .flatMap { normalizedData(it) }
        .map { createRequestFromData(it) }
        .flatMap { database.updateFromRequest(it) }
```

## Never Fail Operation

In some case, one wants to model an always successful operation. `Result<V, NoException>` is a good idea for that. 
`NoException` is to indicate that there is no exception to throw. E.g.

``` Kotlin
// Add operation can never be failure
fun add(i: Int, j: Int) : Result<Int, NoException>
```
 
Nice thing about modelling in this way is to be able to compose it with others "failable" operations in `Result`.

## High Order functions

### Success
`map` and `flatMap`

`map` transforms `Result` with given transformation `(V) -> U`. As a result, we are able to transform `V` into a new `V` in the case where `Result` is `Result.Success`.
When `Result` is `Result.Failure`, `error` is re-wrapped into a new `Result`.

`flatMap` is similar to `map`, however it requires transformation in type of `(V) -> Result<U, ...>`.
 
### Failure
`mapError` and `flatMapError`

`mapError` (`(E) -> E2`) and `flatMapError` (`(E) -> Result<E2, ...>`) are counterpart of `map` and `flatMap`. However, they are operate on `Result.Failure`. It is quite handy when one needs to do some transformation on given `Exception` into a custom type of `Exception` that suits ones' need.

