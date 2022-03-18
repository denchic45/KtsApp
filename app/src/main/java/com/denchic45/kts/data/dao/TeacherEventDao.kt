package com.denchic45.kts.data.dao

import androidx.room.Dao
import com.denchic45.kts.data.model.room.TeacherEventCrossRef

@Dao
abstract class TeacherEventDao : BaseDao<TeacherEventCrossRef>()