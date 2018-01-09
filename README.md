# Result

[![Kotlin](https://img.shields.io/badge/kotlin-1.2.10-blue.svg)](http://kotlinlang.org) [ ![jcenter](https://api.bintray.com/packages/kittinunf/maven/Result/images/download.svg) ](https://bintray.com/kittinunf/maven/Result/_latestVersion) [![Build Status](https://travis-ci.org/kittinunf/Result.svg?branch=master)](https://travis-ci.org/kittinunf/Result)

This is a tiny framework for modelling success/failure of operations in [Kotlin](http://kotlinlang.org). In short, it is a model in type of `Result<V, E : Exception>`.

## Ideology

`Result<V, E: Exception>` is to provide higher abstraction of operation that can be ended with result either success or failure. The is somewhat similar to Kotlin's `nullable types` (`T?`) (https://kotlinlang.org/docs/reference/null-safety.html).

`Result.Success` represents `value` in case of success, and `Result.Failure` represents `error` in case of failure which is upper bounded with `Exception` type.

## Installation

### Gradle

``` Groovy
repositories {
    jcenter()
}

dependencies {
    compile 'com.github.kittinunf.result:result:<latest-version>'
}

```

## TL;DR

This model is highly inspired by "[Railway Oriented Programming](http://fsharpforfunandprofit.com/rop/#monads)" concept.

`Result` allows one to express series of success/failure operations in Kotlin as;

``` Kotlin
Result.of(operation)
      .flatMap { normalizedData(it) }
      .map { createRequestFromData(it) }
      .flatMap { database.updateFromRequest(it) }
```

Work with `Result` is easy

``` Kotlin
//multi-declaration
val (value, error) = result

//get
val value: Int = result.get<Int>() ?: 0
val ex: Exception = result.get<Exception>()!!

//success
result.success {
}

//failure
result.failure {
}

//fold is there, if you want to handle both success and failure
result.fold({ value ->
    //do something with value
}, { error ->
    //do something with error
})
```

Combine several results in a validation (without stopping at the first error)

``` Kotlin
val r1: Result<Int, Exception> = Result.of(1)
val r2: Result<Int, Exception> = Result.of{throw Exception("Not a number")}
val r3: Result<Int, Exception> = Result.of(3)
val r4: Result<Int, Exception> = Result.of{throw Exception("Division by zero")}

val validation = Validation(r1, r2, r3, r4)
validation.hasFailure //true
validation.failures.map{it.message} //[Not a number, Division by zero]
```

## Why

`Result` is suitable whenever there is a need to represent an operation that has the possibility of failure. Error handling can be cumbersome to work with.
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

* Read a file

``` Kotlin
val operation = { File("/path/to/file/foo.txt").readText() }
Result.of(operation)  // Result<String, FileException>
```

* Normalize a data
``` Kotlin
fun normalizedData(foo): Result<Boolean, NormalizedException> {
    Result.of(foo.normalize())
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
fun database.updateFromRequest(request): Result<Boolean, DBException> {
    val transaction = request.transaction
    return Result.of(db.openTransaction {
        val success = db.execute(transaction)
        if (!success) {
            throw DBException("Error")
        }
        return success
    })
}
```

The whole operation can be chained by the following;

``` Kotlin
Result.of(operation)
      .flatMap { normalizedData(it) }
      .map { createRequestFromData(it) }
      .flatMap { database.updateFromRequest(it) }
```

The creates a nice "happy path" of the whole chain, also handle error as appropriate. It looks better and cleaner, right?.

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

## Railway Oriented Programming

If interested, here are more articles that one might enjoy.

* http://fsharpforfunandprofit.com/posts/recipe-part2/
* https://speakerdeck.com/swlaschin/railway-oriented-programming-a-functional-approach-to-error-handling
* https://github.com/swlaschin/Railway-Oriented-Programming-Example

Credit to Scott Wlaschin

## Credits

Result is brought to you by [contributors](https://github.com/kittinunf/Result/graphs/contributors).

## License

Result is released under the [MIT](http://opensource.org/licenses/MIT) license.
>The MIT License (MIT)

>Copyright (c) 2017

>Permission is hereby granted, free of charge, to any person obtaining a copy
>of this software and associated documentation files (the "Software"), to deal
>in the Software without restriction, including without limitation the rights
>to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
>copies of the Software, and to permit persons to whom the Software is
>furnished to do so, subject to the following conditions:

>The above copyright notice and this permission notice shall be included in
>all copies or substantial portions of the Software.

>THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
>IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
>FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
>AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
>LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
>OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
>THE SOFTWARE.
