package com.denchic45.kts.data.model.domain

import java.time.LocalDate

data class GroupTimetable(
    val groupHeader: GroupHeader,
    val weekEvents: List<EventsOfDay>
) {

    companion object {
        fun createEmpty(groupHeader: GroupHeader, mondayDate: LocalDate): GroupTimetable {

            return GroupTimetable(
                groupHeader,
                List(6) { EventsOfDay(mondayDate.plusDays(it.toLong())) }
            )
        }
    }
}