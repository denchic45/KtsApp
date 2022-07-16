package com.denchic45.kts.data.local.db

import com.squareup.sqldelight.db.SqlDriver

expect class DriverFactory {
    val driver: SqlDriver
}