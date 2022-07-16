package com.denchic45.kts

import com.denchic45.kts.util.DatePatterns
import com.denchic45.kts.util.toString
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.LocalDate

class DateTest {

    @Test
    fun mondayOfThisWeekTest() {
        print(
            "Monday is: " + LocalDate.now().plusWeeks(1).with(DayOfWeek.MONDAY)
                .toString(DatePatterns.DD_MM_yy)
        )
    }
}