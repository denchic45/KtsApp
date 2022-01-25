package com.denchic45.kts.data.model.domain

data class GroupWeekLessons(
    var group: CourseGroup,
    var weekLessons: List<EventsOfTheDay>
)