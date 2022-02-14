package com.denchic45.kts.di.modules

import com.denchic45.kts.data.repository.GroupInfoRepository
import com.denchic45.kts.ui.login.choiceOfGroup.ChoiceOfGroupInteractor
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object ChoiceOfGroupModule {
    @Provides
    @Singleton
    fun provideInteractor(
        groupInfoRepository: GroupInfoRepository
    ): ChoiceOfGroupInteractor {
        return ChoiceOfGroupInteractor(groupInfoRepository)
    }
}