package com.example

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BetterRequireTest {
    @Test
    fun helloWorld() {
        val ex = assertThrows<IllegalArgumentException> {
            betterRequire(0.1 + 0.2 == 0.3)
        }

        assertEquals(
            "Precondition failed: 0.1 + 0.2 == 0.3",
            ex.message,
            "io.github.vlsi.kotlin-argument-expression plugin should activate Kotlin compiler plugin," +
                    "and it should report the expression that failed"
        )
    }
}
