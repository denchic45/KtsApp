package com.denchic45.kts.data.database

import androidx.room.DeleteColumn
import androidx.room.migration.AutoMigrationSpec

@DeleteColumn(
    tableName = "user",
    columnName = "phoneNum"
)
class RemovePhoneNumMigration : AutoMigrationSpec