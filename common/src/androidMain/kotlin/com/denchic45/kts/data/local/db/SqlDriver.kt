package com.denchic45.kts.data.local.db

import android.content.Context
import com.denchic45.kts.AppDatabase
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver

actual class DriverFactory(context: Context) {
    actual val driver: SqlDriver = AndroidSqliteDriver(
        AppDatabase.Schema,
        context,
        "database2.db"
    )
}