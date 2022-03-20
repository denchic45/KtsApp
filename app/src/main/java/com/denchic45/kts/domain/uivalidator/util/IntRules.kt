package com.denchic45.kts.domain.uivalidator.util

import com.denchic45.kts.domain.uivalidator.Rule

fun NotZero(errorMessage: (value: Int) -> ErrorMessage): Rule<Int> {
    return Rule(
        predicate = { it != 0 },
        errorMessage = errorMessage
    )
}

