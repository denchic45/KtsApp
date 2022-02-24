package com.denchic45.kts.data.model.domain

import java.time.LocalDate

data class GroupTimetable(
    var group: CourseGroup,
    var weekEvents: List<EventsOfTheDay>
) {


    fun findEventByAbsolutelyPosition(positionEvent: Int): Event {
      return  weekEvents.flatMap { it.events }[positionEvent]
    }

    val lastEvent: Event?
        get() = weekEvents.last().events.lastOrNull()


    companion object {
        fun createEmpty(courseGroup: CourseGroup, mondayDate: LocalDate): GroupTimetable {
            return GroupTimetable(
                courseGroup, listOf(
                    EventsOfTheDay(mondayDate),
                    EventsOfTheDay(mondayDate.plusDays(1)),
                    EventsOfTheDay(mondayDate.plusDays(2)),
                    EventsOfTheDay(mondayDate.plusDays(3)),
                    EventsOfTheDay(mondayDate.plusDays(5))
                )
            )
        }
    }
}