package com.denchic45.kts.utils

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class DatesKtTest {

    @Test
    fun toDateTest() {
        print("date is: ${LocalDate.now().toDate()}")
    }
}