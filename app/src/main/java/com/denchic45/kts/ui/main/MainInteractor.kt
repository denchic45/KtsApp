package com.denchic45.kts.ui.main

import android.content.Context
import androidx.lifecycle.LiveData
import com.denchic45.kts.data.Interactor
import com.denchic45.kts.data.database.DataBase
import com.denchic45.kts.domain.model.CourseHeader
import com.denchic45.kts.domain.model.User
import com.denchic45.kts.data.prefs.TimestampPreference
import com.denchic45.kts.data.repository.*
import com.denchic45.kts.di.modules.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

class MainInteractor @Inject constructor(
    private val context: Context,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
    coroutineScope: CoroutineScope,
    private val groupRepository: GroupRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val eventRepository: EventRepository,
    private val metaRepository: MetaRepository,
    private val timestampPreference: TimestampPreference,
    private val courseRepository: CourseRepository,
    private val dataBase: DataBase
) : Interactor {

    val listenAuthState: Flow<Boolean> = authRepository.listenAuthState

    init {
        coroutineScope.launch(dispatcher) {
            listenAuthState.collect {
                if (!it) {
                    clearAllData(context)
                }
            }
        }
    }

    private fun clearAllData(context: Context) {
        dataBase.clearAllTables()
        val sharedPreferenceFile = File("/data/data/" + context.packageName + "/shared_prefs/")
        val listFiles = sharedPreferenceFile.listFiles()
        for (file in listFiles) {
            val name = file.name
            val sharedPrefs =
                context.getSharedPreferences(name.replace(".xml", ""), Context.MODE_PRIVATE)
            val editor = sharedPrefs.edit()
            editor.clear()
            editor.apply()
        }
    }

    override fun removeListeners() {
        groupRepository.removeListeners()
    }

    val lessonTime: LiveData<Int>
        get() = metaRepository.lessonTime

    fun observeHasGroup(): Flow<Boolean> {
        return groupRepository.observeHasGroup().onEach {
            if (it) {
                eventRepository.observeEventsOfYourGroup()
            }
        }
    }

    fun findThisUser(): User = userRepository.findSelf()

    fun observeThisUser(): Flow<User?> = userRepository.thisUserObserver

    suspend fun startListeners() {
        observeThisUser().collect { user ->
            user?.let {
                if (user.isTeacher) {
                    groupRepository.listenYouGroupByCurator()
                    groupRepository.listenGroupsWhereThisUserIsTeacher(user)
                } else if (user.isStudent) {
                    groupRepository.listenYourGroup()
                }
                courseRepository.observeByYourGroup()
            } ?: run {
                clearAllData(context)
                authRepository.signOut()
            }
        }
    }

    fun findOwnCourses(): Flow<List<CourseHeader>> {
        val thisUser = findThisUser()
        return when {
            thisUser.isTeacher -> courseRepository.findByYourAsTeacher()
            thisUser.isStudent -> courseRepository.findByYourGroup()
            else -> emptyFlow()
        }
    }
}