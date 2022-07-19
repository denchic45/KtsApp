package com.denchic45.kts.domain.usecase

import com.denchic45.kts.domain.model.EventsOfDay
import com.denchic45.kts.domain.model.User
import com.denchic45.kts.data.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class FindEventsOfDayByYourUserUseCase @Inject constructor(
    findSelfUserUseCase: FindSelfUserUseCase,
    private val eventRepository: EventRepository
) {

    private val user: User = findSelfUserUseCase()

    operator fun invoke(date: LocalDate): Flow<EventsOfDay> = when {
        user.isStudent -> eventRepository.findEventsOfDayByYourGroupAndDate(date)
        user.isTeacher -> eventRepository.findEventsForDayForTeacherByDate(date)
        else -> throw IllegalStateException("Illegal user role! ${user.role}")
    }

}