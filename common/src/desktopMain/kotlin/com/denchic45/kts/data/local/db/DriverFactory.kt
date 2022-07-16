package com.denchic45.kts.data.local.db

import com.denchic45.kts.util.appDirectory
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import java.io.File
import java.util.*

actual class DriverFactory {

    actual val driver: SqlDriver = run {
        val file = File(appDirectory)
        file.mkdirs()
        val url = "jdbc:sqlite:${file.path}\\database.db"
        val driver = JdbcSqliteDriver(url, Properties())
        driver
    }



}