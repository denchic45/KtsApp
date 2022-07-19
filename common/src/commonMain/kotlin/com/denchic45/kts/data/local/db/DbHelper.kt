package com.denchic45.kts.data.local.db

import com.denchic45.kts.AppDatabase
import com.denchic45.kts.CourseContentEntity
import com.denchic45.kts.SubmissionEntity
import com.denchic45.kts.data.mapper.ListMapper
import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.db.SqlCursor
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.db.use

class DbHelper(val driver: SqlDriver) {

    val database: AppDatabase by lazy {
        init()
        AppDatabase(
            driver,
            courseContentEntityAdapter = CourseContentEntity.Adapter(
                attachmentsAdapter = object : ColumnAdapter<List<String>, String> {
                    override fun decode(databaseValue: String): List<String> {
                        return ListMapper.tolList(databaseValue)
                    }

                    override fun encode(value: List<String>): String {
                        return ListMapper.fromList(value)
                    }
                }
            ),
            submissionEntityAdapter = SubmissionEntity.Adapter(
                attachmentsAdapter = object : ColumnAdapter<List<String>, String> {
                    override fun decode(databaseValue: String): List<String> {
                        return ListMapper.tolList(databaseValue)
                    }

                    override fun encode(value: List<String>): String {
                        return ListMapper.fromList(value)
                    }
                }
            ))
    }

    private fun init() {
        val currentVer = version
        if (currentVer == 0) {
            AppDatabase.Schema.create(driver)
            version = 1
        } else {
            val schemaVer: Int = AppDatabase.Schema.version
            if (schemaVer > currentVer) {
                AppDatabase.Schema.migrate(driver, currentVer, schemaVer)
                version = schemaVer

            } else {

            }
        }
    }

    private var version: Int
        get() {
            val sqlCursor: SqlCursor = driver.executeQuery(null, "PRAGMA user_version;", 0, null)
            return sqlCursor.use {
                sqlCursor.getLong(0)!!.toInt()
            }
        }
        private set(version) {
            driver.execute(null, "PRAGMA user_version = $version;", 0, null)
        }
}