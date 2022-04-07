package com.denchic45.kts.data.database

import android.content.Context
import androidx.room.*
import com.denchic45.kts.data.dao.*
import com.denchic45.kts.data.model.room.*

@Database(
    entities = [
        SubjectEntity::class,
        EventEntity::class,
        CourseContentEntity::class,
        UserEntity::class,
        DayEntity::class,
        GroupEntity::class,
        SpecialtyEntity::class,
        CourseEntity::class,
        TeacherEventCrossRef::class,
        GroupCourseCrossRef::class,
        SectionEntity::class,
        ContentCommentEntity::class,
        SubmissionCommentEntity::class,
        SubmissionEntity::class
    ],
    autoMigrations = [
//        AutoMigration(from = 1, to = 2, spec = RemovePhoneNumMigration::class),
//        AutoMigration(from = 2, to = 3)
    ],
    version = 3,
    exportSchema = true
)
abstract class DataBase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun lessonDao(): EventDao
    abstract fun courseContentDao(): CourseContentDao
    abstract fun dayDao(): DayDao
    abstract fun userDao(): UserDao
    abstract fun groupDao(): GroupDao
    abstract fun courseDao(): CourseDao
    abstract fun specialtyDao(): SpecialtyDao
    abstract fun teacherEventDao(): TeacherEventDao
    abstract fun groupCourseDao(): GroupCourseDao
    abstract fun sectionDao(): SectionDao
    abstract fun submissionDao(): SubmissionDao
    abstract fun submissionCommentDao(): SubmissionCommentDao
    abstract fun contentCommentDao(): ContentCommentDao

    companion object {
        private var instance: DataBase? = null

        @Synchronized
        fun getInstance(context: Context): DataBase {
            if (instance == null) {
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    DataBase::class.java,
                    "database.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
            }
            return instance!!
        }
    }
}