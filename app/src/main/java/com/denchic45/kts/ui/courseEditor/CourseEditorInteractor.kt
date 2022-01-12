package com.denchic45.kts.ui.courseEditor

import androidx.lifecycle.LiveData
import com.denchic45.kts.data.Interactor
import com.denchic45.kts.data.Resource
import com.denchic45.kts.data.Resource2
import com.denchic45.kts.data.model.domain.Course
import com.denchic45.kts.data.model.domain.Group
import com.denchic45.kts.data.model.domain.Subject
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.data.prefs.GroupPreference
import com.denchic45.kts.data.repository.CourseRepository
import com.denchic45.kts.data.repository.GroupInfoRepository
import com.denchic45.kts.data.repository.SubjectRepository
import com.denchic45.kts.data.repository.TeacherRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CourseEditorInteractor @Inject constructor(
    private val teacherRepository: TeacherRepository,
    private val subjectRepository: SubjectRepository,
    private val groupInfoRepository: GroupInfoRepository,
    private val courseRepository: CourseRepository,
    private val groupPreference: GroupPreference
) : Interactor  {

    fun findSubject(subjectUuid: String): Subject {
        return subjectRepository.getByUuid(subjectUuid)
    }

    fun findTeacherByTypedName(name: String): Flow<Resource2<List<User>>> {
        return teacherRepository.findByTypedName(name)
    }

    fun findSubjectByTypedName(name: String): Flow<Resource<List<Subject>>> {
        return subjectRepository.findByTypedName(name)
    }

    fun findCourse(courseUuid: String): Flow<Course> {
        return courseRepository.find(courseUuid)
    }

    suspend fun addCourse(course: Course) {
        return courseRepository.add(course)
    }

    suspend fun updateCourse(newCourse: Course) {
        return courseRepository.update(newCourse)
    }

    suspend fun removeCourse(course: Course) {
        return courseRepository.remove(course)
    }

    val yourGroupUuid: String
        get() = groupPreference.groupUuid

    override fun removeListeners() {
        courseRepository.removeListeners()
        groupInfoRepository.removeListeners()
        teacherRepository.removeListeners()
        subjectRepository.removeListeners()
    }

    fun findGroup(groupUuid: String): LiveData<Group> {
        return groupInfoRepository.find(groupUuid)
    }
}