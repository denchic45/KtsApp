package com.denchic45.kts.data.dao

import androidx.room.*
import java.util.ArrayList

@Dao
abstract class BaseDao<T> {
    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(obj: T): Long
    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(obj: List<T>): List<Long>
    @Transaction
    @Update
    abstract fun update(obj: T)
    @Transaction
    @Update
    abstract fun update(obj: List<T>)
    @Transaction
    @Delete
    abstract fun delete(obj: T)
    @Transaction
    @Delete
    abstract fun delete(obj: List<T>)
    @Transaction
    open suspend fun upsert(obj: T) {
        val id = insert(obj)
        if (id == -1L) {
            update(obj)
        }
    }

    @Transaction
    open suspend fun upsert(objList: List<T>) {
        val insertResult = insert(objList)
        val updateList: MutableList<T> = ArrayList()
        for (i in insertResult.indices) {
            if (insertResult[i] == -1L) {
                updateList.add(objList[i])
            }
        }
        if (updateList.isNotEmpty()) {
            update(updateList)
        }
    }
}