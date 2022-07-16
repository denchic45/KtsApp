package com.denchic45.kts.util

import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class DateConverterKtTest {

    @Test
    fun toDateTest() {
        print("date is: ${LocalDate.now().toDate()}")
    }
}