package com.denchic45.kts.ui.main

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.asFlow
import com.denchic45.kts.data.DataBase
import com.denchic45.kts.data.Interactor
import com.denchic45.kts.data.model.domain.CourseInfo
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.data.prefs.TimestampPreference
import com.denchic45.kts.data.repository.*
import com.denchic45.kts.di.modules.IoDispatcher
import com.denchic45.kts.utils.TimestampUtil
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainInteractor @Inject constructor(
    private val context: Context,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
    coroutineScope: CoroutineScope,
    private val groupInfoRepository: GroupInfoRepository,
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

    fun hasGroup(): Boolean {
        return groupInfoRepository.hasGroup()
    }

    private fun groupsWereUpdateLongAgo(): Boolean {
        return TimestampUtil.isDateDiffsGreaterThanOrEqualTo(
            timestampPreference.lastUpdateGroupsTimestamp,
            System.currentTimeMillis(),
            TimestampPreference.GROUP_HOURS_TIMEOUT,
            TimeUnit.HOURS
        )
    }

    override fun removeListeners() {
        groupInfoRepository.removeListeners()
    }

    val lessonTime: LiveData<Int>
        get() = metaRepository.lessonTime

    fun observeHasGroup(): Flow<Boolean> {
        return groupInfoRepository.observeHasGroup().onEach {
            if (it) {
                eventRepository.listenLessonsOfYourGroup()
            }
        }

    }

    val roleOfThisUser: String
        get() = userRepository.roleOfThisUser

    fun findThisUser(): User {
        return userRepository.findSelf()
    }

    fun listenThisUser(): Flow<Optional<User>> {
        return userRepository.thisUserObserver
    }

    suspend fun startListeners() {
        listenThisUser().mapLatest { optionalUser: Optional<User> ->
            if (!optionalUser.isPresent) {
                clearAllData(context)
                authRepository.signOut()
            } else {
                val user = optionalUser.get()
                if (user.isTeacher) {
                    groupInfoRepository.listenYouGroupByCurator()
                    groupInfoRepository.listenGroupsWhereThisUserIsTeacher(user)
                } else if (user.isStudent) {
                    groupInfoRepository.listenYourGroup()
                }
                courseRepository.observeByYouGroup()
            }
        }.collect {
            Log.d("lol", "fucking end: ")
        }
    }

    fun findOwnCourses(): Flow<List<CourseInfo>> {
        val thisUser = findThisUser()
        return when {
            thisUser.isTeacher -> courseRepository.findByYourAsTeacher()
            thisUser.isStudent -> courseRepository.findByYouGroup().asFlow()
            else -> emptyFlow()
        }
    }

}