package com.denchic45.kts.utils

import com.google.gson.Gson

fun <T> Gson.mapToObject(map: Map<String, Any?>?, type: Class<T>): T {
    val json = this.toJson(map)
    return this.fromJson(json, type)
}