package com.example

import io.github.vlsi.kae.CallerArgumentExpression

class Named<Value>(
    val value: Value,
    @CallerArgumentExpression("value") val name: String = ""
) {
    override fun toString(): String = "Named(value=$value, name='$name')"
}
