package com.example

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class NamedTest {
    @Test
    fun `named expression`() {
        assertEquals("Named(value=5, name='2 + 3')", Named(2 + 3).toString())
    }
}
