package com.denchic45.kts.di.modules

import com.denchic45.kts.ui.adminPanel.timtableEditor.eventEditor.EventEditorActivity
import com.denchic45.kts.ui.group.editor.GroupEditorActivity
import com.denchic45.kts.ui.login.LoginActivity
import com.denchic45.kts.ui.main.MainActivity
import com.denchic45.kts.ui.profile.fullAvatar.FullAvatarActivity
import com.denchic45.kts.ui.splash.SplashActivity
import com.denchic45.kts.ui.userEditor.UserEditorActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
interface ActivityModule {
    @ContributesAndroidInjector()
    fun contributeMainActivity(): MainActivity

    @ContributesAndroidInjector()
    fun contributeSplashActivity(): SplashActivity

    @ContributesAndroidInjector()
    fun contributeLoginActivity(): LoginActivity

    @ContributesAndroidInjector(modules = [IntentModule::class, RawModule::class])
    fun contributeUserEditorActivity(): UserEditorActivity

    @ContributesAndroidInjector()
    fun contributeEventEditorActivity(): EventEditorActivity

    @ContributesAndroidInjector(modules = [IntentModule::class])
    fun contributeFullAvatarActivity(): FullAvatarActivity

    @ContributesAndroidInjector(modules = [IntentModule::class])
    fun contributeGroupEditorActivity(): GroupEditorActivity

}