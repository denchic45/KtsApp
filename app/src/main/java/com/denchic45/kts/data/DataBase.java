package com.denchic45.kts.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.denchic45.kts.data.dao.CourseDao;
import com.denchic45.kts.data.dao.DayDao;
import com.denchic45.kts.data.dao.GroupCourseDao;
import com.denchic45.kts.data.dao.GroupDao;
import com.denchic45.kts.data.dao.TaskDao;
import com.denchic45.kts.data.dao.LessonDao;
import com.denchic45.kts.data.dao.SpecialtyDao;
import com.denchic45.kts.data.dao.SubjectDao;
import com.denchic45.kts.data.dao.TeacherEventDao;
import com.denchic45.kts.data.dao.UserDao;
import com.denchic45.kts.data.model.room.CourseEntity;
import com.denchic45.kts.data.model.room.DayEntity;
import com.denchic45.kts.data.model.room.EventEntity;
import com.denchic45.kts.data.model.room.GroupCourseCrossRef;
import com.denchic45.kts.data.model.room.GroupEntity;
import com.denchic45.kts.data.model.room.TaskEntity;
import com.denchic45.kts.data.model.room.SpecialtyEntity;
import com.denchic45.kts.data.model.room.SubjectEntity;
import com.denchic45.kts.data.model.room.TeacherEventCrossRef;
import com.denchic45.kts.data.model.room.UserEntity;

@Database(entities = {
        SubjectEntity.class,
        EventEntity.class,
        TaskEntity.class,
        UserEntity.class,
        DayEntity.class,
        GroupEntity.class,
        SpecialtyEntity.class,
        CourseEntity.class,
        TeacherEventCrossRef.class,
        GroupCourseCrossRef.class
},
        version = 1)
public abstract class DataBase extends RoomDatabase {

    private static DataBase instance;

    public static synchronized DataBase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), DataBase.class, "database.db")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }

    public abstract SubjectDao subjectDao();

    public abstract LessonDao lessonDao();

    public abstract TaskDao taskDao();

    public abstract DayDao dayDao();

    public abstract UserDao userDao();

    public abstract GroupDao groupDao();

    public abstract CourseDao courseDao();

    public abstract SpecialtyDao specialtyDao();

    public abstract TeacherEventDao teacherEventDao();

    public abstract GroupCourseDao groupCourseDao();
}
