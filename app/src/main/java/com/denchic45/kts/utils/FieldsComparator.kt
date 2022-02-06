package com.denchic45.kts.utils

import kotlin.reflect.full.memberProperties

object FieldsComparator {

    fun mapOfDifference(oldObj: Any, newObj: Any): MutableMap<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        oldObj.javaClass.kotlin.memberProperties.zip(newObj.javaClass.kotlin.memberProperties)
            .forEach { (property1, property2) ->
                if (property1.get(oldObj) != property2.get(newObj)) {
                    map[property2.name] = property2.get(newObj)
                }
            }
        return map
    }
}