package com.denchic45.kts.domain.uivalidator.util

import com.denchic45.kts.domain.uivalidator.Rule

fun NotEmpty(
    errorMessage: (value: String) -> ErrorMessage
): Rule<String> = Rule(
    predicate = { it.isNotEmpty() },
    errorMessage = errorMessage
)