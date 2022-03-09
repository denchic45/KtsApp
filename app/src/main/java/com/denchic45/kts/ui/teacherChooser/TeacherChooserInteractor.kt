package com.denchic45.kts.ui.teacherChooser

import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.data.repository.TeacherRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TeacherChooserInteractor @Inject constructor() {

    private val selectedCurator = Channel<User>()

    suspend fun postSelectedCurator(o: User) {
        selectedCurator.send(o)
    }

    suspend fun receiveSelectTeacher(): User {
        return selectedCurator.receive()
    }
}