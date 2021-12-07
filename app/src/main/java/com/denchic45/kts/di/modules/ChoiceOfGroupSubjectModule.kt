package com.denchic45.kts.di.modules

import android.content.Context
import com.denchic45.kts.data.repository.SubjectRepository
import dagger.Provides
import javax.inject.Singleton
import com.denchic45.kts.ui.adminPanel.timtableEditor.choiceOfSubject.ChoiceOfSubjectInteractor
import dagger.Module

@Module
object ChoiceOfGroupSubjectModule {
    @Provides
    @Singleton
    fun provideInteractor(subjectRepository: SubjectRepository): ChoiceOfSubjectInteractor {
        return ChoiceOfSubjectInteractor(subjectRepository)
    }
}