package com.denchic45.kts.data.pref

import com.denchic45.kts.GroupEntity
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getStringFlow
import com.russhwolf.settings.int
import com.russhwolf.settings.string
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow


@OptIn(ExperimentalCoroutinesApi::class, ExperimentalSettingsApi::class)
class GroupPreferences constructor(private val observableSettings: ObservableSettings) :
    ObservableSettings by observableSettings {
    var groupId: String by string()
    var groupName: String by string()
    var groupCourse: Int by int()
    var groupSpecialtyId: String by string()

    val observeGroupId: Flow<String> = observableSettings.getStringFlow("groupId", "")

    fun saveGroupInfo(group: GroupEntity) {
        groupName = group.group_name
        groupCourse = group.course
        groupSpecialtyId = group.specialty_id
        groupId = group.group_id
    }
}