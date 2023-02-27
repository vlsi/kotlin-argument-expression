[![GitHub CI](https://github.com/vlsi/kotlin-argument-expression/actions/workflows/main.yml/badge.svg?branch=main)](https://github.com/vlsi/kotlin-argument-expression/actions/workflows/main.yml)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.vlsi.kae/argument-expression-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.vlsi.kae/argument-expression-plugin)

# Argument Expression compiler plugin for Kotlin

A Kotlin compiler plugin that adds expression text based on annotations for calls like `require(...)` so the
implementation can yield better error messages.

See also https://github.com/bnorm/kotlin-power-assert

## Motivation

You might often use preconditions, however, the error reporting is not always helpful:

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
fun betterRequire(value: Boolean, @CallerArgumentExpression("value") valueExpression: String = "") {
    contract {
        returns() implies value
    }
    if (!value) {
        throw IllegalArgumentException("Failed requirement: $valueExpression")
    }
}
```

Then you can use `betterRequire` as `betterRequire(args.isNotEmpty())`, and the compiler plugin
would pass the precondition expression text thanks to `@CallerArgumentExpression` annotation.

See https://learn.microsoft.com/en-us/dotnet/csharp/language-reference/proposals/csharp-10.0/caller-argument-expression

## Compatibility matrix

* `io.github.vlsi.kotlin-argument-expression` 1.0.0 requires
  * Kotlin 1.6.21+
  * Java 1.8+
  * Gradle 6.1.1+ (required by Kotlin 1.6.21)

## Usage

In order to use the compiler plugin, you need:
1. Add the compiler plugin to your build script
2. Add annotations that should be treated for passing the expression text

### Gradle plugin

Sample Gradle configuration:

```kotlin
plugins {
    // io.github.vlsi.kotlin-argument-expression plugin
    // * adds implementation dependency on io.github.vlsi.kae:argument-expression-annotations
    // * configures io.github.vlsi.kae.CallerArgumentExpression annotation for compiler plugin
    id("io.github.vlsi.kotlin-argument-expression") version "1.0.1"
}

// extension type: io.github.vlsi.kae.gradle.KotlinArgumentExpressionExtension
kotlinArgumentExpression {
    // You can add your own CallerArgumentExpression annotations for processing
    argumentExpressionAnnotations.add("com.example.CallerArgumentExpression")
}
```

The above configuration should be good for most of the cases, however, if you want to opt-out
of `io.github.vlsi.kae:argument-expression-annotations` dependency, you can use `-base` plugin:

```kotlin
plugins {
    id("io.github.vlsi.kotlin-argument-expression-base") version "1.0.1"
}

// extension type: io.github.vlsi.kae.gradle.KotlinArgumentExpressionExtension
kotlinArgumentExpression {
    // Configure CallerArgumentExpression annotation
    argumentExpressionAnnotations.add("com.example.CallerArgumentExpression")
}
```

### Declaring annotation

You can use `io.github.vlsi.kae.CallerArgumentExpression` annotation, or create your own as follows:

```kotlin
/**
 * `kotlin-argument-annotations` exposes annotation,
 * however, you can create your own if you like to avoid dependency on `kotlin-argument-annotations`.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class CallerArgumentExpression(
    val value: String
)
```


### Capturing caller expression for simple parameter

```kotlin
fun precondition(
    value: Boolean,
    @CallerArgumentExpression("value") valueExpression: String = ""
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

### Capturing caller expression for vararg parameter

You can capture caller argument expressions as `String` (comma-separated list of expressions),
or as `Array<String>` (array of expressions):

```kotlin
fun dump(
    vararg values: String,
    @CallerArgumentExpression("values") valuesDescription: String = "",
    @CallerArgumentExpression("values") valuesDescriptionAsArray: Array<String>? = null,
) {
    println("values: ${values.contentToString()}")
    println("    single String expression: $valuesDescription")
    println("    array of String expressions: ${valuesDescription.joinToString { "<<$it>>" }}")
}

// Use precondition as follows:
fun main(args: Array<String>) {
    // Note that we do not pas valueExpression explicitly
    dump("a" + "b", "hello" + ", world")

    // Results in
    // single String expression: ab, hello, world
    // array of String expressions: <<"a" + "b">>, <<"hello" + ", world">>
}
```

### Capturing caller expression for lambda parameters

```kotlin
fun dump(
    body: () -> String,
    @CallerArgumentExpression("body") bodyDescription: String = "",
) {
    println("results: ${body()}")
    println("description: $bodyDescription")
}

fun main(args: Array<String>) {
    dump { "hello" + ", world" }

    // Results in
    // results: hello, world
    // description: { "hello" + ", world" }
}
```

## License
The compiler plugin is distributed under terms of Apache License 2.0

## Author
Vladimir Sitnikov <sitnikov.vladimir@gmail.com>
