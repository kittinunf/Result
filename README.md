# Result

[![Kotlin](https://img.shields.io/badge/kotlin-1.9.20-blue.svg)](http://kotlinlang.org)
[![MavenCentral](https://maven-badges.herokuapp.com/maven-central/com.github.kittinunf.result/result-jvm/badge.svg)](https://search.maven.org/search?q=g:com.github.kittinunf.result)
![Run Gradle](https://github.com/kittinunf/Result/workflows/Run%20Gradle%20on%20Main/badge.svg?branch=master)
[![Codecov](https://codecov.io/github/kittinunf/Result/coverage.svg?branch=master)](https://codecov.io/gh/kittinunf/Result)

This is a tiny framework for modelling success/failure of operations
in [Kotlin](http://kotlinlang.org). In short, it is a model in type
of `Result<V: Any?, E : Throwable>`.

## Ideology

`Result<V: Any?, E: Throwable>` is to provide higher abstraction of operation that can be ended with
result either success or failure. `Result.Success` represents `value` in case of success,
and `Result.Failure` represents `error` in case of failure which is upper bounded with `Throwable`
type.

## Installation

### Gradle

``` Groovy
repositories {
    mavenCentral()
}

dependencies {
    // if you are working on JVM or Android only project
    implementation("com.github.kittinunf.result:result-jvm:«version»") //for JVM support
    
    // if you are working in KMM project
    implementation("com.github.kittinunf.result:result:«version»") //for Kotlin Multiplatform support
}

```

## TL;DR

This model is highly inspired
by "[Railway Oriented Programming](http://fsharpforfunandprofit.com/rop/#monads)" concept.

`Result` allows one to express series of success/failure operations in Kotlin as;

``` Kotlin
Result.of<T, Throwable>(doOperation())
      .flatMap { normalizedData(it) }
      .map { createRequestFromData(it) }
      .flatMap { database.updateFromRequest(it) }
```

Work with `Result` is easy

``` Kotlin
//multi-declaration
val (value, error) = result

//get
val value: Int = result.get() // throw exception if error

//terminal operator
//success
result.success {
}

//failure
result.failure {
}

//fold is there, if you want to handle both success and failure
result.fold({ value ->
    //do something with value
}, { err ->
    //do something with err
})
```

## Why

`Result` is suitable whenever there is a need to represent an operation that has the possibility of
failure. Error handling can be cumbersome to work with.
`Result` helps process the operations in a nice, functional way, while maintaining readability to
your code.

Let's consider a need to read data from `foo`, and to perform some further validation

``` Kotlin
fun process(): String {
    try {
        val foo = File("/path/to/file/foo.txt").readText()
        val isSuccessful = processData(foo)
        if (!isSuccessful) {
            return "Data is corrupted and cannot be processed"
        }
    } catch (e: Throwable) {
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
        } catch (dbEx: DBThrowable) {
            return "DB error, cannot update"
        }
    } catch (e: Throwable) {
        //do something if error
        Logger.log(ERROR, e.message())
    }
}
```

Ouch, it looks pretty bleak.

Let's see how `Result` can help us.

First, we break things down into a small set of model in `Result`.

* Read a file

``` Kotlin
val operation = { File("/path/to/file/foo.txt").readText() }
Result.of { operation() }  // Result<String, FileThrowable>
```

* Normalize a data

``` Kotlin
fun normalizedData(foo): Result<Boolean, NormalizedThrowable> {
    Result.of { foo.normalize() }
}
```

* Create a request from data

``` Kotlin
fun createRequestFromData(foo): Request {
    return createRequest(foo)
}
```

* Update DB with Request

``` Kotlin
fun database.updateFromRequest(request): Result<Boolean, DBThrowable> {
    val transaction = request.transaction
    return Result.of { 
        db.openTransaction {
            val success = db.execute(transaction)
            if (!success) {
                throw DBThrowable("Error")
            }
            return success
        }
    }
}
```

The whole operation can be chained by the following;

``` Kotlin
Result.of { doOperation() }
      .flatMap { normalizedData(it) }
      .map { createRequestFromData(it) }
      .flatMap { database.updateFromRequest(it) }
```

The creates a nice "happy path" of the whole chain, also handle error as appropriate. It looks
better and cleaner, right?.

## Never Fail Operation

In some case, one wants to model an always successful operation. `Result<V: Any?, NoException>` is a
good idea for that.
`NoException` is to indicate that there is no exception to throw. E.g.

``` Kotlin
// Add operation can never be failure
fun add(i: Int, j: Int) : Result<Int, NoException>
```

Nice thing about modelling in this way is to be able to compose it with others "fail-able"
operations in `Result`.

## High Order functions

### Success

`map` and `flatMap`

`map` transforms `Result` with given transformation `(V) -> U`. As a result, we are able to
transform `V` into a new `V` in the case where `Result` is `Result.Success`. When `Result`
is `Result.Failure`, `error` is re-wrapped into a new `Result`.

`flatMap` is similar to `map`, however it requires transformation in type of `(V) -> Result<U, ...>`
.

### Failure

`mapError` and `flatMapError`

`mapError` (`(E) -> E2`) and `flatMapError` (`(E) -> Result<E2, ...>`) are counterpart of `map`
and `flatMap`. However, they are operate on `Result.Failure`. It is quite handy when one needs to do
some transformation on given `Throwable` into a custom type of `Throwable` that suits ones' need.

## More features

Please check out more features in
the [ResultTest](./result/src/commonTest/kotlin/com/github/kittinunf/result/ResultTest.kt)

## Railway Oriented Programming

If interested, here are more articles that one might enjoy.

* [Recipe Part 2](http://fsharpforfunandprofit.com/posts/recipe-part2/)
* [Approach Error Handing](https://speakerdeck.com/swlaschin/railway-oriented-programming-a-functional-approach-to-error-handling)
* [Example](https://github.com/swlaschin/Railway-Oriented-Programming-Example)

Credit to Scott Wlaschin

## Credits

Result is brought to you by [contributors](https://github.com/kittinunf/Result/graphs/contributors).

## License

Result is released under the [MIT](http://opensource.org/licenses/MIT) license.
