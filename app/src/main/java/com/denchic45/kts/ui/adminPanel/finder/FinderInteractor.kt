package com.denchic45.kts.ui.adminPanel.finder

import com.denchic45.kts.data.Interactor
import com.denchic45.kts.domain.model.Subject
import com.denchic45.kts.domain.model.User
import com.denchic45.kts.domain.model.User.Companion.isStudent
import com.denchic45.kts.domain.model.User.Companion.isTeacher
import com.denchic45.kts.data.repository.*
import javax.inject.Inject

class FinderInteractor @Inject constructor(
    private val subjectRepository: SubjectRepository,
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository,
    private val studentRepository: StudentRepository,
    private val teacherRepository: TeacherRepository,
    private val specialtyRepository: SpecialtyRepository
) : Interactor {

    override fun removeListeners() {
        userRepository.removeListeners()
        groupRepository.removeListeners()
        subjectRepository.removeListeners()
        specialtyRepository.removeListeners()
    }

   suspend fun removeUser(user: User) {
        if (isStudent(user.role)) {
            return studentRepository.remove(user.id)
        } else if (isTeacher(user.role)) {
            return teacherRepository.remove(user)
        }
        throw IllegalStateException()
    }

    suspend fun removeSubject(subject: Subject) {
        return subjectRepository.remove(subject)
    }
}