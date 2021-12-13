package com.denchic45.kts.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.denchic45.kts.data.dao.*
import com.denchic45.kts.data.model.room.*

@Database(
    entities = [SubjectEntity::class, EventEntity::class, TaskEntity::class, UserEntity::class, DayEntity::class, GroupEntity::class, SpecialtyEntity::class, CourseEntity::class, TeacherEventCrossRef::class, GroupCourseCrossRef::class, SectionEntity::class],
    version = 1
)
abstract class DataBase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun lessonDao(): LessonDao
    abstract fun taskDao(): TaskDao
    abstract fun dayDao(): DayDao
    abstract fun userDao(): UserDao
    abstract fun groupDao(): GroupDao
    abstract fun courseDao(): CourseDao
    abstract fun specialtyDao(): SpecialtyDao
    abstract fun teacherEventDao(): TeacherEventDao
    abstract fun groupCourseDao(): GroupCourseDao
    abstract fun sectionDao(): SectionDao

    companion object {
        private var instance: DataBase? = null
        @Synchronized
        fun getInstance(context: Context): DataBase? {
            if (instance == null) {
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    DataBase::class.java,
                    "database.db"
                )
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()
            }
            return instance
        }
    }
}