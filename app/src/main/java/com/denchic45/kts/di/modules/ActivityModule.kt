package com.denchic45.kts.di.modules

import com.denchic45.kts.ui.adminPanel.timetableEditor.eventEditor.EventEditorActivity
import com.denchic45.kts.ui.login.LoginActivity
import com.denchic45.kts.ui.main.MainActivity
import com.denchic45.kts.ui.profile.fullAvatar.FullAvatarActivity
import com.denchic45.kts.ui.splash.SplashActivity
import com.denchic45.kts.ui.userEditor.UserEditorFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
interface ActivityModule {
    @ContributesAndroidInjector
    fun contributeMainActivity(): MainActivity

    @ContributesAndroidInjector
    fun contributeSplashActivity(): SplashActivity

    @ContributesAndroidInjector
    fun contributeLoginActivity(): LoginActivity

    @ContributesAndroidInjector(modules = [IntentModule::class, RawModule::class])
    fun contributeUserEditorActivity(): UserEditorFragment

    @ContributesAndroidInjector
    fun contributeEventEditorActivity(): EventEditorActivity

    @ContributesAndroidInjector(modules = [IntentModule::class])
    fun contributeFullAvatarActivity(): FullAvatarActivity
}