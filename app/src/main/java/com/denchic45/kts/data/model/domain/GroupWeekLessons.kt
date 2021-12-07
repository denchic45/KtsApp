package com.denchic45.kts.data.model.domain

data class GroupWeekLessons(
    var group: Group,
    var weekLessons: List<EventsOfTheDay>
)