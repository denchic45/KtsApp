package com.denchic45.kts.di.modules

import com.denchic45.kts.data.model.mapper.*
import dagger.Module
import dagger.Provides

@Module
object MapperModule {

    @Provides
    fun provideSubjectMapper(): SubjectMapper = SubjectMapperImpl()

    @Provides
    fun provideCourseMapper(): CourseMapper = CourseMapperImpl()

    @Provides
    fun provideGroupMapper(): GroupMapper = GroupMapperImpl()

    @Provides
    fun provideTaskMapper(): TaskMapper = TaskMapperImpl()

    @Provides
    fun provideUserMapper(): UserMapper = UserMapperImpl()

    @Provides
    fun provideEventMapper(): EventMapper = EventMapperImpl()

    @Provides
    fun provideSpecialtyMapper(): SpecialtyMapper = SpecialtyMapperImpl()
}