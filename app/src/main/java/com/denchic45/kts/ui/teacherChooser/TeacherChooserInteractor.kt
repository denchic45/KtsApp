package com.denchic45.kts.ui.teacherChooser

import com.denchic45.kts.domain.model.User
import kotlinx.coroutines.channels.Channel
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