package com.example

import io.github.vlsi.kae.ArgumentExpression
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
fun betterRequire(condition: Boolean, @ArgumentExpression("condition") description: String = "") {
    contract {
        returns() implies condition
    }
    require(condition) {
        "Precondition failed: $description"
    }
}
