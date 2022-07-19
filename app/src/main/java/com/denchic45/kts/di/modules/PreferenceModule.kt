package com.denchic45.kts.di.modules

import android.content.Context
import com.denchic45.kts.data.pref.GroupPreferences
import com.denchic45.kts.data.pref.UserPreferences
import com.denchic45.kts.data.prefs.AppPreference
import com.denchic45.kts.data.prefs.GroupPreference
import com.denchic45.kts.data.prefs.TimestampPreference
import com.denchic45.kts.data.prefs.UserPreference
import com.russhwolf.settings.AndroidSettings
import dagger.Module
import dagger.Provides

@Module
object PreferenceModule {

    @Provides
    fun provideUserPreferences(context: Context): UserPreferences {
        return UserPreferences(
            AndroidSettings(context.getSharedPreferences("User", Context.MODE_PRIVATE))
        )
    }

    @Provides
    fun provideGroupPreferences(context: Context): GroupPreferences {
        return GroupPreferences(
            AndroidSettings(context.getSharedPreferences("Group", Context.MODE_PRIVATE))
        )
    }

    @Provides
    fun provideAppPreference(context: Context) = AppPreference(context)

    @Provides
    fun provideGroupPreference(context: Context) = GroupPreference(context)

    @Provides
    fun provideTimestampPreference(context: Context) = TimestampPreference(context)

    @Provides
    fun provideUserPreference(context: Context) = UserPreference(context)
}