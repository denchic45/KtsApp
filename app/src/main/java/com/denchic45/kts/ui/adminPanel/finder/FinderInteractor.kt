package com.denchic45.kts.ui.adminPanel.finder

import com.denchic45.kts.data.Interactor
import com.denchic45.kts.data.Repository
import com.denchic45.kts.data.Resource
import com.denchic45.kts.data.model.domain.*
import com.denchic45.kts.data.model.domain.User.Companion.isStudent
import com.denchic45.kts.data.model.domain.User.Companion.isTeacher
import com.denchic45.kts.data.repository.*
import io.reactivex.rxjava3.core.Completable
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FinderInteractor @Inject constructor(
    private val courseRepository: CourseRepository,
    private val subjectRepository: SubjectRepository,
    private val userRepository: UserRepository,
    private val groupInfoRepository: GroupInfoRepository,
    private val studentRepository: StudentRepository,
    private val teacherRepository: TeacherRepository,
    private val specialtyRepository: SpecialtyRepository
) : Interactor {


    private val currentRepository: Repository? = null

    fun findUserByTypedName(name: String): Flow<Resource<List<User>>> {
        return userRepository.getByTypedName(name)
    }

    fun findGroupByTypedName(name: String): Flow<Resource<List<CourseGroup>>> {
        return groupInfoRepository.findByTypedName(name)
    }

    fun findSubjectByTypedName(name: String): Flow<Resource<List<Subject>>> {
        return subjectRepository.findByTypedName(name)
    }

    fun findSpecialtyByTypedName(name: String): Flow<Resource<List<Specialty>>> {
        return specialtyRepository.findByTypedName(name)
    }

    fun findCourseByTypedName(name: String): Flow<Resource<List<Course>>> {
        return courseRepository.findByTypedName(name)
    }

    override fun removeListeners() {
        userRepository.removeListeners()
        groupInfoRepository.removeListeners()
        subjectRepository.removeListeners()
        specialtyRepository.removeListeners()
    }

    fun removeUser(user: User): Completable {
        if (isStudent(user.role)) {
            return studentRepository.remove(user)
        } else if (isTeacher(user.role)) {
            return teacherRepository.remove(user)
        }
        throw IllegalStateException()
    }

    suspend fun removeSubject(subject: Subject) {
        return subjectRepository.remove(subject)
    }

    suspend fun removeGroup(group: Group) {
        courseRepository.removeGroup(group)
        groupInfoRepository.remove(group)
    }
}