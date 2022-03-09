package com.denchic45.kts.utils

import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class DateConverterKtTest {

    @Test
    fun toDateTest() {
        print("date is: ${LocalDate.now().toDate()}")
    }
}