package com.denchic45.kts.di.modules

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.denchic45.kts.data.DataBase
import com.denchic45.kts.data.dao.*
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import javax.inject.Singleton

@Module
object DatabaseModule {
    @Provides
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Singleton
    @Provides
    fun provideDataBase(context: Context): DataBase = Room.databaseBuilder(
        context,
        DataBase::class.java, "database.db"
    ).addCallback(object : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            Executors.newSingleThreadExecutor().execute {
                val dataBase = provideDataBase(context)
                GlobalScope.launch {

                    cancel()
                }
            }
        }
    }).build()

    @Provides
    fun provideSubjectDao(dataBase: DataBase): SubjectDao = dataBase.subjectDao()

    @Provides
    fun provideUserDao(dataBase: DataBase): UserDao = dataBase.userDao()

    @Provides
    fun provideCourseDao(dataBase: DataBase): CourseDao = dataBase.courseDao()

    @Provides
    fun provideTaskDao(dataBase: DataBase): TaskDao = dataBase.taskDao()

    @Provides
    fun provideGroupDao(dataBase: DataBase): GroupDao = dataBase.groupDao()

    @Provides
    fun provideGroupCourseDao(dataBase: DataBase): GroupCourseDao = dataBase.groupCourseDao()

    @Provides
    fun provideLessonDao(dataBase: DataBase): LessonDao = dataBase.lessonDao()

    @Provides
    fun provideSpecialtyDao(dataBase: DataBase): SpecialtyDao = dataBase.specialtyDao()

    @Provides
    fun provideTeacherEventDao(dataBase: DataBase): TeacherEventDao = dataBase.teacherEventDao()

    @Provides
    fun provideDayDao(dataBase: DataBase): DayDao = dataBase.dayDao()

    @Provides
    fun provideSectionDao(dataBase: DataBase): SectionDao = dataBase.sectionDao()
}