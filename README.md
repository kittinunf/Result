# Result

[![Kotlin](https://img.shields.io/badge/Kotlin-0.14.449-blue.svg)](http://kotlinlang.org)

This is a tiny framework for modelling success/failure of operations in [Kotlin](http://kotlinlang.org). In short, it is a `Result<V, E : Exception>`.

## Ideology

`Result<V, E: Exception>` is to provide higher abstraction of operation that can be ended with result either success or failure. The is similar to Kotlin's `nullable types` (`T?`) (https://kotlinlang.org/docs/reference/null-safety.html).

`Result.Success` represents `value` in case of success, and `Result.Failure` represents `error` in case of failure which is upper bounded with `Exception` type. 

## Example

One can use `Result` whenever there is a need to represent an operation that has the possibility of failure. Error handling in Kotlin can be cumbersome to work with.

Let's consider a need to read data from a file, and to perform some further execution 

``` Kotlin
try {
    val foo = File("/path/to/file/foo.txt").readText()
    processData(foo)
} catch (e: Exception) {
    //do something if error 
}
```

However, things get uglier when one need to handle error with an operation that again can fail. Let's say, if foo fail, we need to checkout bar instead.

``` Kotlin
try {
    val foo = File("/path/to/file/foo.txt").readText()
    processData(foo)
} catch (e: Exception) {
    try {
        val bar = File("/path/to/file/bar.txt").readText()
    } catch (ex: Exception) {
        logError(ex.message)
    }
}
```

Ouch, it looks pretty bleak.

Let see how `Result` can help us.

``` Kotlin
val readFoo = { File("/path/to/file/foo.txt").readText() }
val readBar = { File("/path/to/file/bar.txt").readText() }

val foo = Result.create(readFoo)  //foo is a Result<String, Exception>
val bar = Result.create(readBar)  //bar is a Result<String, Exception>

foo.map { processData(it) }.flatMapError { bar }.mapError { logError(it.message) }
```


 






