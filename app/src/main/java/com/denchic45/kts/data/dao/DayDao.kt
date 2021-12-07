package com.denchic45.kts.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.denchic45.kts.data.dao.BaseDao
import com.denchic45.kts.data.model.room.DayEntity
import androidx.room.TypeConverters
import com.denchic45.kts.data.model.room.DateConverter
import java.util.*

@Dao
abstract class DayDao : BaseDao<DayEntity?>() {
    @Query("SELECT * FROM day WHERE day_uuid=:uuid")
    abstract operator fun get(uuid: String?): DayEntity?
    @Query("SELECT day_uuid FROM day WHERE date =:date AND group_uuid =:groupUuid")
    abstract suspend fun getUuidByDateAndGroupUuid(
        @TypeConverters(
            DateConverter::class
        ) date: Date?, groupUuid: String?
    ): String?
}