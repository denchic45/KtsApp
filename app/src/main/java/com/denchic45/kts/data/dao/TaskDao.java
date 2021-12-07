package com.denchic45.kts.data.dao;

import androidx.room.Dao;

import com.denchic45.kts.data.model.room.TaskEntity;

@Dao
public abstract class TaskDao extends BaseDao<TaskEntity> {

//    @Query("UPDATE homework SET complete = :checked")
//    public abstract void updateHomeworkCompletion(int checked);
}
