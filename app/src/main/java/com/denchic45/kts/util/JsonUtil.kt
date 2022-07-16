package com.denchic45.kts.util

import android.content.Context
import com.denchic45.kts.data.model.domain.ListItem
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.util.*

object JsonUtil {
    fun parseToList(context: Context, resId: Int): List<ListItem> {
        val inputStream = context.resources.openRawResource(resId)
        val jsonString = Scanner(inputStream).useDelimiter("\\A").next()
        return GsonBuilder()
            .create()
            .fromJson(jsonString, object : TypeToken<List<ListItem?>?>() {}.type)
    }
}