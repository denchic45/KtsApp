package com.denchic45.kts.di.modules

import dagger.Provides
import javax.inject.Singleton
import com.denchic45.kts.ui.adminPanel.timetableEditor.eventEditor.EventEditorInteractor
import dagger.Module

@Module
object EventEditorModule {
    @Provides
    @Singleton
    fun provideInteractor(): EventEditorInteractor {
        return EventEditorInteractor()
    }
}