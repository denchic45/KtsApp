package com.denchic45.kts.data.model.domain

import java.time.LocalDate

data class GroupTimetable(
    val groupHeader: GroupHeader,
    val weekEvents: List<EventsOfDay>
) {


//    fun findEventByAbsolutelyPosition(positionEvent: Int): Event {
//      return  weekEvents.flatMap { it.events }[positionEvent]
//    }

    val lastEvent: Event?
        get() = weekEvents.last().events.lastOrNull()


    companion object {
        fun createEmpty(groupHeader: GroupHeader, mondayDate: LocalDate): GroupTimetable {

            return GroupTimetable(
                groupHeader,
                List(6) { EventsOfDay(mondayDate.plusDays(it.toLong())) }
                )

//            return GroupTimetable(
//                courseGroup, listOf(
//                    EventsOfTheDay(mondayDate),
//                    EventsOfTheDay(mondayDate.plusDays(1)),
//                    EventsOfTheDay(mondayDate.plusDays(2)),
//                    EventsOfTheDay(mondayDate.plusDays(3)),
//                    EventsOfTheDay(mondayDate.plusDays(4)),
//                    EventsOfTheDay(mondayDate.plusDays(5))
//                )
//            )
        }
    }
}