package com.example

import io.github.vlsi.kae.CallerArgumentExpression
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
fun betterRequire(condition: Boolean, @CallerArgumentExpression("condition") description: String = "") {
    contract {
        returns() implies condition
    }
    require(condition) {
        "Precondition failed: $description"
    }
}
