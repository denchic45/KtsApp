package com.denchic45.kts.ui.courseEditor

import com.denchic45.kts.data.Interactor
import com.denchic45.kts.data.model.domain.Course
import com.denchic45.kts.data.model.domain.GroupHeader
import com.denchic45.kts.data.model.domain.Subject
import com.denchic45.kts.data.prefs.GroupPreference
import com.denchic45.kts.data.repository.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CourseEditorInteractor @Inject constructor(
    private val teacherRepository: TeacherRepository,
    private val subjectRepository: SubjectRepository,
    private val groupRepository: GroupRepository,
    private val courseRepository: CourseRepository,
    private val groupPreference: GroupPreference
) : Interactor {

    fun findSubjectByTypedName(name: String): Flow<List<Subject>> {
        return subjectRepository.findByContainsName(name)
    }

    fun findCourse(courseId: String): Flow<Course?> {
        return courseRepository.find(courseId)
    }

    suspend fun addCourse(course: Course) {
        return courseRepository.add(course)
    }

    suspend fun updateCourse(course: Course) {
        return courseRepository.updateCourse(course)
    }

    suspend fun removeCourse(course: Course) {
        return courseRepository.removeCourse(course.id, course.groupHeaders.map(GroupHeader::id))
    }

    override fun removeListeners() {
        courseRepository.removeListeners()
        groupRepository.removeListeners()
        teacherRepository.removeListeners()
        subjectRepository.removeListeners()
    }
}