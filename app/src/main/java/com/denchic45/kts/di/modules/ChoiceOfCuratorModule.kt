package com.denchic45.kts.di.modules

import com.denchic45.kts.data.repository.TeacherRepository
import com.denchic45.kts.ui.group.choiceOfCurator.ChoiceOfCuratorInteractor
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object ChoiceOfCuratorModule {
    @Provides
    @Singleton
    fun provideInteractor(teacherRepository: TeacherRepository): ChoiceOfCuratorInteractor {
        return ChoiceOfCuratorInteractor(teacherRepository)
    }
}