package com.denchic45.kts.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.denchic45.kts.data.model.room.SpecialtyEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class SpecialtyDao : BaseDao<SpecialtyEntity>() {

    @Query("SELECT * FROM specialty WHERE specialty_id =:id")
    abstract fun get(id: String): SpecialtyEntity?

    @Query("SELECT * FROM specialty WHERE specialty_id =:id")
    abstract fun observe(id: String): Flow<SpecialtyEntity?>
}