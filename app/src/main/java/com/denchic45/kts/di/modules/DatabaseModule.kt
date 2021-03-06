package com.denchic45.kts.di.modules

import android.content.Context
import com.denchic45.kts.AppDatabase
import com.denchic45.kts.data.dao.*
import com.denchic45.kts.data.database.DataBase
import com.denchic45.kts.data.local.db.*
import com.denchic45.kts.data.remote.db.UserRemoteDataSource
import com.denchic45.kts.data.service.AndroidNetworkService
import com.denchic45.kts.data.service.NetworkService
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.sqldelight.db.SqlDriver
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object DatabaseModule {

    @Provides
    fun provideNetworkService(context: Context): NetworkService {
        return AndroidNetworkService(context)
    }

    @Provides
    fun provideSqlDriver(context: Context): SqlDriver {
        return DriverFactory(context).driver
    }

    @Provides
    fun provideAppDatabase(sqlDriver: SqlDriver): AppDatabase {
        return DbHelper(sqlDriver).database
    }

    @Provides
    fun provideUserLocalDataSource(appDatabase: AppDatabase): UserLocalDataSource {
        return UserLocalDataSource(appDatabase)
    }

    @Provides
    fun provideGroupLocalDataSource(appDatabase: AppDatabase): GroupLocalDataSource {
        return GroupLocalDataSource(appDatabase)
    }

    @Provides
    fun provideCourseLocalDataSource(appDatabase: AppDatabase): CourseLocalDataSource {
        return CourseLocalDataSource(appDatabase)
    }

    @Provides
    fun provideSubmissionCommentLocalDataSource(appDatabase: AppDatabase): SubmissionCommentLocalDataSource {
        return SubmissionCommentLocalDataSource(appDatabase)
    }

    @Provides
    fun provideContentCommentLocalDataSource(appDatabase: AppDatabase): ContentCommentLocalDataSource {
        return ContentCommentLocalDataSource(appDatabase)
    }

    @Provides
    fun provideSpecialtyLocalDataSource(appDatabase: AppDatabase): SpecialtyLocalDataSource {
        return SpecialtyLocalDataSource(appDatabase)
    }

    @Provides
    fun provideSubmissionLocalDataSource(appDatabase: AppDatabase): SubmissionLocalDataSource {
        return SubmissionLocalDataSource(appDatabase)
    }

    @Provides
    fun provideSectionLocalDataSource(appDatabase: AppDatabase): SectionLocalDataSource {
        return SectionLocalDataSource(appDatabase)
    }

    @Provides
    fun provideGroupCourseLocalDataSource(appDatabase: AppDatabase): GroupCourseLocalDataSource {
        return GroupCourseLocalDataSource(appDatabase)
    }

    @Provides
    fun provideSubjectLocalDataSource(appDatabase: AppDatabase): SubjectLocalDataSource {
        return SubjectLocalDataSource(appDatabase)
    }

    @Provides
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    fun provideUserRemoteDataSource(firestore: FirebaseFirestore): UserRemoteDataSource {
        return UserRemoteDataSource(firestore)
    }

    @Singleton
    @Provides
    fun provideDataBase(context: Context): DataBase = DataBase.getInstance(
        context
    )

    @Provides
    fun provideSubjectDao(dataBase: DataBase): SubjectDao = dataBase.subjectDao()

    @Provides
    fun provideUserDao(dataBase: DataBase): UserDao = dataBase.userDao()

    @Provides
    fun provideCourseDao(dataBase: DataBase): CourseDao = dataBase.courseDao()

    @Provides
    fun provideCourseContentDao(dataBase: DataBase): CourseContentDao = dataBase.courseContentDao()

    @Provides
    fun provideGroupDao(dataBase: DataBase): GroupDao = dataBase.groupDao()

    @Provides
    fun provideGroupCourseDao(dataBase: DataBase): GroupCourseDao = dataBase.groupCourseDao()

    @Provides
    fun provideLessonDao(dataBase: DataBase): EventDao = dataBase.lessonDao()

    @Provides
    fun provideSpecialtyDao(dataBase: DataBase): SpecialtyDao = dataBase.specialtyDao()

    @Provides
    fun provideTeacherEventDao(dataBase: DataBase): TeacherEventDao = dataBase.teacherEventDao()

    @Provides
    fun provideDayDao(dataBase: DataBase): DayDao = dataBase.dayDao()

    @Provides
    fun provideSectionDao(dataBase: DataBase): SectionDao = dataBase.sectionDao()

    @Provides
    fun provideSubmissionDao(dataBase: DataBase): SubmissionDao = dataBase.submissionDao()

    @Provides
    fun provideSubmissionCommentDao(dataBase: DataBase): SubmissionCommentDao =
        dataBase.submissionCommentDao()

    @Provides
    fun provideContentCommentDao(dataBase: DataBase): ContentCommentDao =
        dataBase.contentCommentDao()
}