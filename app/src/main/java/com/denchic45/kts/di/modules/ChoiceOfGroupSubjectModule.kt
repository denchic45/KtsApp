package com.denchic45.kts.di.modules

import com.denchic45.kts.data.repository.SubjectRepository
import com.denchic45.kts.ui.adminPanel.timetableEditor.choiceOfSubject.ChoiceOfSubjectInteractor
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object ChoiceOfGroupSubjectModule {
    @Provides
    @Singleton
    fun provideInteractor(subjectRepository: SubjectRepository): ChoiceOfSubjectInteractor {
        return ChoiceOfSubjectInteractor(subjectRepository)
    }
}