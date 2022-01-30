package com.denchic45.kts.di.modules

import dagger.Provides
import javax.inject.Singleton
import com.denchic45.kts.ui.adminPanel.timetableEditor.TimetableEditorInteractor
import dagger.Module

@Module
object TimetableEditorModule {
    @Provides
    @Singleton
    fun provideInteractor(): TimetableEditorInteractor {
        return TimetableEditorInteractor()
    }
}