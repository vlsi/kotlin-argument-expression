[![GitHub CI](https://github.com/vlsi/kotlin-argument-expression/actions/workflows/main.yml/badge.svg?branch=master)](https://github.com/vlsi/kotlin-argument-expression/actions/workflows/main.yml)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.vlsi.argument-expression/argument-expression-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.vlsi.argument-expression/argument-expression-plugin)

# Argument Expression compiler plugin for Kotlin

A Kotlin compiler plugin that adds expression text based on annotations for calls like `require(...)` so the
implementation can yield better error messages.

See also https://github.com/bnorm/kotlin-power-assert

## Motivation

You might often used preconditions, however, the error reporting is not always helpful:

```kotlin
fun main(args: Array<String>) {
    require(args.isNotEmpty())
}
```

The error message would be: `java.lang.IllegalArgumentException: Failed requirement.`

It would be way better if the error message would contain the expression text:
`Failed requirement args.isNotEmpty().`

Of course, you can write the expression text manually, however, it is error-prone,
so kotlin-argument-expression compiler plugin can help you.

```kotlin
fun betterRequire(value: Boolean, @ArgumentExpression("value") valueExpression: String = "") {
    contract {
        returns() implies value
    }
    if (!value) {
        throw IllegalArgumentException("Failed requirement: $valueExpression")
    }
}
```

Then you can use `betterRequire` as `betterRequire(args.isNotEmpty())`, and the compiler plugin
would pass the precondition expression text thanks to `@ArgumentExpression` annotation.

See https://learn.microsoft.com/en-us/dotnet/csharp/language-reference/proposals/csharp-10.0/caller-argument-expression

## Supported Kotlin versions

Currently, the plugin is built and tested with Kotlin 1.7.10.


## Usage

In order to use the compiler plugin, you need:
1. Add the compiler plugin to your build script
2. Add annotations that should be treated for passing the expression text

Sample Gradle configuration:
```kotlin
plugins {
    id("io.github.vlsi.kotlin-argument-expression") version "1.0.0"
}

// TODO: describe the way to configure annotation name
```

```kotlin
/**
 * `kotlin-argument-expression` publishes no annotations, so you need to define them yourself.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class ArgumentExpression(
    val value: String
)

fun precondition(
    value: Boolean,
    @ArgumentExpression("value") valueExpression: String = ""
) {
    require(value) { "Precondition failed: $valueExpression" }
}

// Use precondition as follows:
fun main(args: Array<String>) {
    // Note that we do not pas valueExpression explicitly
    precondition(args.isNotEmpty())

    // The failure would look as follows:
    // java.lang.IllegalArgumentException: Precondition failed: args.isNotEmpty()
}
```

## License
The compiler plugin is distributed under terms of Apache License 2.0

## Author
Vladimir Sitnikov <sitnikov.vladimir@gmail.com>
