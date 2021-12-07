package com.denchic45.kts.di.modules

import dagger.Provides
import javax.inject.Singleton
import com.denchic45.kts.ui.iconPicker.IconPickerInteractor
import dagger.Module

@Module
object IconPickerModule {
    @Provides
    @Singleton
    fun provideInteractor(): IconPickerInteractor = IconPickerInteractor()

}